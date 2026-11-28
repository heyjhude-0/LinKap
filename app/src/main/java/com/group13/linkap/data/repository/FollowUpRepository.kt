package com.group13.linkap.data.repository

import com.group13.linkap.data.local.UserSessionManager
import com.group13.linkap.data.local.dao.FollowUpDao
import com.group13.linkap.data.local.entity.FollowUp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class FollowUpRepository(
    private val followUpDao: FollowUpDao,
    private val userSessionManager: UserSessionManager? = null
) {

    fun getAllFollowUps(): Flow<List<FollowUp>> {
        val sessionFlow = userSessionManager?.barangayIdFlow ?: flowOf(userSessionManager?.getBarangayId().orEmpty())
        return sessionFlow.flatMapLatest { barangayId ->
            if (barangayId.isBlank()) {
                flowOf(emptyList())
            } else {
                followUpDao.getAllFollowUps(barangayId)
            }
        }
    }

    fun getFollowUpsByHousehold(householdId: String): Flow<List<FollowUp>> {
        val sessionFlow = userSessionManager?.barangayIdFlow ?: flowOf(userSessionManager?.getBarangayId().orEmpty())
        return sessionFlow.flatMapLatest { barangayId ->
            if (barangayId.isBlank()) {
                flowOf(emptyList())
            } else {
                followUpDao.getFollowUpsByHousehold(householdId, barangayId)
            }
        }
    }

    fun getPendingFollowUps(): Flow<List<FollowUp>> {
        val sessionFlow = userSessionManager?.barangayIdFlow ?: flowOf(userSessionManager?.getBarangayId().orEmpty())
        return sessionFlow.flatMapLatest { barangayId ->
            if (barangayId.isBlank()) {
                flowOf(emptyList())
            } else {
                followUpDao.getPendingFollowUps(barangayId)
            }
        }
    }

    fun getCompletedFollowUps(): Flow<List<FollowUp>> {
        val sessionFlow = userSessionManager?.barangayIdFlow ?: flowOf(userSessionManager?.getBarangayId().orEmpty())
        return sessionFlow.flatMapLatest { barangayId ->
            if (barangayId.isBlank()) {
                flowOf(emptyList())
            } else {
                followUpDao.getCompletedFollowUps(barangayId)
            }
        }
    }

    fun getOverdueFollowUps(currentTime: Long): Flow<List<FollowUp>> {
        val sessionFlow = userSessionManager?.barangayIdFlow ?: flowOf(userSessionManager?.getBarangayId().orEmpty())
        return sessionFlow.flatMapLatest { barangayId ->
            if (barangayId.isBlank()) {
                flowOf(emptyList())
            } else {
                followUpDao.getOverdueFollowUps(currentTime, barangayId)
            }
        }
    }

    suspend fun getFollowUpById(followUpId: String): FollowUp? {
        val barangayId = userSessionManager?.getBarangayId().orEmpty()
        if (barangayId.isBlank()) return null
        return followUpDao.getFollowUpById(followUpId, barangayId)
    }

    fun getFollowUpsByStatus(status: String): Flow<List<FollowUp>> {
        val sessionFlow = userSessionManager?.barangayIdFlow ?: flowOf(userSessionManager?.getBarangayId().orEmpty())
        return sessionFlow.flatMapLatest { barangayId ->
            if (barangayId.isBlank()) {
                flowOf(emptyList())
            } else {
                followUpDao.getFollowUpsByStatus(status, barangayId)
            }
        }
    }

    suspend fun insertFollowUp(followUp: FollowUp) {
        val activeBarangayId = userSessionManager?.getBarangayId()?.takeIf { it.isNotBlank() }
            ?: followUp.barangayId
        followUpDao.insertFollowUp(followUp.copy(barangayId = activeBarangayId))
    }

    suspend fun updateFollowUp(followUp: FollowUp) =
        followUpDao.updateFollowUp(followUp)

    suspend fun deleteFollowUp(followUp: FollowUp) =
        followUpDao.deleteFollowUp(followUp)
}
