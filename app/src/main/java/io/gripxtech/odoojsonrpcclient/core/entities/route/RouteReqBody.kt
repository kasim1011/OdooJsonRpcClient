package io.gripxtech.odoojsonrpcclient.core.entities.route

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RouteReqBody(

        @field:Expose
        @field:SerializedName("id")
        var id: String = "0",

        @field:Expose
        @field:SerializedName("jsonrpc")
        var jsonRPC: String = "2.0",

        @field:Expose
        @field:SerializedName("method")
        var method: String = "call",

        @field:Expose
        @field:SerializedName("params")
        var params: Any = mapOf<String, Any>()
)