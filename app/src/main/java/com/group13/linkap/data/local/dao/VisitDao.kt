package com.group13.linkap.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.group13.linkap.data.local.entity.Resident
import com.group13.linkap.data.local.entity.Visit
import com.group13.linkap.data.local.entity.VisitResident
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: Visit)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisits(visits: List<Visit>)

    @Update
    suspend fun updateVisit(visit: Visit)

    @Delete
    suspend fun deleteVisit(visit: Visit)

    @Query("SELECT * FROM visits WHERE barangayId = :barangayId ORDER BY visitDate DESC")
    fun getAllVisits(barangayId: String): Flow<List<Visit>>

    @Query("SELECT * FROM visits WHERE householdId = :householdId AND barangayId = :barangayId ORDER BY visitDate DESC")
    fun getVisitsByHousehold(householdId: String, barangayId: String): Flow<List<Visit>>

    @Query("SELECT * FROM visits WHERE visitId = :visitId AND barangayId = :barangayId LIMIT 1")
    suspend fun getVisitByIdRaw(visitId: String, barangayId: String): Visit?

    @Query("SELECT * FROM visits WHERE visitId = :visitId LIMIT 1")
    suspend fun getVisitByIdRawAnyBarangay(visitId: String): Visit?

    @Query("SELECT * FROM visits WHERE syncStatus IN ('PENDING', 'FAILED') AND barangayId = :barangayId")
    suspend fun getPendingVisits(barangayId: String): List<Visit>

    @Query("UPDATE visits SET syncStatus = :status, lastSyncedAt = :lastSyncedAt WHERE visitId = :visitId")
    suspend fun updateSyncStatus(visitId: String, status: String, lastSyncedAt: Long? = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitResidents(residents: List<VisitResident>)

    @Query("SELECT * FROM visit_residents WHERE visitId = :visitId")
    suspend fun getVisitResidentsForVisit(visitId: String): List<VisitResident>

    @Transaction
    suspend fun insertVisitWithResidents(
        visit: Visit,
        residents: List<VisitResident>
    ) {
        insertVisit(visit)
        insertVisitResidents(residents)
    }

    @Transaction
    @Query("""
        SELECT residents.* 
        FROM residents
        INNER JOIN visit_residents
        ON residents.residentId = visit_residents.residentId
        WHERE visit_residents.visitId = :visitId AND residents.barangayId = :barangayId
    """)
    suspend fun getResidentsForVisit(
        visitId: String,
        barangayId: String
    ): List<Resident>
}
