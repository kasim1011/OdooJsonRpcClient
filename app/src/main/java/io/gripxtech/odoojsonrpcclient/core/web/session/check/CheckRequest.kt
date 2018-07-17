package io.gripxtech.odoojsonrpcclient.core.web.session.check

import io.gripxtech.odoojsonrpcclient.core.entities.session.check.Check
import io.gripxtech.odoojsonrpcclient.core.entities.session.check.CheckReqBody
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CheckRequest {

    @POST("/web/session/check")
    fun check(
            @Body checkReqBody: CheckReqBody
    ): Observable<Response<Check>>
}
