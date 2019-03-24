package io.gripxtech.odoojsonrpcclient.core

import android.accounts.Account
import android.util.Base64
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.gson.JsonObject
import io.gripxtech.odoojsonrpcclient.App
import io.gripxtech.odoojsonrpcclient.GlideApp
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

    companion object {
        @JvmStatic
        @BindingAdapter("image_small", "name")
        fun loadImage(view: ImageView, imageSmall: String, name: String) {
            GlideApp.with(view.context)
                .asBitmap()
                .load(
                    if (imageSmall.isNotEmpty())
                        Base64.decode(imageSmall, Base64.DEFAULT)
                    else
                        (view.context.applicationContext as App)
                            .getLetterTile(if (name.isNotEmpty()) name else "X")
                )
                .circleCrop()
                .into(view)
        }
    }
}
