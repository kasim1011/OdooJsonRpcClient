package io.gripxtech.odoojsonrpcclient

import android.app.Application
import io.gripxtech.odoojsonrpcclient.core.Odoo
import io.gripxtech.odoojsonrpcclient.core.utils.CookiePrefs
import io.gripxtech.odoojsonrpcclient.core.utils.LetterTileProvider
import io.gripxtech.odoojsonrpcclient.core.utils.Retrofit2Helper
import timber.log.Timber

class App : Application() {

    companion object {
        const val KEY_ACCOUNT_TYPE = "${BuildConfig.APPLICATION_ID}.auth"
    }

    private val letterTileProvider: LetterTileProvider by lazy {
        LetterTileProvider(this)
    }

    val cookiePrefs: CookiePrefs by lazy {
        CookiePrefs(this)
    }

    override fun onCreate() {
        super.onCreate()
        Odoo.app = this
        Retrofit2Helper.app = this

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    fun getLetterTile(displayName: String): ByteArray =
            letterTileProvider.getLetterTile(displayName)
}