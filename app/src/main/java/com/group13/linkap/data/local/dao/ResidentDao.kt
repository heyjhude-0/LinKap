package com.group13.linkap.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.group13.linkap.data.local.entity.Resident
import kotlinx.coroutines.flow.Flow

@Dao
interface ResidentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResident(resident: Resident)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResidents(residents: List<Resident>)

    @Update
    suspend fun updateResident(resident: Resident)

    @Delete
    suspend fun deleteResident(resident: Resident)

    @Query("SELECT * FROM residents ORDER BY lastName ASC")
    fun getAllResidents(): Flow<List<Resident>>

    @Query("SELECT * FROM residents WHERE householdId = :householdId ORDER BY lastName ASC")
    fun getResidentsByHousehold(householdId: String): Flow<List<Resident>>

    @Query("SELECT * FROM residents WHERE residentId = :residentId")
    suspend fun getResidentById(residentId: String): Resident?

    @Query("SELECT * FROM residents WHERE syncStatus IN ('PENDING', 'FAILED')")
    suspend fun getPendingResidents(): List<Resident>

    @Query("UPDATE residents SET syncStatus = :status, lastSyncedAt = :lastSyncedAt WHERE residentId = :residentId")
    suspend fun updateSyncStatus(residentId: String, status: String, lastSyncedAt: Long? = System.currentTimeMillis())
}
