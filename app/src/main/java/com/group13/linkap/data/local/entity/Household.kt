package com.group13.linkap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "households")
data class Household(
    @PrimaryKey
    val householdId: String,

    val householdCode: String,
    val purok: String,
    val street: String?,
    val barangay: String,

    val latitude: Double?,
    val longitude: Double?,

    val createdAt: Long,
    val updatedAt: Long
)
