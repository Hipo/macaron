package com.hipo.maskededittext.maskers

import android.text.InputType
import com.hipo.maskededittext.Mask
import com.hipo.maskededittext.utils.extensions.formatAsDateMonthYear
import com.hipo.maskededittext.utils.extensions.specialChars
import java.util.Calendar
import kotlin.properties.Delegates

class DateMonthYearMasker(
    override val mask: Mask,
    override val onTextMaskedListener: (String, Int?) -> Unit
) : BaseMasker() {

    override val inputType: Int
        get() = InputType.TYPE_CLASS_NUMBER

    private var text: String by Delegates.observable("") { _, _, newValue ->
        if (selection >= newValue.length) selection = newValue.length
        onTextMaskedListener(newValue, selection)
    }

    private val calendar by lazy { Calendar.getInstance() }

    private val lastTwoDigitOfYear by lazy { calendar.get(Calendar.YEAR) % 100 }

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
        val formattedDate = charSequence.toString().formatAsDateMonthYear(lastTwoDigitOfYear, mask)
        endLength = formattedDate.length
        selection = handleCursorIndex(formattedDate, start + (endLength - startLength), false)
        text = formattedDate
    }

    private fun handleDeletion(charSequence: CharSequence, start: Int) {
        val formattedDate = charSequence.toString().formatAsDateMonthYear(lastTwoDigitOfYear, mask)
        if (start == 0) {
            selection = formattedDate.length
            text = formattedDate
            return
        }
        endLength = formattedDate.length
        selection = handleCursorIndex(formattedDate, start, true)
        text = formattedDate
    }

    private fun handleRestoration(charSequence: CharSequence) {
        val formattedDateMonthYear = charSequence.toString().formatAsDateMonthYear(lastTwoDigitOfYear, mask)
        selection = formattedDateMonthYear.length
        text = formattedDateMonthYear
    }

    private fun handleCursorIndex(charSequence: CharSequence, selectionIndex: Int, isDeletion: Boolean): Int = when {
        selectionIndex <= 0 -> 0
        selectionIndex >= charSequence.length -> charSequence.length
        specialChars.contains(charSequence[selectionIndex]) -> {
            handleSpecialCharState(selectionIndex, isDeletion)
        }
        specialChars.contains(charSequence[selectionIndex - 1]) -> {
            handleSpecialCharState(selectionIndex - 1, isDeletion)
        }
        selectionIndex == 0 && selectionIndex <= charSequence.length -> charSequence.length - 1
        else -> selectionIndex
    }

    private fun handleSpecialCharState(cursor: Int, isDeletion: Boolean): Int {
        return if (isDeletion) {
            mask.calculateCurrentMaskDistance(cursor) + 1
        } else {
            mask.calculateUpcomingMaskDistance(cursor) + 1
        }
    }

    override fun getTextWithReturnPattern(): String? {
        return mask.getParsedText(text)
    }
}
