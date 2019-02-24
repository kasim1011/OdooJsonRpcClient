package io.gripxtech.odoojsonrpcclient.core.utils.android.ktx

import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import timber.log.Timber

class SingleObserverEx<T> : SingleObserver<T> {

    private var subscribe: ((disposable: Disposable) -> Unit) = {
        Timber.d("onSubscribe() called")
    }

    fun onSubscribe(subscribe: (disposable: Disposable) -> Unit) {
        this.subscribe = subscribe
    }

    override fun onSubscribe(disposable: Disposable) {
        this.subscribe.invoke(disposable)
    }

    private var success: ((response: T) -> Unit) = {
        Timber.d("onSuccess() called: response is $it")
    }

    fun onSuccess(success: (response: T) -> Unit) {
        this.success = success
    }

    override fun onSuccess(response: T) {
        this.success.invoke(response)
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
}