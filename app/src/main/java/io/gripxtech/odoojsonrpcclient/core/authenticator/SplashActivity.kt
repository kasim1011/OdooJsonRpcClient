package io.gripxtech.odoojsonrpcclient.core.authenticator

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.text.Html
import io.gripxtech.odoojsonrpcclient.*
import io.gripxtech.odoojsonrpcclient.core.Odoo
import io.gripxtech.odoojsonrpcclient.core.OdooUser
import io.gripxtech.odoojsonrpcclient.core.entities.session.authenticate.AuthenticateResult
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class SplashActivity : AppCompatActivity() {

    private lateinit var app: App
    private lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as App
        compositeDisposable = CompositeDisposable()
    }

    override fun onPostResume() {
        super.onPostResume()
        checkUser()
    }

    private fun checkUser() {
        val user = getActiveOdooUser()
        if (user != null) {
            Odoo.user = user
            Odoo.check {
                onSubscribe { disposable ->
                    compositeDisposable.add(disposable)
                }

                onNext { response ->
                    if (response.isSuccessful && response.body()!!.isSuccessful) {
                        startMainActivity()
                    } else {
                        val errorBody = response.errorBody()?.string()
                                ?: getString(R.string.generic_error)
                        @Suppress("DEPRECATION")
                        val message: CharSequence = if (errorBody.contains("jsonrpc", ignoreCase = true)) {
                            response.body()?.errorMessage ?: getString(R.string.generic_error)
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                Html.fromHtml(errorBody, Html.FROM_HTML_MODE_COMPACT)
                            else
                                Html.fromHtml(errorBody)
                        }

                        showMessage(
                                title = getString(R.string.server_request_error, response.code(), response.message()),
                                message = message,
                                positiveButton = getString(R.string.try_again),
                                positiveButtonListener = DialogInterface.OnClickListener { _, _ ->
                                    authenticate(user)
                                },
                                showNegativeButton = true,
                                negativeButton = getString(R.string.quit),
                                negativeButtonListener = DialogInterface.OnClickListener { _, _ ->
                                    ActivityCompat.finishAffinity(this@SplashActivity)
                                }
                        )
                    }
                }

                onError { error ->
                    showMessage(title = getString(R.string.operation_failed),
                            message = error.message,
                            positiveButtonListener = DialogInterface.OnClickListener { _, _ ->
                                ActivityCompat.finishAffinity(this@SplashActivity)
                            })
                }
            }
        } else {
            startLoginActivity()
        }
    }

    private fun authenticate(user: OdooUser) {
        Odoo.authenticate(login = user.login, password = user.password, database = user.database) {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val authenticate = response.body()!!
                    if (authenticate.isSuccessful) {
                        createAccount(authenticateResult = authenticate.result, user = user)
                    } else {
                        // logoutOdooUser(user)
                        deleteOdooUser(user)
                        restartApp()
                    }
                } else {
                    showServerErrorMessage(response, positiveButtonListener = DialogInterface.OnClickListener { _, _ ->
                        ActivityCompat.finishAffinity(this@SplashActivity)
                    })
                }
            }

            onError { error ->
                showMessage(title = getString(R.string.operation_failed),
                        message = error.message,
                        positiveButtonListener = DialogInterface.OnClickListener { _, _ ->
                            ActivityCompat.finishAffinity(this@SplashActivity)
                        })
            }
        }
    }

    private fun createAccount(authenticateResult: AuthenticateResult, user: OdooUser) {
        Observable.fromCallable {
            deleteOdooUser(user)
            if (createOdooUser(authenticateResult)) {
                val odooUser = odooUserByAndroidName(authenticateResult.androidName)
                if (odooUser != null) {
                    loginOdooUser(odooUser)
                    Odoo.user = odooUser
                    app.cookiePrefs.setCookies(Odoo.pendingAuthenticateCookies)
                }
                Odoo.pendingAuthenticateCookies.clear()
                true
            } else {
                false
            }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Boolean> {
                    override fun onSubscribe(d: Disposable) {
                        // Must be complete, not dispose in between
                        // compositeDisposable.add(d)
                    }

                    override fun onNext(t: Boolean) {
                        if (t) {
                            restartApp()
                        } else {
                            closeApp()
                        }
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        closeApp(message = e.message ?: getString(R.string.generic_error))
                    }

                    override fun onComplete() = Unit
                })
    }

    private fun startLoginActivity() {
        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
        finish()
    }

    private fun startMainActivity() {
        // startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        startActivity(Intent(this@SplashActivity, SomeActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}
