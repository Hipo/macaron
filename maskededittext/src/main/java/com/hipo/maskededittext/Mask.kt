package com.hipo.maskededittext

import com.hipo.maskededittext.masks.CreditCardMask
import com.hipo.maskededittext.masks.CurrencyMask
import com.hipo.maskededittext.masks.CustomMask
import com.hipo.maskededittext.masks.DateMask
import com.hipo.maskededittext.masks.DateMonthYearMask
import com.hipo.maskededittext.masks.IBANMask
import com.hipo.maskededittext.masks.PhoneMask
import com.hipo.maskededittext.masks.SSNMask
import com.hipo.maskededittext.masks.StaticTextMask
import com.hipo.maskededittext.masks.TCIdMask
import com.hipo.maskededittext.masks.UnselectedMask
import com.hipo.maskededittext.model.CurrencyMaskerSettings
import com.hipo.maskededittext.utils.extensions.specialChars


abstract class Mask {
    abstract val maskPattern: String
    abstract val returnPattern: String

    abstract fun getParsedText(maskedText: String): String?
    abstract fun isValidToParse(maskedText: String): Boolean
    abstract fun filterMaskedText(maskedText: String): String

    fun calculateUpcomingMaskDistance(start: Int): Int {
        if (start == 0) {
            return 0
        }
        var index = start
        with(this) {
            while (index in maskPattern.indices && specialChars.contains(maskPattern[index])) {
                index++
            }
        }
        return index
    }

    fun calculateCurrentMaskDistance(start: Int): Int {
        if (start == 0) {
            return 0
        }
        var index = start
        with(this) {
            while (index in maskPattern.indices && specialChars.contains(maskPattern[index])) {
                index--
            }
        }
        return index
    }

    // Enum class order must be the same as attrs.xml order
    enum class Type : MaskCreator {
        DATE {
            override fun create(
                maskPattern: String?,
                returnPattern: String?,
                currencyMaskerSettings: CurrencyMaskerSettings?
            ): Mask = DateMask()
        },
        PHONE {
            override fun create(
                maskPattern: String?,
                returnPattern: String?,
                currencyMaskerSettings: CurrencyMaskerSettings?
            ): Mask = PhoneMask()
        },
        SSN {
            override fun create(
                maskPattern: String?,
                returnPattern: String?,
                currencyMaskerSettings: CurrencyMaskerSettings?
            ): Mask = SSNMask()
        },
        CURRENCY {
            override fun create(
                maskPattern: String?,
                returnPattern: String?,
                currencyMaskerSettings: CurrencyMaskerSettings?
            ): Mask = CurrencyMask(maskPattern.orEmpty(), currencyMaskerSettings)
        },
        CUSTOM {
            override fun create(
                maskPattern: String?,
                returnPattern: String?,
                currencyMaskerSettings: CurrencyMaskerSettings?
            ): Mask = CustomMask(maskPattern.orEmpty(), returnPattern.orEmpty())
        },
        STATIC_TEXT {
            override fun create(
                maskPattern: String?,
                returnPattern: String?,
                currencyMaskerSettings: CurrencyMaskerSettings?
            ): Mask = StaticTextMask(maskPattern!!)
        },
        CREDIT_CARD {
            override fun create(
                maskPattern: String?,
                returnPattern: String?,
                currencyMaskerSettings: CurrencyMaskerSettings?
            ): Mask = CreditCardMask()
        },
        DATE_MONTH_YEAR {
            override fun create(
                maskPattern: String?,
                returnPattern: String?,
                currencyMaskerSettings: CurrencyMaskerSettings?
            ): Mask = DateMonthYearMask()
        },
        IBAN {
            override fun create(
                maskPattern: String?,
                returnPattern: String?,
                currencyMaskerSettings: CurrencyMaskerSettings?
            ): Mask = IBANMask()
        },
        TC_ID {
            override fun create(
                maskPattern: String?,
                returnPattern: String?,
                currencyMaskerSettings: CurrencyMaskerSettings?
            ): Mask = TCIdMask()
        },
        UNSELECTED {
            override fun create(
                maskPattern: String?,
                returnPattern: String?,
                currencyMaskerSettings: CurrencyMaskerSettings?
            ): Mask = UnselectedMask()
        },
    }
}
