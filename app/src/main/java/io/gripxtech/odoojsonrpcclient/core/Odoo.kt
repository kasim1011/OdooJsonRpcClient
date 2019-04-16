package io.gripxtech.odoojsonrpcclient.core

import android.accounts.Account
import android.accounts.AccountManager
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.gripxtech.odoojsonrpcclient.App
import io.gripxtech.odoojsonrpcclient.R
import io.gripxtech.odoojsonrpcclient.asIntList
import io.gripxtech.odoojsonrpcclient.core.entities.database.listdb.ListDb
import io.gripxtech.odoojsonrpcclient.core.entities.database.listdb.ListDbReqBody
import io.gripxtech.odoojsonrpcclient.core.entities.dataset.callkw.CallKw
import io.gripxtech.odoojsonrpcclient.core.entities.dataset.callkw.CallKwParams
import io.gripxtech.odoojsonrpcclient.core.entities.dataset.callkw.CallKwReqBody
import io.gripxtech.odoojsonrpcclient.core.entities.dataset.execworkflow.ExecWorkflow
import io.gripxtech.odoojsonrpcclient.core.entities.dataset.execworkflow.ExecWorkflowParams
import io.gripxtech.odoojsonrpcclient.core.entities.dataset.execworkflow.ExecWorkflowReqBody
import io.gripxtech.odoojsonrpcclient.core.entities.dataset.load.Load
import io.gripxtech.odoojsonrpcclient.core.entities.dataset.load.LoadParams
import io.gripxtech.odoojsonrpcclient.core.entities.dataset.load.LoadReqBody
import io.gripxtech.odoojsonrpcclient.core.entities.dataset.searchread.SearchRead
import io.gripxtech.odoojsonrpcclient.core.entities.dataset.searchread.SearchReadParams
import io.gripxtech.odoojsonrpcclient.core.entities.dataset.searchread.SearchReadReqBody
import io.gripxtech.odoojsonrpcclient.core.entities.method.checkaccessrights.CheckAccessRights
import io.gripxtech.odoojsonrpcclient.core.entities.method.create.Create
import io.gripxtech.odoojsonrpcclient.core.entities.method.namecreate.NameCreate
import io.gripxtech.odoojsonrpcclient.core.entities.method.nameget.NameGet
import io.gripxtech.odoojsonrpcclient.core.entities.method.namesearch.NameSearch
import io.gripxtech.odoojsonrpcclient.core.entities.method.read.Read
import io.gripxtech.odoojsonrpcclient.core.entities.method.search.Search
import io.gripxtech.odoojsonrpcclient.core.entities.method.searchcount.SearchCount
import io.gripxtech.odoojsonrpcclient.core.entities.method.unlink.Unlink
import io.gripxtech.odoojsonrpcclient.core.entities.method.write.Write
import io.gripxtech.odoojsonrpcclient.core.entities.route.Route
import io.gripxtech.odoojsonrpcclient.core.entities.route.RouteReqBody
import io.gripxtech.odoojsonrpcclient.core.entities.session.authenticate.Authenticate
import io.gripxtech.odoojsonrpcclient.core.entities.session.authenticate.AuthenticateParams
import io.gripxtech.odoojsonrpcclient.core.entities.session.authenticate.AuthenticateReqBody
import io.gripxtech.odoojsonrpcclient.core.entities.session.check.Check
import io.gripxtech.odoojsonrpcclient.core.entities.session.check.CheckReqBody
import io.gripxtech.odoojsonrpcclient.core.entities.session.destroy.Destroy
import io.gripxtech.odoojsonrpcclient.core.entities.session.destroy.DestroyReqBody
import io.gripxtech.odoojsonrpcclient.core.entities.session.info.GetSessionInfo
import io.gripxtech.odoojsonrpcclient.core.entities.session.info.GetSessionInfoReqBody
import io.gripxtech.odoojsonrpcclient.core.entities.session.modules.Modules
import io.gripxtech.odoojsonrpcclient.core.entities.session.modules.ModulesReqBody
import io.gripxtech.odoojsonrpcclient.core.entities.webclient.versionInfo.VersionInfo
import io.gripxtech.odoojsonrpcclient.core.entities.webclient.versionInfo.VersionInfoReqBody
import io.gripxtech.odoojsonrpcclient.core.utils.Retrofit2Helper
import io.gripxtech.odoojsonrpcclient.core.utils.android.ktx.ResponseObserver
import io.gripxtech.odoojsonrpcclient.core.utils.decryptAES
import io.gripxtech.odoojsonrpcclient.core.web.database.listdb.ListDbRequest
import io.gripxtech.odoojsonrpcclient.core.web.database.listdbv8.ListDbV8Request
import io.gripxtech.odoojsonrpcclient.core.web.database.listdbv9.ListDbV9Request
import io.gripxtech.odoojsonrpcclient.core.web.dataset.callkw.CallKwRequest
import io.gripxtech.odoojsonrpcclient.core.web.dataset.execworkflow.ExecWorkflowRequest
import io.gripxtech.odoojsonrpcclient.core.web.dataset.load.LoadRequest
import io.gripxtech.odoojsonrpcclient.core.web.dataset.searchread.SearchReadRequest
import io.gripxtech.odoojsonrpcclient.core.web.route.Route3PathRequest
import io.gripxtech.odoojsonrpcclient.core.web.route.Route4PathRequest
import io.gripxtech.odoojsonrpcclient.core.web.route.RouteRequest
import io.gripxtech.odoojsonrpcclient.core.web.session.authenticate.AuthenticateRequest
import io.gripxtech.odoojsonrpcclient.core.web.session.check.CheckRequest
import io.gripxtech.odoojsonrpcclient.core.web.session.destroy.DestroyRequest
import io.gripxtech.odoojsonrpcclient.core.web.session.info.GetSessionInfoRequest
import io.gripxtech.odoojsonrpcclient.core.web.session.modules.ModulesRequest
import io.gripxtech.odoojsonrpcclient.core.web.webclient.versionInfo.VersionInfoRequest
import io.gripxtech.odoojsonrpcclient.toJsonObject
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Cookie
import retrofit2.Response

