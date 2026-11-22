
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

    @Update
    suspend fun updateHousehold(household: Household)

    @Delete
    suspend fun deleteHousehold(household: Household)

    @Query("SELECT * FROM households ORDER BY householdCode ASC")
    fun getAllHouseholds(): Flow<List<Household>>

    @Query("SELECT * FROM households WHERE householdId = :householdId")
    fun getHouseholdById(householdId: String): Flow<Household?>

    @Query("SELECT * FROM households WHERE LOWER(householdCode) = LOWER(:householdCode) LIMIT 1")
    suspend fun getHouseholdByCode(householdCode: String): Household?
}
