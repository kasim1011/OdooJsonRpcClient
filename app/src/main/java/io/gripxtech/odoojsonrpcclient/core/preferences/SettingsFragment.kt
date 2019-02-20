package io.gripxtech.odoojsonrpcclient.core.preferences

import android.content.Intent
import android.net.MailTo
import android.net.Uri
import android.os.Bundle
import androidx.core.app.TaskStackBuilder
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import io.gripxtech.odoojsonrpcclient.*
import io.gripxtech.odoojsonrpcclient.core.authenticator.SplashActivity
import io.gripxtech.odoojsonrpcclient.core.utils.LocalePrefs
import io.gripxtech.odoojsonrpcclient.core.utils.android.ktx.subscribeEx
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        const val TAG = "SettingsFragment"
    }

    private lateinit var activity: SettingsActivity

    private lateinit var build: Preference
    private lateinit var language: ListPreference
    private lateinit var organization: Preference
    private lateinit var privacy: Preference
    private lateinit var contact: Preference
    private lateinit var logout: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        activity = getActivity() as SettingsActivity
        addPreferencesFromResource(R.xml.preferences)

        build = findPreference(getString(R.string.preference_build_key))
        language = findPreference(getString(R.string.preference_language_key)) as ListPreference
        organization = findPreference(getString(R.string.preference_organization_key))
        privacy = findPreference(getString(R.string.preference_privacy_policy_key))
        contact = findPreference(getString(R.string.preference_contact_key))
        logout = findPreference(getString(R.string.preference_logout_key))

        build.summary = getString(R.string.preference_build_summary, BuildConfig.VERSION_NAME)

        language.setOnPreferenceChangeListener { _, newValue ->
            if (newValue is String) {
                val localePrefs = LocalePrefs(activity)
                when (newValue) {
                    getString(R.string.language_code_spanish) -> {
                        localePrefs.language = getString(R.string.language_code_spanish)
                    }
                    getString(R.string.language_code_english) -> {
                        localePrefs.language = getString(R.string.language_code_english)
                    }
                    getString(R.string.language_code_arabic) -> {
                        localePrefs.language = getString(R.string.language_code_arabic)
                    }
                    else -> {
                        localePrefs.clear()
                    }
                }
                TaskStackBuilder.create(activity)
                    .addNextIntent(Intent(activity, SplashActivity::class.java))
                    .startActivities()
            }
            true
        }

        organization.setOnPreferenceClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.preference_organization_website))
                )
            )
            true
        }

        privacy.setOnPreferenceClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.preference_privacy_policy_url))
                )
            )
            true
        }

        contact.setOnPreferenceClickListener {
            val lclContext = context
            val url = ("mailto:" + getString(R.string.preference_contact_summary)
                    + "?subject=Contact by " + getString(R.string.app_name) + " user " +
                    if (lclContext != null && lclContext.getActiveOdooUser() != null) {
                        lclContext.getActiveOdooUser()!!.name
                    } else {
                        "N/A"
                    })
            try {
                val mt = MailTo.parse(url)
                val i = emailIntent(arrayOf(mt.to), arrayOf(), mt.subject, "")
                activity.startActivity(i)
            } catch (e: Exception) {
                e.printStackTrace()
                Snackbar.make(
                    activity.binding.root,
                    R.string.preference_error_email_intent,
                    Snackbar.LENGTH_LONG
                ).show()
            }

            true
        }

        logout.setOnPreferenceClickListener {
            Single.fromCallable {
                for (odooUser in activity.getOdooUsers()) {
                    activity.deleteOdooUser(odooUser)
                }
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeEx {
                onSuccess {
                    TaskStackBuilder.create(activity)
                        .addNextIntent(Intent(activity, SplashActivity::class.java))
                        .startActivities()
                }

                onError { error ->
                    error.printStackTrace()
                }
            }
            true
        }
    }

    private fun emailIntent(address: Array<String>, cc: Array<String>, subject: String, body: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"))
        intent.putExtra(Intent.EXTRA_EMAIL, address)
        intent.putExtra(Intent.EXTRA_CC, cc)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, body)
        return Intent.createChooser(intent, getString(R.string.preference_prompt_email_intent))
    }
}
