package io.gripxtech.odoojsonrpcclient.core.entities.dataset.searchread

import com.google.gson.JsonArray
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SearchReadResult(

        @field:Expose
        @field:SerializedName("records")
        var records: JsonArray = JsonArray(),

        @field:Expose
        @field:SerializedName("length")
        var length: Int = 0
)
