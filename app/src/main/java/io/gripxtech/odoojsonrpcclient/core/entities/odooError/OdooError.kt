package io.gripxtech.odoojsonrpcclient.core.entities.odooError

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class OdooError(
        @field:Expose
        @field:SerializedName("message")
        var message: String = "",

        @field:Expose
        @field:SerializedName("code")
        var code: Int = 200,

        @field:Expose
        @field:SerializedName("data")
        var data: Data = Data()
)
