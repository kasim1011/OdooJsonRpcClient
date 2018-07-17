package io.gripxtech.odoojsonrpcclient.core.web.session.authenticate

import io.gripxtech.odoojsonrpcclient.core.entities.session.authenticate.Authenticate
import io.gripxtech.odoojsonrpcclient.core.entities.session.authenticate.AuthenticateReqBody
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthenticateRequest {

    @POST("/web/session/authenticate")
    fun authenticate(
            @Body authenticateReqBody: AuthenticateReqBody
    ): Observable<Response<Authenticate>>
}
