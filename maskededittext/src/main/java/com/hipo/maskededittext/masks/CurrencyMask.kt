package com.hipo.maskededittext.masks

import com.hipo.maskededittext.Mask

class CurrencyMask(override val maskPattern: String = "#,##0.00") : Mask() {

    override val returnPattern: String
        get() = "#,##0.00"

    override fun getParsedText(maskedText: String): String? {
        val filteredText = filterMaskedText(maskedText).takeIf { isValidToParse(maskedText) } ?: "0,00"
        return filteredText.filter { it.isDigit() || it == '.' }
    }

    override fun isValidToParse(maskedText: String): Boolean {
        return true
    }

    override fun filterMaskedText(maskedText: String): String {
        if (maskedText.isBlank()) return "0,00"
        return maskedText.run {
            if (last() == '.') substring(0, length - 1) else this
        }
    }
}
