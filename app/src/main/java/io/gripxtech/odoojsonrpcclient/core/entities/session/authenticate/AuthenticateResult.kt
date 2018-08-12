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
        val superuser: Boolean = false,

        @Expose
        @SerializedName("company_id")
        val companyId: Int = 0,

        @Expose
        @SerializedName("web.base.url")
        val webBaseUrl: String = "",

        @Expose
        @SerializedName("session_id")
        val sessionId: String = "",

        @Expose
        @SerializedName("server_version")
        val serverVersion: String = "",

        @Expose
        @SerializedName("is_admin")
        val admin: Boolean = false,

        @Expose
        @SerializedName("uid")
        val uid: Int = 0,

        @Expose
        @SerializedName("partner_id")
        val partnerId: Int = 0,

        @Expose
        @SerializedName("user_companies")
        val userCompanies: JsonElement = JsonObject(),

        @Expose
        @SerializedName("name")
        var name: String = "",

        @Expose
        @SerializedName("server_version_info")
        val serverVersionInfo: JsonArray = JsonArray(),

        @Expose
        @SerializedName("user_context")
        val userContext: JsonObject = JsonObject(),

        @Expose
        @SerializedName("db")
        val db: String = "",

        @Expose
        @SerializedName("username")
        val username: String = "",

        @Expose
        @SerializedName("currencies")
        val currencies: JsonObject = JsonObject(),

        @Expose
        @SerializedName("web_tours")
        val webTours: JsonArray = JsonArray(),

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
            putString("id", uid.toString())
            putString("partnerId", partnerId.toString())
            putString("name", name)
            putString("imageSmall", imageSmall)
            putString("context", userContext.toString())
            putString("active", "false")
        }
}
