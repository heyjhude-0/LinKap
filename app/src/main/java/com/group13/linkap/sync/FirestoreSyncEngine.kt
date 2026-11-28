package com.group13.linkap.sync

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.group13.linkap.data.local.entity.FollowUp
import com.group13.linkap.data.local.entity.Household
import com.group13.linkap.data.local.entity.Resident
import com.group13.linkap.data.local.entity.Visit
import com.group13.linkap.data.local.entity.VisitResident
import com.group13.linkap.di.AppModule
import kotlinx.coroutines.tasks.await

class FirestoreSyncEngine(context: Context) {

    private val appContext = context.applicationContext
    private val database = AppModule.provideDatabase(appContext)
    private val householdDao = database.householdDao()
    private val residentDao = database.residentDao()
    private val visitDao = database.visitDao()
    private val followUpDao = database.followUpDao()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val syncPreferences = SyncPreferences(appContext)

    suspend fun performSync(): Result<Unit> {
        Log.d("LinKapSync", "ENTERED FirestoreSyncEngine.performSync()")
        return try {
            Log.d("LinKapSync", "Checking FirebaseAuth.getInstance().currentUser...")
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e("LinKapSync", "FirebaseAuth currentUser is NULL. Unauthenticated sync attempt.")
                return Result.failure(Exception("User is not authenticated."))
            }

            val uid = currentUser.uid
            Log.d("LinKapSync", "FirebaseAuth currentUser found -> UID: $uid, Email: ${currentUser.email}")

            Log.d("LinKapSync", "Fetching users/$uid profile document from Firestore...")
            val userDoc = try {
                firestore.collection("users").document(uid).get().await()
            } catch (e: Exception) {
                Log.e("LinKapSync", "Firestore fetch error for users/$uid", e)
                return Result.failure(e)
            }

            Log.d("LinKapSync", "Profile fetch complete -> exists: ${userDoc.exists()}")
            if (!userDoc.exists()) {
                Log.e("LinKapSync", "Profile validation failed: Document users/$uid does not exist.")
                return Result.failure(Exception("User profile users/$uid does not exist."))
            }

            val isActive = userDoc.getBoolean("isActive") ?: false
            val role = userDoc.getString("role")
            val barangayId = userDoc.getString("barangayId")?.trim()

            Log.d("LinKapSync", "Profile validation -> isActive: $isActive, role: '$role', barangayId: '$barangayId'")

            if (!isActive) {
                Log.e("LinKapSync", "Profile validation failed: Account is deactivated.")
                return Result.failure(Exception("Account is deactivated."))
            }
            if (role != "BHW") {
                Log.e("LinKapSync", "Profile validation failed: Account role is '$role' (expected BHW).")
                return Result.failure(Exception("Account is not authorized for BHW access."))
            }
            if (barangayId.isNullOrBlank()) {
                Log.e("LinKapSync", "Profile validation failed: barangayId is null or blank.")
                return Result.failure(Exception("No barangayId assigned to user profile."))
            }

            // 1. Upload local changes in dependency order (Households -> Residents -> Visits -> FollowUps)
            Log.d("LinKapSync", "--- STARTING UPLOADS for barangayId='$barangayId' ---")

            Log.d("LinKapSync", "Calling uploadHouseholds('$barangayId')...")
            val uHouseholds = uploadHouseholds(barangayId)
            Log.d("LinKapSync", "uploadHouseholds completed -> result: $uHouseholds")

            Log.d("LinKapSync", "Calling uploadResidents('$barangayId')...")
            val uResidents = uploadResidents(barangayId)
            Log.d("LinKapSync", "uploadResidents completed -> result: $uResidents")

            Log.d("LinKapSync", "Calling uploadVisits('$barangayId')...")
            val uVisits = uploadVisits(barangayId)
            Log.d("LinKapSync", "uploadVisits completed -> result: $uVisits")

            Log.d("LinKapSync", "Calling uploadFollowUps('$barangayId')...")
            val uFollowUps = uploadFollowUps(barangayId)
            Log.d("LinKapSync", "uploadFollowUps completed -> result: $uFollowUps")

            // 2. Download remote changes in dependency order (Households -> Residents -> Visits -> FollowUps)
            Log.d("LinKapSync", "--- STARTING DOWNLOADS for barangayId='$barangayId' ---")

            Log.d("LinKapSync", "Calling downloadHouseholds('$barangayId')...")
            val dHouseholds = downloadHouseholds(barangayId)
            Log.d("LinKapSync", "downloadHouseholds completed -> result: $dHouseholds")

            Log.d("LinKapSync", "Calling downloadResidents('$barangayId')...")
            val dResidents = downloadResidents(barangayId)
            Log.d("LinKapSync", "downloadResidents completed -> result: $dResidents")

            Log.d("LinKapSync", "Calling downloadVisits('$barangayId')...")
            val dVisits = downloadVisits(barangayId)
            Log.d("LinKapSync", "downloadVisits completed -> result: $dVisits")

            Log.d("LinKapSync", "Calling downloadFollowUps('$barangayId')...")
            val dFollowUps = downloadFollowUps(barangayId)
            Log.d("LinKapSync", "downloadFollowUps completed -> result: $dFollowUps")

            // 3. Query & Log actual Room DB row counts after downloads
            val roomHouseholds = rawQueryCount("SELECT count(*) FROM households WHERE barangayId = '$barangayId'")
            val roomResidents = rawQueryCount("SELECT count(*) FROM residents WHERE barangayId = '$barangayId'")
            val roomVisits = rawQueryCount("SELECT count(*) FROM visits WHERE barangayId = '$barangayId'")
            val roomFollowUps = rawQueryCount("SELECT count(*) FROM follow_ups WHERE barangayId = '$barangayId'")

            Log.d("LinKapSync", "--- ROOM DB COUNTS AFTER SYNC for barangayId='$barangayId' ---")
            Log.d("LinKapSync", "  Room Households: $roomHouseholds")
            Log.d("LinKapSync", "  Room Residents: $roomResidents")
            Log.d("LinKapSync", "  Room Visits: $roomVisits")
            Log.d("LinKapSync", "  Room FollowUps: $roomFollowUps")

            val allSuccess = uHouseholds && uResidents && uVisits && uFollowUps &&
                    dHouseholds && dResidents && dVisits && dFollowUps

            if (allSuccess) {
                Log.d("LinKapSync", "performSync() FINISHED SUCCESSFULLY (Result.success)")
                syncPreferences.setLastSyncTime(System.currentTimeMillis())
                Result.success(Unit)
            } else {
                Log.e("LinKapSync", "performSync() FINISHED WITH PARTIAL ERRORS (Result.failure). Uploads: HH=$uHouseholds, Res=$uResidents, Vis=$uVisits, Fol=$uFollowUps | Downloads: HH=$dHouseholds, Res=$dResidents, Vis=$dVisits, Fol=$dFollowUps")
                Result.failure(Exception("Synchronization completed with partial errors."))
            }
        } catch (e: Exception) {
            Log.e("LinKapSync", "Unhandled Exception in performSync()", e)
            Result.failure(e)
        }
    }

    private suspend fun uploadHouseholds(barangayId: String): Boolean {
        var success = true
        val pending = householdDao.getPendingHouseholds(barangayId)
        Log.d("LinKapSync", "uploadHouseholds() -> pending count: ${pending.size}")
        for (h in pending) {
            try {
                val data = hashMapOf(
                    "householdId" to h.householdId,
                    "householdCode" to h.householdCode,
                    "purok" to h.purok,
                    "street" to h.street,
                    "barangay" to h.barangay,
                    "barangayId" to barangayId,
                    "latitude" to h.latitude,
                    "longitude" to h.longitude,
                    "createdAt" to h.createdAt,
                    "updatedAt" to h.updatedAt
                )
                firestore.collection("households")
                    .document(h.householdId)
                    .set(data, SetOptions.merge())
                    .await()

                householdDao.updateSyncStatus(h.householdId, "SYNCED", System.currentTimeMillis())
                Log.d("LinKapSync", "Uploaded household ID '${h.householdId}' -> SYNCED")
            } catch (e: Exception) {
                Log.e("LinKapSync", "Error uploading household ID '${h.householdId}'", e)
                householdDao.updateSyncStatus(h.householdId, "FAILED", null)
                success = false
            }
        }
        return success
    }

    private suspend fun uploadResidents(barangayId: String): Boolean {
        var success = true
        val pending = residentDao.getPendingResidents(barangayId)
        Log.d("LinKapSync", "uploadResidents() -> pending count: ${pending.size}")
        for (r in pending) {
            try {
                val data = hashMapOf(
                    "residentId" to r.residentId,
                    "householdId" to r.householdId,
                    "firstName" to r.firstName,
                    "middleName" to r.middleName,
                    "lastName" to r.lastName,
                    "birthDate" to r.birthDate,
                    "sex" to r.sex,
                    "relationshipToHead" to r.relationshipToHead,
                    "contactNumber" to r.contactNumber,
                    "isActive" to r.isActive,
                    "barangayId" to barangayId,
                    "createdAt" to r.createdAt,
                    "updatedAt" to r.updatedAt
                )
                firestore.collection("residents")
                    .document(r.residentId)
                    .set(data, SetOptions.merge())
                    .await()

                residentDao.updateSyncStatus(r.residentId, "SYNCED", System.currentTimeMillis())
                Log.d("LinKapSync", "Uploaded resident ID '${r.residentId}' -> SYNCED")
            } catch (e: Exception) {
                Log.e("LinKapSync", "Error uploading resident ID '${r.residentId}'", e)
                residentDao.updateSyncStatus(r.residentId, "FAILED", null)
                success = false
            }
        }
        return success
    }

    private suspend fun uploadVisits(barangayId: String): Boolean {
        var success = true
        val pending = visitDao.getPendingVisits(barangayId)
        Log.d("LinKapSync", "uploadVisits() -> pending count: ${pending.size}")
        for (v in pending) {
            try {
                val visitResidents = visitDao.getVisitResidentsForVisit(v.visitId)
                val residentIds = visitResidents.map { it.residentId }

                val data = hashMapOf(
                    "visitId" to v.visitId,
                    "householdId" to v.householdId,
                    "visitDate" to v.visitDate,
                    "visitCategory" to v.visitCategory,
                    "notes" to v.notes,
                    "latitude" to v.latitude,
                    "longitude" to v.longitude,
                    "residentIds" to residentIds,
                    "barangayId" to barangayId,
                    "createdAt" to v.createdAt,
                    "updatedAt" to v.updatedAt
                )
                firestore.collection("visits")
                    .document(v.visitId)
                    .set(data, SetOptions.merge())
                    .await()

                visitDao.updateSyncStatus(v.visitId, "SYNCED", System.currentTimeMillis())
                Log.d("LinKapSync", "Uploaded visit ID '${v.visitId}' -> SYNCED")
            } catch (e: Exception) {
                Log.e("LinKapSync", "Error uploading visit ID '${v.visitId}'", e)
                visitDao.updateSyncStatus(v.visitId, "FAILED", null)
                success = false
            }
        }
        return success
    }

    private suspend fun uploadFollowUps(barangayId: String): Boolean {
        var success = true
        val pending = followUpDao.getPendingSyncFollowUps(barangayId)
        Log.d("LinKapSync", "uploadFollowUps() -> pending count: ${pending.size}")
        for (f in pending) {
            try {
                val data = hashMapOf(
                    "followUpId" to f.followUpId,
                    "householdId" to f.householdId,
                    "title" to f.title,
                    "description" to f.description,
                    "dueDate" to f.dueDate,
                    "status" to f.status,
                    "barangayId" to barangayId,
                    "createdAt" to f.createdAt,
                    "updatedAt" to f.updatedAt
                )
                firestore.collection("follow_ups")
                    .document(f.followUpId)
                    .set(data, SetOptions.merge())
                    .await()

                followUpDao.updateSyncStatus(f.followUpId, "SYNCED", System.currentTimeMillis())
                Log.d("LinKapSync", "Uploaded follow-up ID '${f.followUpId}' -> SYNCED")
            } catch (e: Exception) {
                Log.e("LinKapSync", "Error uploading follow-up ID '${f.followUpId}'", e)
                followUpDao.updateSyncStatus(f.followUpId, "FAILED", null)
                success = false
            }
        }
        return success
    }

    private suspend fun downloadHouseholds(barangayId: String): Boolean {
        Log.d("LinKapSync", "downloadHouseholds() Query Started for barangayId='$barangayId'")
        return try {
            val snapshot = firestore.collection("households")
                .whereEqualTo("barangayId", barangayId)
                .get()
                .await()

            val docs = snapshot.documents
            Log.d("LinKapSync", "downloadHouseholds() Firestore Query Succeeded -> Returned ${docs.size} documents.")

            val now = System.currentTimeMillis()
            var insertedCount = 0
            var skippedCount = 0

            for (doc in docs) {
                val docBarangayId = doc.getString("barangayId")
                if (docBarangayId != barangayId) {
                    Log.d("LinKapSync", "downloadHouseholds() -> Barangay Mismatch in doc '${doc.id}' (found '$docBarangayId', expected '$barangayId'). Skipping.")
                    skippedCount++
                    continue
                }

                val id = doc.getString("householdId") ?: doc.id
                val existingAny = householdDao.getHouseholdByIdRawAnyBarangay(id)

                if (existingAny != null) {
                    if (existingAny.barangayId != barangayId && existingAny.barangayId.isNotBlank()) {
                        Log.d("LinKapSync", "downloadHouseholds() -> Cross-Barangay Conflict for household '$id' (local: '${existingAny.barangayId}', remote: '$barangayId'). Skipping.")
                        skippedCount++
                        continue
                    }
                    if (existingAny.syncStatus == "PENDING" || existingAny.syncStatus == "FAILED") {
                        Log.d("LinKapSync", "downloadHouseholds() -> Local household '$id' has un-synced status '${existingAny.syncStatus}'. Skipping.")
                        skippedCount++
                        continue
                    }
                }

                val remote = Household(
                    householdId = id,
                    householdCode = doc.getString("householdCode") ?: "",
                    purok = doc.getString("purok") ?: "",
                    street = doc.getString("street"),
                    barangay = doc.getString("barangay") ?: "",
                    latitude = doc.getDouble("latitude"),
                    longitude = doc.getDouble("longitude"),
                    barangayId = barangayId,
                    syncStatus = "SYNCED",
                    createdAt = doc.getLong("createdAt") ?: now,
                    updatedAt = doc.getLong("updatedAt") ?: now,
                    lastSyncedAt = now
                )
                householdDao.insertHousehold(remote)
                insertedCount++
            }
            Log.d("LinKapSync", "downloadHouseholds() Finished -> Inserted: $insertedCount, Skipped: $skippedCount")
            true
        } catch (e: Exception) {
            Log.e("LinKapSync", "downloadHouseholds() Firestore Query or Processing Failed for barangayId='$barangayId'", e)
            false
        }
    }

    private suspend fun downloadResidents(barangayId: String): Boolean {
        Log.d("LinKapSync", "downloadResidents() Query Started for barangayId='$barangayId'")
        return try {
            val snapshot = firestore.collection("residents")
                .whereEqualTo("barangayId", barangayId)
                .get()
                .await()

            val docs = snapshot.documents
            Log.d("LinKapSync", "downloadResidents() Firestore Query Succeeded -> Returned ${docs.size} documents.")

            val now = System.currentTimeMillis()
            var insertedCount = 0
            var skippedCount = 0
            var parentRelFailures = 0

            for (doc in docs) {
                val docBarangayId = doc.getString("barangayId")
                if (docBarangayId != barangayId) {
                    skippedCount++
                    continue
                }

                val householdId = doc.getString("householdId") ?: ""
                if (householdId.isBlank()) {
                    Log.d("LinKapSync", "downloadResidents() -> Blank householdId in doc '${doc.id}'. Skipping.")
                    parentRelFailures++
                    continue
                }

                val parentHousehold = householdDao.getHouseholdByIdRaw(householdId, barangayId)
                if (parentHousehold == null) {
                    Log.d("LinKapSync", "downloadResidents() Parent Household Validation Failure -> Parent household '$householdId' not found in Room for barangayId='$barangayId'. Skipping resident '${doc.id}'.")
                    parentRelFailures++
                    continue
                }

                val id = doc.getString("residentId") ?: doc.id
                val existingAny = residentDao.getResidentByIdAnyBarangay(id)

                if (existingAny != null) {
                    if (existingAny.barangayId != barangayId && existingAny.barangayId.isNotBlank()) {
                        Log.d("LinKapSync", "downloadResidents() -> Cross-Barangay Conflict for resident '$id'. Skipping.")
                        skippedCount++
                        continue
                    }
                    if (existingAny.syncStatus == "PENDING" || existingAny.syncStatus == "FAILED") {
                        skippedCount++
                        continue
                    }
                }

                val remote = Resident(
                    residentId = id,
                    householdId = householdId,
                    firstName = doc.getString("firstName") ?: "",
                    middleName = doc.getString("middleName"),
                    lastName = doc.getString("lastName") ?: "",
                    birthDate = doc.getLong("birthDate"),
                    sex = doc.getString("sex") ?: "Other",
                    relationshipToHead = doc.getString("relationshipToHead") ?: "Member",
                    contactNumber = doc.getString("contactNumber"),
                    isActive = doc.getBoolean("isActive") ?: true,
                    barangayId = barangayId,
                    syncStatus = "SYNCED",
                    createdAt = doc.getLong("createdAt") ?: now,
                    updatedAt = doc.getLong("updatedAt") ?: now,
                    lastSyncedAt = now
                )
                residentDao.insertResident(remote)
                insertedCount++
            }
            Log.d("LinKapSync", "downloadResidents() Finished -> Inserted: $insertedCount, Skipped: $skippedCount, ParentRelFailures: $parentRelFailures")
            true
        } catch (e: Exception) {
            Log.e("LinKapSync", "downloadResidents() Firestore Query or Processing Failed for barangayId='$barangayId'", e)
            false
        }
    }

    private suspend fun downloadVisits(barangayId: String): Boolean {
        Log.d("LinKapSync", "downloadVisits() Query Started for barangayId='$barangayId'")
        return try {
            val snapshot = firestore.collection("visits")
                .whereEqualTo("barangayId", barangayId)
                .get()
                .await()

            val docs = snapshot.documents
            Log.d("LinKapSync", "downloadVisits() Firestore Query Succeeded -> Returned ${docs.size} documents.")

            val now = System.currentTimeMillis()
            var insertedCount = 0
            var skippedCount = 0
            var parentRelFailures = 0

            for (doc in docs) {
                val docBarangayId = doc.getString("barangayId")
                if (docBarangayId != barangayId) {
                    skippedCount++
                    continue
                }

                val householdId = doc.getString("householdId") ?: ""
                if (householdId.isBlank()) {
                    parentRelFailures++
                    continue
                }

                val parentHousehold = householdDao.getHouseholdByIdRaw(householdId, barangayId)
                if (parentHousehold == null) {
                    Log.d("LinKapSync", "downloadVisits() Parent Household Validation Failure -> Parent household '$householdId' not found in Room for barangayId='$barangayId'. Skipping visit '${doc.id}'.")
                    parentRelFailures++
                    continue
                }

                val id = doc.getString("visitId") ?: doc.id
                val existingAny = visitDao.getVisitByIdRawAnyBarangay(id)

                if (existingAny != null) {
                    if (existingAny.barangayId != barangayId && existingAny.barangayId.isNotBlank()) {
                        Log.d("LinKapSync", "downloadVisits() -> Cross-Barangay Conflict for visit '$id'. Skipping.")
                        skippedCount++
                        continue
                    }
                    if (existingAny.syncStatus == "PENDING" || existingAny.syncStatus == "FAILED") {
                        skippedCount++
                        continue
                    }
                }

                val remote = Visit(
                    visitId = id,
                    householdId = householdId,
                    visitDate = doc.getLong("visitDate") ?: now,
                    visitCategory = doc.getString("visitCategory") ?: "Routine Checkup",
                    notes = doc.getString("notes"),
                    latitude = doc.getDouble("latitude"),
                    longitude = doc.getDouble("longitude"),
                    barangayId = barangayId,
                    syncStatus = "SYNCED",
                    createdAt = doc.getLong("createdAt") ?: now,
                    updatedAt = doc.getLong("updatedAt") ?: now,
                    lastSyncedAt = now
                )

                val rawResidentIds = doc.get("residentIds")
                val residentIds = (rawResidentIds as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

                val validVisitResidents = residentIds.mapNotNull { residentId ->
                    if (residentDao.getResidentById(residentId, barangayId) != null) {
                        VisitResident(visitId = id, residentId = residentId)
                    } else {
                        Log.d("LinKapSync", "downloadVisits() Resident Rel Failure -> Resident '$residentId' not found in Room for visit '$id'.")
                        null
                    }
                }

                visitDao.insertVisitWithResidents(remote, validVisitResidents)
                insertedCount++
            }
            Log.d("LinKapSync", "downloadVisits() Finished -> Inserted: $insertedCount, Skipped: $skippedCount, ParentRelFailures: $parentRelFailures")
            true
        } catch (e: Exception) {
            Log.e("LinKapSync", "downloadVisits() Firestore Query or Processing Failed for barangayId='$barangayId'", e)
            false
        }
    }

    private suspend fun downloadFollowUps(barangayId: String): Boolean {
        Log.d("LinKapSync", "downloadFollowUps() Query Started for barangayId='$barangayId'")
        return try {
            val snapshot = firestore.collection("follow_ups")
                .whereEqualTo("barangayId", barangayId)
                .get()
                .await()

            val docs = snapshot.documents
            Log.d("LinKapSync", "downloadFollowUps() Firestore Query Succeeded -> Returned ${docs.size} documents.")

            val now = System.currentTimeMillis()
            var insertedCount = 0
            var skippedCount = 0
            var parentRelFailures = 0

            for (doc in docs) {
                val docBarangayId = doc.getString("barangayId")
                if (docBarangayId != barangayId) {
                    skippedCount++
                    continue
                }

                val householdId = doc.getString("householdId") ?: ""
                if (householdId.isBlank()) {
                    parentRelFailures++
                    continue
                }

                val parentHousehold = householdDao.getHouseholdByIdRaw(householdId, barangayId)
                if (parentHousehold == null) {
                    Log.d("LinKapSync", "downloadFollowUps() Parent Household Validation Failure -> Parent household '$householdId' not found in Room for barangayId='$barangayId'. Skipping follow-up '${doc.id}'.")
                    parentRelFailures++
                    continue
                }

                val id = doc.getString("followUpId") ?: doc.id
                val existingAny = followUpDao.getFollowUpByIdAnyBarangay(id)

                if (existingAny != null) {
                    if (existingAny.barangayId != barangayId && existingAny.barangayId.isNotBlank()) {
                        Log.d("LinKapSync", "downloadFollowUps() -> Cross-Barangay Conflict for follow-up '$id'. Skipping.")
                        skippedCount++
                        continue
                    }
                    if (existingAny.syncStatus == "PENDING" || existingAny.syncStatus == "FAILED") {
                        skippedCount++
                        continue
                    }
                }

                val remote = FollowUp(
                    followUpId = id,
                    householdId = householdId,
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description"),
                    dueDate = doc.getLong("dueDate") ?: now,
                    status = doc.getString("status") ?: "PENDING",
                    barangayId = barangayId,
                    syncStatus = "SYNCED",
                    createdAt = doc.getLong("createdAt") ?: now,
                    updatedAt = doc.getLong("updatedAt") ?: now,
                    lastSyncedAt = now
                )
                followUpDao.insertFollowUp(remote)
                insertedCount++
            }
            Log.d("LinKapSync", "downloadFollowUps() Finished -> Inserted: $insertedCount, Skipped: $skippedCount, ParentRelFailures: $parentRelFailures")
            true
        } catch (e: Exception) {
            Log.e("LinKapSync", "downloadFollowUps() Firestore Query or Processing Failed for barangayId='$barangayId'", e)
            false
        }
    }

    private fun rawQueryCount(sql: String): Int {
        val cursor = database.openHelper.readableDatabase.query(sql)
        var count = 0
        if (cursor.moveToNext()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }
}
