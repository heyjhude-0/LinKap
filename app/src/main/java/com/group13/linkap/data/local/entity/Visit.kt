package com.group13.linkap.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "visits",
    foreignKeys = [
        ForeignKey(
            entity = Household::class,
            parentColumns = ["householdId"],
            childColumns = ["householdId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("householdId"), Index("visitDate")]
)
data class Visit(
    @PrimaryKey
    val visitId: String,

    val householdId: String,

    val visitDate: Long,
    val visitCategory: String,
    val notes: String?,

    val latitude: Double?,
    val longitude: Double?,

    val syncStatus: String = "PENDING",
    val createdAt: Long,
    val updatedAt: Long,
    val lastSyncedAt: Long? = null
)
