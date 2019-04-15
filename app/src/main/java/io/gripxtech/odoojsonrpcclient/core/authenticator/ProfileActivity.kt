package io.gripxtech.odoojsonrpcclient.core.authenticator

import android.os.Bundle
import android.util.Base64
import io.gripxtech.odoojsonrpcclient.*
import io.gripxtech.odoojsonrpcclient.core.utils.BaseActivity
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : BaseActivity() {

    private lateinit var app: App
    lateinit var glideRequests: GlideRequests

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        app = application as App
        glideRequests = GlideApp.with(this)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val user = getActiveOdooUser()
        if (user != null) {
            val imageSmall = user.imageSmall.trimFalse()
            val name = user.name.trimFalse()

            glideRequests.asBitmap().load(
                if (imageSmall.isNotEmpty())
                    Base64.decode(imageSmall, Base64.DEFAULT)
                else
                    app.getLetterTile(if (name.isNotEmpty()) name else "X")
            ).circleCrop().into(ivProfile)

            ctl.title = name
            tvName.text = name
            tvLogin.text = user.login
            tvServerURL.text = user.host
            tvDatabase.text = user.database
            tvVersion.text = user.serverVersion
            tvTimezone.text = user.timezone
        }
    }
}
