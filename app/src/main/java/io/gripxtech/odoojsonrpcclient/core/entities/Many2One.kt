package io.gripxtech.odoojsonrpcclient.core.entities

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.JsonElement
import io.gripxtech.odoojsonrpcclient.toJsonElement
import io.gripxtech.odoojsonrpcclient.toStringList

data class Many2One(
        private var jsonElement: JsonElement
) : Parcelable {
    val isManyToOne: Boolean
        get() = jsonElement.isJsonArray && jsonElement.asJsonArray.size() == 2

    var id: Int
        get() = if (isManyToOne) jsonElement.asJsonArray[0].asInt else 0
        set(value) {
            if (isManyToOne) jsonElement.asJsonArray.set(0, value.toString().toJsonElement())
        }

    var name: String
        get() = if (isManyToOne) jsonElement.asJsonArray[1].asString else ""
        set(value) {
            if (isManyToOne) jsonElement.asJsonArray.set(1, value.toJsonElement())
        }

    constructor(parcel: Parcel) : this(
        arrayListOf<String>().apply {
            parcel.readStringList(this)
        }.toJsonElement()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(toStringList())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Many2One> {
        override fun createFromParcel(parcel: Parcel): Many2One {
            return Many2One(parcel)
        }

        override fun newArray(size: Int): Array<Many2One?> {
            return arrayOfNulls(size)
        }
    }
}