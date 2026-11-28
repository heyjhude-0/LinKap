package com.group13.linkap.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.group13.linkap.data.local.entity.Household
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseholdDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHousehold(household: Household)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHouseholds(households: List<Household>)

    @Update
    suspend fun updateHousehold(household: Household)

    @Delete
    suspend fun deleteHousehold(household: Household)

    @Query("SELECT * FROM households WHERE barangayId = :barangayId ORDER BY householdCode ASC")
    fun getAllHouseholds(barangayId: String): Flow<List<Household>>

    @Query("SELECT * FROM households WHERE householdId = :householdId AND barangayId = :barangayId")
    fun getHouseholdById(householdId: String, barangayId: String): Flow<Household?>

    @Query("SELECT * FROM households WHERE householdId = :householdId AND barangayId = :barangayId LIMIT 1")
    suspend fun getHouseholdByIdRaw(householdId: String, barangayId: String): Household?

    @Query("SELECT * FROM households WHERE householdId = :householdId LIMIT 1")
    suspend fun getHouseholdByIdRawAnyBarangay(householdId: String): Household?

    @Query("SELECT * FROM households WHERE LOWER(householdCode) = LOWER(:householdCode) AND barangayId = :barangayId LIMIT 1")
    suspend fun getHouseholdByCode(householdCode: String, barangayId: String): Household?

    @Query("SELECT * FROM households WHERE syncStatus IN ('PENDING', 'FAILED') AND barangayId = :barangayId")
    suspend fun getPendingHouseholds(barangayId: String): List<Household>

    @Query("UPDATE households SET syncStatus = :status, lastSyncedAt = :lastSyncedAt WHERE householdId = :householdId")
    suspend fun updateSyncStatus(householdId: String, status: String, lastSyncedAt: Long? = System.currentTimeMillis())
}
