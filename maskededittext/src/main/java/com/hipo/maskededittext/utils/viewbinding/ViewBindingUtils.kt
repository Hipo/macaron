package com.hipo.maskededittext.utils.viewbinding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

internal inline fun <T : ViewBinding> ViewGroup.viewBinding(
    crossinline inflater: (LayoutInflater, ViewGroup) -> T
): T {
    return inflater.invoke(LayoutInflater.from(context), this)
}
