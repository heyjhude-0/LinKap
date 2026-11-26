package com.group13.linkap.sync

import android.content.Context
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
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User is not authenticated."))

            val uid = currentUser.uid
            val userDoc = firestore.collection("users").document(uid).get().await()

            if (!userDoc.exists()) {
                return Result.failure(Exception("User profile users/$uid does not exist."))
            }

            val isActive = userDoc.getBoolean("isActive") ?: false
            val role = userDoc.getString("role")
            val barangayId = userDoc.getString("barangayId")?.trim()

            if (!isActive) {
                return Result.failure(Exception("Account is deactivated."))
            }
            if (role != "BHW") {
                return Result.failure(Exception("Account is not authorized for BHW access."))
            }
            if (barangayId.isNullOrBlank()) {
                return Result.failure(Exception("No barangayId assigned to user profile."))
            }

            // 1. Upload local changes in dependency order (Households -> Residents -> Visits -> FollowUps)
            val uHouseholds = uploadHouseholds(barangayId)
            val uResidents = uploadResidents(barangayId)
            val uVisits = uploadVisits(barangayId)
            val uFollowUps = uploadFollowUps(barangayId)

            // 2. Download remote changes in dependency order (Households -> Residents -> Visits -> FollowUps)
            val dHouseholds = downloadHouseholds(barangayId)
            val dResidents = downloadResidents(barangayId)
            val dVisits = downloadVisits(barangayId)
            val dFollowUps = downloadFollowUps(barangayId)

            val allSuccess = uHouseholds && uResidents && uVisits && uFollowUps &&
                    dHouseholds && dResidents && dVisits && dFollowUps

            if (allSuccess) {
                syncPreferences.setLastSyncTime(System.currentTimeMillis())
                Result.success(Unit)
            } else {
                Result.failure(Exception("Synchronization completed with partial errors."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun uploadHouseholds(barangayId: String): Boolean {
        var success = true
        val pending = householdDao.getPendingHouseholds()
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

                householdDao.updateSyncStatus(
                    h.householdId,
                    "SYNCED",
                    System.currentTimeMillis()
                )
            } catch (e: Exception) {
                householdDao.updateSyncStatus(h.householdId, "FAILED", null)
                success = false
            }
        }
        return success
    }

    private suspend fun uploadResidents(barangayId: String): Boolean {
        var success = true
        val pending = residentDao.getPendingResidents()
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

                residentDao.updateSyncStatus(
                    r.residentId,
                    "SYNCED",
                    System.currentTimeMillis()
                )
            } catch (e: Exception) {
                residentDao.updateSyncStatus(r.residentId, "FAILED", null)
                success = false
            }
        }
        return success
    }

    private suspend fun uploadVisits(barangayId: String): Boolean {
        var success = true
        val pending = visitDao.getPendingVisits()
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

                visitDao.updateSyncStatus(
                    v.visitId,
                    "SYNCED",
                    System.currentTimeMillis()
                )
            } catch (e: Exception) {
                visitDao.updateSyncStatus(v.visitId, "FAILED", null)
                success = false
            }
        }
        return success
    }

    private suspend fun uploadFollowUps(barangayId: String): Boolean {
        var success = true
        val pending = followUpDao.getPendingSyncFollowUps()
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

                followUpDao.updateSyncStatus(
                    f.followUpId,
                    "SYNCED",
                    System.currentTimeMillis()
                )
            } catch (e: Exception) {
                followUpDao.updateSyncStatus(f.followUpId, "FAILED", null)
                success = false
            }
        }
        return success
    }

    private suspend fun downloadHouseholds(barangayId: String): Boolean {
        return try {
            val snapshot = firestore.collection("households")
                .whereEqualTo("barangayId", barangayId)
                .get()
                .await()

            val now = System.currentTimeMillis()

            for (doc in snapshot.documents) {
                val docBarangayId = doc.getString("barangayId")
                if (docBarangayId != barangayId) {
                    continue
                }

                val id = doc.getString("householdId") ?: doc.id
                val local = householdDao.getHouseholdByIdRaw(id)

                // Conflict handling: Preserve pending or failed local changes
                if (local != null && (local.syncStatus == "PENDING" || local.syncStatus == "FAILED")) {
                    continue
                }

                val remote = Household(
                    householdId = id,
                    householdCode = doc.getString("householdCode") ?: "",
                    purok = doc.getString("purok") ?: "",
                    street = doc.getString("street"),
                    barangay = doc.getString("barangay") ?: "",
                    latitude = doc.getDouble("latitude"),
                    longitude = doc.getDouble("longitude"),
                    syncStatus = "SYNCED",
                    createdAt = doc.getLong("createdAt") ?: now,
                    updatedAt = doc.getLong("updatedAt") ?: now,
                    lastSyncedAt = now
                )
                householdDao.insertHousehold(remote)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun downloadResidents(barangayId: String): Boolean {
        return try {
            val snapshot = firestore.collection("residents")
                .whereEqualTo("barangayId", barangayId)
                .get()
                .await()

            val now = System.currentTimeMillis()

            for (doc in snapshot.documents) {
                val docBarangayId = doc.getString("barangayId")
                if (docBarangayId != barangayId) {
                    continue
                }

                val householdId = doc.getString("householdId") ?: ""
                if (householdId.isBlank()) continue

                // Relationship Validation: Verify referenced parent household exists in Room DB
                val parentHousehold = householdDao.getHouseholdByIdRaw(householdId)
                if (parentHousehold == null) {
                    continue
                }

                val id = doc.getString("residentId") ?: doc.id
                val local = residentDao.getResidentById(id)

                if (local != null && (local.syncStatus == "PENDING" || local.syncStatus == "FAILED")) {
                    continue
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
                    syncStatus = "SYNCED",
                    createdAt = doc.getLong("createdAt") ?: now,
                    updatedAt = doc.getLong("updatedAt") ?: now,
                    lastSyncedAt = now
                )
                residentDao.insertResident(remote)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun downloadVisits(barangayId: String): Boolean {
        return try {
            val snapshot = firestore.collection("visits")
                .whereEqualTo("barangayId", barangayId)
                .get()
                .await()

            val now = System.currentTimeMillis()

            for (doc in snapshot.documents) {
                val docBarangayId = doc.getString("barangayId")
                if (docBarangayId != barangayId) {
                    continue
                }

                val householdId = doc.getString("householdId") ?: ""
                if (householdId.isBlank()) continue

                // Relationship Validation: Verify referenced parent household exists in Room DB
                val parentHousehold = householdDao.getHouseholdByIdRaw(householdId)
                if (parentHousehold == null) {
                    continue
                }

                val id = doc.getString("visitId") ?: doc.id
                val local = visitDao.getVisitByIdRaw(id)

                if (local != null && (local.syncStatus == "PENDING" || local.syncStatus == "FAILED")) {
                    continue
                }

                val remote = Visit(
                    visitId = id,
                    householdId = householdId,
                    visitDate = doc.getLong("visitDate") ?: now,
                    visitCategory = doc.getString("visitCategory") ?: "Routine Checkup",
                    notes = doc.getString("notes"),
                    latitude = doc.getDouble("latitude"),
                    longitude = doc.getDouble("longitude"),
                    syncStatus = "SYNCED",
                    createdAt = doc.getLong("createdAt") ?: now,
                    updatedAt = doc.getLong("updatedAt") ?: now,
                    lastSyncedAt = now
                )

                val rawResidentIds = doc.get("residentIds")
                val residentIds = (rawResidentIds as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

                // Filter visitResidents to valid local residents to ensure foreign key integrity
                val validVisitResidents = residentIds.mapNotNull { residentId ->
                    if (residentDao.getResidentById(residentId) != null) {
                        VisitResident(visitId = id, residentId = residentId)
                    } else {
                        null
                    }
                }

                visitDao.insertVisitWithResidents(remote, validVisitResidents)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun downloadFollowUps(barangayId: String): Boolean {
        return try {
            val snapshot = firestore.collection("follow_ups")
                .whereEqualTo("barangayId", barangayId)
                .get()
                .await()

            val now = System.currentTimeMillis()

            for (doc in snapshot.documents) {
                val docBarangayId = doc.getString("barangayId")
                if (docBarangayId != barangayId) {
                    continue
                }

                val householdId = doc.getString("householdId") ?: ""
                if (householdId.isBlank()) continue

                // Relationship Validation: Verify referenced parent household exists in Room DB
                val parentHousehold = householdDao.getHouseholdByIdRaw(householdId)
                if (parentHousehold == null) {
                    continue
                }

                val id = doc.getString("followUpId") ?: doc.id
                val local = followUpDao.getFollowUpById(id)

                if (local != null && (local.syncStatus == "PENDING" || local.syncStatus == "FAILED")) {
                    continue
                }

                val remote = FollowUp(
                    followUpId = id,
                    householdId = householdId,
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description"),
                    dueDate = doc.getLong("dueDate") ?: now,
                    status = doc.getString("status") ?: "PENDING",
                    syncStatus = "SYNCED",
                    createdAt = doc.getLong("createdAt") ?: now,
                    updatedAt = doc.getLong("updatedAt") ?: now,
                    lastSyncedAt = now
                )
                followUpDao.insertFollowUp(remote)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
