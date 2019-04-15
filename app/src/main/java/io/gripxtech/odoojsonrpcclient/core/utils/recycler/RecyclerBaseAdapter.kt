package io.gripxtech.odoojsonrpcclient.core.utils.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.gripxtech.odoojsonrpcclient.R
import io.gripxtech.odoojsonrpcclient.core.utils.recycler.entities.*
import kotlinx.android.synthetic.main.item_view_recycler_empty.view.*
import kotlinx.android.synthetic.main.item_view_recycler_error.view.*


abstract class RecyclerBaseAdapter(
        val items: ArrayList<Any>,
        private var recyclerView: RecyclerView
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    companion object {
        const val TAG = "RecyclerBaseAdapter"

        // The Empty view type
        protected const val VIEW_TYPE_EMPTY = -1

        // The Error view type
        protected const val VIEW_TYPE_ERROR = -2

        // The Less view type
        protected const val VIEW_TYPE_LESS = -3

        // The More view type
        protected const val VIEW_TYPE_MORE = -4

        const val ACTION_RV_HIDE_EMPTY = -5

        const val ACTION_RV_HIDE_ERROR = -6

        const val ACTION_RV_HIDE_LESS = -7

        const val ACTION_RV_HIDE_MORE = -8

        const val ACTION_RV_SHOW_EMPTY = -9

        const val ACTION_RV_SHOW_ERROR = -10

        const val ACTION_RV_SHOW_LESS = -11

        const val ACTION_RV_SHOW_MORE = -12

        const val ACTION_RV_HIDE_REFRESH = -13

        const val ACTION_RV_SHOW_REFRESH = -14

        const val ACTION_RV_ADD_MORE_LISTENER = -15

        const val ACTION_RV_REMOVE_MORE_LISTENER = -16

        private const val DefaultVisibleThreshold = 2
    }

    protected val searchItems: ArrayList<Any> = ArrayList(items)
    private var isMoreLoading: Boolean = true
    var isLoading: Boolean = true
        private set
    var moreVisibleThreshold: Int = DefaultVisibleThreshold
    var lessVisibleThreshold: Int = DefaultVisibleThreshold

    private var pvtMoreListener: (() -> Unit)? = null
    private var pvtLessListener: (() -> Unit)? = null
    private var pvtRetryListener: (() -> Unit)? = null

    init {
        setupScrollListener(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            VIEW_TYPE_EMPTY -> {
                val view = inflater.inflate(
                        R.layout.item_view_recycler_empty,
                        parent,
                        false
                )
                return EmptyViewHolder(view)
            }
            VIEW_TYPE_ERROR -> {
                val view = inflater.inflate(
                        R.layout.item_view_recycler_error,
                        parent,
                        false
                )
                return ErrorViewHolder(view)
            }
            VIEW_TYPE_LESS -> {
                val view = inflater.inflate(
                        R.layout.item_view_recycler_less,
                        parent,
                        false
                )
                return LessViewHolder(view)
            }
            VIEW_TYPE_MORE -> {
                val view = inflater.inflate(
                        R.layout.item_view_recycler_more,
                        parent,
                        false
                )
                return MoreViewHolder(view)
            }
        }
        return super.createViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(baseHolder: RecyclerView.ViewHolder, basePosition: Int) {
        val position = baseHolder.adapterPosition
        when (getItemViewType(position)) {
            VIEW_TYPE_EMPTY -> {
                val holder = baseHolder as EmptyViewHolder
                val item = items[position] as EmptyItem
                holder.itemView.tvMessage.text = item.message
                val drawableResId = item.drawableResId
                if (drawableResId > 0) {
                    holder.itemView.ivIcon.setImageResource(drawableResId)
                }
            }
            VIEW_TYPE_ERROR -> {
                val holder = baseHolder as ErrorViewHolder
                val item = items[position] as ErrorItem
                holder.itemView.tvCause.text = item.message
                holder.itemView.bnRetry.setOnClickListener {
                    hideError()
                    pvtRetryListener?.invoke()
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is EmptyItem -> {
                VIEW_TYPE_EMPTY
            }
            is ErrorItem -> {
                VIEW_TYPE_ERROR
            }
            is Int -> {
                item
            }
            else -> {
                super.getItemViewType(position)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                filterResults.count = searchItems.size
                filterResults.values = searchItems
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                clear()
                @Suppress("UNCHECKED_CAST")
                addAll(results!!.values!! as ArrayList<Any>)
            }
        }
    }


    fun setupScrollListener(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        val layoutManager = recyclerView.layoutManager
        if (layoutManager != null && layoutManager is LinearLayoutManager) {
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(lclRecyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (pvtMoreListener != null || pvtLessListener != null) {
                        val totalItemCount = layoutManager.itemCount
                        val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                        val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                        if (!isMoreLoading && !isLoading) {
                            if (totalItemCount <= lastVisibleItem + moreVisibleThreshold) {
                                if (pvtMoreListener != null) {
                                    synchronized(this) {
                                        isMoreLoading = true
                                        lclRecyclerView.post {
                                            hideMore()
                                            showMore()
                                            val lclPvtMoreListener = pvtMoreListener
                                            lclPvtMoreListener?.invoke()
                                        }
                                    }
                                }
                            } else if (0 >= firstVisibleItem - lessVisibleThreshold) {
                                if (pvtLessListener != null) {
                                    synchronized(this) {
                                        isMoreLoading = true
                                        lclRecyclerView.post {
                                            hideLess()
                                            showLess()
                                            val lclPvtLessListener = pvtLessListener
                                            lclPvtLessListener?.invoke()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    open fun clear() {
        removeMoreListener()
        removeLessListener()
        val start = 0
        val count = itemCount
        items.clear()
        notifyItemRangeRemoved(start, count)
    }

    protected fun addAll(items: ArrayList<Any>) {
        if (items.size == 0) {
            return
        }
        val start = itemCount
        val count = items.size
        this.items.addAll(items)
        notifyItemRangeInserted(start, count)
    }

    fun showEmpty(
            message: CharSequence = recyclerView.context.getString(R.string.recycler_empty_title),
            @DrawableRes
            drawableResId: Int = R.drawable.ic_format_list_bulleted_black_24dp
    ) {
        clear()
        items += EmptyItem(message, drawableResId)
        notifyItemInserted(0)
    }

    fun hideEmpty() {
        if (itemCount > 0) {
            val position = itemCount - 1
            val item = items[position]
            if (item is EmptyItem) {
                items.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    fun showError(message: CharSequence) {
        clear()
        items += ErrorItem(message)
        notifyItemInserted(0)
    }

    fun hideError() {
        if (itemCount > 0) {
            val position = itemCount - 1
            val item = items[position]
            if (item is ErrorItem) {
                items.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    fun showLess() {
        isLoading = true
        val position = 0
        val start = position + 1
        val count = itemCount - position
        items.add(position, VIEW_TYPE_LESS)
        notifyItemInserted(position)
        notifyItemRangeChanged(start, count)
    }

    fun hideLess() {
        if (itemCount > 0) {
            val item = items[0]
            if (item is Int && item == VIEW_TYPE_LESS) {
                val position = 0
                @Suppress("UnnecessaryVariable")
                val start = position
                val count = itemCount - 1 - position
                items.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(start, count)
            }
        }
    }

    fun showMore() {
        isLoading = true
        val position = itemCount
        items += VIEW_TYPE_MORE
        notifyItemInserted(position)
    }

    fun hideMore() {
        if (itemCount > 0) {
            val position = itemCount - 1
            val item = items[position]
            if (item is Int && item == VIEW_TYPE_MORE) {
                items.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    fun updateSearchItems() {
        searchItems.clear()
        searchItems.addAll(items)
    }

    fun moreListener(
            moreListener: () -> Unit
    ) {
        pvtMoreListener = moreListener
    }

    fun hasMoreListener(): Boolean = pvtMoreListener != null

    fun removeMoreListener() {
        pvtMoreListener = null
    }

    fun finishedlessLoading() {
        isMoreLoading = false
        isLoading = false
    }

    fun lessListener(
            visibleThreshold: Int = DefaultVisibleThreshold,
            lessListener: () -> Unit
    ) {
        lessVisibleThreshold = visibleThreshold
        pvtLessListener = lessListener
    }

    fun hasLessListener(): Boolean = pvtLessListener != null

    fun removeLessListener() {
        pvtLessListener = null
    }

    fun finishedMoreLoading() {
        isMoreLoading = false
        isLoading = false
    }

    fun retryListener(retryListener: () -> Unit) {
        pvtRetryListener = retryListener
    }

    fun hasRetryListener(): Boolean = pvtRetryListener != null

    fun removeRetryListener() {
        pvtRetryListener = null
    }
}
