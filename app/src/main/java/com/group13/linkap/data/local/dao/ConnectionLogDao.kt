
package com.group13.linkap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.group13.linkap.data.local.entity.ConnectionLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionLogDao {

    @Insert
    suspend fun insertLog(log: ConnectionLog)

    @Query("SELECT * FROM connection_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ConnectionLog>>

    @Query("DELETE FROM connection_logs")
    suspend fun clearLogs()
}
