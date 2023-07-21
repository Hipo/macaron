package com.hipo.maskededittext.maskers

import android.graphics.Rect
import android.text.method.DigitsKeyListener
import com.hipo.maskededittext.Mask

abstract class BaseMasker {
    abstract fun onTextChanged(charSequence: CharSequence?, start: Int, count: Int, before: Int, selectionStart: Int)
    abstract fun getTextWithReturnPattern(): String?

    open fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {}
    open fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int, selectionStart: Int) {}

    abstract val onTextMaskedListener: (String, Int?) -> Unit
    abstract val mask: Mask
    abstract val inputType: Int

    open val keyListener: DigitsKeyListener
        get() = DigitsKeyListener.getInstance(DIGITS)

    companion object {
        const val HASH = '#'
        const val IS_REMOVED = 0
        const val IS_ADDED = 1

        const val DIGITS = "1234567890"
        const val DIGITS_WITH_DOT_AND_COMMA = "1234567890.,"
    }
}
