package io.gripxtech.odoojsonrpcclient.core.entities.session.authenticate

import android.os.Bundle
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.gripxtech.odoojsonrpcclient.core.Odoo
import io.gripxtech.odoojsonrpcclient.core.utils.encryptAES

data class AuthenticateResult(

        @field:Expose
        @field:SerializedName("is_superuser")
        var superuser: Boolean = false,

        @Expose
        @SerializedName("company_id")
        var companyId: Int = 0,

        @Expose
        @SerializedName("web.base.url")
        var webBaseUrl: String = "",

        @Expose
        @SerializedName("session_id")
        var sessionId: String = "",

        @Expose
        @SerializedName("server_version")
        var serverVersion: String = "",

        @Expose
        @SerializedName("is_admin")
        var admin: Boolean = false,

        @Expose
        @SerializedName("uid")
        var uid: Int = 0,

        @Expose
        @SerializedName("partner_id")
        var partnerId: Int = 0,

        @Expose
        @SerializedName("user_companies")
        var userCompanies: JsonElement = JsonObject(),

        @Expose
        @SerializedName("name")
        var name: String = "",

        @Expose
        @SerializedName("server_version_info")
        var serverVersionInfo: JsonArray = JsonArray(),

        @Expose
        @SerializedName("user_context")
        var userContext: JsonObject = JsonObject(),

        @Expose
        @SerializedName("db")
        var db: String = "",

        @Expose
        @SerializedName("username")
        var username: String = "",

        @Expose
        @SerializedName("currencies")
        var currencies: JsonObject = JsonObject(),

        @Expose
        @SerializedName("web_tours")
        var webTours: JsonArray = JsonArray(),

        var imageSmall: String = "",
        var password: String = ""
) {
    val androidName: String
        get() = "$username[$db]"

    val toBundle: Bundle
        get() = Bundle().apply {
            putString("protocol", Odoo.protocol.name)
            putString("host", Odoo.host)
            putString("login", username)
            putString("password", password.encryptAES())
            putString("database", db)
            putString("serverVersion", serverVersion)
            putString("isAdmin", admin.toString())
            putString("isSuperuser", superuser.toString())
            putString("id", uid.toString())
            putString("partnerId", partnerId.toString())
            putString("name", name)
            putString("imageSmall", imageSmall)
            putString("context", userContext.toString())
            putString("active", "false")
        }
}
