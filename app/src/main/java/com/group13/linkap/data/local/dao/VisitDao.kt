
package com.group13.linkap.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.group13.linkap.data.local.entity.Visit
import kotlinx.coroutines.flow.Flow
import androidx.room.Transaction
import com.group13.linkap.data.local.entity.Resident
import com.group13.linkap.data.local.entity.VisitResident

@Dao
interface VisitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: Visit)

    @Update
    suspend fun updateVisit(visit: Visit)

    @Delete
    suspend fun deleteVisit(visit: Visit)

    @Query("SELECT * FROM visits ORDER BY visitDate DESC")
    fun getAllVisits(): Flow<List<Visit>>

    @Query("SELECT * FROM visits WHERE householdId = :householdId ORDER BY visitDate DESC")
    fun getVisitsByHousehold(householdId: String): Flow<List<Visit>>

    @Query("SELECT * FROM visits WHERE syncStatus = 'PENDING'")
    suspend fun getPendingVisits(): List<Visit>

    @Query("UPDATE visits SET syncStatus = :status WHERE visitId = :visitId")
    suspend fun updateSyncStatus(visitId: String, status: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitResidents(residents: List<VisitResident>)

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
        WHERE visit_residents.visitId = :visitId
    """)
        suspend fun getResidentsForVisit(
            visitId: String
        ): List<Resident>
}
