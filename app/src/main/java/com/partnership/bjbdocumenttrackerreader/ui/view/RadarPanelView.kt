package com.partnership.bjbdocumenttrackerreader.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.rscja.deviceapi.entity.RadarLocationEntity

class RadarPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var viewSize = 100 // Panel size (width and height are equal, using the minimum value)
    private var labelRadius = 0 // Label dot radius

    private val paintYellow = Paint().apply {
        strokeWidth = 5f
        isAntiAlias = true
        style = Paint.Style.FILL
        color = 0xFFFFD700.toInt()
    }

    private val paintBlue = Paint().apply {
        strokeWidth = 5f
        isAntiAlias = true
        style = Paint.Style.FILL
        color = 0xFF0083FF.toInt()
    }

    private val paintWhite = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private var pointList: List<RadarLocationEntity>? = null
    private var targetEpc: String = ""

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getSize(widthMeasureSpec)
        val height = getSize(heightMeasureSpec)
        viewSize = minOf(width, height)
        labelRadius = viewSize / 40
        paintWhite.strokeWidth = viewSize / 200f
        setMeasuredDimension(viewSize, viewSize)
    }

    private fun getSize(measureSpec: Int): Int {
        val size = MeasureSpec.getSize(measureSpec)
        return when (MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.UNSPECIFIED -> 100
            MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> size
            else -> 100
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        pointList?.let { points ->
            val r = viewSize / 2f
            canvas.translate(r, r)

            // Draw blue circles
            for (info in points) {
                canvas.save()
                canvas.rotate(info.angle.toFloat())
                val distance = (info.value / 100.0 * r).toFloat()
                canvas.drawCircle(0f, distance - r + 10, labelRadius.toFloat(), paintBlue)
                canvas.drawCircle(0f, distance - r + 10, labelRadius.toFloat(), paintWhite)
                canvas.restore()
            }

            // Draw yellow circles for target EPC
            for (info in points) {
                if (targetEpc.isNotEmpty() && info.tag == targetEpc) {
                    canvas.save()
                    canvas.rotate(info.angle.toFloat())
                    val distance = (info.value / 100.0 * r).toFloat()
                    canvas.drawCircle(0f, distance - r + 10, labelRadius.toFloat(), paintYellow)
                    canvas.drawCircle(0f, distance - r + 10, labelRadius.toFloat(), paintWhite)
                    canvas.restore()
                }
            }
        }
    }

    /**
     * Bind data
     * @param pointList List of tag positions
     * @param targetEpc Target tag
     */
    fun bindingData(pointList: List<RadarLocationEntity>?, targetEpc: String) {
        this.pointList = pointList
        this.targetEpc = targetEpc
        invalidate()
    }

    /**
     * Clear panel
     */
    fun clearPanel() {
        pointList = null
        invalidate()
    }
}
