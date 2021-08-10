package com.hipo.maskededittext.masks

import com.hipo.maskededittext.Mask
import com.hipo.maskededittext.model.CurrencyMaskerSettings

class CurrencyMask(
    override val maskPattern: String = DEFAULT_CURRENCY_MASK,
    private val currencyMaskerSettings: CurrencyMaskerSettings?
) : Mask() {

    override val returnPattern: String
        get() = DEFAULT_CURRENCY_MASK

    private val defaultCurrencyValue
        get() = "0${currencyMaskerSettings?.decimalSeparator}00"

    override fun getParsedText(maskedText: String): String? {
        val filteredText = filterMaskedText(maskedText).takeIf { isValidToParse(maskedText) } ?: defaultCurrencyValue
        return filteredText.filter { it.isDigit() || it == currencyMaskerSettings?.decimalSeparator }
    }

    override fun isValidToParse(maskedText: String): Boolean {
        return true
    }

    override fun filterMaskedText(maskedText: String): String {
        if (maskedText.isBlank()) return defaultCurrencyValue
        return maskedText
    }

    companion object {
        private const val DEFAULT_CURRENCY_MASK = "#,##0.00"
    }
}
