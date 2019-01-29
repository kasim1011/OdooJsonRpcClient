package io.gripxtech.odoojsonrpcclient.customer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.gripxtech.odoojsonrpcclient.core.utils.recycler.RecyclerBaseAdapter
import io.gripxtech.odoojsonrpcclient.customer.entities.Customer
import io.gripxtech.odoojsonrpcclient.databinding.ItemViewCustomerBinding

class CustomerAdapter(
    private val fragment: CustomerFragment,
    items: ArrayList<Any>
) : RecyclerBaseAdapter(items, fragment.binding.rv) {

    companion object {
        const val TAG: String = "CustomerAdapter"

        private const val VIEW_TYPE_ITEM = 0
    }

    private val rowItems: ArrayList<Customer> = ArrayList(
            items.filterIsInstance<Customer>()
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            VIEW_TYPE_ITEM -> {
                val binding = ItemViewCustomerBinding.inflate(
                        inflater,
                        parent,
                        false
                )
                return CustomerViewHolder(binding)
            }
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(baseHolder: RecyclerView.ViewHolder, basePosition: Int) {
        super.onBindViewHolder(baseHolder, basePosition)
        val position = baseHolder.adapterPosition
        when (getItemViewType(basePosition)) {
            VIEW_TYPE_ITEM -> {
                val holder = baseHolder as CustomerViewHolder
                val item = items[position] as Customer
                val binding = holder.binding
                binding.customer = item
                if (!binding.root.hasOnClickListeners()) {
                    binding.root.setOnClickListener {
                        // val clickedPosition = holder.adapterPosition
                        // val clickedItem = items[clickedPosition] as Customer
                    }
                }
            }
        }
    }

    val rowItemCount: Int get() = rowItems.size

    override fun getItemViewType(position: Int): Int {
        val o = items[position]
        if (o is Customer) {
            return VIEW_TYPE_ITEM
        }
        return super.getItemViewType(position)
    }

    private fun updateRowItems() {
        updateSearchItems()
        rowItems.clear()
        rowItems.addAll(ArrayList(
                items.filterIsInstance<Customer>()))
    }

    fun addRowItems(rowItems: ArrayList<Customer>) {
        this.rowItems.addAll(rowItems)
        addAll(rowItems.toMutableList<Any>() as ArrayList<Any>)
    }

    override fun clear() {
        rowItems.clear()
        super.clear()
    }
}
