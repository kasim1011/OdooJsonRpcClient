package io.gripxtech.odoojsonrpcclient.core.utils.recycler.decorators

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class GridItemDecorator(
        private val sizeGridSpacingPx: Int,
        private val gridSize: Int
) : RecyclerView.ItemDecoration() {
    private var needLeftSpacing = false

    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) =
            if (outRect != null && view != null && parent != null) {
                val frameWidth: Int = ((parent.width - sizeGridSpacingPx.toFloat() * (gridSize - 1)) / gridSize).toInt()
                val padding: Int = parent.width / gridSize - frameWidth
                val itemPosition: Int = (view.layoutParams as RecyclerView.LayoutParams).viewAdapterPosition
                if (itemPosition < gridSize) {
                    outRect.top = 0
                } else {
                    outRect.top = sizeGridSpacingPx
                }
                if (itemPosition % gridSize == 0) {
                    outRect.left = 0
                    outRect.right = padding
                    needLeftSpacing = true
                } else if ((itemPosition + 1) % gridSize == 0) {
                    needLeftSpacing = false
                    outRect.right = 0
                    outRect.left = padding
                } else if (needLeftSpacing) {
                    needLeftSpacing = false
                    outRect.left = sizeGridSpacingPx - padding
                    if ((itemPosition + 2) % gridSize == 0) {
                        outRect.right = sizeGridSpacingPx - padding
                    } else {
                        outRect.right = sizeGridSpacingPx / 2
                    }
                } else if ((itemPosition + 2) % gridSize == 0) {
                    needLeftSpacing = false
                    outRect.left = sizeGridSpacingPx / 2
                    outRect.right = sizeGridSpacingPx - padding
                } else {
                    needLeftSpacing = false
                    outRect.left = sizeGridSpacingPx / 2
                    outRect.right = sizeGridSpacingPx / 2
                }
                outRect.bottom = 0
            } else {
                super.getItemOffsets(outRect, view, parent, state)
            }
}