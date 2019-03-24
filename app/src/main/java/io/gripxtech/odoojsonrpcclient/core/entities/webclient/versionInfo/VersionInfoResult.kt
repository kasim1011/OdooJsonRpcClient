package io.gripxtech.odoojsonrpcclient.core.entities.webclient.versionInfo

import com.google.gson.JsonArray
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.gripxtech.odoojsonrpcclient.core.Odoo

data class VersionInfoResult(

        @field:Expose
        @SerializedName("server_serie")
        var serverSerie: String = "",


        @Expose
        @SerializedName("server_version_info")
        var serverVersionInfo: JsonArray = JsonArray(),

        @Expose
        @SerializedName("server_version")
        var serverVersion: String = "",

        @Expose
        @SerializedName("protocol_version")
        var protocolVersion: Int = 0
) {
    val serverVersionType: String
        get() = if (serverVersionInfo.size() > 0) serverVersionInfo[3].asString else ""

    val isServerVersionEnterprise: Boolean
        get() = if (serverVersionInfo.size() > 0) serverVersionInfo[5].asString.contains("e", true) else false

    val serverVersionIsSupported: Boolean
        get() = Odoo.supportedOdooVersions.any { serverVersion.startsWith(it) }
}