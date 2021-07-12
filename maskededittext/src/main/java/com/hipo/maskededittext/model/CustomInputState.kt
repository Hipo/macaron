package com.hipo.maskededittext.model

import android.os.Parcelable
import android.view.View

class CustomInputState(
    superState: Parcelable?,
    val text: String
) : View.BaseSavedState(superState)
