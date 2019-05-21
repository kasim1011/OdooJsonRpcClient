package io.gripxtech.odoojsonrpcclient.core

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.gripxtech.odoojsonrpcclient.App
import io.gripxtech.odoojsonrpcclient.core.persistence.AppTypeConverters
import io.gripxtech.odoojsonrpcclient.customer.entities.Customer
import io.gripxtech.odoojsonrpcclient.customer.entities.CustomerDao

@Database(
    entities = [
        /* Add Room Entities here: BEGIN */

        Customer::class // res.partner

        /* Add Room Entities here: END */
    ], version = 1, exportSchema = true
)
@TypeConverters(AppTypeConverters::class)
abstract class OdooDatabase : RoomDatabase() {

    companion object {

        lateinit var app: App

        val dbName: String
            get() = "${Odoo.user.androidName}.db"

        var database: OdooDatabase? = null
            get() {
                if (field == null) {
                    field = Room.databaseBuilder(app, OdooDatabase::class.java, dbName).build()
                }
                return field
            }
    }

    /* Add Room DAO(s) here: BEGIN */

    abstract fun customerDao(): CustomerDao

    /* Add Room DAO(s) here: END */
}
