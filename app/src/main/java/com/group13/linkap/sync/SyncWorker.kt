package com.group13.linkap.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val syncEngine = FirestoreSyncEngine(applicationContext)
            val result = syncEngine.performSync()

            if (result.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (exception: Exception) {
            Result.retry()
        }
    }
}
