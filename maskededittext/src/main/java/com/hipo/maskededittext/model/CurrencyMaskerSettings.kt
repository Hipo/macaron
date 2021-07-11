package com.hipo.maskededittext.model

import java.util.Locale

data class CurrencyMaskerSettings(
    var decimalLimit: Int = DEFAULT_DECIMAL_LIMIT,
    var decimalSeparator: String = COMMA,
    var groupingSeparator: String = DOT,
    var prefix: String = "",
    var suffix: String = "",
    var locale: Locale = Locale.getDefault()
) {
    companion object {
        const val DEFAULT_DECIMAL_LIMIT = 2
        private const val DOT = "."
        private const val COMMA = ","

        fun create(
            suffix: String,
            prefix: String,
            groupingSeparator: String,
            decimalSeparator: String
        ): CurrencyMaskerSettings {
            return CurrencyMaskerSettings(
                decimalLimit = DEFAULT_DECIMAL_LIMIT,
                locale = Locale.getDefault(),
                suffix = suffix,
                prefix = prefix,
                groupingSeparator = groupingSeparator,
                decimalSeparator = decimalSeparator
            )
        }
    }
}
