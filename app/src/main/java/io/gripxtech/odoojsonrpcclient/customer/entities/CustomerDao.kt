package io.gripxtech.odoojsonrpcclient.customer.entities

import androidx.room.*
import io.gripxtech.odoojsonrpcclient.core.persistence.OdooDao

@Dao
interface CustomerDao : OdooDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCustomer(customer: Customer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCustomers(customers: List<Customer>): List<Long>

    @Query("SELECT * FROM `res.partner`")
    fun getCustomers(): List<Customer>

    @Query("SELECT COUNT(*) FROM `res.partner`")
    override fun getCount(): Int

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun updateCustomer(customer: Customer): Int

    @Delete
    fun deleteCustomer(customer: Customer): Int
}
