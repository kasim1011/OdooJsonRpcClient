package io.gripxtech.odoojsonrpcclient

import android.content.Intent
import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import io.gripxtech.odoojsonrpcclient.core.authenticator.LoginActivity
import io.gripxtech.odoojsonrpcclient.core.authenticator.ManageAccountActivity
import io.gripxtech.odoojsonrpcclient.core.authenticator.ProfileActivity
import io.gripxtech.odoojsonrpcclient.core.preferences.SettingsActivity
import io.gripxtech.odoojsonrpcclient.core.utils.NavHeaderViewHolder
import io.gripxtech.odoojsonrpcclient.core.utils.android.ktx.postEx
import io.gripxtech.odoojsonrpcclient.customer.CustomerFragment
import io.gripxtech.odoojsonrpcclient.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        private const val ACTION_CUSTOMER = 1
        private const val ACTION_SUPPLIER = 2
        private const val ACTION_COMPANY = 3
    }

    lateinit var app: App private set
    lateinit var binding: ActivityMainBinding private set
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var navHeader: NavHeaderViewHolder

    private var currentDrawerItemID: Int = 0
    private var drawerClickStatus: Boolean = false

    private val customerFragment: CustomerFragment by lazy {
        CustomerFragment.newInstance(CustomerFragment.Companion.CustomerType.Customer)
    }

    private val supplierFragment: CustomerFragment by lazy {
        CustomerFragment.newInstance(CustomerFragment.Companion.CustomerType.Supplier)
    }

    private val companyFragment: CustomerFragment by lazy {
        CustomerFragment.newInstance(CustomerFragment.Companion.CustomerType.Company)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as App
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.tb)

        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        binding.tb.setNavigationOnClickListener {
            binding.dl.openDrawer(GravityCompat.START)
        }
        setTitle(R.string.app_name)

        drawerToggle = ActionBarDrawerToggle(
                this, binding.dl, binding.tb,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.dl.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        val view = binding.nv.getHeaderView(0)
        if (view != null) {
            navHeader = NavHeaderViewHolder(view)
            val user = getActiveOdooUser()
            if (user != null) {
                navHeader.setUser(user)
            }
        }

        drawerClickStatus = false

        navHeader.menuToggle.setOnClickListener {
            val menu = binding.nv.menu
            if (drawerClickStatus) {
                menu.setGroupVisible(R.id.nav_menu_1, true)
                menu.setGroupVisible(R.id.nav_menu_2, true)
                menu.setGroupVisible(R.id.nav_menu_3, true)
                menu.setGroupVisible(R.id.nav_menu_4, false)
                navHeader.menuToggleImage.setImageResource(R.drawable.ic_arrow_drop_down_white_24dp)
            } else {
                menu.setGroupVisible(R.id.nav_menu_1, false)
                menu.setGroupVisible(R.id.nav_menu_2, false)
                menu.setGroupVisible(R.id.nav_menu_3, false)
                menu.setGroupVisible(R.id.nav_menu_4, true)
                navHeader.menuToggleImage.setImageResource(R.drawable.ic_arrow_drop_up_white_24dp)
            }
            drawerClickStatus = !drawerClickStatus
        }

        binding.nv.setNavigationItemSelectedListener { item ->
            binding.dl.postEx { closeDrawer(GravityCompat.START) }
            when (item.itemId) {
                R.id.nav_customer -> {
                    if (currentDrawerItemID != ACTION_CUSTOMER) {
                        loadFragment(ACTION_CUSTOMER)
                    }
                    true
                }
                R.id.nav_supplier -> {
                    if (currentDrawerItemID != ACTION_SUPPLIER) {
                        loadFragment(ACTION_SUPPLIER)
                    }
                    true
                }
                R.id.nav_company -> {
                    if (currentDrawerItemID != ACTION_COMPANY) {
                        loadFragment(ACTION_COMPANY)
                    }
                    true
                }
                R.id.nav_profile -> {
                    if (getActiveOdooUser() != null) {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    } else {
                        showMessage(message = getString(R.string.error_active_user))
                    }
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                R.id.nav_add_account -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.putExtra(LoginActivity.FROM_APP_SETTINGS, true)
                    startActivity(intent)
                    true
                }
                R.id.nav_manage_account -> {
                    startActivity(Intent(this, ManageAccountActivity::class.java))
                    true
                }
                else -> {
                    true
                }
            }
        }

        if (savedInstanceState == null) {
            loadFragment(ACTION_CUSTOMER)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    private fun loadFragment(currentDrawerItemID: Int) {
        clearBackStack()
        this.currentDrawerItemID = currentDrawerItemID
        when (currentDrawerItemID) {
            ACTION_CUSTOMER -> {
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.clMain, customerFragment, getString(R.string.action_customer))
                        .commit()
            }
            ACTION_SUPPLIER -> {
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.clMain, supplierFragment, getString(R.string.action_supplier))
                        .commit()
            }
            ACTION_COMPANY -> {
                supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.clMain, companyFragment, getString(R.string.action_company))
                        .commit()
            }
        }
    }

    private fun clearBackStack() {
        val fragmentManager = supportFragmentManager
        for (i in 0 until fragmentManager.backStackEntryCount) {
            fragmentManager.popBackStackImmediate()
        }
    }
}
