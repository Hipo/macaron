package com.hipo.maskededittext.utils.extensions

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible

internal fun TextView.setTextAndVisibility(text: String?) {
    setText(text)
    isVisible = text.isNullOrEmpty().not()
}

internal fun View.show() {
    this.visibility = View.VISIBLE
}

internal fun View.hide() {
    this.visibility = View.GONE
}

internal fun View.invisible() {
    this.visibility = View.INVISIBLE
}

internal fun View.hideKeyboard(): Boolean {
    clearFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    return imm.hideSoftInputFromWindow(windowToken, 0)
}

internal fun EditText.showKeyboard(): Boolean {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    return imm.showSoftInput(this, 0)
}
