package io.gripxtech.odoojsonrpcclient.core.persistence.entities

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Table(

    @Expose
    @SerializedName("name")
    var name: String = "",

    @Expose
    @SerializedName("sql")
    var sql: String = "",

    @Expose
    @SerializedName("fields")
    var fields: List<Field> = listOf()

) {

    val fieldsName: List<String>
        get() = fields.map { it.name }

}
