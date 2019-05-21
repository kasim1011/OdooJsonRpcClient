package io.gripxtech.odoojsonrpcclient.core.persistence.entities

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Field(

    @Expose
    @SerializedName("id")
    var id: Int = 0,

    @Expose
    @SerializedName("model")
    var model: String = "",

    @Expose
    @SerializedName("modules")
    var modules: String = "",

    @Expose
    @SerializedName("name")
    var name: String = "",

    @Expose
    @SerializedName("relation")
    var relation: String = "",

    @Expose
    @SerializedName("relation_field")
    var relationField: String = "",

    @Expose
    @SerializedName("relation_table")
    var relationTable: String = "",

    @Expose
    @SerializedName("required")
    var required: Boolean = false,

    @Expose
    @SerializedName("ttype")
    var ttype: String = ""
)
