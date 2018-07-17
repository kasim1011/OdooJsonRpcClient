package io.gripxtech.odoojsonrpcclient.core.entities.session.authenticate

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AuthenticateParams(

        @field:Expose
        @field:SerializedName("base_location")
        val baseLocation: String = "",

        @field:Expose
        @field:SerializedName("login")
        val login: String = "",

        @field:Expose
        @field:SerializedName("password")
        val password: String = "",

        @field:Expose
        @field:SerializedName("db")
        val db: String = ""
)
