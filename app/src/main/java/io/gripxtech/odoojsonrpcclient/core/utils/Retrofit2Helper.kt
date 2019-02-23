package io.gripxtech.odoojsonrpcclient.core.utils

import io.gripxtech.odoojsonrpcclient.App
import io.gripxtech.odoojsonrpcclient.R
import io.gripxtech.odoojsonrpcclient.core.Odoo
import io.gripxtech.odoojsonrpcclient.gson
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class Retrofit2Helper(
    _protocol: Retrofit2Helper.Companion.Protocol,
    _host: String
) {
    companion object {
        const val TAG = "Retrofit2Helper"

        enum class Protocol {
            HTTP, HTTPS
        }

        lateinit var app: App
    }

    var protocol: Retrofit2Helper.Companion.Protocol = _protocol
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
                            Retrofit2Helper.Companion.Protocol.HTTP -> {
                                "http://"
                            }
                            Retrofit2Helper.Companion.Protocol.HTTPS -> {
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
                    if (url.toString().contains("/web/session/authenticate") || url.toString().contains("web/session/check")) {
                        this.cookies = cookies
                        if (cookies != null) {
                            Odoo.pendingAuthenticateCookies.clear()
                            Odoo.pendingAuthenticateCookies.addAll(cookies)
                        }
                    }
                }

                override fun loadForRequest(url: HttpUrl?): MutableList<Cookie>? =
                    cookies
            })
            .addInterceptor { chain: Interceptor.Chain? ->
                val original = chain!!.request()

                val request = original.newBuilder()
                    .header("User-Agent", android.os.Build.MODEL)
                    .method(original.method(), original.body())
                    .build()

                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor {
                Timber.tag("OkHttp").d(it)
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            /*.apply(::unsafeCert)*/
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
}