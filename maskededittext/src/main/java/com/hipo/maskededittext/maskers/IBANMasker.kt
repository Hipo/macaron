package com.hipo.maskededittext.maskers

import android.text.InputType
import com.hipo.maskededittext.Mask
import com.hipo.maskededittext.masks.IBANMask
import com.hipo.maskededittext.utils.extensions.formatAsIban
import com.hipo.maskededittext.utils.extensions.specialChars
import kotlin.properties.Delegates

class IBANMasker(
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
            selection = IBANMask.IBAN_PREFIX.length
            text = IBANMask.IBAN_PREFIX
            return
        }

        if (charSequence.length < IBANMask.IBAN_PREFIX.length) {
            selection = IBANMask.IBAN_PREFIX.length
            text = IBANMask.IBAN_PREFIX
            return
        }

        if (start < IBANMask.IBAN_PREFIX.length) {
            selection = text.length
            text = text
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
        val formattedIban = charSequence.toString().formatAsIban()
        endLength = formattedIban.length
        selection = handleCursorIndex(formattedIban, start, false)
        text = formattedIban
    }

    private fun handleDeletion(charSequence: CharSequence, start: Int) {
        val formattedIban: String = charSequence.toString().formatAsIban()
        if (start == 0) {
            selection = formattedIban.length
            text = formattedIban
            return
        }
        endLength = formattedIban.length
        selection = handleCursorIndex(formattedIban, start, true)
        text = formattedIban
    }

    private fun handleRestoration(charSequence: CharSequence) {
        val formattedIban = charSequence.toString().formatAsIban()
        selection = formattedIban.length
        text = formattedIban
    }

    private fun handleCursorIndex(charSequence: CharSequence, selectionIndex: Int, isDeletion: Boolean): Int = when {
        selectionIndex <= IBANMask.IBAN_PREFIX.length -> charSequence.length
        selectionIndex >= charSequence.length -> charSequence.length
        specialChars.contains(charSequence[selectionIndex - 1]) -> {
            handleSpecialCharState(selectionIndex, isDeletion)
        }
        else -> selectionIndex
    }

    private fun handleSpecialCharState(cursor: Int, isDeletion: Boolean): Int {
        return if (isDeletion) {
            mask.calculateCurrentMaskDistance(cursor)
        } else {
            mask.calculateUpcomingMaskDistance(cursor) + 1
        }
    }

    override fun getTextWithReturnPattern(): String? {
        return mask.getParsedText(text)
    }
}
