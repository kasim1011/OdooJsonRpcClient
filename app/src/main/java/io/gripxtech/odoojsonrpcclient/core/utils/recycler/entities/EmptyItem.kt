package io.gripxtech.odoojsonrpcclient.core.utils.recycler.entities

import android.support.annotation.DrawableRes

data class EmptyItem(
        val message: CharSequence,
        @DrawableRes
        val drawableResId: Int
)
