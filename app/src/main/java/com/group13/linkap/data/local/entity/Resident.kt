package com.group13.linkap.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "residents",
    foreignKeys = [
        ForeignKey(
            entity = Household::class,
            parentColumns = ["householdId"],
            childColumns = ["householdId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("householdId")]
)
data class Resident(
    @PrimaryKey
    val residentId: String,

    val householdId: String,

    val firstName: String,
    val middleName: String?,
    val lastName: String,

    val birthDate: Long?,
    val sex: String,
    val relationshipToHead: String,

    val contactNumber: String?,
    val isActive: Boolean = true,

    val syncStatus: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)
