
package com.group13.linkap.data.repository

import com.group13.linkap.data.local.dao.HouseholdDao
import com.group13.linkap.data.local.entity.Household
import kotlinx.coroutines.flow.Flow

class HouseholdRepository(
    private val householdDao: HouseholdDao
) {
    fun getAllHouseholds() = householdDao.getAllHouseholds()

    fun getHouseholdById(householdId: String): Flow<Household?> {
        return householdDao.getHouseholdById(householdId)
    }

    suspend fun getHouseholdByCode(code: String) =
        householdDao.getHouseholdByCode(code)

    suspend fun insertHousehold(household: Household) =
        householdDao.insertHousehold(household)

    suspend fun updateHousehold(household: Household) =
        householdDao.updateHousehold(household)

    suspend fun deleteHousehold(household: Household) =
        householdDao.deleteHousehold(household)

    suspend fun isHouseholdCodeTaken(householdCode: String): Boolean {
        return householdDao.getHouseholdByCode(householdCode) != null
    }
}
