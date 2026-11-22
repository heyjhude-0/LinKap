
package com.group13.linkap.data.repository

import com.group13.linkap.data.local.dao.FollowUpDao
import com.group13.linkap.data.local.entity.FollowUp

class FollowUpRepository(
    private val followUpDao: FollowUpDao
) {
    fun getAllFollowUps() = followUpDao.getAllFollowUps()

    fun getFollowUpsByHousehold(householdId: String) =
        followUpDao.getFollowUpsByHousehold(householdId)

    fun getPendingFollowUps() =
        followUpDao.getPendingFollowUps()

    suspend fun insertFollowUp(followUp: FollowUp) =
        followUpDao.insertFollowUp(followUp)

    suspend fun updateFollowUp(followUp: FollowUp) =
        followUpDao.updateFollowUp(followUp)

    suspend fun deleteFollowUp(followUp: FollowUp) =
        followUpDao.deleteFollowUp(followUp)
}
