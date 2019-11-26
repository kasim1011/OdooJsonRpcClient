package io.gripxtech.odoojsonrpcclient.core.persistence

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

abstract class OdooModel(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = LocalId)
    var localId: Long = 0,

    @Expose
    @SerializedName(Id)
    @ColumnInfo(name = Id)
    var id: Long = 0,

    @Expose
    @SerializedName(CreateDate)
    @ColumnInfo(name = CreateDate)
    var createDate: String = "false",

    @Expose
    @SerializedName(WriteDate)
    @ColumnInfo(name = WriteDate)
    var writeDate: String = "false",

    @ColumnInfo(name = LocalWriteDate)
    var localWriteDate: String = "false",

    @ColumnInfo(name = LocalDirty)
    var localDirty: Boolean = false,

    @ColumnInfo(name = LocalActive)
    var localActive: Boolean = false

) {
    companion object {
        const val LocalId = "_id"
        const val Id = "id"
        const val CreateDate = "create_date"
        const val WriteDate = "write_date"
        const val LocalWriteDate = "_write_date"
        const val LocalDirty = "_dirty"
        const val LocalActive = "_active"
    }
}
