
package com.group13.linkap.data.repository

import com.group13.linkap.data.local.dao.VisitDao
import com.group13.linkap.data.local.entity.Visit
import com.group13.linkap.data.local.entity.VisitResident

class VisitRepository(
    private val visitDao: VisitDao
) {
    fun getAllVisits() = visitDao.getAllVisits()

    fun getVisitsByHousehold(householdId: String) =
        visitDao.getVisitsByHousehold(householdId)

    suspend fun getPendingVisits() =
        visitDao.getPendingVisits()

    suspend fun insertVisit(visit: Visit) =
        visitDao.insertVisit(visit)

    suspend fun updateVisit(visit: Visit) =
        visitDao.updateVisit(visit)

    suspend fun deleteVisit(visit: Visit) =
        visitDao.deleteVisit(visit)

    suspend fun updateSyncStatus(visitId: String, status: String) =
        visitDao.updateSyncStatus(visitId, status)

    suspend fun insertVisitWithResidents(
        visit: Visit,
        residents: List<VisitResident>
    ) = visitDao.insertVisitWithResidents(visit, residents)

    suspend fun getResidentsForVisit(visitId: String) =
        visitDao.getResidentsForVisit(visitId)
}
