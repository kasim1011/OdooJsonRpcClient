package io.gripxtech.odoojsonrpcclient.core.entities.method.write

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.gripxtech.odoojsonrpcclient.core.entities.odooError.OdooError

data class Write(

        @field:Expose
        @field:SerializedName("result")
        var result: Boolean = false,

        @field:Expose
        @field:SerializedName("error")
        var odooError: OdooError = OdooError()
) {
    val isSuccessful get() = !isOdooError
    val isOdooError get() = odooError.message.isNotEmpty()
    val errorCode get() = odooError.code
    val errorMessage get() = odooError.data.message
}