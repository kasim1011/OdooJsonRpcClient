package io.gripxtech.odoojsonrpcclient.customer.entities

import androidx.room.*

@Dao
interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCustomer(customer: Customer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCustomers(customers: List<Customer>): List<Long>

    @Query("SELECT * FROM `res.partner`")
    fun getCustomers(): List<Customer>

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun updateCustomer(customer: Customer): Int

    @Delete
    fun deleteCustomer(customer: Customer): Int
}
