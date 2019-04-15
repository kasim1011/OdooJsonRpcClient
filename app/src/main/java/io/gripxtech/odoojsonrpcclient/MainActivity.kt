package io.gripxtech.odoojsonrpcclient

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import io.gripxtech.odoojsonrpcclient.core.preferences.SettingsActivity
import io.gripxtech.odoojsonrpcclient.core.utils.BaseActivity
import io.gripxtech.odoojsonrpcclient.core.utils.NavHeaderViewHolder
import io.gripxtech.odoojsonrpcclient.core.utils.android.ktx.postEx
import io.gripxtech.odoojsonrpcclient.customer.CustomerFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        private const val ACTION_CUSTOMER = 1
        private const val ACTION_SUPPLIER = 2
        private const val ACTION_COMPANY = 3
    }

    lateinit var app: App private set
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var navHeader: NavHeaderViewHolder

    private var currentDrawerItemID: Int = 0

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
        setContentView(R.layout.activity_main)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(tb)

        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        tb.setNavigationOnClickListener {
            dl.openDrawer(GravityCompat.START)
        }
        setTitle(R.string.app_name)

        drawerToggle = ActionBarDrawerToggle(
            this, dl, tb,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        dl.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        val view = nv.getHeaderView(0)
        if (view != null) {
            navHeader = NavHeaderViewHolder(view)
            val user = getActiveOdooUser()
            if (user != null) {
                navHeader.setUser(user, GlideApp.with(this@MainActivity))
            }
        }

        nv.setNavigationItemSelectedListener { item ->
            dl.postEx { closeDrawer(GravityCompat.START) }
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
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
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

    override fun onConfigurationChanged(newConfig: Configuration) {
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
