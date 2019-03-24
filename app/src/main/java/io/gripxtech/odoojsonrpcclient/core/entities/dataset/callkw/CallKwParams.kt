package io.gripxtech.odoojsonrpcclient.core.entities.dataset.callkw

import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CallKwParams(
        @field:Expose
        @field:SerializedName("model")
        var model: String = "",

        @field:Expose
        @field:SerializedName("method")
        var method: String = "",

        @field:Expose
        @field:SerializedName("args")
        var args: List<Any> = listOf(),

        @field:Expose
        @field:SerializedName("kwargs")
        var kwArgs: Map<String, Any> = mapOf(),

        @field:Expose
        @field:SerializedName("context")
        var context: JsonObject = JsonObject()
)
