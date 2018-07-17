package io.gripxtech.odoojsonrpcclient.core.authenticator

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.gripxtech.odoojsonrpcclient.*
import io.gripxtech.odoojsonrpcclient.core.Odoo
import io.gripxtech.odoojsonrpcclient.core.OdooUser
import io.gripxtech.odoojsonrpcclient.core.utils.android.ktx.subscribeEx
import io.gripxtech.odoojsonrpcclient.core.utils.recycler.RecyclerBaseAdapter
import io.gripxtech.odoojsonrpcclient.databinding.ItemViewManageAccountBinding
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ManageAccountAdapter(
        private val activity: ManageAccountActivity,
        items: ArrayList<Any>
) : RecyclerBaseAdapter(items, activity.binding.rv) {

    companion object {
        private const val VIEW_TYPE_ITEM = 0
    }

    private val rowItems: ArrayList<OdooUser> = ArrayList(
            items.filterIsInstance<OdooUser>()
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            ManageAccountAdapter.VIEW_TYPE_ITEM -> {
                val binding = ItemViewManageAccountBinding.inflate(
                        inflater,
                        parent,
                        false
                )
                return ManageAccountViewHolder(binding)
            }
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(baseHolder: RecyclerView.ViewHolder, basePosition: Int) {
        super.onBindViewHolder(baseHolder, basePosition)
        val position = baseHolder.adapterPosition
        when (getItemViewType(basePosition)) {
            ManageAccountAdapter.VIEW_TYPE_ITEM -> {
                val holder = baseHolder as ManageAccountViewHolder
                val item = items[position] as OdooUser
                val binding = holder.binding
                binding.user = item

                val loginDrawable = ContextCompat
                        .getDrawable(activity, R.drawable.ic_done_all_white_24dp)
                val logoutDrawable = ContextCompat
                        .getDrawable(activity, R.drawable.ic_exit_to_app_white_24dp)
                val deleteDrawable = ContextCompat
                        .getDrawable(activity, R.drawable.ic_close_white_24dp)

                binding.civLogin.setImageDrawable(loginDrawable)
                binding.civLogout.setImageDrawable(logoutDrawable)
                binding.civDelete.setImageDrawable(deleteDrawable)

                val activeUser = activity.getActiveOdooUser()
                if (activeUser != null && item == activeUser) {
                    binding.civLogin.visibility = View.GONE
                    binding.civLogout.visibility = View.VISIBLE
                } else {
                    binding.civLogin.visibility = View.VISIBLE
                    binding.civLogout.visibility = View.GONE
                }

                binding.civLogin.setOnClickListener {
                    val clickedPosition = baseHolder.adapterPosition
                    val clickedItem = items[clickedPosition] as OdooUser
                    Odoo.user = clickedItem
                    @Suppress("DEPRECATION")
                    val dialog = android.app.ProgressDialog(activity)
                    dialog.setCancelable(false)
                    dialog.setMessage(activity.getString(R.string.login_progress))
                    dialog.show()
                    authenticate(user = clickedItem, dialog = dialog)
                }

                binding.civLogout.setOnClickListener {
                    val clickedPosition = baseHolder.adapterPosition
                    val clickedItem = items[clickedPosition] as OdooUser
                    Odoo.destroy {}
                    logoutUser(clickedItem)
                }

                binding.civDelete.setOnClickListener {
                    val clickedPosition = baseHolder.adapterPosition
                    val clickedItem = items[clickedPosition] as OdooUser
                    val clickedActiveUser = activity.getActiveOdooUser()
                    if ((clickedActiveUser != null && clickedItem == clickedActiveUser)) {
                        Odoo.destroy {}
                    }
                    deleteUser(clickedItem, clickedActiveUser, clickedPosition)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val o = items[position]
        if (o is OdooUser) {
            return ManageAccountAdapter.VIEW_TYPE_ITEM
        }
        return super.getItemViewType(position)
    }

    fun removeRow(position: Int) {
        @Suppress("UnnecessaryVariable")
        val start = position
        val count = itemCount - 1 - position
        items.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(start, count)
        updateRowItems()
    }

    private fun updateRowItems() {
        updateSearchItems()
        rowItems.clear()
        rowItems.addAll(ArrayList(
                items.filterIsInstance<OdooUser>()))
    }

    override fun clear() {
        rowItems.clear()
        super.clear()
    }

    private fun authenticate(user: OdooUser, @Suppress("DEPRECATION") dialog: android.app.ProgressDialog) {
        Odoo.authenticate(user.login, user.password, user.database) {

            onSubscribe { disposable ->
                activity.compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
                if (response.isSuccessful) {
                    val authenticate = response.body()!!
                    if (authenticate.isSuccessful) {
                        loginUser(user)
                    } else {
                        activity.showMessage(
                                title = activity.getString(R.string.odoo_error),
                                message = authenticate.errorMessage
                        )
                    }
                } else {
                    activity.showServerErrorMessage(response)
                }
            }

            onError { error ->
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
                activity.showMessage(
                        title = activity.getString(R.string.operation_failed),
                        message = error.message
                )
            }
        }
    }

    private fun loginUser(user: OdooUser) {
        Observable.fromCallable {
            val result = activity.loginOdooUser(user)
            Odoo.user = user
            activity.app.cookiePrefs.setCookies(Odoo.pendingAuthenticateCookies)
            Odoo.pendingAuthenticateCookies.clear()
            result
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeEx {
                    onSubscribe { _ ->
                        // Must be complete, not dispose in between
                        // compositeDisposable.add(d)
                    }

                    onNext { _ ->
                        activity.restartApp()
                    }

                    onError { error ->
                        error.printStackTrace()
                        activity.showMessage(
                                title = activity.getString(R.string.operation_failed),
                                message = error.message
                                        ?: activity.getString(R.string.generic_error)
                        )
                    }
                }
    }

    private fun logoutUser(user: OdooUser) {
        Observable.fromCallable {
            activity.logoutOdooUser(user)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeEx {
                    onSubscribe { _ ->
                        // Must be complete, not dispose in between
                        // compositeDisposable.add(d)
                    }

                    onNext { _ ->
                        activity.restartApp()
                    }

                    onError { error ->
                        error.printStackTrace()
                        activity.showMessage(
                                title = activity.getString(R.string.operation_failed),
                                message = error.message
                                        ?: activity.getString(R.string.generic_error)
                        )
                    }
                }
    }

    private fun deleteUser(user: OdooUser, activeOdooUser: OdooUser?, position: Int) {
        Observable.fromCallable {
            activity.deleteOdooUser(user)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeEx {
                    onSubscribe { _ ->
                        // Must be complete, not dispose in between
                        // compositeDisposable.add(d)
                    }

                    onNext { t ->
                        if (t) {
                            removeRow(position)
                            if (rowItems.size == 0 || (activeOdooUser != null && user == activeOdooUser)) {
                                activity.restartApp()
                            }
                        } else {
                            activity.showMessage(
                                    title = activity.getString(R.string.operation_failed),
                                    message = activity.getString(R.string.manage_account_remove_error)
                            )
                        }
                    }

                    onError { error ->
                        error.printStackTrace()
                        activity.showMessage(
                                title = activity.getString(R.string.operation_failed),
                                message = error.message
                                        ?: activity.getString(R.string.generic_error)
                        )
                    }
                }
    }
}