object Odoo {

    lateinit var app: App

    var protocol: Retrofit2Helper.Companion.Protocol = Retrofit2Helper.Companion.Protocol.HTTP
        set(value) {
            field = value
            retrofit2Helper.protocol = value
        }
    var host: String = ""
        set(value) {
            field = value
            retrofit2Helper.host = value
        }

    var user: OdooUser = OdooUser()
        set(value) {
            field = value
            protocol = value.protocol
            host = value.host
            OdooDatabase.database = null
        }

    @Suppress("PlatformExtensionReceiverOfInline")
    fun fromAccount(manager: AccountManager, account: Account) = OdooUser(
        Retrofit2Helper.Companion.Protocol.valueOf(
            manager.getUserData(account, "protocol")
        ),
        manager.getUserData(account, "host"),
        manager.getUserData(account, "login"),
        manager.getUserData(account, "password").decryptAES(),
        manager.getUserData(account, "database"),
        manager.getUserData(account, "serverVersion"),
        manager.getUserData(account, "isAdmin").toBoolean(),
        manager.getUserData(account, "isSuperuser").toBoolean(),
        manager.getUserData(account, "id").toInt(),
        manager.getUserData(account, "name"),
        manager.getUserData(account, "imageSmall"),
        manager.getUserData(account, "partnerId").toInt(),
        manager.getUserData(account, "context").toJsonObject(),
        manager.getUserData(account, "active").toBoolean(),
        account
    )

    private val retrofit2Helper = Retrofit2Helper(
        protocol,
        host
    )

    fun resetRetrofitClient() {
        retrofit2Helper.resetClient()
    }

    private val retrofit
        get() = retrofit2Helper.retrofit

    private var jsonRpcId: String = "0"
        get() {
            field = (field.toInt() + 1).toString()
            if (user.id > 0) {
                return "r$field"
            }
            return field
        }

    val supportedOdooVersions: Array<String> by lazy { app.resources.getStringArray(R.array.supported_odoo_versions) }

    fun versionInfo(callback: ResponseObserver<VersionInfo>.() -> Unit) {
        val request = retrofit.create(VersionInfoRequest::class.java)
        val requestBody = VersionInfoReqBody(id = jsonRpcId)
        val observable = request.versionInfo(requestBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ResponseObserver<VersionInfo>().apply(callback))
    }

