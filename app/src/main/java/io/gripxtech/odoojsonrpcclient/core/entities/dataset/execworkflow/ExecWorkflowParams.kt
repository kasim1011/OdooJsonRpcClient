package io.gripxtech.odoojsonrpcclient.core.entities.dataset.execworkflow

import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ExecWorkflowParams(
        @field:Expose
        @field:SerializedName("model")
        var model: String = "",

        @field:Expose
        @field:SerializedName("id")
        var id: Int = 0,

        @field:Expose
        @field:SerializedName("signal")
        var signal: String = "",

        @field:Expose
        @field:SerializedName("context")
        var context: JsonObject = JsonObject()
)
