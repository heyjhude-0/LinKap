package com.group13.linkap

import org.junit.Test
import java.io.File
import java.sql.DriverManager

class ExampleUnitTest {
    @Test
    fun inspectDatabase() {
        val dbPath = "C:/Users/Admin/AppData/Local/Google/AndroidStudio2026.2.2/projects/linkap.f8454038/.artifacts/9f93adcf-e0be-405c-85dc-54fe53755bf1/scratch/linkap_database"
        val dbFile = File(dbPath)
        println("DB File exists: ${dbFile.exists()}, size: ${dbFile.length()} bytes")

        try {
            Class.forName("org.sqlite.JDBC")
            val conn = DriverManager.getConnection("jdbc:sqlite:$dbPath")
            val stmt = conn.createStatement()

            val versionRes = stmt.executeQuery("PRAGMA user_version;")
            if (versionRes.next()) {
                println("PRAGMA user_version: ${versionRes.getInt(1)}")
            }

            val tables = listOf("households", "residents", "visits", "follow_ups")
            for (t in tables) {
                val totalRes = stmt.executeQuery("SELECT count(*) FROM $t")
                val total = if (totalRes.next()) totalRes.getInt(1) else 0

                val emptyRes = stmt.executeQuery("SELECT count(*) FROM $t WHERE barangayId = '' OR barangayId IS NULL")
                val emptyCount = if (emptyRes.next()) emptyRes.getInt(1) else 0

                val alangilanRes = stmt.executeQuery("SELECT count(*) FROM $t WHERE barangayId = 'alangilan'")
                val alangilanCount = if (alangilanRes.next()) alangilanRes.getInt(1) else 0

                println("Table '$t': total=$total, empty_or_null=$emptyCount, alangilan=$alangilanCount")
            }

            println("\n--- Households Data ---")
            val hRes = stmt.executeQuery("SELECT householdId, householdCode, barangay, barangayId, syncStatus FROM households")
            while (hRes.next()) {
                println("Household: id=${hRes.getString(1)}, code=${hRes.getString(2)}, barangayName=${hRes.getString(3)}, barangayId='${hRes.getString(4)}', sync=${hRes.getString(5)}")
            }

            println("\n--- Residents Data ---")
            val rRes = stmt.executeQuery("SELECT residentId, householdId, firstName, lastName, barangayId, syncStatus FROM residents")
            while (rRes.next()) {
                println("Resident: id=${rRes.getString(1)}, hId=${rRes.getString(2)}, name=${rRes.getString(3)} ${rRes.getString(4)}, barangayId='${rRes.getString(5)}', sync=${rRes.getString(6)}")
            }

            conn.close()
        } catch (e: Exception) {
            println("JDBC Error: ${e.message}")
            e.printStackTrace()
        }
    }
}
