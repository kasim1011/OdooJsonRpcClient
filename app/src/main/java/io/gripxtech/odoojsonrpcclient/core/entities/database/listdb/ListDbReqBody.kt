package io.gripxtech.odoojsonrpcclient.core.entities.database.listdb

import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ListDbReqBody(

        @Expose
        @SerializedName("id")
        var id: String = "0",

        @Expose
        @SerializedName("jsonrpc")
        var jsonRPC: String = "2.0",

        @Expose
        @SerializedName("method")
        var method: String = "call",

        @Expose
        @SerializedName("params")
        var params: JsonObject = JsonObject()
)