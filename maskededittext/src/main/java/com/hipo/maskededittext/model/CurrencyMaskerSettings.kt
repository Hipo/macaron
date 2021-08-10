package com.hipo.maskededittext.model

import java.util.Locale

data class CurrencyMaskerSettings(
    var decimalLimit: Int = DEFAULT_DECIMAL_LIMIT,
    var decimalSeparator: Char = COMMA,
    var groupingSeparator: Char = DOT,
    var prefix: String = "",
    var suffix: String = "",
    var locale: Locale = Locale.getDefault()
) {
    companion object {
        const val DEFAULT_DECIMAL_LIMIT = 2
        private const val DOT = '.'
        private const val COMMA = ','
    }
}
