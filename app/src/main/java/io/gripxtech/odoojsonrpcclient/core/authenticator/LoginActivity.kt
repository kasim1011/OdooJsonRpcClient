package io.gripxtech.odoojsonrpcclient.core.authenticator

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import io.gripxtech.odoojsonrpcclient.*
import io.gripxtech.odoojsonrpcclient.core.Odoo
import io.gripxtech.odoojsonrpcclient.core.entities.session.authenticate.AuthenticateResult
import io.gripxtech.odoojsonrpcclient.core.entities.webclient.versionInfo.VersionInfo
import io.gripxtech.odoojsonrpcclient.core.utils.LocaleHelper
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
    private var compositeDisposable: CompositeDisposable? = null
    private var selfHostedUrl: Boolean = false
    private var preConfigDatabase: Boolean = false
    private var preConfigDatabaseName: String = ""

    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            super.attachBaseContext(LocaleHelper.setLocale(newBase))
        } else {
            super.attachBaseContext(newBase)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.setLocale(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as App
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        compositeDisposable?.dispose()
        compositeDisposable = CompositeDisposable()
        selfHostedUrl = resources.getBoolean(R.bool.self_hosted_url)
        preConfigDatabase = resources.getBoolean(R.bool.pre_config_database)
        preConfigDatabaseName = getString(R.string.pre_config_database_name)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        if (selfHostedUrl) {
            binding.grpCheckVersion.postEx {
                visibility = View.VISIBLE
            }
            binding.spProtocol.setOnItemSelectedListenerEx {
                onItemSelected { _, _, _, _ ->
                    resetLoginLayout(resetCheckVersion = true)
                }
            }

            binding.tlHost.isErrorEnabled = false

            binding.etHost.addTextChangedListenerEx {
                afterTextChanged {
                    binding.tlHost.isErrorEnabled = false
                    resetLoginLayout(resetCheckVersion = true)
                }
            }

            binding.bnCheckVersion.setOnClickListener {
                val host = binding.etHost.text.toString().trim()
                if (host.isBlank()) {
                    binding.tlHost.error = getString(R.string.login_host_error)
                    return@setOnClickListener
                }

                val urls = host.extractWebUrls()
                if (!(urls.size == 1 && urls[0] == host)) {
                    binding.tlHost.error = getString(R.string.login_host_error2)
                    return@setOnClickListener
                }

                if (host.startsWith("http")) {
                    binding.tlHost.error = getString(R.string.login_host_error1)
                    return@setOnClickListener
                }

                hideSoftKeyboard()
                binding.spProtocol.isEnabled = false
                binding.tlHost.isEnabled = false
                binding.bnCheckVersion.isEnabled = false
                resetLoginLayout(resetCheckVersion = false)
                prepareUiForCheckVersion()
                Odoo.protocol = when (binding.spProtocol.selectedItemPosition) {
                    0 -> {
                        Retrofit2Helper.Companion.Protocol.HTTP
                    }
                    else -> {
                        Retrofit2Helper.Companion.Protocol.HTTPS
                    }
                }
                Odoo.host = binding.etHost.text.toString()
                checkVersion()
            }
        } else {
            prepareUiForCheckVersion()
            Odoo.protocol = when (resources.getInteger(R.integer.protocol)) {
                0 -> {
                    Retrofit2Helper.Companion.Protocol.HTTP
                }
                else -> {
                    Retrofit2Helper.Companion.Protocol.HTTPS
                }
            }
            Odoo.host = getString(R.string.host_url)
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
            if (database == null || database.toString().isBlank()) {
                showMessage(message = getString(R.string.login_database_error))
                return@setOnClickListener
            }

            hideSoftKeyboard()
            prepareUiForAuthenticate()
            authenticate(login = login, password = password, database = database.toString())
        }

        val users = getOdooUsers()
        if (users.isNotEmpty()) {
            binding.bnOtherAccount.postEx {
                visibility = View.VISIBLE
            }
            binding.bnOtherAccount.setOnClickListener {
                startActivity(Intent(this@LoginActivity, ManageAccountActivity::class.java))
            }
        }
    }

    private fun prepareUiForCheckVersion() {
        binding.llCheckingVersion.postEx {
            visibility = View.VISIBLE
        }
        binding.llCheckVersionResult.postEx {
            visibility = View.GONE
        }
        binding.ivCheckVersionResultSuccess.postEx {
            visibility = View.GONE
        }
        binding.ivCheckVersionResultFail.postEx {
            visibility = View.GONE
        }
    }

    private fun checkVersion() {
        Odoo.versionInfo {
            onSubscribe { disposable ->
                compositeDisposable?.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val versionInfo = response.body()!!
                    if (versionInfo.isSuccessful) {
                        if (versionInfo.result.serverVersionIsSupported) {
                            getDbList(versionInfo)
                        } else {
                            toggleCheckVersionWidgets(
                                isSuccess = false, resultMessage = getString(
                                    R.string.login_server_error,
                                    versionInfo.result.serverVersion
                                )
                            )
                        }
                    } else {
                        toggleCheckVersionWidgets(isSuccess = false, resultMessage = versionInfo.errorMessage)
                    }
                } else {
                    toggleCheckVersionWidgets(
                        isSuccess = false,
                        resultMessage = "${response.code()}: ${response.message()}"
                    )
                }
            }

            onError { error ->
                toggleCheckVersionWidgets(
                    isSuccess = false, resultMessage = error.message
                        ?: getString(R.string.generic_error)
                )
            }
        }
    }

    private fun getDbList(versionInfo: VersionInfo) {
        Odoo.listDb(versionInfo.result.serverVersion) {
            onSubscribe { disposable ->
                compositeDisposable?.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val listDb = response.body()!!
                    if (listDb.isSuccessful) {
                        toggleCheckVersionWidgets(
                            isSuccess = true, resultMessage = getString(
                                R.string.login_server_success,
                                versionInfo.result.serverVersion
                            )
                        )
                        if (preConfigDatabase && listDb.result.contains(preConfigDatabaseName)) {
                            binding.spDatabase.adapter = ArrayAdapter<String>(
                                this@LoginActivity,
                                R.layout.support_simple_spinner_dropdown_item,
                                listOf(preConfigDatabaseName)
                            )
                            changeGroupLoginVisibility(View.VISIBLE)
                            changeDbSpinnerVisibility(View.GONE)
                        } else {
                            binding.spDatabase.adapter = ArrayAdapter<String>(
                                this@LoginActivity,
                                R.layout.support_simple_spinner_dropdown_item,
                                listDb.result
                            )
                            changeGroupLoginVisibility(View.VISIBLE)
                            changeDbSpinnerVisibility(if (listDb.result.size == 1) View.GONE else View.VISIBLE)
                        }
                    } else {
                        toggleCheckVersionWidgets(isSuccess = false, resultMessage = listDb.errorMessage)
                    }
                } else {
                    toggleCheckVersionWidgets(
                        isSuccess = false,
                        resultMessage = "${response.code()}: ${response.message()}"
                    )
                }
            }

            onError { error ->
                toggleCheckVersionWidgets(
                    isSuccess = false, resultMessage = error.message
                        ?: getString(R.string.generic_error)
                )
            }
        }
    }

    private fun toggleCheckVersionWidgets(isSuccess: Boolean, resultMessage: String) {
        binding.spProtocol.isEnabled = true
        binding.tlHost.isEnabled = true
        binding.bnCheckVersion.isEnabled = true
        binding.llCheckingVersion.postEx {
            visibility = View.GONE
        }
        binding.llCheckVersionResult.postEx {
            visibility = View.VISIBLE
        }

        if (isSuccess) {
            binding.ivCheckVersionResultSuccess.postEx {
                visibility = View.VISIBLE
            }
            binding.ivCheckVersionResultFail.postEx {
                visibility = View.GONE
            }
        } else {
            binding.ivCheckVersionResultSuccess.postEx {
                visibility = View.GONE
            }
            binding.ivCheckVersionResultFail.postEx {
                visibility = View.VISIBLE
            }
        }
        binding.tvCheckVersionResultMessage.text = resultMessage
    }

    private fun resetLoginLayout(resetCheckVersion: Boolean = false) {
        if (resetCheckVersion) {
            binding.llCheckingVersion.postEx {
                visibility = View.GONE
            }
            binding.llCheckVersionResult.postEx {
                visibility = View.GONE
            }
            binding.ivCheckVersionResultSuccess.postEx {
                visibility = View.GONE
            }
            binding.ivCheckVersionResultFail.postEx {
                visibility = View.GONE
            }
        }
        // changeDbSpinnerVisibility(View.GONE)
        binding.spDatabase.adapter.run {
            when (this) {
                is ArrayAdapter<*> -> {
                    this.clear()
                }
            }
        }
        binding.spcLoginTop.postEx {
            visibility = View.GONE
        }
        binding.tlLogin.postEx {
            visibility = View.GONE
        }
        binding.tlPassword.postEx {
            visibility = View.GONE
        }
        binding.lblDatabase.postEx {
            visibility = View.GONE
        }
        binding.spcDatabaseTop.postEx {
            visibility = View.GONE
        }
        binding.spDatabase.postEx {
            visibility = View.GONE
        }
        binding.spcDatabaseBottom.postEx {
            visibility = View.GONE
        }
        binding.bn.postEx {
            visibility = View.GONE
        }
        binding.llLoginProgress.postEx {
            visibility = View.GONE
        }
        binding.llLoginError.postEx {
            visibility = View.GONE
        }
    }

    /**
     * Set the visibility state of this view.
     *
     * @param flag One of {@link #VISIBLE}, {@link #INVISIBLE}, or {@link #GONE}.
     * @attr ref android.R.styleable#View_visibility
     */
    private fun changeGroupLoginVisibility(flag: Int) {
        binding.spcLoginTop.postEx {
            visibility = flag
        }
        binding.tlLogin.postEx {
            visibility = flag
        }
        binding.tlPassword.postEx {
            visibility = flag
        }
        binding.bn.postEx {
            visibility = flag
        }
    }

    /**
     * Set the visibility state of this view.
     *
     * @param flag One of {@link #VISIBLE}, {@link #INVISIBLE}, or {@link #GONE}.
     * @attr ref android.R.styleable#View_visibility
     */
    private fun changeDbSpinnerVisibility(flag: Int) {
        binding.lblDatabase.postEx {
            visibility = flag
        }
        binding.spcDatabaseTop.postEx {
            visibility = flag
        }
        binding.spDatabase.postEx {
            visibility = flag
        }
        binding.spcDatabaseBottom.postEx {
            visibility = flag
        }
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

        binding.llLoginProgress.postEx {
            visibility = View.VISIBLE
        }
        binding.llLoginError.postEx {
            visibility = View.GONE
        }
    }

    private fun authenticate(login: String, password: String, database: String) {
        Odoo.authenticate(login = login, password = password, database = database) {
            onSubscribe { disposable ->
                compositeDisposable?.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val authenticate = response.body()!!
                    if (authenticate.isSuccessful) {
                        val authenticateResult = authenticate.result
                        authenticateResult.password = password
                        searchReadUserInfo(authenticateResult = authenticateResult)
                    } else {
                        val errorMessage = authenticate.errorMessage
                        toggleLoginWidgets(
                            showErrorBody = true,
                            errorMessage = if (errorMessage.contains(
                                    "Expected singleton: res.users()",
                                    ignoreCase = true
                                )
                            ) {
                                getString(R.string.login_credential_error)
                            } else {
                                authenticate.errorMessage
                            }
                        )
                    }
                } else {
                    toggleLoginWidgets(showErrorBody = false)
                    showServerErrorMessage(response)
                }
            }

            onError { error ->
                val pattern1 = "an int but was BOOLEAN"
                val pattern2 = "result.uid"
                val message = error.message ?: getString(R.string.generic_error)
                toggleLoginWidgets(
                    showErrorBody = true, errorMessage = if (message.contains(pattern1) && message.contains(pattern2))
                        getString(R.string.login_credential_error)
                    else
                        message
                )
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
                compositeDisposable?.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val searchRead = response.body()!!
                    if (searchRead.isSuccessful) {
                        if (searchRead.result.records.size() > 0) {
                            val row = searchRead.result.records[0].asJsonObject
                            row?.get("image")?.asString?.let {
                                authenticateResult.imageSmall = it
                            }
                            row?.get("name")?.asString?.trimFalse()?.let {
                                authenticateResult.name = it
                            }
                        }
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
                toggleLoginWidgets(
                    showErrorBody = true, errorMessage = error.message
                        ?: getString(R.string.generic_error)
                )
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

        binding.llLoginProgress.postEx {
            visibility = View.GONE
        }

        if (showErrorBody) {
            binding.llLoginError.postEx {
                visibility = View.VISIBLE
            }
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
                onSubscribe {
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
        compositeDisposable?.dispose()
        super.onDestroy()
    }
}
