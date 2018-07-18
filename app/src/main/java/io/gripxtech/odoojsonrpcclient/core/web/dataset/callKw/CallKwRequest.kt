package io.gripxtech.odoojsonrpcclient.core.web.dataset.callKw

import io.gripxtech.odoojsonrpcclient.core.entities.dataset.callKw.CallKw
import io.gripxtech.odoojsonrpcclient.core.entities.dataset.callKw.CallKwReqBody
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CallKwRequest {

    @POST("/web/dataset/call_kw")
    fun callKw(
            @Body callKwReqBody: CallKwReqBody
    ): Observable<Response<CallKw>>

}