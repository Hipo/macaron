package com.hipo.maskededittext.maskers

import android.text.InputType.TYPE_CLASS_NUMBER
import android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
import android.text.method.DigitsKeyListener
import com.hipo.maskededittext.Mask
import com.hipo.maskededittext.model.CurrencyMaskerSettings
import com.hipo.maskededittext.utils.extensions.removeWithRegex
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParseException
import kotlin.properties.Delegates

class CurrencyMasker(
    override val mask: Mask,
    override val onTextMaskedListener: (String, Int?) -> Unit,
    private val currencyMaskerSettings: CurrencyMaskerSettings
) : BaseMasker() {

    override val keyListener: DigitsKeyListener
        get() = DigitsKeyListener.getInstance(DIGITS_WITH_DOT_AND_COMMA)

    override val inputType: Int
        get() = TYPE_CLASS_NUMBER or TYPE_NUMBER_FLAG_DECIMAL

    private val formattedCurrencyPrefix: String
        get() = if (currencyMaskerSettings.prefix.isEmpty()) "" else "${currencyMaskerSettings.prefix} "

    private val formattedCurrencySuffix: String
        get() = if (currencyMaskerSettings.suffix.isEmpty()) "" else " ${currencyMaskerSettings.suffix}"

    private val currencyPrefixLength: Int
        get() = formattedCurrencyPrefix.length

    private val currencySuffixLength
        get() = formattedCurrencySuffix.length

    private val defaultText by lazy {
        "${formattedCurrencyPrefix}0${localeDecimalFormatSymbols.decimalSeparator}00${formattedCurrencySuffix}"
    }

    private var selection = 0

    private var text by Delegates.observable("", { _, _, newValue ->
        onTextMaskedListener(newValue, selection)
    })

    private val localeDecimalFormatSymbols = DecimalFormatSymbols(currencyMaskerSettings.locale).apply {
        decimalSeparator = currencyMaskerSettings.decimalSeparator.first()
        groupingSeparator = currencyMaskerSettings.groupingSeparator.first()
    }

    private val currencyFormatter = DecimalFormat().apply {
        minimumIntegerDigits = 1
        maximumIntegerDigits = 2
        minimumFractionDigits = 0
        maximumFractionDigits = 2
        decimalFormatSymbols = localeDecimalFormatSymbols
    }

    override fun onTextChanged(charSequence: CharSequence?, start: Int, count: Int, before: Int, selectionStart: Int) {
        val isDeletion = count == IS_REMOVED

        if (charSequence.isNullOrEmpty()) {
            selection = 0
            text = defaultText
            return
        }

        if (charSequence.length < start) {
            val currentCurrencyValue = if (text.isEmpty()) defaultText else text
            selection = getFormattedCurrencyDecimalLimitLeftIndex(currentCurrencyValue)
            text = currentCurrencyValue
            return
        }

        if (isDeletion) {
            handleDeletion(charSequence, start)
        } else {
            handleAddition(charSequence, start, selectionStart)
        }
    }

    private fun handleAddition(charSequence: CharSequence, start: Int, selectionStart: Int) {
        if ((charSequence[start] == localeDecimalFormatSymbols.decimalSeparator ||
                charSequence[start] == localeDecimalFormatSymbols.groupingSeparator)
        ) {
            val currentCurrencyValue = if (text.isEmpty()) defaultText else text
            selection = getFormattedCurrencyDecimalLimitRightIndex(currentCurrencyValue) -
                currencyPrefixLength -
                currencySuffixLength
            text = currentCurrencyValue
            return
        }
        var rawCurrency = removePrefixAndSuffix(charSequence)
        if (isAddedAfterDecimalPart(rawCurrency)) {
            rawCurrency = replaceWithAddedNumber(rawCurrency, start - currencyPrefixLength)
        }
        val startLength = rawCurrency.length

        var formattedCurrency = applyCurrencyPattern(rawCurrency)
        val endLength = formattedCurrency.length

        val cursorIndex = handleCursorForAddition(
            subtractCurrencyPrefixIfValidOrZero(selectionStart + (endLength - startLength)),
            formattedCurrency
        )

        formattedCurrency = addSuffixAndPrefix(formattedCurrency)
        selection = cursorIndex
        text = formattedCurrency

    }

    private fun handleDeletion(charSequence: CharSequence, start: Int) {
        if (!charSequence.contains(localeDecimalFormatSymbols.decimalSeparator)) {
            onTextMaskedListener(text, start)
            return
        }
        var rawCurrency = removePrefixAndSuffix(charSequence)

        if (isDeletedBeforeDecimalPart(rawCurrency)) {
            rawCurrency = insertZero(rawCurrency, start - currencyPrefixLength)
        }

        val startLength = rawCurrency.length

        var formattedCurrency = applyCurrencyPattern(rawCurrency)
        val endLength = formattedCurrency.length

        val cursorIndex = handleCursorForDeletion(
            subtractCurrencyPrefixIfValidOrZero(start + (endLength - startLength)),
            formattedCurrency,
            endLength - startLength == 0
        )

        formattedCurrency = addSuffixAndPrefix(formattedCurrency)
        selection = cursorIndex
        text = formattedCurrency
    }

    private fun handleCursorForDeletion(selection: Int, formattedCurrency: String, isItemDeleted: Boolean): Int = when {
        selection <= 0 -> getFormattedCurrencyDecimalLimitLeftIndex(formattedCurrency)
        selection >= formattedCurrency.length -> {
            if (isItemDeleted) selection + currencyPrefixLength else selection + currencyPrefixLength - 1
        }
        formattedCurrency[selection - 1] == localeDecimalFormatSymbols.groupingSeparator -> {
            selection + currencyPrefixLength - 1
        }
        selection in (formattedCurrency.length - currencyMaskerSettings.decimalLimit)..formattedCurrency.length -> {
            selection + currencyPrefixLength
        }
        selection > 0 && selection <= formattedCurrency.length -> selection + currencyPrefixLength
        else -> selection - 1
    }

    private fun handleCursorForAddition(selection: Int, formattedCurrency: String): Int = when {
        selection <= 0 -> getFormattedCurrencyDecimalLimitLeftIndex(formattedCurrency)
        selection >= formattedCurrency.length -> getFormattedCurrencyDecimalLimitLeftIndex(formattedCurrency)
        selection in (formattedCurrency.length - currencyMaskerSettings.decimalLimit)..formattedCurrency.length -> {
            if (selection + 1 > formattedCurrency.length) {
                getFormattedCurrencyDecimalLimitLeftIndex(formattedCurrency)
            } else {
                selection + currencyPrefixLength
            }
        }
        selection > 0 && selection <= formattedCurrency.length -> selection + currencyPrefixLength
        else -> selection
    }

    private fun removePrefixAndSuffix(charSequence: CharSequence): CharSequence {
        return if (formattedCurrencyPrefix.isNotBlank() || formattedCurrencySuffix.isNotBlank()) {
            charSequence.removeWithRegex("[$formattedCurrencyPrefix$formattedCurrencySuffix]")
        } else {
            charSequence
        }
    }

    private fun removeGroupingSeparator(charSequence: CharSequence): String {
        return charSequence.removeWithRegex("[${localeDecimalFormatSymbols.groupingSeparator}]")
    }

    private fun applyCurrencyPattern(charSequence: CharSequence): String {
        return try {
            val currencyWithoutGroupSeparator = removeGroupingSeparator(charSequence)
            currencyFormatter.applyPattern(WHOLE_NUMBER_FORMAT)
            val parsedNumber = currencyFormatter.parse(currencyWithoutGroupSeparator)!!
            currencyFormatter.applyPattern(mask.maskPattern)
            currencyFormatter.format(parsedNumber)
        } catch (e: ParseException) {
            e.printStackTrace()
            ""
        }

    }

    private fun subtractCurrencyPrefixIfValidOrZero(cursor: Int): Int {
        return if (cursor - currencyPrefixLength > 0) cursor - currencyPrefixLength else 0
    }

    private fun getFormattedCurrencyDecimalLimitLeftIndex(currencyInput: String): Int {
        return currencyInput.length - currencyMaskerSettings.decimalLimit - 1 + currencyPrefixLength
    }

    private fun getFormattedCurrencyDecimalLimitRightIndex(currencyInput: String): Int {
        return currencyInput.length - currencyMaskerSettings.decimalLimit + currencyPrefixLength
    }

    private fun addSuffixAndPrefix(currencyValue: String): String {
        return "$formattedCurrencyPrefix$currencyValue$formattedCurrencySuffix"
    }

    private fun replaceWithAddedNumber(charSequence: CharSequence, start: Int): String {
        val currencyValue = charSequence.toString()
        if (charSequence.length - 1 <= start) {
            return currencyValue.substring(0, charSequence.length - 1)
        }
        return currencyValue.replaceRange(start, start + 2, charSequence[start].toString())
    }

    private fun insertZero(charSequence: CharSequence, start: Int): String {
        val currencyValue = charSequence.toString()
        return currencyValue.replaceRange(start, start, ZERO)
    }

    private fun isAddedAfterDecimalPart(charSequence: CharSequence): Boolean {
        val decimalValue = charSequence.toString()
            .substringAfter(localeDecimalFormatSymbols.decimalSeparator)
            .filter { it.isDigit() }
        return decimalValue.length >= currencyMaskerSettings.decimalLimit + 1
    }

    private fun isDeletedBeforeDecimalPart(charSequence: CharSequence): Boolean {
        val decimalValue = charSequence.toString()
            .substringAfter(localeDecimalFormatSymbols.decimalSeparator)
            .filter { it.isDigit() }
        return decimalValue.length <= currencyMaskerSettings.decimalLimit - 1
    }

    override fun getTextWithReturnPattern(): String? {
        return null
    }

    companion object {
        private const val NO_DECIMAL_LIMIT = -1

        private const val WHOLE_NUMBER_FORMAT = "#,##0"

        private const val ZERO = "0"

        fun checkIfLimitSafe(currencyDecimalLimit: Int) {
            if (currencyDecimalLimit < NO_DECIMAL_LIMIT) {
                throw IllegalArgumentException("currencyDecimalLimit must be equal or bigger than -1")
            }
        }
    }
}
