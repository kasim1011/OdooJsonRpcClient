package io.gripxtech.odoojsonrpcclient.customer.entities

import android.util.Base64
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.gripxtech.odoojsonrpcclient.App
import io.gripxtech.odoojsonrpcclient.GlideApp

data class Customer(

    @Expose
    @SerializedName("id")
    val id: Int,

    @Expose
    @SerializedName("name")
    val name: String,

    @Expose
    @SerializedName("email")
    val email: String,

    @Expose
    @SerializedName("company_name")
    val companyName: String,

    @Expose
    @SerializedName("image_small")
    val imageSmall: String,

    @Expose
    @SerializedName("website")
    val website: String,

    @Expose
    @SerializedName("phone")
    val phone: String,

    @Expose
    @SerializedName("mobile")
    val mobile: String,

    @Expose
    @SerializedName("full_address")
    val fullAddress: String,

    @Expose
    @SerializedName("state_id")
    val stateId: JsonElement,

    @Expose
    @SerializedName("country_id")
    val countryId: JsonElement,

    @Expose
    @SerializedName("comment")
    val comment: String,

    @Expose
    @SerializedName("is_company")
    val isCompany: Boolean
) {
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
                .dontAnimate()
                .circleCrop()
                .into(view)
        }

        @JvmField
        val fieldsMap: Map<String, String> = mapOf(
            "id" to "id", "name" to "Name", "email" to "Email",
            "company_name" to "Company Name", "image_small" to "Image", "website" to "Website",
            "phone" to "Phone Number", "mobile" to "Mobile Number",/* "full_address" to "Full Address",*/
            "state_id" to "State", "country_id" to "Country", "comment" to "Internal Note",
            "is_company" to "Is Company"
        )

        @JvmField
        val fields: ArrayList<String> = fieldsMap.keys.toMutableList() as ArrayList<String>
    }
}