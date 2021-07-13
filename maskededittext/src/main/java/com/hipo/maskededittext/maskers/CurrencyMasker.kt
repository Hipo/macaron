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

        if (charSequence.length <= start) {
            val currentCurrencyValue = if (text.isEmpty()) defaultText else text
            selection = getFormattedCurrencyDecimalLimitLeftIndex(currentCurrencyValue)
            text = currentCurrencyValue
            return
        }

        if (!charSequence.contains(localeDecimalFormatSymbols.decimalSeparator) && isDeletion) {
            onTextMaskedListener(text, start)
            return
        }

        if ((charSequence[start] == localeDecimalFormatSymbols.decimalSeparator ||
                charSequence[start] == localeDecimalFormatSymbols.groupingSeparator) && !isDeletion
        ) {
            val currentCurrencyValue = if (text.isEmpty()) defaultText else text
            selection =
                getFormattedCurrencyDecimalLimitRightIndex(currentCurrencyValue) - currencyPrefixLength - currencySuffixLength
            text = currentCurrencyValue
            return
        }

        val inputWithoutPrefixAndSuffix =
            charSequence.removeWithRegex("[$formattedCurrencyPrefix$formattedCurrencySuffix]")
        val startLength = inputWithoutPrefixAndSuffix.length

        try {
            val currencyWithoutGroupSeparator =
                inputWithoutPrefixAndSuffix.removeWithRegex("[${localeDecimalFormatSymbols.groupingSeparator}]")

            currencyFormatter.applyPattern(WHOLE_NUMBER_FORMAT)
            val parsedNumber = currencyFormatter.parse(currencyWithoutGroupSeparator)!!
            currencyFormatter.applyPattern(mask.maskPattern)
            var formattedCurrency = currencyFormatter.format(parsedNumber)

            val endLength = formattedCurrency.length
            val cursorIndex = handleCursor(
                subtractCurrencyPrefixIfValidOrZero(selectionStart + (endLength - startLength)),
                formattedCurrency,
                isDeletion
            )

            formattedCurrency = addSuffixAndPrefix(formattedCurrency)
            selection = cursorIndex
            text = formattedCurrency
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    private fun handleCursor(selection: Int, formattedCurrency: String, isDeletion: Boolean): Int = when {
        selection <= 0 -> getFormattedCurrencyDecimalLimitLeftIndex(formattedCurrency)
        selection >= formattedCurrency.length -> {
            if (isDeletion) {
                selection - 1 + currencyPrefixLength
            } else {
                getFormattedCurrencyDecimalLimitLeftIndex(formattedCurrency)
            }
        }
        isDeletion && (formattedCurrency[selection - 1] == localeDecimalFormatSymbols.groupingSeparator) -> {
            selection + currencyPrefixLength - 1
        }
        selection in (formattedCurrency.length - currencyMaskerSettings.decimalLimit)..formattedCurrency.length -> {
            if (isDeletion) {
                selection - 1 + currencyPrefixLength
            } else {
                if (selection + 1 >= formattedCurrency.length) {
                    getFormattedCurrencyDecimalLimitLeftIndex(formattedCurrency)
                } else {
                    selection + 1 + currencyPrefixLength
                }
            }
        }
        selection > 0 && selection <= formattedCurrency.length -> selection + currencyPrefixLength
        else -> if (isDeletion) selection - 1 else selection
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

    override fun getTextWithReturnPattern(): String? {
        return null
    }

    companion object {
        private const val NO_DECIMAL_LIMIT = -1

        private const val WHOLE_NUMBER_FORMAT = "#,##0"

        fun checkIfLimitSafe(currencyDecimalLimit: Int) {
            if (currencyDecimalLimit < NO_DECIMAL_LIMIT) {
                throw IllegalArgumentException("currencyDecimalLimit must be equal or bigger than -1")
            }
        }
    }
}
