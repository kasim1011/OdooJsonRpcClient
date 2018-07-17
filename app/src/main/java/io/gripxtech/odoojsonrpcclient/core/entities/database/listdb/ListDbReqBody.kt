package io.gripxtech.odoojsonrpcclient.core.entities.database.listdb

import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ListDbReqBody(

        @Expose
        @SerializedName("id")
        val id: String = "0",

        @Expose
        @SerializedName("jsonrpc")
        val jsonRPC: String = "2.0",

        @Expose
        @SerializedName("method")
        val method: String = "call",

        @Expose
        @SerializedName("params")
        val params: JsonObject = JsonObject()
)