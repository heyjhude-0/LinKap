package com.group13.linkap.data.repository

import com.group13.linkap.data.local.UserSessionManager
import com.group13.linkap.data.local.dao.ResidentDao
import com.group13.linkap.data.local.entity.Resident
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class ResidentRepository(
    private val residentDao: ResidentDao,
    private val userSessionManager: UserSessionManager? = null
) {
    fun getAllResidents(): Flow<List<Resident>> {
        val sessionFlow = userSessionManager?.barangayIdFlow ?: flowOf(userSessionManager?.getBarangayId().orEmpty())
        return sessionFlow.flatMapLatest { barangayId ->
            if (barangayId.isBlank()) {
                flowOf(emptyList())
            } else {
                residentDao.getAllResidents(barangayId)
            }
        }
    }

    fun getResidentsByHousehold(householdId: String): Flow<List<Resident>> {
        val sessionFlow = userSessionManager?.barangayIdFlow ?: flowOf(userSessionManager?.getBarangayId().orEmpty())
        return sessionFlow.flatMapLatest { barangayId ->
            if (barangayId.isBlank()) {
                flowOf(emptyList())
            } else {
                residentDao.getResidentsByHousehold(householdId, barangayId)
            }
        }
    }

    suspend fun getResidentById(residentId: String): Resident? {
        val barangayId = userSessionManager?.getBarangayId().orEmpty()
        if (barangayId.isBlank()) return null
        return residentDao.getResidentById(residentId, barangayId)
    }

    suspend fun insertResident(resident: Resident) {
        val activeBarangayId = userSessionManager?.getBarangayId()?.takeIf { it.isNotBlank() }
            ?: resident.barangayId
        residentDao.insertResident(resident.copy(barangayId = activeBarangayId))
    }

    suspend fun insertResidents(residents: List<Resident>) {
        val activeBarangayId = userSessionManager?.getBarangayId()?.takeIf { it.isNotBlank() }
        val updated = residents.map { r ->
            r.copy(barangayId = activeBarangayId ?: r.barangayId)
        }
        residentDao.insertResidents(updated)
    }

    suspend fun updateResident(resident: Resident) =
        residentDao.updateResident(resident)

    suspend fun deleteResident(resident: Resident) =
        residentDao.deleteResident(resident)
}
