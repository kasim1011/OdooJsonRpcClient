package io.gripxtech.odoojsonrpcclient.core.entities.odooError

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class OdooError(
        @field:Expose
        @field:SerializedName("message")
        val message: String = "",

        @field:Expose
        @field:SerializedName("code")
        val code: Int = 200,

        @field:Expose
        @field:SerializedName("data")
        val data: Data = Data()
)
