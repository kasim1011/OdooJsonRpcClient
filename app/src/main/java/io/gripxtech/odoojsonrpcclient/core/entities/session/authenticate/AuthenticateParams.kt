package io.gripxtech.odoojsonrpcclient.core.entities.session.authenticate

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AuthenticateParams(

        @field:Expose
        @field:SerializedName("base_location")
        var baseLocation: String = "",

        @field:Expose
        @field:SerializedName("login")
        var login: String = "",

        @field:Expose
        @field:SerializedName("password")
        var password: String = "",

        @field:Expose
        @field:SerializedName("db")
        var db: String = ""
)
