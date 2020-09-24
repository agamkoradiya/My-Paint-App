package com.example.mypaint.model

import android.graphics.MaskFilter
import android.graphics.Path

data class FingerPath(
    var color: Int,
    var blur: Boolean,
    var blurEffect: MaskFilter?,
    var strokeWidth: Int,
    var path: Path
)