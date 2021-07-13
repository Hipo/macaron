package com.hipo.maskededittext.masks

import com.hipo.maskededittext.Mask
import com.hipo.maskededittext.utils.extensions.isValidIban

class IBANMask : Mask() {

    override val maskPattern: String
        get() = "TR## #### #### #### #### #### ##"

    override val returnPattern: String
        get() = "TR########################"

    override fun getParsedText(maskedText: String): String? {
        return filterMaskedText(maskedText).takeIf { isValidToParse(maskedText) }
    }

    override fun isValidToParse(maskedText: String): Boolean {
        return filterMaskedText(maskedText).isValidIban()
    }

    override fun filterMaskedText(maskedText: String): String {
        return maskedText.filter { it.isDigit() }.prependIndent(IBAN_PREFIX)
    }

    companion object {
        const val IBAN_PREFIX = "TR"
        const val IBAN_NUMBERS_LENGTH = 24
    }
}
