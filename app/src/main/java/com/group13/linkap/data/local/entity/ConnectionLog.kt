
package com.group13.linkap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connection_logs")
data class ConnectionLog(
    @PrimaryKey(autoGenerate = true)
    val logId: Int = 0,

    val eventType: String,
    val message: String?,
    val timestamp: Long,

    val syncStatus: String
)
