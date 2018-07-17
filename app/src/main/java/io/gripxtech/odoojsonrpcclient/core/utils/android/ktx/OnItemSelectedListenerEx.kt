package io.gripxtech.odoojsonrpcclient.core.utils.android.ktx

import android.view.View
import android.widget.AdapterView

class OnItemSelectedListenerEx : AdapterView.OnItemSelectedListener {

    private var nothingSelected: ((parent: AdapterView<*>?) -> Unit)? = null

    fun onNothingSelected(nothingSelected: (parent: AdapterView<*>?) -> Unit) {
        this.nothingSelected = nothingSelected
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        this.nothingSelected?.invoke(parent)
    }

    private var itemSelected: ((parent: AdapterView<*>?, view: View?, position: Int, id: Long) -> Unit)? = null

    fun onItemSelected(itemSelected: (parent: AdapterView<*>?, view: View?, position: Int, id: Long) -> Unit) {
        this.itemSelected = itemSelected
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        this.itemSelected?.invoke(parent, view, position, id)
    }
}