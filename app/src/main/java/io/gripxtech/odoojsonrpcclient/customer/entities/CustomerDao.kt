package io.gripxtech.odoojsonrpcclient.customer.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCustomer(customer: Customer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCustomers(customers: List<Customer>): List<Long>

    @Query("SELECT * FROM `res.partner`")
    fun getCustomers(): List<Customer>
}
