package com.hipo.maskededittext.maskers

import android.text.InputType
import com.hipo.maskededittext.Mask
import com.hipo.maskededittext.utils.extensions.formatAsTcId
import kotlin.properties.Delegates

class TCIdMasker(
    override val mask: Mask,
    override val onTextMaskedListener: (String, Int?) -> Unit
) : BaseMasker() {

    override val inputType: Int
        get() = InputType.TYPE_CLASS_NUMBER

    private var text: String by Delegates.observable("") { _, _, newValue ->
        if (selection >= newValue.length) selection = newValue.length
        onTextMaskedListener(newValue, selection)
    }

    private var oldCursorPosition = 0

    private var startLength = 0
    private var endLength = 0
    private var selection = 0

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int, selectionStart: Int) {
        oldCursorPosition = selectionStart
    }

    override fun onTextChanged(charSequence: CharSequence?, start: Int, count: Int, before: Int, selectionStart: Int) {
        if (charSequence.isNullOrEmpty()) {
            selection = 0
            text = ""
            return
        }
        startLength = charSequence.length
        when (count) {
            IS_REMOVED -> handleDeletion(charSequence, start)
            IS_ADDED -> handleAddition(charSequence, selectionStart)
            else -> handleRestoration(charSequence)
        }
    }

    private fun handleAddition(charSequence: CharSequence, start: Int) {
        if (charSequence.length > mask.maskPattern.length) {
            selection = oldCursorPosition
            text = text
            return
        }
        val formattedTcId = charSequence.toString().formatAsTcId(mask)
        endLength = formattedTcId.length
        selection = handleCursorIndex(formattedTcId, start, false)
        text = formattedTcId
    }

    private fun handleDeletion(charSequence: CharSequence, start: Int) {
        val formattedTcId: String = charSequence.toString().formatAsTcId(mask)
        if (start == 0) {
            selection = formattedTcId.length
            text = formattedTcId
            return
        }
        endLength = formattedTcId.length
        selection = handleCursorIndex(formattedTcId, start, true)
        text = formattedTcId
    }

    private fun handleRestoration(charSequence: CharSequence) {
        val formattedTcId = charSequence.toString().formatAsTcId(mask)
        selection = formattedTcId.length
        text = formattedTcId
    }

    private fun handleCursorIndex(charSequence: CharSequence, selectionIndex: Int, isDeletion: Boolean): Int = when {
        selectionIndex >= charSequence.length -> charSequence.length
        else -> selectionIndex
    }

    override fun getTextWithReturnPattern(): String? {
        return mask.getParsedText(text)
    }
}
