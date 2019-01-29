package io.gripxtech.odoojsonrpcclient.core.utils

import android.content.Context
import android.content.res.Configuration
import java.util.*

object LocaleHelper {

    fun setLocale(context: Context): Context = setNewLocale(context, getLanguage(context))

    private fun setNewLocale(context: Context, language: String): Context {
        persistLanguage(context, language)
        return updateResources(context, language)
    }

    private fun getLanguage(context: Context): String = LocalePrefs(context).language

    private fun persistLanguage(context: Context, language: String) {
        LocalePrefs(context).language = language
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val res = context.resources
        val config = Configuration(res.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}