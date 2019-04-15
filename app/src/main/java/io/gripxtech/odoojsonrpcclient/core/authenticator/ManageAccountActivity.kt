package io.gripxtech.odoojsonrpcclient.core.authenticator

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.gripxtech.odoojsonrpcclient.*
import io.gripxtech.odoojsonrpcclient.core.utils.BaseActivity
import io.gripxtech.odoojsonrpcclient.core.utils.recycler.decorators.VerticalLinearItemDecorator
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_manage_account.*

class ManageAccountActivity : BaseActivity() {

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    lateinit var app: App private set
    lateinit var glideRequests: GlideRequests
    var compositeDisposable: CompositeDisposable? = null
        private set
    lateinit var adapter: ManageAccountAdapter private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as App
        glideRequests = GlideApp.with(this)
        compositeDisposable?.dispose()
        compositeDisposable = CompositeDisposable()
        setContentView(R.layout.activity_manage_account)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val users = getOdooUsers()
        val layoutManager = LinearLayoutManager(
            this, RecyclerView.VERTICAL, false
        )
        rv.layoutManager = layoutManager
        rv.addItemDecoration(
            VerticalLinearItemDecorator(
                resources.getDimensionPixelOffset(R.dimen.default_8dp)
            )
        )

        adapter = ManageAccountAdapter(this, ArrayList(users))
        rv.adapter = adapter
    }

    override fun onDestroy() {
        compositeDisposable?.dispose()
        super.onDestroy()
    }
}
