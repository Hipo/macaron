package com.hipo.maskededittext.utils.extensions

import com.hipo.maskededittext.Mask
import com.hipo.maskededittext.masks.CreditCardMask
import com.hipo.maskededittext.masks.IBANMask

//region Regex Patterns

private const val ibanRegexPattern = "^(TR[0-9]{24})\$"
private const val tcIdRegexPattern = "^\\d{11}\$"
private const val dateMonthYearPattern = "^\\d{4}\$"
private const val datePattern = "^\\d{8}\$"

//endregion

private const val MONTH_FIELD_UPPER_LIMIT = "12"
private const val MONTH_FIELD_LOWER_LIMIT = "01"

private const val DAY_FIELD_UPPER_LIMIT = "31"
private const val DAY_FIELD_LOWER_LIMIT = "01"

internal val specialChars = listOf('(', ')', '-', ' ', '/')

internal fun String.trimAllSpaces() = this.filter { !it.isWhitespace() }

internal fun String.removeWithRegex(pattern: String) = replace(pattern.toRegex(), "").trim()

internal fun CharSequence.removeWithRegex(pattern: String) = replace(pattern.toRegex(), "").trim()

//region Validators

internal fun String?.isValidIban() = this?.matches(Regex(ibanRegexPattern)) ?: false

internal fun String?.isValidIdentificationNumber() = this?.matches(Regex(tcIdRegexPattern)) ?: false

internal fun String?.isValidDateMonthYear() = this?.matches(Regex(dateMonthYearPattern)) ?: false

internal fun String?.isValidDate() = this?.matches(Regex(datePattern)) ?: false

//endregion

//region Formatters

internal fun String.formatAsIban(): String {
    val ibanNumbers = filter { it.isDigit() }
    val formattedIban = if (ibanNumbers.length > IBANMask.IBAN_NUMBERS_LENGTH) {
        ibanNumbers.substring(0, IBANMask.IBAN_NUMBERS_LENGTH)
    } else {
        ibanNumbers
    }.prependIndent(IBANMask.IBAN_PREFIX)
    return formattedIban.replace("(\\w{4})".toRegex(), "$1 ").trim()
}

internal fun String.formatAsCreditCard(): String {
    val cardNumber = filter { it.isDigit() }
    val formattedIban = if (cardNumber.length > CreditCardMask.CREDIT_CARD_NUMBER_LENGTH) {
        cardNumber.substring(0, CreditCardMask.CREDIT_CARD_NUMBER_LENGTH)
    } else {
        cardNumber
    }
    return formattedIban.replace("(\\w{4})".toRegex(), "$1 ").trim()
}

internal fun String.formatAsDateMonthYear(year: Int, mask: Mask): String {
    var dateMonthYear = filter { it.isDigit() }
    if (dateMonthYear.isEmpty()) return ""

    val monthField: String
    var yearField: String

    if (dateMonthYear[0].digitToInt() > 1) {
        monthField = "0${dateMonthYear[0]}"
        dateMonthYear.drop(1)
    } else {
        if (dateMonthYear.length < 2) return "${dateMonthYear[0]}".formatAsMask(mask)
        monthField = when {
            dateMonthYear.take(2).toInt() >= 12 -> MONTH_FIELD_UPPER_LIMIT
            dateMonthYear.take(2) == "00" -> MONTH_FIELD_LOWER_LIMIT
            else -> dateMonthYear.take(2)
        }
        dateMonthYear.drop(2)
    }.also { dateMonthYear = it }

    if (dateMonthYear.isEmpty()) return monthField.formatAsMask(mask)

    yearField = dateMonthYear

    return "$monthField$yearField".formatAsMask(mask)
}

internal fun String.formatAsDate(mask: Mask): String {
    var date = filter { it.isDigit() }
    if (date.isEmpty()) return ""

    val dayField: String
    val monthField: String
    val yearField: String

    if (date[0].digitToInt() > 3) {
        dayField = "0${date[0]}"
        date.drop(1)
    } else {
        if (date.length < 2) return "${date[0]}".formatAsMask(mask)
        dayField = when {
            date.take(2).toInt() >= 31 -> DAY_FIELD_UPPER_LIMIT
            date.take(2) == "00" -> DAY_FIELD_LOWER_LIMIT
            else -> date.take(2)
        }
        date.drop(2)
    }.also { date = it }

    if (date.isEmpty()) return dayField.formatAsMask(mask)

    if (date[0].digitToInt() > 1) {
        monthField = "0${date[0]}"
        date.drop(1)
    } else {
        if (date.length < 2) return "$dayField${date[0]}".formatAsMask(mask)
        monthField = when {
            date.take(2).toInt() >= 12 -> MONTH_FIELD_UPPER_LIMIT
            date.take(2) == "00" -> MONTH_FIELD_LOWER_LIMIT
            else -> date.take(2)
        }
        date.drop(2)
    }.also { date = it }

    if (date.isEmpty()) return "$dayField$monthField".formatAsMask(mask)

    yearField = date.take(4)

    return "$dayField$monthField$yearField".formatAsMask(mask)
}

internal fun String.formatAsMask(mask: Mask): String {
    val rawInput = filter { it.isDigit() }
    var formattedInput = ""
    var index = 0
    var nestedIndex = 0
    val pattern = mask.maskPattern

    pattern.forEach { patternChar ->
        when {
            specialChars.any { it == patternChar } -> {
                formattedInput += patternChar
                ++nestedIndex
            }
            else -> {
                formattedInput += rawInput[index]
                ++index
                ++nestedIndex
                if (index >= rawInput.length) {
                    return formattedInput
                }
            }
        }
    }
    return formattedInput
}

internal fun String.formatAsTcId(mask: Mask): String {
    val tcId = filter { it.isDigit() }
    return if (tcId.length > mask.maskPattern.length) {
        tcId.substring(0, mask.maskPattern.length)
    } else {
        tcId
    }
}

//endregion
