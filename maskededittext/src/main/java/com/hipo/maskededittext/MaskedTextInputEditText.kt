package com.hipo.maskededittext

import android.content.Context
import android.text.Editable
import android.text.InputType.TYPE_CLASS_NUMBER
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.core.content.res.use
import com.google.android.material.textfield.TextInputEditText
import com.hipo.maskededittext.maskers.BaseMasker
import com.hipo.maskededittext.maskers.CurrencyMasker
import com.hipo.maskededittext.maskers.DateMasker
import com.hipo.maskededittext.maskers.DateMonthYearMasker
import com.hipo.maskededittext.maskers.IBANMasker
import com.hipo.maskededittext.maskers.Masker
import com.hipo.maskededittext.maskers.PhoneNumberMasker
import com.hipo.maskededittext.maskers.StaticTextMasker
import com.hipo.maskededittext.maskers.TCIdMasker
import com.hipo.maskededittext.masks.CurrencyMask
import com.hipo.maskededittext.masks.CustomMask
import com.hipo.maskededittext.masks.DateMask
import com.hipo.maskededittext.masks.DateMonthYearMask
import com.hipo.maskededittext.masks.IBANMask
import com.hipo.maskededittext.masks.PhoneMask
import com.hipo.maskededittext.masks.StaticTextMask
import com.hipo.maskededittext.masks.TCIdMask
import com.hipo.maskededittext.masks.UnselectedMask
import com.hipo.maskededittext.model.CurrencyMaskerSettings
import kotlin.properties.Delegates

class MaskedTextInputEditText : TextInputEditText {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initAttributes(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initAttributes(attrs, defStyle)
    }

    val rawText: String
        get() = text.toString()

    val parsedText: String
        get() {
            return if (maskType is UnselectedMask) {
                text.toString()
            } else {
                maskType.getParsedText(text.toString()).orEmpty()
            }
        }

    var isEnabled: Boolean? = true
        set(value) {
            field = value
            if (value == true) requestFocus() else clearFocus()
        }

    val isValid: Boolean
        get() {
            if (maskType is UnselectedMask) {
                throw RuntimeException("You must initialize the maskType attr.")
            }
            return maskType.isValidToParse(text.toString())
        }

    private var textWatcher: TextWatcher? = null
    private var masker: BaseMasker? = null

    private var maskPattern: String = ""
    private var returnMaskPattern: String = ""

    private var maskType: Mask by Delegates.observable<Mask>(UnselectedMask()) { _, _, newValue ->
        setMasker(newValue)
    }

    private var currencySettings = CurrencyMaskerSettings()

    var onTextChangedListener: ((String) -> Unit)? = null

    private fun initAttributes(attrs: AttributeSet?, defStyle: Int = -1) {
        context.obtainStyledAttributes(attrs, R.styleable.MaskedTextInputEditText, defStyle, 0).use { typedArray ->
            currencySettings.decimalSeparator =
                typedArray.getString(R.styleable.MaskedTextInputEditText_decimalSeparator) ?: COMMA
            currencySettings.groupingSeparator =
                typedArray.getString(R.styleable.MaskedTextInputEditText_groupingSeparator) ?: DOT
            maskPattern = typedArray.getString(R.styleable.MaskedTextInputEditText_maskPattern)
                ?: context.getString(R.string.currency_mask_pattern)
            returnMaskPattern = typedArray.getString(R.styleable.MaskedTextInputEditText_returnPattern).orEmpty()
            currencySettings.prefix = typedArray.getString(R.styleable.MaskedTextInputEditText_currencySuffix).orEmpty()
            currencySettings.suffix = typedArray.getString(R.styleable.MaskedTextInputEditText_currencyPrefix).orEmpty()
            currencySettings.decimalLimit = typedArray.getInteger(
                R.styleable.MaskedTextInputEditText_currencyDecimalLimit,
                CurrencyMaskerSettings.DEFAULT_DECIMAL_LIMIT
            ).also { decimalLimit -> CurrencyMasker.checkIfLimitSafe(decimalLimit) }
            maskType = Mask.Type.values()[
                typedArray.getInt(R.styleable.MaskedTextInputEditText_maskType, Mask.Type.UNSELECTED.ordinal)
            ].create(maskPattern, returnMaskPattern)
        }
        inputType = masker?.inputType ?: TYPE_CLASS_NUMBER
        keyListener = masker?.keyListener
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initMaskedEditText()
    }

