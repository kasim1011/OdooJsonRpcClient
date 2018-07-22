package io.gripxtech.odoojsonrpcclient.core.web.dataset.callkw

import io.gripxtech.odoojsonrpcclient.core.entities.dataset.callkw.CallKw
import io.gripxtech.odoojsonrpcclient.core.entities.dataset.callkw.CallKwReqBody
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