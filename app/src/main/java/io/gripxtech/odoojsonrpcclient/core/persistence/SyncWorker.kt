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
import io.gripxtech.odoojsonrpcclient.core.persistence.entities.LocalField
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

        private const val OneToMany = "one2many"
        private const val ManyToMany = "many2many"
        private const val ManyToOne = "many2one"

        fun initWorkManager(context: Context) {
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

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                uniqueWorkName,
                if (BuildConfig.DEBUG) ExistingPeriodicWorkPolicy.REPLACE else ExistingPeriodicWorkPolicy.KEEP,
                backgroundRequest
            )

            WorkManager.getInstance(context)
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

    override fun doWork(): Result {
        val context = applicationContext
        val genericError = context.getString(R.string.generic_error)
//        val tag = "$TAG::doWork"

        if (context.getOdooUsers().isEmpty()) {
            return Result.success(Data.Builder().build())
        }

        val db = OdooDatabase.database ?: return failure("failed to get database instance")

        // val prefs = SyncPrefs(applicationContext)
        // val lastSyncDate = prefs.lastSyncDate

        val cursor = db.query(
            "SELECT name, sql FROM sqlite_master WHERE type = 'table' AND name != 'android_metadata' AND name != 'room_master_table'",
            null
        )

        val data = JsonArray()
        while (cursor.moveToNext()) {
            val row = JsonObject()
            for (i in 0 until cursor.columnCount) {
                val columnName = cursor.getColumnName(i)
                val value = cursor.getString(i)
                if (columnName == "name" && value.startsWith("sqlite_")) {
                    break
                }
                row.addProperty(columnName, value)
            }
            if (row.has("name")) {
                data.add(row)
            }
        }
        cursor.close()

        val tables: List<Table> = gson.fromJson(data, object : TypeToken<ArrayList<Table>>() {}.type)
        if (tables.isEmpty()) {
            return failure("no record found in query result on table name sqlite_master")
        }

        for (i in tables.indices) {
            val table = tables[i]
            val tableCursor = db.query("PRAGMA table_info('${table.name}')", null)

            val tableData = JsonArray()
            while (tableCursor.moveToNext()) {
                val row = JsonObject()
                for (i1 in 0 until tableCursor.columnCount) {
                    row.addProperty(tableCursor.getColumnName(i1), tableCursor.getString(i1))
                }
                tableData.add(row)
            }
            tableCursor.close()

            val localFields: List<LocalField> = gson.fromJson(tableData, object : TypeToken<List<LocalField>>() {}.type)
            table.localFields = localFields
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
            ),
            modelFields = tables.flatMap {
                it.localFieldsName
            }.toSet().toList()
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

        for (i in tables.indices) {
            val table = tables[i]
            table.fields = fields.filter {
                it.model == table.name
            }
        }

        val missingModels = ArrayList<String>()
        val tableNames = tables.map { it.name }
        for (i in tables.indices) {
            val table = tables[i]
            table.fields.filter {
                it.ttype in listOf(OneToMany, ManyToMany, ManyToOne)
            }.forEach {
                val relationTableName = it.relation
                if (relationTableName !in tableNames) {
                    missingModels += "Model $relationTableName is not found but, it is referenced by field ${it.name} of ${table.name} model"
                } else {
                    val relationTable = tables.find { table -> table.name == relationTableName }
                    if (relationTable != null) {
                        table dependsOn relationTable
                    }
                }
            }
        }
        if (missingModels.isNotEmpty()) {
            val causeBuilder = StringBuilder()
            missingModels.forEach {
                causeBuilder.append(it)
                causeBuilder.append('\n')
            }
            return failure(causeBuilder.toString())
        }

        for (i in tables.indices) {
            val table = tables[i]
            table.dependencyTree.addAll(table.calculateDependencyTree())
        }

        val sortedTables = tables.sorted()
        for (i in sortedTables.indices) {
            val table = sortedTables[i]
            table.syncOrder = i
            table.dependencies = arrayListOf()
            table.dependencyTree = arrayListOf()
        }

        for (i in sortedTables.indices) {
            val table = sortedTables[i]
            Timber.i("table is $table")
        }

        return Result.success(Data.Builder().build())
    }
}
