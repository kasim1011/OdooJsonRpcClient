package io.gripxtech.odoojsonrpcclient.core

import android.accounts.Account
import android.accounts.AccountManager
import com.google.gson.JsonObject
import io.gripxtech.odoojsonrpcclient.App
import io.gripxtech.odoojsonrpcclient.core.entities.database.listdb.ListDb
import io.gripxtech.odoojsonrpcclient.core.entities.database.listdb.ListDbReqBody
import io.gripxtech.odoojsonrpcclient.core.entities.dataset.searchread.SearchRead
import io.gripxtech.odoojsonrpcclient.core.entities.dataset.searchread.SearchReadParams
import io.gripxtech.odoojsonrpcclient.core.entities.dataset.searchread.SearchReadReqBody
import io.gripxtech.odoojsonrpcclient.core.entities.session.authenticate.Authenticate
import io.gripxtech.odoojsonrpcclient.core.entities.session.authenticate.AuthenticateParams
import io.gripxtech.odoojsonrpcclient.core.entities.session.authenticate.AuthenticateReqBody
import io.gripxtech.odoojsonrpcclient.core.entities.session.check.Check
import io.gripxtech.odoojsonrpcclient.core.entities.session.check.CheckReqBody
import io.gripxtech.odoojsonrpcclient.core.entities.session.destroy.Destroy
import io.gripxtech.odoojsonrpcclient.core.entities.session.destroy.DestroyReqBody
import io.gripxtech.odoojsonrpcclient.core.entities.session.modules.Modules
import io.gripxtech.odoojsonrpcclient.core.entities.session.modules.ModulesReqBody
import io.gripxtech.odoojsonrpcclient.core.entities.webclient.versionInfo.VersionInfo
import io.gripxtech.odoojsonrpcclient.core.entities.webclient.versionInfo.VersionInfoReqBody
import io.gripxtech.odoojsonrpcclient.core.utils.Retrofit2Helper
import io.gripxtech.odoojsonrpcclient.core.utils.android.ktx.ResponseObserver
import io.gripxtech.odoojsonrpcclient.core.web.database.listdb.ListDbRequest
import io.gripxtech.odoojsonrpcclient.core.web.database.listdbv8.ListDbV8Request
import io.gripxtech.odoojsonrpcclient.core.web.database.listdbv9.ListDbV9Request
import io.gripxtech.odoojsonrpcclient.core.web.dataset.searchread.SearchReadRequest
import io.gripxtech.odoojsonrpcclient.core.web.session.authenticate.AuthenticateRequest
import io.gripxtech.odoojsonrpcclient.core.web.session.check.CheckRequest
import io.gripxtech.odoojsonrpcclient.core.web.session.destroy.DestroyRequest
import io.gripxtech.odoojsonrpcclient.core.web.session.modules.ModulesRequest
import io.gripxtech.odoojsonrpcclient.core.web.webclient.versionInfo.VersionInfoRequest
import io.gripxtech.odoojsonrpcclient.toJsonObject
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Cookie

object Odoo {

    const val TAG = "Odoo"
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
        }

    @Suppress("PlatformExtensionReceiverOfInline")
    fun fromAccount(manager: AccountManager, account: Account) = OdooUser(
            Retrofit2Helper.Companion.Protocol.valueOf(
                    manager.getUserData(account, "protocol")
            ),
            manager.getUserData(account, "host"),
            manager.getUserData(account, "login"),
            manager.getUserData(account, "password"),
            manager.getUserData(account, "database"),
            manager.getUserData(account, "serverVersion"),
            manager.getUserData(account, "isAdmin").toBoolean(),
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

    val supportedOdooVersions = listOf("10.", "11.")

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
    fun authenticate(login: String, password: String, database: String,
                     callback: ResponseObserver<Authenticate>.() -> Unit) {
        pendingAuthenticateCallbacks += callback
        if (pendingAuthenticateCallbacks.size == 1) {
            val request = retrofit.create(AuthenticateRequest::class.java)
            val requestBody = AuthenticateReqBody(id = jsonRpcId, params = AuthenticateParams(
                    host, login, password, database
            ))
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
        val requestBody = SearchReadReqBody(id = jsonRpcId, params = SearchReadParams(
                model, fields, domain, offset, limit, sort, context
        ))
        val observable = request.searchRead(requestBody)
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ResponseObserver<SearchRead>().apply(callback))
    }

}