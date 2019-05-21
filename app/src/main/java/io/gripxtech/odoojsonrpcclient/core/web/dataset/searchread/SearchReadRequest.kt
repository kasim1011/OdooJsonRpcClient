package io.gripxtech.odoojsonrpcclient.core.web.dataset.searchread

import io.gripxtech.odoojsonrpcclient.core.entities.dataset.searchread.SearchRead
import io.gripxtech.odoojsonrpcclient.core.entities.dataset.searchread.SearchReadReqBody
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SearchReadRequest {

    companion object {
        const val Route = "/web/dataset/search_read"
    }

    @POST(Route)
    fun searchRead(
            @Body searchReadReqBody: SearchReadReqBody
    ): Observable<Response<SearchRead>>

    @POST(Route)
    fun searchReadW(
        @Body searchReadReqBody: SearchReadReqBody
    ): Call<SearchRead>
}