    private fun initMaskedEditText() {
        if (masker?.mask is UnselectedMask) return
        initTextWatcher()
    }

    private fun initTextWatcher() {
        textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                masker?.beforeTextChanged(s ?: "", start, count, after, selectionStart)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                masker?.onTextChanged(s, start, count, before, selectionStart)
            }
        }
        addTextChangedListener(textWatcher)
    }

    private fun setMasker(mask: Mask) {
        masker = when (mask) {
            is UnselectedMask -> return
            is CustomMask -> handleCustomMask(mask)
            is StaticTextMask -> handleStaticTextMask(mask)
            is CurrencyMask -> CurrencyMasker(mask, ::setEditTextWithoutTriggerListener, currencySettings)
            is IBANMask -> IBANMasker(mask, ::setEditTextWithoutTriggerListener)
            is PhoneMask -> PhoneNumberMasker(mask, ::setEditTextWithoutTriggerListener)
            is DateMonthYearMask -> DateMonthYearMasker(mask, ::setEditTextWithoutTriggerListener)
            is DateMask -> DateMasker(mask, ::setEditTextWithoutTriggerListener)
            is TCIdMask -> TCIdMasker(mask, ::setEditTextWithoutTriggerListener)
            else -> Masker(mask, ::setEditTextWithoutTriggerListener)
        }
        setDefaultText(mask)
        requestFocus()
        setSelection(text?.length ?: 0)
    }

    private fun setDefaultText(mask: Mask) {
        val defaultText = if (masker is CurrencyMasker) {
            "${currencySettings.prefix} 0${currencySettings.decimalSeparator}00 ${currencySettings.suffix}".trim()
        } else {
            mask.maskPattern.substringBefore(POUND)
        }
        setText(defaultText)
    }

    private fun setEditTextWithoutTriggerListener(newText: String, selection: Int?) {
        removeTextChangedListener(textWatcher)
        setText(newText)
        setSelection(selection ?: newText.length)
        textWatcher?.let { textWatcher ->
            addTextChangedListener(textWatcher)
        }
        onTextChangedListener?.invoke(newText)
    }

    private fun handleCustomMask(mask: Mask): Masker = when {
        maskPattern.contains(POUND).not() -> {
            throw Exception("$LOG_TAG: ${context.getString(R.string.exception_mask_pound)}")
        }
        returnMaskPattern.contains(POUND).not() -> {
            throw Exception("$LOG_TAG: ${context.getString(R.string.exception_return_pound)}")
        }
        maskPattern.count { it == POUND } != returnMaskPattern.count { it == POUND } -> {
            throw Exception("$LOG_TAG: ${context.getString(R.string.exception_pound_count)}")
        }
        else -> {
            Masker(mask, ::setEditTextWithoutTriggerListener)
        }
    }

    private fun handleStaticTextMask(mask: Mask): StaticTextMasker = when {
        maskPattern.isBlank() -> {
            throw Exception("$LOG_TAG: ${context.getString(R.string.exception_mask_pound)}")
        }
        else -> StaticTextMasker(mask, ::setEditTextWithoutTriggerListener)
    }

    override fun onDetachedFromWindow() {
        textWatcher = null
        super.onDetachedFromWindow()
    }

    fun updateCurrencyModel(currencyMaskerSettings: CurrencyMaskerSettings) {
        this.currencySettings = currencyMaskerSettings
        maskType = CurrencyMask()
    }

    fun setCurrencyText(currency: String) {
        // TODO this is a quick fix, create a generic setter that works with every masker
        masker.takeIf { it is CurrencyMasker }?.onTextChanged(currency, 0, currency.length, 0, selectionStart)
    }

    companion object {
        private val LOG_TAG = this::class.java.simpleName
        private const val POUND = '#'
        private const val COMMA = ","
        private const val DOT = "."
    }
}
