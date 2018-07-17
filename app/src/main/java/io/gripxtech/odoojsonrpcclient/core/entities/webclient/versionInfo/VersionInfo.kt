package io.gripxtech.odoojsonrpcclient.core.entities.webclient.versionInfo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.gripxtech.odoojsonrpcclient.core.entities.odooError.OdooError

data class VersionInfo(

        @field:Expose
        @field:SerializedName("result")
        val result: VersionInfoResult = VersionInfoResult(),

        @field:Expose
        @field:SerializedName("error")
        val odooError: OdooError = OdooError()
) {
    val isSuccessful get() = !isOdooError
    val isOdooError get() = odooError.message.isNotEmpty()
    val errorCode get() = odooError.code
    val errorMessage get() = odooError.data.message
}
