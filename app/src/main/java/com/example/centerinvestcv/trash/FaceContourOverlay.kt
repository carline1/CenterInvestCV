package com.example.centerinvestcv.trash

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.centerinvestcv.R

class FaceContourOverlay(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val faceContours: MutableList<RectF> = mutableListOf()
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(getContext(), R.color.cardview_dark_background)
        strokeWidth = 4f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        faceContours.forEach { canvas?.drawRect(it, paint) }
    }

    fun drawFaceContour(faceContours: List<RectF>) {
        this.faceContours.clear()
        this.faceContours.addAll(faceContours)
        invalidate()
    }
}


