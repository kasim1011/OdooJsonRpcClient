package io.gripxtech.odoojsonrpcclient.core.utils

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import okhttp3.Cookie

@Parcelize
data class ClonedCookie(

        @field:Expose
        @field:SerializedName("name")
        private val name: String,

        @field:Expose
        @field:SerializedName("value")
        private val value: String,

        @field:Expose
        @field:SerializedName("domain")
        private val domain: String,

        @field:Expose
        @field:SerializedName("path")
        private val path: String,

        @field:Expose
        @field:SerializedName("expiresAt")
        private val expiresAt: Long,

        @field:Expose
        @field:SerializedName("secure")
        private val secure: Boolean,

        @field:Expose
        @field:SerializedName("httpOnly")
        private val httpOnly: Boolean,

        @field:Expose
        @field:SerializedName("persistent")
        private val persistent: Boolean,

        @field:Expose
        @field:SerializedName("hostOnly")
        private val hostOnly: Boolean

) : Parcelable {
    companion object {

        fun fromCookie(cookie: Cookie) = ClonedCookie(
                cookie.name(), cookie.value(),
                cookie.domain(), cookie.path(),
                cookie.expiresAt(), cookie.secure(),
                cookie.httpOnly(), cookie.persistent(),
                cookie.hostOnly()
        )
    }

    fun toCookie(): Cookie = Cookie.Builder().apply {
        name(name)
        value(value)
        domain(domain)
        path(path)
        expiresAt(expiresAt)
        if (secure) secure()
        if (httpOnly) httpOnly()
        if (hostOnly) hostOnlyDomain(domain)
    }.build()
}
