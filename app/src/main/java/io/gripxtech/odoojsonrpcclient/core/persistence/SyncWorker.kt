package io.gripxtech.odoojsonrpcclient.core.persistence

import android.content.Context
import android.os.Build
import androidx.work.*
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import io.gripxtech.odoojsonrpcclient.BuildConfig
import io.gripxtech.odoojsonrpcclient.R
import io.gripxtech.odoojsonrpcclient.core.Odoo
import io.gripxtech.odoojsonrpcclient.core.OdooDatabase
import io.gripxtech.odoojsonrpcclient.core.entities.dataset.searchread.SearchRead
import io.gripxtech.odoojsonrpcclient.core.persistence.entities.Field
import io.gripxtech.odoojsonrpcclient.core.persistence.entities.Table
import io.gripxtech.odoojsonrpcclient.getOdooUsers
import io.gripxtech.odoojsonrpcclient.gson
import retrofit2.Response
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context, workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {

        private const val TAG = "SyncWorker"
        private const val uniqueWorkName = "${BuildConfig.APPLICATION_ID}.core.persistence.sync_worker"
        private const val KeyCause = "cause"

        fun initWorkManager() {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .apply {
                    if (!BuildConfig.DEBUG) {
                        setRequiresBatteryNotLow(true)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            setRequiresDeviceIdle(true)
                        }
                    }
                }
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
                if (BuildConfig.DEBUG) ExistingPeriodicWorkPolicy.REPLACE else ExistingPeriodicWorkPolicy.KEEP,
                backgroundRequest
            )

            WorkManager.getInstance()
                .getWorkInfosForUniqueWorkLiveData(uniqueWorkName)
                .observeForever { workInfoList: MutableList<WorkInfo>? ->
                    if (workInfoList != null) {
                        for (i in 0 until workInfoList.size) {
                            val workInfo = workInfoList[i]
                            val state = JsonObject().apply {
                                val data = workInfo.state
                                addProperty("name", data.name)
                                addProperty("isFinished", data.isFinished)
                            }
                            val outputData = JsonObject().apply {
                                val data = workInfo.outputData
                                for (entry in data.keyValueMap.entries) {
                                    addProperty(entry.key ?: "", entry.value?.toString() ?: "")
                                }
                            }
                            val workInfoObj = JsonObject().apply {
                                addProperty("id", workInfo.id.toString())
                                add("state", state)
                                addProperty("tags", workInfo.tags.toString())
                                add("outputData", outputData)
                            }.toString()
                            Timber.i("workInfo is $workInfoObj")
                        }
                    }
                }
        }
    }

    private fun failure(cause: String): Result {
        Timber.tag(TAG).e(cause)
        return Result.failure(
            Data.Builder().putString(
                KeyCause,
                cause
            ).build()
        )
    }

    // TODO("See what Local records are missing on Remote")

    // TODO("See what Remote records are missing on Local")

    // TODO("Updating remote records")

    // TODO("Updating local records")
    override fun doWork(): Result {
        val context = applicationContext
        val genericError = context.getString(R.string.generic_error)
        val tag = "$TAG::doWork"

        if (context.getOdooUsers().isEmpty()) {
            return Result.success(Data.Builder().build())
        }

        val db = OdooDatabase.database ?: return failure("failed to get database instance")

        // val prefs = SyncPrefs(applicationContext)
        // val lastSyncDate = prefs.lastSyncDate

        val cursor = db.query(
            "SELECT name, sql FROM sqlite_master WHERE type = 'table' AND name != 'android_metadata' AND name != 'room_master_table'",
            null
        ) ?: return failure("null cursor returned by query on sqlite_master")

        val data = JsonArray()
        while (cursor.moveToNext()) {
            val row = JsonObject()
            for (i in 0 until cursor.columnCount) {
                row.addProperty(cursor.getColumnName(i), cursor.getString(i))
            }
            data.add(row)
        }
        cursor.close()

        val tables: List<Table> = gson.fromJson(data, object : TypeToken<ArrayList<Table>>() {}.type)
        if (tables.isEmpty()) {
            return failure("no record found in query result on table name sqlite_master")
        }

        val call = Odoo.fieldsGetW(
            models = tables.map { it.name },
            fields = listOf(
                "model",
                "name",
                "ttype",
                "modules",
                "relation",
                "relation_field",
                "relation_table",
                "required"
            )
        )
        val response: Response<SearchRead>?
        try {
            response = call.execute()
        } catch (e: Exception) {
            Odoo.resetRetrofitClientW()
            return failure(e.message ?: genericError)
        }

        if (!response.isSuccessful) {
            Odoo.resetRetrofitClientW()
            return failure(response.errorBody()?.string() ?: genericError)
        }

        val searchRead = response.body() ?: return failure("null searchRead returned")

        if (!searchRead.isSuccessful) {
            return failure(searchRead.odooError.toString())
        }

        val fields: List<Field> = gson.fromJson(searchRead.result.records, object : TypeToken<List<Field>>() {}.type)
        if (fields.isEmpty()) {
            return failure("no fields found")
        }

        for (i in 0 until tables.size) {
            val table = tables[i]
            table.fields = fields.filter {
                it.model == table.name
            }
        }



        return Result.success(Data.Builder().build())
    }
}
