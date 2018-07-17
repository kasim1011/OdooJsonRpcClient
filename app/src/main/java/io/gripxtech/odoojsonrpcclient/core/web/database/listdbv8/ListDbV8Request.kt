package io.gripxtech.odoojsonrpcclient.core.web.database.listdbv8

import io.gripxtech.odoojsonrpcclient.core.entities.database.listdb.ListDb
import io.gripxtech.odoojsonrpcclient.core.entities.database.listdb.ListDbReqBody
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ListDbV8Request {

    @POST("/web/database/get_list")
    fun listDb(
            @Body listDbReqBody: ListDbReqBody
    ): Observable<Response<ListDb>>
}