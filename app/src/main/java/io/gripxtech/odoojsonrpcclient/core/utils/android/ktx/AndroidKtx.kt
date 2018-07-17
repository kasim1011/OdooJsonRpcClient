package io.gripxtech.odoojsonrpcclient.core.utils.android.ktx

import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import io.reactivex.Observable

inline fun AdapterView<*>.setOnItemSelectedListenerEx(listener: OnItemSelectedListenerEx.() -> Unit) {
    val listenerEx = OnItemSelectedListenerEx()
    listenerEx.listener()
    onItemSelectedListener = listenerEx
}

inline fun TextView.addTextChangedListenerEx(listener: TextWatcherEx.() -> Unit) {
    val listenerEx = TextWatcherEx()
    listenerEx.listener()
    addTextChangedListener(listenerEx)
}

inline fun <T : View> T.postEx(crossinline callback: T.() -> Unit) = post { callback() }

inline fun <T : View> T.setOnClickListenerEx(crossinline callback: T.() -> Unit) = setOnClickListener { callback() }

inline fun <T> Observable<T>.subscribeEx(crossinline observer: ObserverEx<T>.() -> Unit) {
    val observerEx = ObserverEx<T>()
    observerEx.observer()
    subscribe(observerEx)
}
