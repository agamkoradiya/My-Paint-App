package com.example.mypaint.menusheet

import android.graphics.BlurMaskFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mypaint.R
import com.example.mypaint.customview.PaintView
import com.example.mypaint.ui.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.blur_bottom_sheet_layout.*

class BlurBottomSheet(private val paintView: PaintView) : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.blur_bottom_sheet_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        choice_chip_group.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.normal_chip -> {
                    paintView.doBlur(true)
                    paintView.blurEffect(BlurMaskFilter.Blur.NORMAL)
                    dismiss()
                }
                R.id.inner_chip -> {
                    paintView.doBlur(true)
                    paintView.blurEffect(BlurMaskFilter.Blur.INNER)
                    dismiss()
                }
                R.id.outer_chip -> {
                    paintView.doBlur(true)
                    paintView.blurEffect(BlurMaskFilter.Blur.OUTER)
                    dismiss()
                }
                R.id.solid_chip -> {
                    paintView.doBlur(true)
                    paintView.blurEffect(BlurMaskFilter.Blur.SOLID)
                    dismiss()
                }
                R.id.cancel_chip -> {
                    MainActivity.selectedColor = "#000000"
                    paintView.normal()
                    dismiss()
                }
            }
        }
    }
}