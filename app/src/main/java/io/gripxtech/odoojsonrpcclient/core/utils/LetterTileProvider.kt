package io.gripxtech.odoojsonrpcclient.core.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.text.TextPaint
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import io.gripxtech.odoojsonrpcclient.R
import java.io.ByteArrayOutputStream

/**
 * Used to create a [Bitmap] that contains a letter used in the English
 * alphabet or digit, if there is no letter or digit available, a default image
 * is shown instead
 */
class LetterTileProvider
/**
 * Constructor for `LetterTileProvider`
 *
 * @param context The [Context] to use
 */
(context: Context) {

    /**
     * The [TextPaint] used to draw the letter onto the tile
     */
    private val mPaint = TextPaint()
    /**
     * The bounds that enclose the letter
     */
    private val mBounds = Rect()
    /**
     * The [Canvas] to draw on
     */
    private val mCanvas = Canvas()
    /**
     * The first char of the name being displayed
     */
    private val mFirstChar = CharArray(1)

    private val res: Resources = context.resources
    /**
     * The font size used to display the letter
     */
    private val mTileLetterFontSize: Int
    /**
     * The default image to display
     */
    private val mDefaultBitmap: Bitmap
    /**
     * The default background tile size
     */
    private val mDefaultTileSize: Int

    init {
        mPaint.typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        mPaint.color = Color.WHITE
        mPaint.textAlign = Paint.Align.CENTER
        mPaint.isAntiAlias = true

        mTileLetterFontSize = res.getDimensionPixelSize(R.dimen.tile_letter_font_size)

        mDefaultBitmap = drawableToBitmap(context, android.R.drawable.sym_def_app_icon)
        mDefaultTileSize = res.getDimensionPixelSize(R.dimen.letter_tile_size)
    }

    companion object {

        /**
         * The number of available tile colors (see R.array.letter_tile_colors)
         */
        private const val NUM_OF_TILE_COLORS = 10

        /**
         * @param c The char to check
         * @return True if `c` is in the English alphabet or is a digit,
         * false otherwise
         */
        private fun isEnglishLetterOrDigit(c: Char): Boolean =
                c in 'A'..'Z' || c in 'a'..'z' || c in '0'..'9'
    }

    /**
     * @param displayName The name used to create the letter for the tile
     * @return A [ByteArray] that contains a letter used in the English
     * alphabet or digit, if there is no letter or digit available, a
     * default image is shown instead
     */
    fun getLetterTile(displayName: String): ByteArray =
            getLetterTile(displayName, displayName, mDefaultTileSize, mDefaultTileSize)

    /**
     * @param displayName The name used to create the letter for the tile
     * @param key         The key used to generate the background color for the tile
     * @param width       The desired width of the tile
     * @param height      The desired height of the tile
     * @return A [ByteArray] that contains a letter used in the English
     * alphabet or digit, if there is no letter or digit available, a
     * default image is shown instead
     */
    private fun getLetterTile(displayName: String, key: String, width: Int, height: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val firstChar = displayName[0]

        val c = mCanvas
        c.setBitmap(bitmap)
        c.drawColor(pickColor(key))

        if (LetterTileProvider.isEnglishLetterOrDigit(firstChar)) {
            mFirstChar[0] = Character.toUpperCase(firstChar)
            mPaint.textSize = mTileLetterFontSize.toFloat()
            mPaint.getTextBounds(mFirstChar, 0, 1, mBounds)
            c.drawText(mFirstChar, 0, 1, (width / 2).toFloat(), (height / 2 + (mBounds.bottom - mBounds.top) / 2).toFloat(), mPaint)
        } else {
            c.drawBitmap(mDefaultBitmap, 0f, 0f, null)
        }

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    /**
     * @param key The key used to generate the tile color
     * @return A new or previously chosen color for `key` used as the
     * tile background color
     */
    private fun pickColor(key: String): Int {
        // String.hashCode() is not supposed to change across java versions, so
        // this should guarantee the same key always maps to the same color
        val color = Math.abs(key.hashCode()) % LetterTileProvider.NUM_OF_TILE_COLORS
        val colors = res.obtainTypedArray(R.array.letter_tile_colors)
        return try {
            colors.getColor(color, Color.BLACK)
        } finally {
            colors.recycle()
        }
    }

    private fun drawableToBitmap(context: Context, @DrawableRes id: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, id) ?: throw IllegalStateException("Drawable can't be null")
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }
}
