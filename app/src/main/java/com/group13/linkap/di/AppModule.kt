
package com.group13.linkap.di

import android.content.Context
import androidx.room.Room
import com.group13.linkap.data.local.AppDatabase
import com.group13.linkap.data.repository.FollowUpRepository
import com.group13.linkap.data.repository.HouseholdRepository
import com.group13.linkap.data.repository.ResidentRepository
import com.group13.linkap.data.repository.VisitRepository

object AppModule {

    @Volatile
    private var database: AppDatabase? = null

    fun provideDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "linkap_database"
            ).build().also {
                database = it
            }
        }
    }

    fun provideHouseholdRepository(context: Context): HouseholdRepository {
        return HouseholdRepository(
            provideDatabase(context).householdDao()
        )
    }

    fun provideResidentRepository(context: Context): ResidentRepository {
        return ResidentRepository(
            provideDatabase(context).residentDao()
        )
    }

    fun provideVisitRepository(context: Context): VisitRepository {
        return VisitRepository(
            provideDatabase(context).visitDao()
        )
    }

    fun provideFollowUpRepository(context: Context): FollowUpRepository {
        return FollowUpRepository(
            provideDatabase(context).followUpDao()
        )
    }
}
