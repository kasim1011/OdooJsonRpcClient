package io.gripxtech.odoojsonrpcclient.core.utils.recycler.decorators

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class HorizontalLinearItemDecorator(
        private val horizontalSpaceHeight: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) =
            if (outRect != null && view != null && parent != null) {
                if (parent.getChildAdapterPosition(view)
                        != parent.adapter.itemCount - 1) {
                    outRect.right = horizontalSpaceHeight
                } else {
                }
            } else {
                super.getItemOffsets(outRect, view, parent, state)
            }
}