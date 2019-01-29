package io.gripxtech.odoojsonrpcclient.core.authenticator

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import io.gripxtech.odoojsonrpcclient.App
import io.gripxtech.odoojsonrpcclient.R
import io.gripxtech.odoojsonrpcclient.core.utils.LocaleHelper
import io.gripxtech.odoojsonrpcclient.databinding.ActivityProfileBinding
import io.gripxtech.odoojsonrpcclient.getActiveOdooUser

class ProfileActivity : AppCompatActivity() {

    private lateinit var app: App
    private lateinit var binding: ActivityProfileBinding

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

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        app = application as App
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val user = getActiveOdooUser()
        if (user != null) {
            binding.user = user
        }
    }
}
