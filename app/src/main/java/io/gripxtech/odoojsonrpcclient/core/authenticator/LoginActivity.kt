package io.gripxtech.odoojsonrpcclient.core.authenticator

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.View
import android.widget.ArrayAdapter
import io.gripxtech.odoojsonrpcclient.*
import io.gripxtech.odoojsonrpcclient.core.Odoo
import io.gripxtech.odoojsonrpcclient.core.entities.session.authenticate.AuthenticateResult
import io.gripxtech.odoojsonrpcclient.core.entities.webclient.versionInfo.VersionInfo
import io.gripxtech.odoojsonrpcclient.core.utils.Retrofit2Helper
import io.gripxtech.odoojsonrpcclient.core.utils.android.ktx.addTextChangedListenerEx
import io.gripxtech.odoojsonrpcclient.core.utils.android.ktx.postEx
import io.gripxtech.odoojsonrpcclient.core.utils.android.ktx.setOnItemSelectedListenerEx
import io.gripxtech.odoojsonrpcclient.core.utils.android.ktx.subscribeEx
import io.gripxtech.odoojsonrpcclient.databinding.ActivityLoginBinding
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class LoginActivity : AppCompatActivity() {

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        // Add account from Android Settings App -> Accounts
        const val FROM_ANDROID_ACCOUNTS: String = "from_android_accounts"

        // Add account from navigation drawer
        const val FROM_APP_SETTINGS: String = "from_app_settings"
    }

    private lateinit var app: App
    private lateinit var binding: ActivityLoginBinding
    private lateinit var compositeDisposable: CompositeDisposable
    private var selfHostedUrl: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as App
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        compositeDisposable = CompositeDisposable()
        selfHostedUrl = resources.getBoolean(R.bool.self_hosted_url)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        if (selfHostedUrl) {
            binding.grpCheckVersion.visibility = View.VISIBLE
            binding.spProtocol.setOnItemSelectedListenerEx {
                onItemSelected { _, _, _, _ ->
                    resetLoginLayout(resetCheckVersion = true)
                }
            }

            binding.tlHost.isErrorEnabled = false

            binding.etHost.addTextChangedListenerEx {
                afterTextChanged { _ ->
                    binding.tlHost.isErrorEnabled = false
                    resetLoginLayout(resetCheckVersion = true)
                }
            }

            binding.bnCheckVersion.setOnClickListener {
                if (binding.etHost.text.toString().isBlank()) {
                    binding.tlHost.error = getString(R.string.login_host_error)
                    return@setOnClickListener
                }

                hideSoftKeyboard()
                binding.spProtocol.isEnabled = false
                binding.tlHost.isEnabled = false
                binding.bnCheckVersion.isEnabled = false
                resetLoginLayout(resetCheckVersion = false)
                prepareUiForCheckVersion()
                checkVersion()
            }
        } else {
            prepareUiForCheckVersion()
            checkVersion()
        }

        binding.bn.setOnClickListener {
            val login = binding.etLogin.text.toString()
            if (login.isBlank()) {
                binding.tlLogin.error = getString(R.string.login_username_error)
                return@setOnClickListener
            }

            val password = binding.etPassword.text.toString()
            if (password.isBlank()) {
                binding.tlPassword.error = getString(R.string.login_password_error)
                return@setOnClickListener
            }

            val database = binding.spDatabase.selectedItem
            if (database != null && database.toString().isBlank()) {
                showMessage(message = getString(R.string.login_database_error))
                return@setOnClickListener
            }

            hideSoftKeyboard()
            prepareUiForAuthenticate()
            authenticate(login = login, password = password, database = database.toString())
        }

        val users = getOdooUsers()
        if (users.isNotEmpty()) {
            binding.bnOtherAccount.visibility = View.VISIBLE
            binding.bnOtherAccount.setOnClickListener {
                startActivity(Intent(this@LoginActivity, ManageAccountActivity::class.java))
            }
        }
    }

    private fun prepareUiForCheckVersion() {
        binding.llCheckingVersion.visibility = View.VISIBLE
        binding.llCheckVersionResult.visibility = View.GONE
        binding.ivCheckVersionResultSuccess.visibility = View.GONE
        binding.ivCheckVersionResultFail.visibility = View.GONE
        Odoo.protocol = when (resources.getInteger(R.integer.protocol)) {
            0 -> {
                Retrofit2Helper.Companion.Protocol.HTTP
            }
            else -> {
                Retrofit2Helper.Companion.Protocol.HTTPS
            }
        }
        Odoo.host = getString(R.string.host_url)
    }

    private fun checkVersion() {
        Odoo.versionInfo {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val versionInfo = response.body()!!
                    if (versionInfo.isSuccessful) {
                        if (versionInfo.result.serverVersionIsSupported) {
                            getDbList(versionInfo)
                        } else {
                            toggleCheckVersionWidgets(isSuccess = false, resultMessage = getString(
                                    R.string.login_server_error,
                                    versionInfo.result.serverVersion
                            ))
                        }
                    } else {
                        toggleCheckVersionWidgets(isSuccess = false, resultMessage = versionInfo.errorMessage)
                    }
                } else {
                    toggleCheckVersionWidgets(isSuccess = false, resultMessage = "${response.code()}: ${response.message()}")
                }
            }

            onError { error ->
                toggleCheckVersionWidgets(isSuccess = false, resultMessage = error.message
                        ?: getString(R.string.generic_error))
            }
        }
    }

    private fun getDbList(versionInfo: VersionInfo) {
        Odoo.listDb(versionInfo.result.serverVersion) {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val listDb = response.body()!!
                    if (listDb.isSuccessful) {
                        toggleCheckVersionWidgets(isSuccess = true, resultMessage = getString(
                                R.string.login_server_success,
                                versionInfo.result.serverVersion
                        ))

                        binding.spDatabase.adapter = ArrayAdapter<String>(
                                this@LoginActivity,
                                R.layout.support_simple_spinner_dropdown_item,
                                listDb.result
                        )
                        binding.groupLogin.visibility = View.VISIBLE
                        changeDbSpinnerVisibility(if (listDb.result.size == 1) View.GONE else View.VISIBLE)
                    } else {
                        toggleCheckVersionWidgets(isSuccess = false, resultMessage = listDb.errorMessage)
                    }
                } else {
                    toggleCheckVersionWidgets(isSuccess = false, resultMessage = "${response.code()}: ${response.message()}")
                }
            }

            onError { error ->
                toggleCheckVersionWidgets(isSuccess = false, resultMessage = error.message
                        ?: getString(R.string.generic_error))
            }
        }
    }

    private fun toggleCheckVersionWidgets(isSuccess: Boolean, resultMessage: String) {
        binding.spProtocol.isEnabled = true
        binding.tlHost.isEnabled = true
        binding.bnCheckVersion.isEnabled = true
        binding.llCheckingVersion.visibility = View.GONE
        binding.llCheckVersionResult.visibility = View.VISIBLE

        if (isSuccess) {
            binding.ivCheckVersionResultSuccess.visibility = View.VISIBLE
            binding.ivCheckVersionResultFail.visibility = View.GONE
        } else {
            binding.ivCheckVersionResultSuccess.visibility = View.GONE
            binding.ivCheckVersionResultFail.visibility = View.VISIBLE
        }
        binding.tvCheckVersionResultMessage.text = resultMessage
    }

    private fun resetLoginLayout(resetCheckVersion: Boolean = false) {
        if (resetCheckVersion) {
            binding.llCheckingVersion.visibility = View.GONE
            binding.llCheckVersionResult.visibility = View.GONE
            binding.ivCheckVersionResultSuccess.visibility = View.GONE
            binding.ivCheckVersionResultFail.visibility = View.GONE
        }
        // changeDbSpinnerVisibility(View.GONE)
        binding.spDatabase.adapter.run {
            when (this) {
                is ArrayAdapter<*> -> {
                    this.clear()
                }
            }
        }
        binding.groupLogin.visibility = View.GONE
        binding.llLoginProgress.visibility = View.GONE
        binding.llLoginError.visibility = View.GONE
    }

    /**
     * Set the visibility state of this view.
     *
     * @param visibility One of {@link #VISIBLE}, {@link #INVISIBLE}, or {@link #GONE}.
     * @attr ref android.R.styleable#View_visibility
     */
    private fun changeDbSpinnerVisibility(visibility: Int) {
        binding.lblDatabase.visibility = visibility
        binding.spcDatabaseTop.visibility = visibility
        binding.spDatabase.visibility = visibility
        binding.spcDatabaseBottom.visibility = visibility
    }

    private fun prepareUiForAuthenticate() {
        if (selfHostedUrl) {
            binding.spProtocol.isEnabled = false
            binding.tlHost.isEnabled = false
            binding.bnCheckVersion.isEnabled = false
        }
        binding.etLogin.isEnabled = false
        binding.etPassword.isEnabled = false
        binding.spDatabase.isEnabled = false
        binding.bn.isEnabled = false

        binding.llLoginProgress.visibility = View.VISIBLE
        binding.llLoginError.visibility = View.GONE
    }

    private fun authenticate(login: String, password: String, database: String) {
        Odoo.authenticate(login = login, password = password, database = database) {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val authenticate = response.body()!!
                    if (authenticate.isSuccessful) {
                        val authenticateResult = authenticate.result
                        authenticateResult.password = password
                        searchReadUserInfo(authenticateResult = authenticateResult)
                    } else {
                        toggleLoginWidgets(showErrorBody = true, errorMessage = authenticate.errorMessage)
                    }
                } else {
                    toggleLoginWidgets(showErrorBody = false)
                    showServerErrorMessage(response)
                }
            }

            onError { error ->
                toggleLoginWidgets(showErrorBody = true, errorMessage = error.message
                        ?: getString(R.string.generic_error))
            }
        }
    }

    private fun searchReadUserInfo(authenticateResult: AuthenticateResult) {
        Odoo.searchRead(
                model = "res.users",
                fields = listOf("name", "image"),
                domain = listOf(listOf("id", "=", authenticateResult.uid)),
                offset = 0, limit = 0, sort = "id DESC", context = authenticateResult.userContext
        ) {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val searchRead = response.body()!!
                    if (searchRead.isSuccessful) {
                        val row = searchRead.result.records[0].asJsonObject
                        authenticateResult.imageSmall = row.get("image").asString
                        authenticateResult.name = row.get("name").asString.trimFalse()
                        binding.tvLoginProgress.text = getString(R.string.login_success)
                        createAccount(authenticateResult = authenticateResult)
                    } else {
                        toggleLoginWidgets(showErrorBody = true, errorMessage = searchRead.errorMessage)
                    }
                } else {
                    toggleLoginWidgets(showErrorBody = false)
                    showServerErrorMessage(response)
                }
            }

            onError { error ->
                toggleLoginWidgets(showErrorBody = true, errorMessage = error.message
                        ?: getString(R.string.generic_error))
            }
        }
    }

    private fun toggleLoginWidgets(showErrorBody: Boolean = false, errorMessage: String = "") {
        if (selfHostedUrl) {
            binding.spProtocol.isEnabled = true
            binding.tlHost.isEnabled = true
            binding.bnCheckVersion.isEnabled = true
        }
        binding.etLogin.isEnabled = true
        binding.etPassword.isEnabled = true
        binding.spDatabase.isEnabled = true
        binding.bn.isEnabled = true

        binding.llLoginProgress.visibility = View.GONE

        if (showErrorBody) {
            binding.llLoginError.visibility = View.VISIBLE
            binding.tvLoginError.text = errorMessage
        }
    }

    private fun createAccount(authenticateResult: AuthenticateResult) {
        Observable.fromCallable {
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
                .subscribeEx {
                    onSubscribe { _ ->
                        // Must be complete, not dispose in between
                        // compositeDisposable.add(d)
                    }

                    onNext { t ->
                        resultCallback(t)
                    }

                    onError { error ->
                        error.printStackTrace()
                        if (!isFinishing && !isDestroyed) {
                            binding.llLoginProgress.postEx {
                                visibility = View.GONE
                            }
                            binding.llLoginError.postEx {
                                visibility = View.VISIBLE
                            }
                            binding.tvLoginError.postEx {
                                text = getString(R.string.login_create_account_error)
                            }
                        }
                    }
                }
    }

    private fun resultCallback(result: Boolean) {
        if (result) {
            intent?.let {
                when {
                    it.hasExtra(FROM_ANDROID_ACCOUNTS) -> {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                    it.hasExtra(FROM_APP_SETTINGS) -> {
                        restartApp()
                    }
                    else -> {
                        restartApp()
                    }
                }
                Unit
            }
        } else {
            toggleLoginWidgets(showErrorBody = true, errorMessage = getString(R.string.login_create_account_error))
        }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}
