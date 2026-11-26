
package com.group13.linkap.data.repository

import com.group13.linkap.data.local.dao.FollowUpDao

class FollowUpRepository(
    private val followUpDao: FollowUpDao
) {

    fun getAllFollowUps() = followUpDao.getAllFollowUps()

    fun getFollowUpsByHousehold(householdId: String) =
        followUpDao.getFollowUpsByHousehold(householdId)

    fun getPendingFollowUps() =
        followUpDao.getPendingFollowUps()

    fun getCompletedFollowUps() =
        followUpDao.getCompletedFollowUps()

    fun getOverdueFollowUps(currentTime: Long) =
        followUpDao.getOverdueFollowUps(currentTime)

    suspend fun getFollowUpById(followUpId: String) =
        followUpDao.getFollowUpById(followUpId)

    fun getFollowUpsByStatus(status: String) =
        followUpDao.getFollowUpsByStatus(status)

    suspend fun insertFollowUp(followUp: com.group13.linkap.data.local.entity.FollowUp) =
        followUpDao.insertFollowUp(followUp)

    suspend fun updateFollowUp(followUp: com.group13.linkap.data.local.entity.FollowUp) =
        followUpDao.updateFollowUp(followUp)

    suspend fun deleteFollowUp(followUp: com.group13.linkap.data.local.entity.FollowUp) =
        followUpDao.deleteFollowUp(followUp)
}
