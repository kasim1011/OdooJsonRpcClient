package io.gripxtech.odoojsonrpcclient.core.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import io.gripxtech.odoojsonrpcclient.App
import io.gripxtech.odoojsonrpcclient.R
import io.gripxtech.odoojsonrpcclient.core.Odoo
import io.gripxtech.odoojsonrpcclient.core.utils.android.ktx.subscribeEx
import io.gripxtech.odoojsonrpcclient.gson
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.io.File
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class Retrofit2Helper(
    _protocol: Protocol,
    _host: String
) {
    companion object {
        const val TAG = "Retrofit2Helper"
        const val networkLogFile = "network_log_file.txt"

        fun getLogfile(): Uri {
            val file = File("${app.filesDir}${File.pathSeparator}$networkLogFile")
            return FileProvider.getUriForFile(
                app, "${app.packageName}.fileprovider", file
            )
        }

        enum class Protocol {
            HTTP, HTTPS
        }

        lateinit var app: App
    }

    var protocol: Protocol = _protocol
        set(value) {
            field = value
            _retrofit = null
        }

    var host: String = _host
        set(value) {
            field = value
            _retrofit = null
        }

    private var _retrofit: Retrofit? = null

    fun resetClient() {
        _retrofit = null
    }

    val retrofit: Retrofit
        get() {
            if (_retrofit == null) {
                if (host.isEmpty()) {
                    host = app.getString(R.string.host_url)
                }
                _retrofit = Retrofit.Builder()
                    .baseUrl(
                        when (protocol) {
                            Companion.Protocol.HTTP -> {
                                "http://"
                            }
                            Companion.Protocol.HTTPS -> {
                                "https://"
                            }
                        } + host
                    )
                    .client(client)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
            }
            return _retrofit!!
        }

    private val client: OkHttpClient
        get() = OkHttpClient()
            .newBuilder()
            .cookieJar(object : CookieJar {

                private var cookies: MutableList<Cookie>? = Retrofit2Helper.app.cookiePrefs.getCookies()

                override fun saveFromResponse(url: HttpUrl?, cookies: MutableList<Cookie>?) {
                    if (cookies != null && cookies.isNotEmpty()) {
                        this.cookies = cookies
                        Odoo.pendingAuthenticateCookies.clear()
                        Odoo.pendingAuthenticateCookies.addAll(cookies)
                    }
                }

                override fun loadForRequest(url: HttpUrl?): MutableList<Cookie>? =
                    cookies
            })
            .addInterceptor { chain: Interceptor.Chain? ->
                writeFile(dateStamp, Context.MODE_PRIVATE)
                val original = chain!!.request()

                val request = original.newBuilder()
                    .header("User-Agent", android.os.Build.MODEL)
                    .method(original.method(), original.body())
                    .build()

                chain.proceed(request).also {
                    writeFile(dateStamp, Context.MODE_APPEND)
                }
            }
            .addInterceptor(HttpLoggingInterceptor {
                Timber.tag("OkHttp").d(it)
                writeFile("$it\n", Context.MODE_APPEND)
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .apply {
                if (app.resources.getBoolean(R.bool.self_signed_cert)) {
                    unsafeCert(this)
                }
            }
            .build()

    private fun unsafeCert(builder: OkHttpClient.Builder) {
        val trustManagers = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit

            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

        })

        val sslSocketFactory = SSLContext.getInstance("SSL").apply {
            init(null, trustManagers, SecureRandom())
        }.socketFactory

        builder.sslSocketFactory(sslSocketFactory, trustManagers[0] as X509TrustManager)
        builder.hostnameVerifier { _, _ -> true }
    }

    private val dateStamp: String
        get() = "------------------------- ${Date()}: -------------------------\n"

    private var logOperationRunning = false
    private var logOperationParams = ArrayList<Pair<String, Int>>()

    private fun writeFile(fileContents: String, mode: Int, skipCheck: Boolean = false) {
        if (skipCheck || !logOperationRunning) {
            logOperationRunning = true
            Completable.fromCallable {
                app.openFileOutput(networkLogFile, mode)?.use {
                    if (fileContents.startsWith("Set-Cookie:")) {
                        it.write(fileContents.encryptAES().toByteArray(charset = Charsets.UTF_8))
                    } else {
                        it.write(fileContents.toByteArray(charset = Charsets.UTF_8))
                    }
                }
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeEx {

                fun popFirstParams() {
                    if (logOperationParams.isNotEmpty()) {
                        logOperationParams.removeAt(0).let {
                            writeFile(it.first, it.second, true)
                        }
                    } else {
                        logOperationRunning = false
                    }
                }

                onSubscribe { }

                onError {
                    it.printStackTrace()
                    popFirstParams()
                }

                onComplete {
                    popFirstParams()
                }
            }
        } else {
            logOperationParams.add(Pair(fileContents, mode))
        }
    }
}