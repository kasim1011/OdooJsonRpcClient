package io.gripxtech.odoojsonrpcclient.customer.entities

import androidx.room.Dao
import androidx.room.Query
import io.gripxtech.odoojsonrpcclient.core.persistence.OdooDao

@Dao
interface StateDao : OdooDao {

    @Query("SELECT COUNT(*) FROM `res.country.state`")
    override fun getCount(): Int

}
