package io.gripxtech.odoojsonrpcclient.core.entities.dataset.searchread

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.gripxtech.odoojsonrpcclient.core.entities.odooError.OdooError

data class SearchRead(

        @field:Expose
        @field:SerializedName("result")
        var result: SearchReadResult = SearchReadResult(),

        @field:Expose
        @field:SerializedName("error")
        var odooError: OdooError = OdooError()
) {
    val isSuccessful get() = !isOdooError
    val isOdooError get() = odooError.message.isNotEmpty()
    val errorCode get() = odooError.code
    val errorMessage get() = odooError.data.message
}
