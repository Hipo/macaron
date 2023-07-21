package com.hipo.maskededittext.masks

import com.hipo.maskededittext.Mask
import com.hipo.maskededittext.utils.extensions.isValidIdentificationNumber

class TCIdMask : Mask() {

    override val maskPattern: String
        get() = "###########"

    override val returnPattern: String
        get() = "###########"

    override fun getParsedText(maskedText: String): String? {
        return filterMaskedText(maskedText).takeIf { isValidToParse(maskedText) }
    }

    override fun isValidToParse(maskedText: String): Boolean {
        return filterMaskedText(maskedText).isValidIdentificationNumber()
    }

    override fun filterMaskedText(maskedText: String): String {
        return maskedText.filter { it.isDigit() }
    }
}
