package io.gripxtech.odoojsonrpcclient.core.preferences

import android.content.Intent
import android.net.MailTo
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import io.gripxtech.odoojsonrpcclient.BuildConfig
import io.gripxtech.odoojsonrpcclient.R
import io.gripxtech.odoojsonrpcclient.getActiveOdooUser

class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        const val TAG = "SettingsFragment"
    }

    private lateinit var activity: SettingsActivity

    private lateinit var build: Preference
    private lateinit var organization: Preference
    private lateinit var privacy: Preference
    private lateinit var contact: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        activity = getActivity() as SettingsActivity
        addPreferencesFromResource(R.xml.preferences)

        build = findPreference(getString(R.string.preference_build_key))
        organization = findPreference(getString(R.string.preference_organization_key))
        privacy = findPreference(getString(R.string.preference_privacy_policy_key))
        contact = findPreference(getString(R.string.preference_contact_key))

        build.summary = getString(R.string.preference_build_summary, BuildConfig.VERSION_NAME)

        organization.setOnPreferenceClickListener { _: Preference ->
            startActivity(Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.preference_organization_website))
            ))
            true
        }

        privacy.setOnPreferenceClickListener { _: Preference ->
            startActivity(Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.preference_privacy_policy_url))
            ))
            true
        }

        contact.setOnPreferenceClickListener { _: Preference ->
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
