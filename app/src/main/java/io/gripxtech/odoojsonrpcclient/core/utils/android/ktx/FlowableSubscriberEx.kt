package io.gripxtech.odoojsonrpcclient.core.utils.android.ktx

import io.reactivex.FlowableSubscriber
import org.reactivestreams.Subscription
import timber.log.Timber

class FlowableSubscriberEx<T> : FlowableSubscriber<T> {

    private var subscribe: ((subscription: Subscription) -> Unit) = {
        Timber.d("onSubscribe() called")
    }

    fun onSubscribe(subscribe: (subscription: Subscription) -> Unit) {
        this.subscribe = subscribe
    }

    override fun onSubscribe(subscription: Subscription) {
        this.subscribe.invoke(subscription)
    }

    private var next: ((response: T) -> Unit) = {
        Timber.d("onNext() called: response is $it")
    }

    fun onNext(next: (response: T) -> Unit) {
        this.next = next
    }

    override fun onNext(response: T) {
        this.next.invoke(response)
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