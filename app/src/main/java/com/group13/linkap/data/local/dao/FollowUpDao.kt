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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUps(followUps: List<FollowUp>)

    @Update
    suspend fun updateFollowUp(followUp: FollowUp)

    @Delete
    suspend fun deleteFollowUp(followUp: FollowUp)

    @Query("SELECT * FROM follow_ups WHERE barangayId = :barangayId ORDER BY dueDate ASC")
    fun getAllFollowUps(barangayId: String): Flow<List<FollowUp>>

    @Query("""
        SELECT * FROM follow_ups
        WHERE householdId = :householdId AND barangayId = :barangayId
        ORDER BY dueDate ASC
    """)
    fun getFollowUpsByHousehold(householdId: String, barangayId: String): Flow<List<FollowUp>>

    @Query("""
        SELECT * FROM follow_ups
        WHERE status = 'PENDING' AND barangayId = :barangayId
        ORDER BY dueDate ASC
    """)
    fun getPendingFollowUps(barangayId: String): Flow<List<FollowUp>>

    @Query("""
        SELECT * FROM follow_ups
        WHERE syncStatus IN ('PENDING', 'FAILED') AND barangayId = :barangayId
    """)
    suspend fun getPendingSyncFollowUps(barangayId: String): List<FollowUp>

    @Query("""
        SELECT * FROM follow_ups
        WHERE status = 'COMPLETED' AND barangayId = :barangayId
        ORDER BY dueDate DESC
    """)
    fun getCompletedFollowUps(barangayId: String): Flow<List<FollowUp>>

    @Query("""
        SELECT * FROM follow_ups
        WHERE status = 'PENDING' AND dueDate < :currentTime AND barangayId = :barangayId
        ORDER BY dueDate ASC
    """)
    fun getOverdueFollowUps(currentTime: Long, barangayId: String): Flow<List<FollowUp>>

    @Query("SELECT * FROM follow_ups WHERE followUpId = :followUpId AND barangayId = :barangayId LIMIT 1")
    suspend fun getFollowUpById(followUpId: String, barangayId: String): FollowUp?

    @Query("SELECT * FROM follow_ups WHERE followUpId = :followUpId LIMIT 1")
    suspend fun getFollowUpByIdAnyBarangay(followUpId: String): FollowUp?

    @Query("SELECT * FROM follow_ups WHERE status = :status AND barangayId = :barangayId ORDER BY dueDate ASC")
    fun getFollowUpsByStatus(status: String, barangayId: String): Flow<List<FollowUp>>

    @Query("UPDATE follow_ups SET syncStatus = :status, lastSyncedAt = :lastSyncedAt WHERE followUpId = :followUpId")
    suspend fun updateSyncStatus(followUpId: String, status: String, lastSyncedAt: Long? = System.currentTimeMillis())
}
