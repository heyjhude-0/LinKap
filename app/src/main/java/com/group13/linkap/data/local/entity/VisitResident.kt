package com.group13.linkap.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "visit_residents",
    primaryKeys = ["visitId", "residentId"],
    foreignKeys = [
        ForeignKey(
            entity = Visit::class,
            parentColumns = ["visitId"],
            childColumns = ["visitId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Resident::class,
            parentColumns = ["residentId"],
            childColumns = ["residentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("residentId")
    ]
)
data class VisitResident(
    val visitId: String,
    val residentId: String
)