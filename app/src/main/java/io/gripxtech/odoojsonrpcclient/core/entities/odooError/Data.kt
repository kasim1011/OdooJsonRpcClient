package io.gripxtech.odoojsonrpcclient.core.entities.odooError

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Data(
        @field:Expose
        @field:SerializedName("debug")
        var debug: String = "",

        @field:Expose
        @field:SerializedName("exception_type")
        var exceptionType: String = "",

        @field:Expose
        @field:SerializedName("message")
        var message: String = "",

        @field:Expose
        @field:SerializedName("name")
        var name: String = "",

        @field:Expose
        @field:SerializedName("arguments")
        var arguments: List<String> = listOf()
)
