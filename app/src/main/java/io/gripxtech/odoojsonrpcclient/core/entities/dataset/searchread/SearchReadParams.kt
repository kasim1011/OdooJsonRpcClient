package io.gripxtech.odoojsonrpcclient.core.entities.dataset.searchread

import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SearchReadParams(

        @field:Expose
        @field:SerializedName("model")
        var model: String = "",

        @field:Expose
        @field:SerializedName("fields")
        var fields: List<String> = listOf(),

        @field:Expose
        @field:SerializedName("domain")
        var domain: List<Any> = listOf(),

        @field:Expose
        @field:SerializedName("offset")
        var offset: Int = 0,

        @field:Expose
        @field:SerializedName("limit")
        var limit: Int = 0,

        @field:Expose
        @field:SerializedName("sort")
        var sort: String = "",

        @field:Expose
        @field:SerializedName("context")
        var context: JsonObject = JsonObject()
)
