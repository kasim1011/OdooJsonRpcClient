package io.gripxtech.odoojsonrpcclient.customer.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.gripxtech.odoojsonrpcclient.core.persistence.OdooModel
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "res.country", primaryKeys = [OdooModel.LocalId, OdooModel.Id])
data class Country(

    @ColumnInfo(name = OdooModel.LocalId)
    override var localId: Long = 0,

    @Expose
    @SerializedName(OdooModel.Id)
    @ColumnInfo(name = OdooModel.Id)
    override var id: Long = 0,

    @Expose
    @SerializedName(OdooModel.CreateDate)
    @ColumnInfo(name = OdooModel.CreateDate)
    override var createDate: String = "false",

    @Expose
    @SerializedName(OdooModel.WriteDate)
    @ColumnInfo(name = OdooModel.WriteDate)
    override var writeDate: String = "false",

    @ColumnInfo(name = OdooModel.LocalWriteDate)
    override var localWriteDate: String = "false",

    @ColumnInfo(name = OdooModel.LocalDirty)
    override var localDirty: Boolean = false,

    @ColumnInfo(name = OdooModel.LocalActive)
    override var localActive: Boolean = false,

    @Expose
    @SerializedName("name")
    @ColumnInfo(name = "name")
    var name: String = "false"

) : OdooModel, Parcelable