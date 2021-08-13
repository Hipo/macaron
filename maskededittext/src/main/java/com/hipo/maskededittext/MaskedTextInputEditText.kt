package com.hipo.maskededittext

import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.core.content.res.use
import com.google.android.material.textfield.TextInputEditText
import com.hipo.maskededittext.maskers.BaseMasker
import com.hipo.maskededittext.maskers.CreditCardMasker
import com.hipo.maskededittext.maskers.CurrencyMasker
import com.hipo.maskededittext.maskers.DateMasker
import com.hipo.maskededittext.maskers.DateMonthYearMasker
import com.hipo.maskededittext.maskers.IBANMasker
import com.hipo.maskededittext.maskers.Masker
import com.hipo.maskededittext.maskers.PhoneNumberMasker
import com.hipo.maskededittext.maskers.StaticTextMasker
import com.hipo.maskededittext.maskers.TCIdMasker
import com.hipo.maskededittext.masks.CreditCardMask
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

class MaskedTextInputEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : TextInputEditText(context, attrs) {

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
    private var isCursorFixed = false

    var maskPattern: String = ""
    var returnMaskPattern: String = ""

    var maskType: Mask by Delegates.observable<Mask>(UnselectedMask()) { _, _, newValue ->
        setMasker(newValue)
    }

    var currencySettings = CurrencyMaskerSettings()

    var onTextChangedListener: ((String) -> Unit)? = null

    init {
        loadAttrs(attrs)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initMaskedEditText()
    }

    override fun onDetachedFromWindow() {
        textWatcher = null
        super.onDetachedFromWindow()
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        if (isCursorFixed) {
            setSelection(rawText.length, rawText.length)
            return
        }
        super.onSelectionChanged(selStart, selEnd)
    }

    fun updateCurrencyModel(currencyMaskerSettings: CurrencyMaskerSettings) {
        this.currencySettings = currencyMaskerSettings
        maskType = CurrencyMask(currencyMaskerSettings = currencySettings)
    }

    fun setCurrencyText(currency: String) {
        // TODO this is a quick fix, create a generic setter that works with every masker
        masker.takeIf { it is CurrencyMasker }?.onTextChanged(currency, 0, currency.length, 0, selectionStart)
    }

    fun loadAttrs(attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.MaskedInputLayout).use { attrs ->
            isCursorFixed = attrs.getBoolean(R.styleable.MaskedInputLayout_isCursorFixed, false)

            currencySettings.decimalSeparator =
                attrs.getString(R.styleable.MaskedInputLayout_decimalSeparator)?.first() ?: COMMA

            currencySettings.groupingSeparator =
                attrs.getString(R.styleable.MaskedInputLayout_groupingSeparator)?.first() ?: DOT

            maskPattern = attrs.getString(R.styleable.MaskedInputLayout_maskPattern)
                ?: context.getString(R.string.currency_mask_pattern)

            returnMaskPattern = attrs.getString(R.styleable.MaskedInputLayout_returnPattern).orEmpty()

            currencySettings.prefix = attrs.getString(R.styleable.MaskedInputLayout_currencyPrefix).orEmpty()
            currencySettings.suffix = attrs.getString(R.styleable.MaskedInputLayout_currencySuffix).orEmpty()
            currencySettings.decimalLimit = attrs.getInteger(
                R.styleable.MaskedInputLayout_currencyDecimalLimit,
                CurrencyMaskerSettings.DEFAULT_DECIMAL_LIMIT
            ).also { decimalLimit -> CurrencyMasker.checkIfLimitSafe(decimalLimit) }

            maskType = Mask.Type.values()[
                attrs.getInt(R.styleable.MaskedInputLayout_maskType, Mask.Type.UNSELECTED.ordinal)
            ].create(maskPattern, returnMaskPattern, currencySettings)
        }
        masker?.let {
            inputType = it.inputType
            keyListener = it.keyListener
        }
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

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        post {
            masker?.onFocusChanged(focused, direction, previouslyFocusedRect)
        }
    }

    private fun setMasker(mask: Mask) {
        masker = when (mask) {
            is UnselectedMask -> return
            is CustomMask -> handleCustomMask(mask)
            is StaticTextMask -> handleStaticTextMask(mask)
            is CurrencyMask -> CurrencyMasker(mask, ::setEditTextWithoutTriggerListener, ::updateSelection, currencySettings)
            is CreditCardMask -> CreditCardMasker(mask, ::setEditTextWithoutTriggerListener)
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
            mask.maskPattern.substringBefore(HASH)
        }
        setText(defaultText)
    }

    fun setEditTextWithoutTriggerListener(newText: String, selection: Int? = null) {
        removeTextChangedListener(textWatcher)
        setText(newText)
        setSelection(selection ?: newText.length)
        textWatcher?.let { textWatcher ->
            addTextChangedListener(textWatcher)
        }
        onTextChangedListener?.invoke(newText)
    }

    private fun updateSelection(selection: Int? = null) {
        setSelection(selection ?: text?.length ?: 0)
    }

    private fun handleCustomMask(mask: Mask): Masker = when {
        maskPattern.contains(HASH).not() -> {
            throw Exception("$logTag: ${context.getString(R.string.exception_mask_hash)}")
        }
        returnMaskPattern.contains(HASH).not() -> {
            throw Exception("$logTag: ${context.getString(R.string.exception_return_hash)}")
        }
        maskPattern.count { it == HASH } != returnMaskPattern.count { it == HASH } -> {
            throw Exception("$logTag: ${context.getString(R.string.exception_hash_count)}")
        }
        else -> {
            Masker(mask, ::setEditTextWithoutTriggerListener)
        }
    }

    private fun handleStaticTextMask(mask: Mask): StaticTextMasker = when {
        maskPattern.isBlank() -> {
            throw Exception("$logTag: ${context.getString(R.string.exception_mask_hash)}")
        }
        else -> StaticTextMasker(mask, ::setEditTextWithoutTriggerListener)
    }

    companion object {
        private val logTag = this::class.java.simpleName
        private const val HASH = '#'
        private const val COMMA = ','
        private const val DOT = '.'
    }
}
