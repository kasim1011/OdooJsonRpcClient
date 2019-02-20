package io.gripxtech.odoojsonrpcclient

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.text.Html
import android.text.Spanned
import android.util.Base64
import android.util.Patterns
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.TaskStackBuilder
import com.google.gson.*
import io.gripxtech.odoojsonrpcclient.core.Odoo
import io.gripxtech.odoojsonrpcclient.core.OdooUser
import io.gripxtech.odoojsonrpcclient.core.authenticator.SplashActivity
import io.gripxtech.odoojsonrpcclient.core.entities.Many2One
import io.gripxtech.odoojsonrpcclient.core.entities.odooError.OdooError
import io.gripxtech.odoojsonrpcclient.core.entities.session.authenticate.AuthenticateResult
import io.gripxtech.odoojsonrpcclient.core.utils.decryptAES
import io.gripxtech.odoojsonrpcclient.core.utils.encryptAES
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


const val RECORD_LIMIT = 10

val gson: Gson by lazy {
    GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
}

fun Context.createOdooUser(authenticateResult: AuthenticateResult): Boolean {
    val accountManager = AccountManager.get(this)
    val account = Account(authenticateResult.androidName, App.KEY_ACCOUNT_TYPE)
    val result = accountManager.addAccountExplicitly(
        account,
        authenticateResult.password.encryptAES(),
        authenticateResult.toBundle
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        accountManager.notifyAccountAuthenticated(account)
    }
    return result
}

fun Context.getOdooUsers(): List<OdooUser> {
    val manager = AccountManager.get(this)
    val odooUsers = ArrayList<OdooUser>()
    manager.getAccountsByType(App.KEY_ACCOUNT_TYPE)
        .map {
            Odoo.fromAccount(manager, it)
        }
        .forEach { odooUsers += it }
    return odooUsers.toList()
}

fun Context.odooUserByAndroidName(androidName: String): OdooUser? {
    getOdooUsers()
        .filter { it.androidName == androidName }
        .forEach { return it }
    return null
}

fun Context.getActiveOdooUser(): OdooUser? {
    getOdooUsers()
        .filter { it.isActive }
        .forEach { return it }
    return null
}

fun Context.loginOdooUser(odooUser: OdooUser): OdooUser? {
    do {
        val user = getActiveOdooUser()
        if (user != null) {
            logoutOdooUser(user)
        }
    } while (user != null)
    val accountManager = AccountManager.get(this)
    accountManager.setUserData(odooUser.account, "active", "true")

    return getActiveOdooUser()
}

fun Context.logoutOdooUser(odooUser: OdooUser) {
    val accountManager = AccountManager.get(this)
    accountManager.setUserData(odooUser.account, "active", "false")
}

fun Context.getCookies(odooUser: OdooUser): String {
    val accountManager = AccountManager.get(this)
    return accountManager.getUserData(odooUser.account, "cookies")?.decryptAES() ?: ""
}

fun Context.setCookies(odooUser: OdooUser, cookiesStr: String) {
    val accountManager = AccountManager.get(this)
    accountManager.setUserData(odooUser.account, "cookies", cookiesStr.encryptAES())
}

fun Context.deleteOdooUser(odooUser: OdooUser): Boolean {
    val accountManager = AccountManager.get(this)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        accountManager.removeAccountExplicitly(odooUser.account)
    } else {
        @Suppress("DEPRECATION")
        val result = accountManager.removeAccount(odooUser.account, {

        }, Handler(this.mainLooper))
        result != null && result.result != null && result.result!!
    }
}

fun String.toDate(dateFormat: String = "yyyy-MM-dd HH:mm:ss"): Date {
    val parser = SimpleDateFormat(dateFormat, Locale.US)
    return parser.parse(this)
}

fun Date.formatTo(dateFormat: String): String {
    val formatter = SimpleDateFormat(dateFormat, Locale.US)
    return formatter.format(this)
}

fun String.toDate(dateFormat: String = "yyyy-MM-dd HH:mm:ss", timeZone: TimeZone = TimeZone.getTimeZone("UTC")): Date {
    val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
    parser.timeZone = timeZone
    return parser.parse(this)
}

fun Date.formatTo(dateFormat: String, timeZone: TimeZone = TimeZone.getDefault()): String {
    val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
    formatter.timeZone = timeZone
    return formatter.format(this)
}

fun Bitmap.toBase64(): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

val JsonElement.isManyToOne: Boolean get() = isJsonArray && asJsonArray.size() == 2

val JsonElement.asManyToOne: Many2One
    get() = if (isManyToOne) {
        Many2One(asJsonArray)
    } else {
        Many2One(JsonArray().apply { add(0); add("") })
    }

fun Many2One.toStringList(): ArrayList<String> = ArrayList<String>().apply {
    add(id.toString())
    add(name)
}

fun ArrayList<String>.toJsonElement(): JsonElement = JsonArray().apply {
    add(this[0].asInt)
    add(this[1])
}

val JsonArray.asIntList: List<Int>
    get() = this.map {
        it.asInt
    }

@Suppress("DEPRECATION")
val Response<*>.errorBodySpanned: Spanned
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        Html.fromHtml(errorBody()!!.string(), Html.FROM_HTML_MODE_COMPACT)
    else
        Html.fromHtml(errorBody()!!.string())

fun AppCompatActivity.hideSoftKeyboard() {
    val view = currentFocus
    if (view != null) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)

        view.clearFocus()
    }
}

fun AppCompatActivity.restartApp() {
    TaskStackBuilder.create(this)
        .addNextIntent(Intent(this, SplashActivity::class.java))
        .startActivities()
}

var alertDialog: AlertDialog? = null

