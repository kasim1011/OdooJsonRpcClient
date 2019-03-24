package io.gripxtech.odoojsonrpcclient.core.entities.dataset.load

import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LoadParams(
        @field:Expose
        @field:SerializedName("id")
        var id: Int = 0,

        @field:Expose
        @field:SerializedName("model")
        var model: String = "",

        @field:Expose
        @field:SerializedName("fields")
        var fields: List<String> = listOf(),

        @field:Expose
        @field:SerializedName("context")
        var context: JsonObject = JsonObject()
)
