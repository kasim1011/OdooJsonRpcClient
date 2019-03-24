package io.gripxtech.odoojsonrpcclient.core.utils.android.ktx

import io.gripxtech.odoojsonrpcclient.core.Odoo
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import retrofit2.Response
import timber.log.Timber

class ResponseObserver<T> : Observer<Response<T>> {

    companion object {
        const val TAG = "ResponseObserver"
    }

    private var subscribe: ((disposable: Disposable) -> Unit) = {
        Timber.tag(TAG).d("onSubscribe() called")
    }

    fun onSubscribe(subscribe: (disposable: Disposable) -> Unit) {
        this.subscribe = subscribe
    }

    override fun onSubscribe(disposable: Disposable) {
        this.subscribe.invoke(disposable)
    }

    private var next: ((response: Response<T>) -> Unit) = {
        Timber.tag(TAG).d("onNext() called: response is $it")
    }

    fun onNext(next: (response: Response<T>) -> Unit) {
        this.next = next
    }

    override fun onNext(response: Response<T>) {
        if (!response.isSuccessful) {
            Odoo.resetRetrofitClient()
        }
        this.next.invoke(response)
    }

    private var error: ((error: Throwable) -> Unit) = {
        Timber.tag(TAG).e("onError() called: ${it::class.java.simpleName}: ${it.message}")
        it.printStackTrace()
    }

    fun onError(error: (error: Throwable) -> Unit) {
        this.error = error
    }

    override fun onError(error: Throwable) {
        Odoo.resetRetrofitClient()
        this.error.invoke(error)
    }

    private var complete: (() -> Unit) = {
        Timber.tag(TAG).d("onComplete() called")
    }

    fun onComplete(complete: () -> Unit) {
        this.complete = complete
    }

    override fun onComplete() {
        this.complete.invoke()
    }
}