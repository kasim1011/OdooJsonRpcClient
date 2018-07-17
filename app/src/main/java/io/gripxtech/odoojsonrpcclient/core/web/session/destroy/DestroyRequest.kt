package io.gripxtech.odoojsonrpcclient.core.web.session.destroy

import io.gripxtech.odoojsonrpcclient.core.entities.session.destroy.Destroy
import io.gripxtech.odoojsonrpcclient.core.entities.session.destroy.DestroyReqBody
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface DestroyRequest {

    @POST("/web/session/destroy")
    fun destroy(
            @Body destroyReqBody: DestroyReqBody
    ): Observable<Response<Destroy>>
}
