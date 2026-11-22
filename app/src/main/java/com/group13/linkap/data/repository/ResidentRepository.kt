
package com.group13.linkap.data.repository

import com.group13.linkap.data.local.dao.ResidentDao
import com.group13.linkap.data.local.entity.Resident
import kotlinx.coroutines.flow.Flow

class ResidentRepository(
    private val residentDao: ResidentDao
) {
    fun getAllResidents() = residentDao.getAllResidents()

    fun getResidentsByHousehold(householdId: String): Flow<List<Resident>> {
        return residentDao.getResidentsByHousehold(householdId)
    }

    suspend fun getResidentById(residentId: String) =
        residentDao.getResidentById(residentId)

    suspend fun insertResident(resident: Resident) =
        residentDao.insertResident(resident)

    suspend fun insertResidents(residents: List<Resident>) =
        residentDao.insertResidents(residents)

    suspend fun updateResident(resident: Resident) =
        residentDao.updateResident(resident)

    suspend fun deleteResident(resident: Resident) =
        residentDao.deleteResident(resident)

}
