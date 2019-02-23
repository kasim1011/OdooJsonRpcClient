package io.gripxtech.odoojsonrpcclient.core.authenticator

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.gripxtech.odoojsonrpcclient.App
import io.gripxtech.odoojsonrpcclient.R
import io.gripxtech.odoojsonrpcclient.core.utils.BaseActivity
import io.gripxtech.odoojsonrpcclient.core.utils.recycler.decorators.VerticalLinearItemDecorator
import io.gripxtech.odoojsonrpcclient.databinding.ActivityManageAccountBinding
import io.gripxtech.odoojsonrpcclient.getOdooUsers
import io.reactivex.disposables.CompositeDisposable

class ManageAccountActivity : BaseActivity() {

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    lateinit var app: App private set
    var compositeDisposable: CompositeDisposable? = null
        private set
    lateinit var binding: ActivityManageAccountBinding private set
    lateinit var adapter: ManageAccountAdapter private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as App
        compositeDisposable?.dispose()
        compositeDisposable = CompositeDisposable()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_account)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val users = getOdooUsers()
        val layoutManager = LinearLayoutManager(
            this, RecyclerView.VERTICAL, false
        )
        binding.rv.layoutManager = layoutManager
        binding.rv.addItemDecoration(VerticalLinearItemDecorator(
                resources.getDimensionPixelOffset(R.dimen.default_8dp)
        ))

        adapter = ManageAccountAdapter(this, ArrayList(users))
        binding.rv.adapter = adapter
    }

    override fun onDestroy() {
        compositeDisposable?.dispose()
        super.onDestroy()
    }
}
