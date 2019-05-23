package io.gripxtech.odoojsonrpcclient.core.persistence.entities

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LocalField(

    @Expose
    @SerializedName("cid")
    var cid: Int = 0,

    @Expose
    @SerializedName("name")
    var name: String = "",

    @Expose
    @SerializedName("notnull")
    var notnull: Int = 0,

    @Expose
    @SerializedName("pk")
    var pk: Int = 0,

    @Expose
    @SerializedName("type")
    var type: String = ""
)
