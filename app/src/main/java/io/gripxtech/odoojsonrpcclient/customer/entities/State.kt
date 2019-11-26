package io.gripxtech.odoojsonrpcclient.customer.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.gripxtech.odoojsonrpcclient.core.persistence.OdooModel
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "res.country.state")
data class State(

    @Expose
    @SerializedName("name")
    @ColumnInfo(name = "name")
    var name: String = "false"

) : OdooModel(), Parcelable