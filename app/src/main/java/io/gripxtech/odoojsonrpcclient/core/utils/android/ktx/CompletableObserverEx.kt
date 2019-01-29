package io.gripxtech.odoojsonrpcclient.core.utils.android.ktx

import io.reactivex.CompletableObserver
import io.reactivex.disposables.Disposable
import timber.log.Timber

class CompletableObserverEx : CompletableObserver {

    private var subscribe: ((disposable: Disposable) -> Unit) = {
        Timber.d("onSubscribe() called")
    }

    fun onSubscribe(subscribe: (disposable: Disposable) -> Unit) {
        this.subscribe = subscribe
    }

    override fun onSubscribe(disposable: Disposable) {
        this.subscribe.invoke(disposable)
    }

    private var error: ((error: Throwable) -> Unit) = {
        Timber.e("onError() called: ${it::class.java.simpleName}: ${it.message}")
        it.printStackTrace()
    }

    fun onError(error: (error: Throwable) -> Unit) {
        this.error = error
    }

    override fun onError(error: Throwable) {
        this.error.invoke(error)
    }

    private var complete: (() -> Unit) = {
        Timber.d("onComplete() called")
    }

    fun onComplete(complete: () -> Unit) {
        this.complete = complete
    }

    override fun onComplete() {
        this.complete.invoke()
    }
}