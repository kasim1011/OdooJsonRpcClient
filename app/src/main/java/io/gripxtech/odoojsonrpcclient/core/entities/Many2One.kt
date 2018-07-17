package io.gripxtech.odoojsonrpcclient.core.entities

import com.google.gson.JsonElement
import io.gripxtech.odoojsonrpcclient.toJsonElement

data class Many2One(
        private val jsonElement: JsonElement
) {
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
}