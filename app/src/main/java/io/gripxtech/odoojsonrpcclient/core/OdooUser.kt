package io.gripxtech.odoojsonrpcclient.core

import android.accounts.Account
import com.google.gson.JsonObject
import io.gripxtech.odoojsonrpcclient.App
import io.gripxtech.odoojsonrpcclient.core.utils.Retrofit2Helper

data class OdooUser(
    var protocol: Retrofit2Helper.Companion.Protocol = Retrofit2Helper.Companion.Protocol.HTTP,
    var host: String = "",
    var login: String = "",
    var password: String = "",
    var database: String = "",
    var serverVersion: String = "",
    var isAdmin: Boolean = false,
    var isSuperUser: Boolean = false,
    var id: Int = 0,
    var name: String = "",
    var imageSmall: String = "",
    var partnerId: Int = 0,
    var context: JsonObject = JsonObject(),
    var isActive: Boolean = false,
    var account: Account = Account("false", App.KEY_ACCOUNT_TYPE)
) {
    val androidName: String
        get() = "$login[$database]"

    val timezone: String
        get() = context["tz"].asString
}
