package com.hipo.maskededittext.masks

import com.hipo.maskededittext.Mask
import com.hipo.maskededittext.utils.extensions.isValidDateMonthYear
import java.text.SimpleDateFormat

class DateMonthYearMask : Mask() {

    override val maskPattern: String
        get() = "##/##"

    override val returnPattern: String
        get() = "##-##"

    override fun getParsedText(maskedText: String): String? {
        return if (isValidToParse(maskedText)) {
            SimpleDateFormat(OUTPUT_DATE_FORMAT).format(SimpleDateFormat(INPUT_DATE_FORMAT).parse(maskedText))
        } else {
            null
        }
    }

    override fun isValidToParse(maskedText: String): Boolean {
        return filterMaskedText(maskedText).isValidDateMonthYear()
    }

    override fun filterMaskedText(maskedText: String): String {
        return maskedText.filter { it.isDigit() }
    }

    companion object {
        private const val INPUT_DATE_FORMAT = "MM/yy"
        private const val OUTPUT_DATE_FORMAT = "MM-yy"
    }
}
