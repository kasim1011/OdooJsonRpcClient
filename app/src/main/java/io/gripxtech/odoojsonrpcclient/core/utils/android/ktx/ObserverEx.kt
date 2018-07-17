package io.gripxtech.odoojsonrpcclient.core.utils.android.ktx

import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import timber.log.Timber

class ObserverEx<T> : Observer<T> {

    private var subscribe: ((disposable: Disposable) -> Unit) = {
        Timber.d("onSubscribe() called")
    }

    fun onSubscribe(subscribe: (disposable: Disposable) -> Unit) {
        this.subscribe = subscribe
    }

    override fun onSubscribe(disposable: Disposable) {
        this.subscribe.invoke(disposable)
    }

    private var next: ((t: T) -> Unit) = {
        Timber.d("onNext() called: response is $it")
    }

    fun onNext(next: (t: T) -> Unit) {
        this.next = next
    }

    override fun onNext(t: T) {
        this.next.invoke(t)
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