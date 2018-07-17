package io.gripxtech.odoojsonrpcclient.core.utils.android.ktx

import android.text.Editable
import android.text.TextWatcher

class TextWatcherEx : TextWatcher {

    private var before: ((s: CharSequence, start: Int, count: Int, after: Int) -> Unit)? = null

    fun beforeTextChanged(before: (s: CharSequence, start: Int, count: Int, after: Int) -> Unit) {
        this.before = before
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        this.before?.invoke(s!!, start, count, after)
    }

    private var textChanged: ((s: CharSequence, start: Int, before: Int, count: Int) -> Unit)? = null

    fun onTextChanged(textChanged: ((s: CharSequence, start: Int, before: Int, count: Int) -> Unit)) {
        this.textChanged = textChanged
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        this.textChanged?.invoke(s!!, start, before, count)
    }

    private var after: ((s: Editable) -> Unit)? = null

    fun afterTextChanged(after: (s: Editable) -> Unit) {
        this.after = after
    }

    override fun afterTextChanged(s: Editable?) {
        this.after?.invoke(s!!)
    }
}