package io.gripxtech.odoojsonrpcclient.core.entities.dataset.callKw

import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CallKwParams(
        @field:Expose
        @field:SerializedName("model")
        val model: String = "",

        @field:Expose
        @field:SerializedName("method")
        val method: String = "",

        @field:Expose
        @field:SerializedName("args")
        val args: List<Any> = listOf(),

        @field:Expose
        @field:SerializedName("kwargs")
        val kwArgs: Map<String, Any> = mapOf(),

        @field:Expose
        @field:SerializedName("context")
        val context: JsonObject = JsonObject()
)
