package io.gripxtech.odoojsonrpcclient

import android.content.Context
import android.content.res.Configuration
import androidx.multidex.MultiDexApplication
import io.gripxtech.odoojsonrpcclient.core.Odoo
import io.gripxtech.odoojsonrpcclient.core.OdooDatabase
import io.gripxtech.odoojsonrpcclient.core.utils.CookiePrefs
import io.gripxtech.odoojsonrpcclient.core.utils.LetterTileProvider
import io.gripxtech.odoojsonrpcclient.core.utils.LocaleHelper
import io.gripxtech.odoojsonrpcclient.core.utils.Retrofit2Helper
import timber.log.Timber

class App : MultiDexApplication() {

    companion object {
        const val KEY_ACCOUNT_TYPE = "${BuildConfig.APPLICATION_ID}.auth"
    }

    private val letterTileProvider: LetterTileProvider by lazy {
        LetterTileProvider(this)
    }

    val cookiePrefs: CookiePrefs by lazy {
        CookiePrefs(this)
    }

    override fun attachBaseContext(base: Context?) {
        if (base != null) {
            super.attachBaseContext(LocaleHelper.setLocale(base))
        } else {
            super.attachBaseContext(base)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.setLocale(this)
    }

    override fun onCreate() {
        super.onCreate()
        Retrofit2Helper.app = this
        Odoo.app = this
        OdooDatabase.app = this

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    fun getLetterTile(displayName: String): ByteArray =
        letterTileProvider.getLetterTile(displayName)
}