    fun listDb(serverVersion: String, callback: ResponseObserver<ListDb>.() -> Unit) {
        val requestBody = ListDbReqBody(id = jsonRpcId)
        val observable =
            when {
                serverVersion.startsWith("8.") -> {
                    val request = retrofit.create(ListDbV8Request::class.java)
                    request.listDb(requestBody)
                }
                serverVersion.startsWith("9.") -> {
                    val request = retrofit.create(ListDbV9Request::class.java)
                    request.listDb(requestBody)
                }
                else -> {
                    val request = retrofit.create(ListDbRequest::class.java)
                    request.listDb(requestBody)
                }
            }
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ResponseObserver<ListDb>().apply(callback))
    }

    private val pendingAuthenticateCallbacks: ArrayList<ResponseObserver<Authenticate>.() -> Unit> = arrayListOf()
    val pendingAuthenticateCookies: ArrayList<Cookie> = arrayListOf()

    @Synchronized
    fun authenticate(
        login: String, password: String, database: String,
        callback: ResponseObserver<Authenticate>.() -> Unit
    ) {
        pendingAuthenticateCallbacks += callback
        if (pendingAuthenticateCallbacks.size == 1) {
            val request = retrofit.create(AuthenticateRequest::class.java)
            val requestBody = AuthenticateReqBody(
                id = jsonRpcId, params = AuthenticateParams(
                    host, login, password, database
                )
            )
            val observable = request.authenticate(requestBody)
            observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ResponseObserver<Authenticate>().apply {
                    (pendingAuthenticateCallbacks.size - 1 downTo 0).map {
                        pendingAuthenticateCallbacks.removeAt(it)
                    }.forEach { it() }
                })
        }
    }

    fun check(callback: ResponseObserver<Check>.() -> Unit) {
        val request = retrofit.create(CheckRequest::class.java)
        val requestBody = CheckReqBody(id = jsonRpcId)
        val observable = request.check(requestBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ResponseObserver<Check>().apply(callback))
    }

    fun destroy(callback: ResponseObserver<Destroy>.() -> Unit) {
        val request = retrofit.create(DestroyRequest::class.java)
        val requestBody = DestroyReqBody(id = jsonRpcId)
        val observable = request.destroy(requestBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ResponseObserver<Destroy>().apply(callback))
    }

    fun modules(callback: ResponseObserver<Modules>.() -> Unit) {
        val request = retrofit.create(ModulesRequest::class.java)
        val requestBody = ModulesReqBody(id = jsonRpcId)
        val observable = request.modules(requestBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ResponseObserver<Modules>().apply(callback))
    }

    fun getSessionInfo(callback: ResponseObserver<GetSessionInfo>.() -> Unit) {
        val request = retrofit.create(GetSessionInfoRequest::class.java)
        val requestBody = GetSessionInfoReqBody(id = jsonRpcId)
        val observable = request.getSessionInfo(requestBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ResponseObserver<GetSessionInfo>().apply(callback))
    }

    fun searchRead(
        model: String,
        fields: List<String> = listOf(),
        domain: List<Any> = listOf(),
        offset: Int = 0,
        limit: Int = 0,
        sort: String = "",
        context: JsonObject = user.context,
        callback: ResponseObserver<SearchRead>.() -> Unit
    ) {
        val request = retrofit.create(SearchReadRequest::class.java)
        val requestBody = SearchReadReqBody(
            id = jsonRpcId, params = SearchReadParams(
                model, fields, domain, offset, limit, sort, context
            )
        )
        val observable = request.searchRead(requestBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ResponseObserver<SearchRead>().apply(callback))
    }

    fun load(
        id: Int,
        model: String,
        fields: List<String> = listOf(),
        context: JsonObject = user.context,
        callback: ResponseObserver<Load>.() -> Unit
    ) {
        val request = retrofit.create(LoadRequest::class.java)
        val requestBody = LoadReqBody(
            id = jsonRpcId, params = LoadParams(
                id, model, fields, context
            )
        )
        val observable = request.load(requestBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ResponseObserver<Load>().apply(callback))
    }

    fun callKw(
        model: String,
        method: String,
        args: List<Any>,
        kwArgs: Map<String, Any> = mapOf(),
        context: JsonObject = user.context,
        callback: ResponseObserver<CallKw>.() -> Unit
    ) {
        val request = retrofit.create(CallKwRequest::class.java)
        val requestBody = CallKwReqBody(
            id = jsonRpcId, params = CallKwParams(
                model, method, args, kwArgs, context
            )
        )
        val observable = request.callKw(requestBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ResponseObserver<CallKw>().apply(callback))
    }

    fun execWorkflow(
        model: String,
        id: Int,
        signal: String,
        context: JsonObject = user.context,
        callback: ResponseObserver<ExecWorkflow>.() -> Unit
    ) {
        val request = retrofit.create(ExecWorkflowRequest::class.java)
        val requestBody = ExecWorkflowReqBody(
            id = jsonRpcId, params = ExecWorkflowParams(
                model, id, signal, context
            )
        )
        val observable = request.execWorkflow(requestBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ResponseObserver<ExecWorkflow>().apply(callback))
    }

    fun route(
        path1: String,
        path2: String,
        args: Any = mapOf<String, Any>(),
        callback: ResponseObserver<Route>.() -> Unit
    ) {
        val request = retrofit.create(RouteRequest::class.java)
        val requestBody = RouteReqBody(id = jsonRpcId, params = args)
        val observable = request.route(path1, path2, requestBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ResponseObserver<Route>().apply(callback))
    }

    fun route3Path(
        path1: String,
        path2: String,
        path3: String,
        args: Any = mapOf<String, Any>(),
        callback: ResponseObserver<Route>.() -> Unit
    ) {
        val request = retrofit.create(Route3PathRequest::class.java)
        val requestBody = RouteReqBody(id = jsonRpcId, params = args)
        val observable = request.route(path1, path2, path3, requestBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ResponseObserver<Route>().apply(callback))
    }

    fun route4Path(
        path1: String,
        path2: String,
        path3: String,
        path4: String,
        args: Any = mapOf<String, Any>(),
        callback: ResponseObserver<Route>.() -> Unit
    ) {
        val request = retrofit.create(Route4PathRequest::class.java)
        val requestBody = RouteReqBody(id = jsonRpcId, params = args)
        val observable = request.route(path1, path2, path3, path4, requestBody)
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ResponseObserver<Route>().apply(callback))
    }

    fun create(
        model: String,
        values: Any,
        kwArgs: Map<String, Any> = mapOf(),
        context: JsonObject = user.context,
        callback: ResponseObserver<Create>.() -> Unit
    ) {
        val callbackEx = ResponseObserver<Create>()
        callbackEx.callback()
        callKw(model, "create", listOf(values), kwArgs, context) {
            onSubscribe { disposable ->
                callbackEx.onSubscribe(disposable)
            }

            onNext { response ->
                callbackEx.onNext(
                    if (response.isSuccessful)
                        Response.success<Create>(
                            Create(
                                if (response.body()!!.isSuccessful)
                                    response.body()!!.result.asLong
                                else
                                    0L
                                , response.body()!!.odooError
                            )
                        )
                    else
                        Response.error<Create>(response.code(), response.errorBody()!!)
                )
            }

            onError { error ->
                callbackEx.onError(error)
            }

            onComplete {
                callbackEx.onComplete()
            }
        }
    }

    fun read(
        model: String,
        ids: List<Int>,
        fields: List<String>,
        kwArgs: Map<String, Any> = mapOf(),
        context: JsonObject = user.context,
        callback: ResponseObserver<Read>.() -> Unit
    ) {
        val callbackEx = ResponseObserver<Read>()
        callbackEx.callback()
        callKw(model, "read", listOf(ids, fields), kwArgs, context) {
            onSubscribe { disposable ->
                callbackEx.onSubscribe(disposable)
            }

            onNext { response ->
                callbackEx.onNext(
                    if (response.isSuccessful)
                        Response.success<Read>(
                            Read(
                                if (response.body()!!.isSuccessful)
                                    response.body()!!.result
                                else
                                    JsonArray()
                                , response.body()!!.odooError
                            )
                        )
                    else
                        Response.error<Read>(response.code(), response.errorBody()!!)
                )
            }

            onError { error ->
                callbackEx.onError(error)
            }

            onComplete {
                callbackEx.onComplete()
            }
        }
    }

    fun write(
        model: String,
        ids: List<Int>,
        values: Any,
        kwArgs: Map<String, Any> = mapOf(),
        context: JsonObject = user.context,
        callback: ResponseObserver<Write>.() -> Unit
    ) {
        val callbackEx = ResponseObserver<Write>()
        callbackEx.callback()
        callKw(model, "write", listOf(ids, values), kwArgs, context) {
            onSubscribe { disposable ->
                callbackEx.onSubscribe(disposable)
            }

            onNext { response ->
                callbackEx.onNext(
                    if (response.isSuccessful)
                        Response.success<Write>(
                            Write(
                                if (response.body()!!.isSuccessful)
                                    response.body()!!.result.asBoolean
                                else
                                    false
                                , response.body()!!.odooError
                            )
                        )
                    else
                        Response.error<Write>(response.code(), response.errorBody()!!)
                )
            }

            onError { error ->
                callbackEx.onError(error)
            }

            onComplete {
                callbackEx.onComplete()
            }
        }
    }

    fun unlink(
        model: String,
        ids: List<Int>,
        kwArgs: Map<String, Any> = mapOf(),
        context: JsonObject = user.context,
        callback: ResponseObserver<Unlink>.() -> Unit
    ) {
        val callbackEx = ResponseObserver<Unlink>()
        callbackEx.callback()
        callKw(model, "unlink", listOf(ids), kwArgs, context) {
            onSubscribe { disposable ->
                callbackEx.onSubscribe(disposable)
            }

            onNext { response ->
                callbackEx.onNext(
                    if (response.isSuccessful)
                        Response.success<Unlink>(
                            Unlink(
                                if (response.body()!!.isSuccessful)
                                    response.body()!!.result.asBoolean
                                else
                                    false
                                , response.body()!!.odooError
                            )
                        )
                    else
                        Response.error<Unlink>(response.code(), response.errorBody()!!)
                )
            }

            onError { error ->
                callbackEx.onError(error)
            }

            onComplete {
                callbackEx.onComplete()
            }
        }
    }

    fun nameGet(
        model: String,
        ids: List<Int>,
        kwArgs: Map<String, Any> = mapOf(),
        context: JsonObject = user.context,
        callback: ResponseObserver<NameGet>.() -> Unit
    ) {
        val callbackEx = ResponseObserver<NameGet>()
        callbackEx.callback()
        callKw(model, "name_get", listOf(ids), kwArgs, context) {
            onSubscribe { disposable ->
                callbackEx.onSubscribe(disposable)
            }

            onNext { response ->
                callbackEx.onNext(
                    if (response.isSuccessful)
                        Response.success<NameGet>(
                            NameGet(
                                if (response.body()!!.isSuccessful)
                                    response.body()!!.result.asJsonArray
                                else
                                    JsonArray()
                                , response.body()!!.odooError
                            )
                        )
                    else
                        Response.error<NameGet>(response.code(), response.errorBody()!!)
                )
            }

            onError { error ->
                callbackEx.onError(error)
            }

            onComplete {
                callbackEx.onComplete()
            }
        }
    }

    fun nameCreate(
        model: String,
        name: String,
        kwArgs: Map<String, Any> = mapOf(),
        context: JsonObject = user.context,
        callback: ResponseObserver<NameCreate>.() -> Unit
    ) {
        val callbackEx = ResponseObserver<NameCreate>()
        callbackEx.callback()
        callKw(model, "name_create", listOf(name), kwArgs, context) {
            onSubscribe { disposable ->
                callbackEx.onSubscribe(disposable)
            }

            onNext { response ->
                callbackEx.onNext(
                    if (response.isSuccessful)
                        Response.success<NameCreate>(
                            NameCreate(
                                if (response.body()!!.isSuccessful)
                                    response.body()!!.result.asJsonArray
                                else
                                    JsonArray()
                                , response.body()!!.odooError
                            )
                        )
                    else
                        Response.error<NameCreate>(response.code(), response.errorBody()!!)
                )
            }

            onError { error ->
                callbackEx.onError(error)
            }

            onComplete {
                callbackEx.onComplete()
            }
        }
    }

    fun nameSearch(
        model: String,
        name: String = "",
        args: List<Any> = listOf(),
        operator: String = "ilike",
        limit: Int = 0,
        context: JsonObject = user.context,
        callback: ResponseObserver<NameSearch>.() -> Unit
    ) {
        val callbackEx = ResponseObserver<NameSearch>()
        callbackEx.callback()
        callKw(
            model, "name_search", listOf(), mapOf(
                "name" to name,
                "args" to args,
                "operator" to operator,
                "limit" to limit
            ), context
        ) {
            onSubscribe { disposable ->
                callbackEx.onSubscribe(disposable)
            }

            onNext { response ->
                callbackEx.onNext(
                    if (response.isSuccessful)
                        Response.success<NameSearch>(
                            NameSearch(
                                if (response.body()!!.isSuccessful)
                                    response.body()!!.result.asJsonArray
                                else
                                    JsonArray()
                                , response.body()!!.odooError
                            )
                        )
                    else
                        Response.error<NameSearch>(response.code(), response.errorBody()!!)
                )
            }

            onError { error ->
                callbackEx.onError(error)
            }

            onComplete {
                callbackEx.onComplete()
            }
        }
    }

    fun search(
        model: String,
        domain: List<Any> = listOf(),
        offset: Int = 0,
        limit: Int = 0,
        sort: String = "",
        count: Boolean = false,
        kwArgs: Map<String, Any> = mapOf(),
        context: JsonObject = user.context,
        callback: ResponseObserver<Search>.() -> Unit
    ) {
        val callbackEx = ResponseObserver<Search>()
        callbackEx.callback()
        callKw(model, "search", listOf(domain, offset, limit, sort, count), kwArgs, context) {
            onSubscribe { disposable ->
                callbackEx.onSubscribe(disposable)
            }

            onNext { response ->
                callbackEx.onNext(
                    if (response.isSuccessful)
                        Response.success<Search>(
                            Search(
                                if (response.body()!!.isSuccessful)
                                    response.body()!!.result.asJsonArray.asIntList
                                else
                                    listOf()
                                , response.body()!!.odooError
                            )
                        )
                    else
                        Response.error<Search>(response.code(), response.errorBody()!!)
                )
            }

            onError { error ->
                callbackEx.onError(error)
            }

            onComplete {
                callbackEx.onComplete()
            }
        }
    }

    fun searchCount(
        model: String,
        args: List<Any> = listOf(),
        kwArgs: Map<String, Any> = mapOf(),
        context: JsonObject = user.context,
        callback: ResponseObserver<SearchCount>.() -> Unit
    ) {
        val callbackEx = ResponseObserver<SearchCount>()
        callbackEx.callback()
        callKw(model, "search_count", listOf(args), kwArgs, context) {
            onSubscribe { disposable ->
                callbackEx.onSubscribe(disposable)
            }

            onNext { response ->
                callbackEx.onNext(
                    if (response.isSuccessful)
                        Response.success<SearchCount>(
                            SearchCount(
                                if (response.body()!!.isSuccessful)
                                    response.body()!!.result.asInt
                                else
                                    0
                                , response.body()!!.odooError
                            )
                        )
                    else
                        Response.error<SearchCount>(response.code(), response.errorBody()!!)
                )
            }

            onError { error ->
                callbackEx.onError(error)
            }

            onComplete {
                callbackEx.onComplete()
            }
        }
    }

    fun checkAccessRights(
        model: String,
        operation: String,
        raiseException: Boolean = false,
        kwArgs: Map<String, Any> = mapOf(),
        context: JsonObject = user.context,
        callback: ResponseObserver<CheckAccessRights>.() -> Unit
    ) {
        val callbackEx = ResponseObserver<CheckAccessRights>()
        callbackEx.callback()
        callKw(model, "check_access_rights", listOf(operation, raiseException), kwArgs, context) {
            onSubscribe { disposable ->
                callbackEx.onSubscribe(disposable)
            }

            onNext { response ->
                callbackEx.onNext(
                    if (response.isSuccessful)
                        Response.success<CheckAccessRights>(
                            CheckAccessRights(
                                if (response.body()!!.isSuccessful)
                                    response.body()!!.result.asBoolean
                                else
                                    false
                                , response.body()!!.odooError
                            )
                        )
                    else
                        Response.error<CheckAccessRights>(response.code(), response.errorBody()!!)
                )
            }

            onError { error ->
                callbackEx.onError(error)
            }

            onComplete {
                callbackEx.onComplete()
            }
        }
    }

    fun fieldsGet(
        model: String = "",
        fields: List<String> = listOf(),
        callback: ResponseObserver<SearchRead>.() -> Unit
    ) =
        searchRead(
            "ir.model.fields", fields,
            if (model.isNotEmpty()) listOf(listOf("model_id", "=", model)) else listOf(),
            callback = callback
        )


    fun accessGet(
        model: String = "",
        fields: List<String> = listOf(),
        callback: ResponseObserver<SearchRead>.() -> Unit
    ) =
        searchRead(
            "ir.model.access", fields,
            if (model.isNotEmpty()) listOf(listOf("model_id", "=", model)) else listOf(),
            callback = callback
        )

    fun groupsGet(
        fields: List<String> = listOf(),
        callback: ResponseObserver<SearchRead>.() -> Unit
    ) =
        searchRead(
            "res.groups", fields,
            listOf(listOf("users", "in", listOf(user.id))),
            callback = callback
        )

}
