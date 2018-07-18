package io.gripxtech.odoojsonrpcclient.core.entities.dataset.execworkflow

import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ExecWorkflowParams(
        @field:Expose
        @field:SerializedName("model")
        val model: String = "",

        @field:Expose
        @field:SerializedName("id")
        val id: Int = 0,

        @field:Expose
        @field:SerializedName("signal")
        val signal: String = "",

        @field:Expose
        @field:SerializedName("context")
        val context: JsonObject = JsonObject()
)
