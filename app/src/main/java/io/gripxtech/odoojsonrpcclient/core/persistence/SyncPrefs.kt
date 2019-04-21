package io.gripxtech.odoojsonrpcclient.core.persistence

import android.content.Context
import io.gripxtech.odoojsonrpcclient.BuildConfig
import io.gripxtech.odoojsonrpcclient.core.utils.Prefs

class SyncPrefs(
    context: Context
) : Prefs(TAG, context) {

    companion object {
        private const val TAG = "${BuildConfig.APPLICATION_ID}.sync_prefs"

        private const val LastSyncDate = "LastSyncDate"
    }

    var lastSyncDate: Long
        get() = getLong(LastSyncDate, 0)
        set(value) = putLong(LastSyncDate, value)
}