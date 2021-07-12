package com.hipo.maskededittext

import android.content.Context
import android.os.Parcelable
import android.text.InputFilter
import android.util.AttributeSet
import android.util.SparseArray
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.widget.addTextChangedListener
import com.hipo.maskededittext.databinding.CustomMaskedInputLayoutBinding
import com.hipo.maskededittext.model.CurrencyMaskerSettings
import com.hipo.maskededittext.model.CustomInputState
import com.hipo.maskededittext.utils.extensions.dp
import com.hipo.maskededittext.utils.extensions.setTextAndVisibility
import com.hipo.maskededittext.utils.extensions.show
import com.hipo.maskededittext.utils.extensions.showKeyboard
import com.hipo.maskededittext.utils.viewbinding.viewBinding

class MaskedTextInputLayout @JvmOverloads constructor(
    context: Context,
    private val attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private val binding = viewBinding(CustomMaskedInputLayoutBinding::inflate)

    val editText = binding.textInputEditText

    var endIconClick: (() -> Unit)? = null

    var text: String
        get() = binding.textInputEditText.text.toString()
        set(value) {
            binding.textInputEditText.setText(value)
        }

    var error: String?
        get() = binding.errorTextView.text.toString()
        set(value) {
            binding.errorTextView.setTextAndVisibility(value)
        }

    var helper: String?
        get() = binding.helperTextView.text.toString()
        set(value) {
            binding.helperTextView.setTextAndVisibility(value)
        }

    var inputType: Int?
        get() = binding.textInputEditText.inputType
        set(value) {
            binding.textInputEditText.apply {
                if (value != null && value != -1) {
                    inputType = value
                }
            }
        }

    var maxLength: Int? = null
        set(value) {
            field = value
            binding.textInputEditText.apply {
                if (value != null && value != -1) {
                    filters = arrayOf(InputFilter.LengthFilter(value))
                }
            }
        }

    var hint: String?
        get() = binding.textInputLayout.hint.toString()
        set(value) {
            binding.textInputLayout.hint = value
        }

    var maskPattern: String = ""
        set(value) {
            field = value
            binding.textInputEditText.maskPattern = value
        }

    var returnMaskPattern: String = ""
        set(value) {
            field = value
            binding.textInputEditText.returnMaskPattern = value
        }

    var currencyDecimalLimit = CurrencyMaskerSettings.DEFAULT_DECIMAL_LIMIT
        set(value) {
            field = value
            binding.textInputEditText.currencySettings.decimalLimit = value
        }

    var maskType: Mask? = null
        set(value) {
            field = value
            if (value != null) {
                binding.textInputEditText.maskType = value
            }
        }

    var onTextChanged: ((text: String) -> Unit)? = null

    init {
        loadAttrs()
        initUi()
    }

    private fun loadAttrs() {
        context.obtainStyledAttributes(attrs, R.styleable.MaskedInputLayout).use { attrs ->
            text = attrs.getString(R.styleable.MaskedInputLayout_android_text).orEmpty()
            hint = attrs.getString(R.styleable.MaskedInputLayout_android_hint)
            error = attrs.getString(R.styleable.MaskedInputLayout_error)
            helper = attrs.getString(R.styleable.MaskedInputLayout_helper)
            inputType = attrs.getInteger(R.styleable.MaskedInputLayout_android_inputType, -1)
            maxLength = attrs.getInteger(R.styleable.MaskedInputLayout_android_maxLength, -1)
        }
    }

    private fun initUi() {
        binding.textInputEditText.addTextChangedListener {
            onTextChanged?.invoke(text)
        }

        binding.iconImageView.setOnClickListener {
            endIconClick?.invoke()
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return CustomInputState(super.onSaveInstanceState(), text)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        (state as? CustomInputState)?.run {
            super.onRestoreInstanceState(superState)
            this@MaskedTextInputLayout.text = text
        }
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>?) {
        super.dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>?) {
        super.dispatchThawSelfOnly(container)
    }

    fun showKeyboard() {
        binding.textInputEditText.showKeyboard()
    }

    fun showEndIcon(@DrawableRes drawableRes: Int, @ColorRes tint: Int? = null) {
        with(binding) {
            iconImageView.setImageDrawable(ContextCompat.getDrawable(context, drawableRes))
            tint?.let {
                iconImageView.imageTintList = ContextCompat.getColorStateList(context, it)
            }
            textInputEditText.setPadding(
                textInputEditText.paddingLeft,
                textInputEditText.paddingTop,
                context.resources.getDimension(R.dimen.spacing_40).toInt().dp,
                textInputEditText.paddingBottom
            )
            iconImageView.show()
        }
    }
}
