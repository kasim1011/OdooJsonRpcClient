package io.gripxtech.odoojsonrpcclient.core.persistence

import android.content.Context
import androidx.work.*
import io.gripxtech.odoojsonrpcclient.BuildConfig
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context, workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {

        private const val TAG = "SyncWorker"
        private const val uniqueWorkName = "${BuildConfig.APPLICATION_ID}.core.persistence.sync_worker"

        fun initWorkManager() {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val backgroundRequest = PeriodicWorkRequest.Builder(
                SyncWorker::class.java,
                PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                TimeUnit.MILLISECONDS
            )
                .setConstraints(constraints)
                .addTag(TAG)
                .build()

            WorkManager.getInstance().enqueueUniquePeriodicWork(
                uniqueWorkName,
                ExistingPeriodicWorkPolicy.REPLACE,
                backgroundRequest
            )

            WorkManager.getInstance()
                .getWorkInfosForUniqueWorkLiveData(uniqueWorkName)
                .observeForever { workInfoList: MutableList<WorkInfo>? ->
                    if (workInfoList != null) {
                        for (i in 0 until workInfoList.size) {
                            val workInfo = workInfoList[i]
                            Timber.i("workInfo is $workInfo")
                        }
                    }
                }
        }
    }

    override fun doWork(): Result {
        val prefs = SyncPrefs(applicationContext)
        val lastSyncDate = prefs.lastSyncDate


        return Result.success(Data.Builder().build())
    }
}
