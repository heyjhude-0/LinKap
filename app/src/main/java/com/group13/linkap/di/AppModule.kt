package com.group13.linkap.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.group13.linkap.data.local.AppDatabase
import com.group13.linkap.data.local.UserSessionManager
import com.group13.linkap.data.repository.FollowUpRepository
import com.group13.linkap.data.repository.HouseholdRepository
import com.group13.linkap.data.repository.ResidentRepository
import com.group13.linkap.data.repository.VisitRepository

object AppModule {

    @Volatile
    private var database: AppDatabase? = null

    @Volatile
    private var userSessionManager: UserSessionManager? = null

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
            CREATE TABLE IF NOT EXISTS `visit_residents` (
                `visitId` TEXT NOT NULL,
                `residentId` TEXT NOT NULL,
                PRIMARY KEY(`visitId`, `residentId`),
                FOREIGN KEY(`visitId`) REFERENCES `visits`(`visitId`)
                    ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`residentId`) REFERENCES `residents`(`residentId`)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
            )

            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_visit_residents_residentId` ON `visit_residents` (`residentId`)"
            )
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE households ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING'")
            db.execSQL("ALTER TABLE households ADD COLUMN lastSyncedAt INTEGER DEFAULT NULL")

            db.execSQL("ALTER TABLE residents ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING'")
            db.execSQL("ALTER TABLE residents ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE residents ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE residents ADD COLUMN lastSyncedAt INTEGER DEFAULT NULL")

            db.execSQL("ALTER TABLE visits ADD COLUMN lastSyncedAt INTEGER DEFAULT NULL")

            db.execSQL("ALTER TABLE follow_ups ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING'")
            db.execSQL("ALTER TABLE follow_ups ADD COLUMN lastSyncedAt INTEGER DEFAULT NULL")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE households ADD COLUMN barangayId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE residents ADD COLUMN barangayId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE visits ADD COLUMN barangayId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE follow_ups ADD COLUMN barangayId TEXT NOT NULL DEFAULT ''")
        }
    }

    fun provideDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "linkap_database"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
                .also {
                    database = it
                }
        }
    }

    fun provideUserSessionManager(context: Context): UserSessionManager {
        return userSessionManager ?: synchronized(this) {
            userSessionManager ?: UserSessionManager(context.applicationContext).also {
                userSessionManager = it
            }
        }
    }

    fun provideHouseholdRepository(context: Context): HouseholdRepository {
        return HouseholdRepository(
            provideDatabase(context).householdDao(),
            provideUserSessionManager(context)
        )
    }

    fun provideResidentRepository(context: Context): ResidentRepository {
        return ResidentRepository(
            provideDatabase(context).residentDao(),
            provideUserSessionManager(context)
        )
    }

    fun provideVisitRepository(context: Context): VisitRepository {
        return VisitRepository(
            provideDatabase(context).visitDao(),
            provideUserSessionManager(context)
        )
    }

    fun provideFollowUpRepository(context: Context): FollowUpRepository {
        return FollowUpRepository(
            provideDatabase(context).followUpDao(),
            provideUserSessionManager(context)
        )
    }
}
