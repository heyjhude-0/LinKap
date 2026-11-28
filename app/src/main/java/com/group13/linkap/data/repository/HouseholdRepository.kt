package com.group13.linkap.data.repository

import com.group13.linkap.data.local.UserSessionManager
import com.group13.linkap.data.local.dao.HouseholdDao
import com.group13.linkap.data.local.entity.Household
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class HouseholdRepository(
    private val householdDao: HouseholdDao,
    private val userSessionManager: UserSessionManager? = null
) {
    fun getAllHouseholds(): Flow<List<Household>> {
        val sessionFlow = userSessionManager?.barangayIdFlow ?: flowOf(userSessionManager?.getBarangayId().orEmpty())
        return sessionFlow.flatMapLatest { barangayId ->
            if (barangayId.isBlank()) {
                flowOf(emptyList())
            } else {
                householdDao.getAllHouseholds(barangayId)
            }
        }
    }

    fun getHouseholdById(householdId: String): Flow<Household?> {
        val sessionFlow = userSessionManager?.barangayIdFlow ?: flowOf(userSessionManager?.getBarangayId().orEmpty())
        return sessionFlow.flatMapLatest { barangayId ->
            if (barangayId.isBlank()) {
                flowOf(null)
            } else {
                householdDao.getHouseholdById(householdId, barangayId)
            }
        }
    }

    suspend fun getHouseholdByCode(code: String): Household? {
        val barangayId = userSessionManager?.getBarangayId().orEmpty()
        if (barangayId.isBlank()) return null
        return householdDao.getHouseholdByCode(code, barangayId)
    }

    suspend fun insertHousehold(household: Household) {
        val activeBarangayId = userSessionManager?.getBarangayId()?.takeIf { it.isNotBlank() }
            ?: household.barangayId
        householdDao.insertHousehold(household.copy(barangayId = activeBarangayId))
    }

    suspend fun updateHousehold(household: Household) =
        householdDao.updateHousehold(household)

    suspend fun deleteHousehold(household: Household) =
        householdDao.deleteHousehold(household)

    suspend fun isHouseholdCodeTaken(householdCode: String): Boolean {
        val barangayId = userSessionManager?.getBarangayId().orEmpty()
        if (barangayId.isBlank()) return false
        return householdDao.getHouseholdByCode(householdCode, barangayId) != null
    }
}
