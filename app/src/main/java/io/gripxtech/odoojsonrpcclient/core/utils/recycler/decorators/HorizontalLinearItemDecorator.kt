package io.gripxtech.odoojsonrpcclient.core.utils.recycler.decorators

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalLinearItemDecorator(
    private val horizontalSpaceHeight: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val viewHolder = parent.adapter
        if (viewHolder != null && parent.getChildAdapterPosition(view) != viewHolder.itemCount - 1) {
            outRect.right = horizontalSpaceHeight
        } else {
            super.getItemOffsets(outRect, view, parent, state)
        }
    }
}