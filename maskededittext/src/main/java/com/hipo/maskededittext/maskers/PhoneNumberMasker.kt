package com.hipo.maskededittext.maskers

import android.text.InputType
import com.hipo.maskededittext.Mask
import com.hipo.maskededittext.utils.extensions.formatAsMask
import com.hipo.maskededittext.utils.extensions.specialChars
import kotlin.properties.Delegates

class PhoneNumberMasker(
    override val mask: Mask,
    override val onTextMaskedListener: (String, Int?) -> Unit
) : BaseMasker() {

    override val inputType: Int
        get() = InputType.TYPE_CLASS_NUMBER

    private var text: String by Delegates.observable("") { _, _, newValue ->
        if (selection >= newValue.length) selection = newValue.length
        onTextMaskedListener(newValue, selection)
    }

    private val phoneMaskMinCharCount = mask.maskPattern.substringBefore(HASH).length

    private var oldCursorPosition = 0
    private var isUpcomingFieldSpecialChar = false

    private var startLength = 0
    private var endLength = 0
    private var selection = 0

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int, selectionStart: Int) {
        oldCursorPosition = selectionStart
    }

    override fun onTextChanged(charSequence: CharSequence?, start: Int, count: Int, before: Int, selectionStart: Int) {
        if (charSequence.isNullOrEmpty() || charSequence.length <= phoneMaskMinCharCount) {
            val defaultPhoneFormat = mask.maskPattern.substringBefore(HASH)
            selection = defaultPhoneFormat.length
            text = defaultPhoneFormat
            return
        }

        if (start < phoneMaskMinCharCount) {
            selection = text.length
            text = if (text.isEmpty()) charSequence.toString() else text
            return
        }

        startLength = charSequence.length
        when (count) {
            IS_REMOVED -> handleDeletion(charSequence, selectionStart)
            IS_ADDED -> handleAddition(charSequence, selectionStart)
            else -> handleRestoration(charSequence)
        }
    }

    private fun handleAddition(charSequence: CharSequence, start: Int) {
        if (text.length >= mask.maskPattern.length) {
            selection = oldCursorPosition
            text = text
            return
        }
        val formattedPhone = charSequence.toString().formatAsMask(mask)
        endLength = formattedPhone.length
        selection = handleCursorIndex(formattedPhone, getLengthDifferencesIfCursorEnd(start), false)
        text = formattedPhone
    }

    private fun handleDeletion(charSequence: CharSequence, start: Int) {
        val formattedPhone = charSequence.toString().formatAsMask(mask)
        if (start == 0) {
            selection = formattedPhone.length
            text = formattedPhone
            return
        }
        endLength = formattedPhone.length
        selection = handleCursorIndex(formattedPhone, start, true)
        text = formattedPhone
    }

    private fun handleRestoration(charSequence: CharSequence) {
        val formattedPhone = charSequence.toString().formatAsMask(mask)
        selection = formattedPhone.length
        text = formattedPhone
    }

    private fun handleCursorIndex(charSequence: CharSequence, selectionIndex: Int, isDeletion: Boolean): Int = when {
        selectionIndex <= phoneMaskMinCharCount -> charSequence.length
        selectionIndex >= charSequence.length -> charSequence.length
        specialChars.contains(charSequence[selectionIndex - 1]) -> {
            handleSpecialCharState(selectionIndex - 1, isDeletion)
        }
        else -> selectionIndex
    }

    private fun handleSpecialCharState(cursor: Int, isDeletion: Boolean): Int {
        return if (isDeletion) {
            mask.calculateCurrentMaskDistance(cursor) + 1
        } else {
            mask.calculateUpcomingMaskDistance(cursor) + 1
        }
    }

    private fun getLengthDifferencesIfCursorEnd(start: Int): Int {
        return if (start < startLength) start else start + (endLength - startLength)
    }

    override fun getTextWithReturnPattern(): String? {
        return mask.getParsedText(text)
    }
}
