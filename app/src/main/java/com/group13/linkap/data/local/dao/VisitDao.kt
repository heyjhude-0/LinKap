
package com.group13.linkap.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.group13.linkap.data.local.entity.Visit
import kotlinx.coroutines.flow.Flow

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
}
