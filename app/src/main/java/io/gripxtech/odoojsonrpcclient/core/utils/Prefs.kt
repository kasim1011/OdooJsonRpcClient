package io.gripxtech.odoojsonrpcclient.core.utils

import android.content.Context
import android.content.SharedPreferences

abstract class Prefs(val name: String, val context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    protected fun getBoolean(key: String, defValue: Boolean = false): Boolean = preferences.getBoolean(key, defValue)

    protected fun putBoolean(key: String, value: Boolean) = preferences.edit().putBoolean(key, value).apply()

    protected fun getInt(key: String, defValue: Int = 0): Int = preferences.getInt(key, defValue)

    protected fun putInt(key: String, value: Int) = preferences.edit().putInt(key, value).apply()

    protected fun getFloat(key: String, defValue: Float = 0f): Float = preferences.getFloat(key, defValue)

    protected fun putFloat(key: String, value: Float) = preferences.edit().putFloat(key, value).apply()

    protected fun getLong(key: String, defValue: Long = 0L): Long = preferences.getLong(key, defValue)

    protected fun putLong(key: String, value: Long) = preferences.edit().putLong(key, value).apply()

    protected fun getString(key: String, defValue: String = ""): String =
        preferences.getString(key, defValue) ?: defValue

    protected fun putString(key: String, value: String) = preferences.edit().putString(key, value).apply()

    protected fun getStringSet(key: String, defValue: MutableSet<String> = mutableSetOf()): MutableSet<String> =
        preferences.getStringSet(key, defValue) ?: defValue

    protected fun putStringSet(key: String, value: MutableSet<String>) = preferences.edit().putStringSet(key, value).apply()

    fun clear() = preferences.edit().clear().apply()
}
