package io.gripxtech.odoojsonrpcclient.core.authenticator

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import io.gripxtech.odoojsonrpcclient.App
import io.gripxtech.odoojsonrpcclient.R
import io.gripxtech.odoojsonrpcclient.core.utils.BaseActivity
import io.gripxtech.odoojsonrpcclient.databinding.ActivityProfileBinding
import io.gripxtech.odoojsonrpcclient.getActiveOdooUser

class ProfileActivity : BaseActivity() {

    private lateinit var app: App
    private lateinit var binding: ActivityProfileBinding

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
