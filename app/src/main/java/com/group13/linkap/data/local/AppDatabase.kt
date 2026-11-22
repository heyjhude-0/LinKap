
package com.group13.linkap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.group13.linkap.data.local.dao.ConnectionLogDao
import com.group13.linkap.data.local.dao.FollowUpDao
import com.group13.linkap.data.local.dao.HouseholdDao
import com.group13.linkap.data.local.dao.ResidentDao
import com.group13.linkap.data.local.dao.VisitDao
import com.group13.linkap.data.local.entity.ConnectionLog
import com.group13.linkap.data.local.entity.FollowUp
import com.group13.linkap.data.local.entity.Household
import com.group13.linkap.data.local.entity.Resident
import com.group13.linkap.data.local.entity.Visit

@Database(
    entities = [
        Household::class,
        Resident::class,
        Visit::class,
        FollowUp::class,
        ConnectionLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun householdDao(): HouseholdDao
    abstract fun residentDao(): ResidentDao
    abstract fun visitDao(): VisitDao
    abstract fun followUpDao(): FollowUpDao
    abstract fun connectionLogDao(): ConnectionLogDao
}
