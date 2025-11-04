package com.partnership.bjbdocumenttrackerreader.ui.search.radar.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.rscja.deviceapi.entity.RadarLocationEntity
import androidx.core.graphics.withRotation

class RadarPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var viewSize = 100
    private var labelRadius = 0

    private val paintYellow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeWidth = 5f
        color = 0xFFFFD700.toInt()
    }
    private val paintBlue = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeWidth = 5f
        color = 0xFF0083FF.toInt()
    }
    private val paintWhite = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.WHITE
    }

    // --- data yang aman untuk diakses dari UI thread ---
    @Volatile private var targetEpc: String = ""
    @Volatile private var showAllTag: Boolean = false

    // list diproteksi + di-snapshot agar tidak berubah saat onDraw
    private val pointsLock = Any()
    private var pointsSnapshot: List<RadarLocationEntity> = emptyList()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        viewSize = minOf(w, h).coerceAtLeast(100)
        labelRadius = viewSize / 40
        paintWhite.strokeWidth = viewSize / 200f
        setMeasuredDimension(viewSize, viewSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val r = viewSize / 2f
        canvas.translate(r, r)

        // ambil snapshot sekali, lalu iterasi; hindari CME
        val localPoints: List<RadarLocationEntity> = synchronized(pointsLock) { pointsSnapshot }
        if (localPoints.isEmpty()) return

        val drawAll = showAllTag || targetEpc.isEmpty()

        for (info in localPoints) {
            val isTarget = targetEpc.isNotEmpty() && info.tag == targetEpc
            if (drawAll || isTarget) {
                val angle = info.angle.toFloat()
                val distance = (info.value.coerceIn(0, 100) * 0.01f * r) // clamp 0..100
                val cy = distance - r + 10f

                canvas.withRotation(angle) {
                    drawCircle(0f, cy, labelRadius.toFloat(), if (isTarget) paintYellow else paintBlue)
                    drawCircle(0f, cy, labelRadius.toFloat(), paintWhite)
                }
            }
        }
    }

    /** Update data: panggil dari UI thread atau gunakan post { ... } saat terima dari worker thread */
    fun bindingData(pointList: List<RadarLocationEntity>?, targetEpc: String, showAllTag: Boolean = false) {
        // defensive copy (hindari referensi ke LinkedList yang bisa berubah)
        val snapshot = pointList?.let { ArrayList(it) } ?: emptyList()
        synchronized(pointsLock) {
            this.pointsSnapshot = snapshot
            this.targetEpc = targetEpc
            this.showAllTag = showAllTag
        }
        postInvalidateOnAnimation()
    }

    fun clearPanel() {
        synchronized(pointsLock) { pointsSnapshot = emptyList() }
        postInvalidateOnAnimation()
    }
}

