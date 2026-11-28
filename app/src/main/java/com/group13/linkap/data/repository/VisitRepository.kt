package com.group13.linkap.data.repository

import com.group13.linkap.data.local.UserSessionManager
import com.group13.linkap.data.local.dao.VisitDao
import com.group13.linkap.data.local.entity.Visit
import com.group13.linkap.data.local.entity.VisitResident
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class VisitRepository(
    private val visitDao: VisitDao,
    private val userSessionManager: UserSessionManager? = null
) {
    fun getAllVisits(): Flow<List<Visit>> {
        val sessionFlow = userSessionManager?.barangayIdFlow ?: flowOf(userSessionManager?.getBarangayId().orEmpty())
        return sessionFlow.flatMapLatest { barangayId ->
            if (barangayId.isBlank()) {
                flowOf(emptyList())
            } else {
                visitDao.getAllVisits(barangayId)
            }
        }
    }

    fun getVisitsByHousehold(householdId: String): Flow<List<Visit>> {
        val sessionFlow = userSessionManager?.barangayIdFlow ?: flowOf(userSessionManager?.getBarangayId().orEmpty())
        return sessionFlow.flatMapLatest { barangayId ->
            if (barangayId.isBlank()) {
                flowOf(emptyList())
            } else {
                visitDao.getVisitsByHousehold(householdId, barangayId)
            }
        }
    }

    suspend fun getPendingVisits(): List<Visit> {
        val barangayId = userSessionManager?.getBarangayId().orEmpty()
        if (barangayId.isBlank()) return emptyList()
        return visitDao.getPendingVisits(barangayId)
    }

    suspend fun insertVisit(visit: Visit) {
        val activeBarangayId = userSessionManager?.getBarangayId()?.takeIf { it.isNotBlank() }
            ?: visit.barangayId
        visitDao.insertVisit(visit.copy(barangayId = activeBarangayId))
    }

    suspend fun updateVisit(visit: Visit) =
        visitDao.updateVisit(visit)

    suspend fun deleteVisit(visit: Visit) =
        visitDao.deleteVisit(visit)

    suspend fun updateSyncStatus(visitId: String, status: String) =
        visitDao.updateSyncStatus(visitId, status)

    suspend fun insertVisitWithResidents(
        visit: Visit,
        residents: List<VisitResident>
    ) {
        val activeBarangayId = userSessionManager?.getBarangayId()?.takeIf { it.isNotBlank() }
            ?: visit.barangayId
        visitDao.insertVisitWithResidents(visit.copy(barangayId = activeBarangayId), residents)
    }

    suspend fun getResidentsForVisit(visitId: String) =
        userSessionManager?.getBarangayId()?.let { bId ->
            if (bId.isBlank()) emptyList() else visitDao.getResidentsForVisit(visitId, bId)
        } ?: emptyList()
}
