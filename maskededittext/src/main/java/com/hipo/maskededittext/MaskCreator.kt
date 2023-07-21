package com.hipo.maskededittext

import com.hipo.maskededittext.model.CurrencyMaskerSettings

interface MaskCreator {
    fun create(
        maskPattern: String? = null,
        returnPattern: String? = null,
        currencyMaskerSettings: CurrencyMaskerSettings? = null
    ): Mask
}
