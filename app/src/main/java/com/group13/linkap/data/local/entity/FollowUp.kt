
package com.group13.linkap.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "follow_ups",
    foreignKeys = [
        ForeignKey(
            entity = Household::class,
            parentColumns = ["householdId"],
            childColumns = ["householdId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("householdId"), Index("dueDate")]
)
data class FollowUp(
    @PrimaryKey
    val followUpId: String,

    val householdId: String,

    val title: String,
    val description: String?,
    val dueDate: Long,

    val status: String = "PENDING",
    val createdAt: Long,
    val updatedAt: Long
)
