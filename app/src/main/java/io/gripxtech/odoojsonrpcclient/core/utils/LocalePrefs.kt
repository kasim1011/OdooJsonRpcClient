package io.gripxtech.odoojsonrpcclient.core.utils

import android.content.Context
import androidx.core.os.ConfigurationCompat

class LocalePrefs(context: Context) : Prefs(LocalePrefs.TAG, context) {

    companion object {
        const val TAG = "LocalePrefs"

        private const val Language = "Language"
    }

    var language: String
        get() = getString(Language, ConfigurationCompat.getLocales(context.resources.configuration)[0].language)
        set(value) = putString(Language, value)
}
