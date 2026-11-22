
package com.group13.linkap.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.group13.linkap.data.local.entity.FollowUp
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowUpDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUp(followUp: FollowUp)

    @Update
    suspend fun updateFollowUp(followUp: FollowUp)

    @Delete
    suspend fun deleteFollowUp(followUp: FollowUp)

    @Query("SELECT * FROM follow_ups ORDER BY dueDate ASC")
    fun getAllFollowUps(): Flow<List<FollowUp>>

    @Query("SELECT * FROM follow_ups WHERE householdId = :householdId ORDER BY dueDate ASC")
    fun getFollowUpsByHousehold(householdId: String): Flow<List<FollowUp>>

    @Query("SELECT * FROM follow_ups WHERE status = 'PENDING' ORDER BY dueDate ASC")
    fun getPendingFollowUps(): Flow<List<FollowUp>>
}
