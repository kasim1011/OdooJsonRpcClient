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
    @SerializedName("local_fields")
    var localFields: List<LocalField> = listOf(),

    @Expose
    @SerializedName("fields")
    var fields: List<Field> = listOf()

) {

    val fieldsName: List<String>
        get() = fields.map { it.name }

    val localFieldsName: List<String>
        get() = localFields.map { it.name }

}
