package com.hipo.maskededittext.maskers

import android.text.InputType
import com.hipo.maskededittext.Mask
import com.hipo.maskededittext.utils.extensions.formatAsCreditCard
import com.hipo.maskededittext.utils.extensions.specialChars
import kotlin.properties.Delegates

class CreditCardMasker(
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
        val formattedCreditCardNumber = charSequence.toString().formatAsCreditCard()
        endLength = formattedCreditCardNumber.length
        selection = handleCursorIndex(formattedCreditCardNumber, start, false)
        text = formattedCreditCardNumber
    }

    private fun handleDeletion(charSequence: CharSequence, start: Int) {
        val formattedCreditCardNumber: String = charSequence.toString().formatAsCreditCard()
        if (start == 0) {
            selection = formattedCreditCardNumber.length
            text = formattedCreditCardNumber
            return
        }
        endLength = formattedCreditCardNumber.length
        selection = handleCursorIndex(formattedCreditCardNumber, start, true)
        text = formattedCreditCardNumber
    }

    private fun handleRestoration(charSequence: CharSequence) {
        val formattedCreditCardNumber = charSequence.toString().formatAsCreditCard()
        selection = formattedCreditCardNumber.length
        text = formattedCreditCardNumber
    }

    private fun handleCursorIndex(charSequence: CharSequence, selectionIndex: Int, isDeletion: Boolean): Int = when {
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
