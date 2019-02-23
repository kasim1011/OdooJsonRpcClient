package io.gripxtech.odoojsonrpcclient.core.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            super.attachBaseContext(LocaleHelper.setLocale(newBase))
        } else {
            super.attachBaseContext(newBase)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.setLocale(this)
    }
}