fun AppCompatActivity.showMessage(
    title: CharSequence? = null,
    message: CharSequence?,
    cancelable: Boolean = false,
    icon: Drawable? = null,
    positiveButton: CharSequence = getString(R.string.ok),
    positiveButtonListener: DialogInterface.OnClickListener? = DialogInterface.OnClickListener { _, _ -> }
): AlertDialog = showMessage(
    title, message, cancelable, icon, positiveButton, positiveButtonListener,
    false, getString(R.string.cancel), DialogInterface.OnClickListener { _, _ -> }
)

fun AppCompatActivity.showMessage(
    title: CharSequence? = null,
    message: CharSequence?,
    cancelable: Boolean = false,
    icon: Drawable? = null,
    positiveButton: CharSequence = getString(R.string.ok),
    positiveButtonListener: DialogInterface.OnClickListener? = DialogInterface.OnClickListener { _, _ -> },
    showNegativeButton: Boolean = false,
    negativeButton: CharSequence = getString(R.string.cancel),
    negativeButtonListener: DialogInterface.OnClickListener? = DialogInterface.OnClickListener { _, _ -> },
    showNeutralButton: Boolean = false,
    neutralButton: CharSequence = getString(R.string.cancel),
    neutralButtonListener: DialogInterface.OnClickListener? = DialogInterface.OnClickListener { _, _ -> }
): AlertDialog {
    alertDialog?.dismiss()
    alertDialog = AlertDialog.Builder(this, R.style.AppAlertDialogTheme)
        .setTitle(title)
        .setMessage(
            if (message?.isNotEmpty() == true) {
                message
            } else {
                getString(R.string.generic_error)
            }
        )
        .setCancelable(cancelable)
        .setIcon(icon)
        .setPositiveButton(positiveButton, positiveButtonListener)
        .apply {
            if (showNegativeButton) {
                setNegativeButton(negativeButton, negativeButtonListener)
            }
            if (showNeutralButton) {
                setNeutralButton(neutralButton, neutralButtonListener)
            }
        }
        .show()
    return alertDialog!!
}

fun AppCompatActivity.promptReport(odooError: OdooError) {
    showMessage(
        message = odooError.data.message,
        showNeutralButton = true,
        neutralButton = getString(R.string.error_report),
        neutralButtonListener = DialogInterface.OnClickListener { _, _ ->
            val intent = emailIntent(
                address = arrayOf(getString(R.string.preference_contact_summary)),
                cc = arrayOf(),
                subject = "${getString(R.string.app_name)} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) " +
                        getString(R.string.report_feedback),
                body = "Name: ${odooError.data.name}\n\n" +
                        "Message: ${odooError.data.message}\n\n" +
                        "Exception Type: ${odooError.data.exceptionType}\n\n" +
                        "Arguments: ${odooError.data.arguments}\n\n" +
                        "Debug: ${odooError.data.debug}\n\n"
            )
            try {
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                showMessage(message = getString(R.string.preference_error_email_intent))
            }
        }
    )
}

fun AppCompatActivity.emailIntent(
    address: Array<String>,
    cc: Array<String>,
    subject: String,
    body: String
): Intent {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"))
    intent.putExtra(Intent.EXTRA_EMAIL, address)
    intent.putExtra(Intent.EXTRA_CC, cc)
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, body)
    return Intent.createChooser(intent, getString(R.string.preference_prompt_email_intent))
}

@Suppress("DEPRECATION")
fun AppCompatActivity.showServerErrorMessage(
    response: Response<*>,
    positiveButtonListener: DialogInterface.OnClickListener = DialogInterface.OnClickListener { _, _ -> }
): AlertDialog =
    showMessage(
        title = getString(R.string.server_request_error, response.code(), response.body()),
        message = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Html.fromHtml(response.errorBody()!!.string(), Html.FROM_HTML_MODE_COMPACT)
        else
            Html.fromHtml(response.errorBody()!!.string()),
        positiveButtonListener = positiveButtonListener
    )

fun AppCompatActivity.closeApp(message: String = getString(R.string.generic_error)): AlertDialog =
    showMessage(
        getString(R.string.fatal_error),
        message,
        false,
        null,
        getString(R.string.exit),
        DialogInterface.OnClickListener { _, _ ->
            ActivityCompat.finishAffinity(this)
        })

fun String.trimFalse(): String = if (this != "false") this else ""

fun String.extractWebUrls(): List<String> {
    val urls = arrayListOf<String>()
    val matcher = Patterns.WEB_URL.matcher(this)
    while (matcher.find()) {
        val url = matcher.group() ?: ""
        if (url.isNotEmpty()) {
            urls.add(url)
        }
    }
    return urls
}

fun AppCompatActivity.filteredErrorMessage(errorMessage: String): String = when (errorMessage) {
    "Expected singleton: res.users()" -> {
        getString(R.string.login_credential_error)
    }
    else -> {
        errorMessage
    }
}

@Suppress("DEPRECATION")
fun AppCompatActivity.getProgressDialog(): android.app.ProgressDialog {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        return android.app.ProgressDialog(this, R.style.AppAlertDialogTheme)
    }
    return android.app.ProgressDialog(this)
}

fun AppCompatActivity.isDeviceOnline(): Boolean {
    var isConnected = false
    val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val nInfo = manager.activeNetworkInfo
    if (nInfo != null && nInfo.isConnected) {
        isConnected = true
    }
    return isConnected
}

fun String.toJsonElement(): JsonElement = gson.fromJson(this, JsonElement::class.java)

fun String.toJsonPrimitive(): JsonPrimitive = toJsonElement().asJsonPrimitive

fun String.toJsonObject(): JsonObject = toJsonElement().asJsonObject

fun String.toJsonArray(): JsonArray = toJsonElement().asJsonArray
