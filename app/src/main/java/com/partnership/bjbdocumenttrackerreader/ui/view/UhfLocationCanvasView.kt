package com.partnership.bjbdocumenttrackerreader.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.View

class UhfLocationCanvasView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val TAG = "UHF_LocationCanvasView"
    private var value: Int = 0

    // Properti untuk menggambar indikator
    private var valueTop: Float = 0f
    private var barWidth: Int = 100

    // Paint untuk isi dan border
    private var fillPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = Color.BLUE
        isFakeBoldText = true
        textSize = 16f
    }

    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = Color.RED
        isFakeBoldText = true
        textSize = 16f
    }

    // Handler untuk memicu invalidate
    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == 0) {
                invalidate()
            }
        }
    }

    private fun clean(canvas: Canvas?) {
        canvas?.let {
            fillPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            it.drawPaint(fillPaint)
            it.drawARGB(255, 255, 255, 255)

            // Reset fillPaint ke properti normal
            fillPaint = Paint().apply {
                style = Paint.Style.FILL
                isAntiAlias = true
                color = Color.BLUE
                isFakeBoldText = true
                textSize = 16f
            }
        }
    }

    // Method public untuk reset data
    fun clean() {
        setData(0)
    }

    // Method untuk set data indikator (misalnya kekuatan sinyal)
    fun setData(value: Int) {
        this.value = if (value < 0) 0 else value // simpan value untuk onDraw
        Log.e(TAG, "value=$value")
        valueTop = (100 - value) * (height / 100f)
        handler.sendEmptyMessage(0)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val left = width / 2 - barWidth / 2
        val right = width / 2 + barWidth / 2
        val bottom = height.toFloat()

        clean(canvas)

        // Asumsi value antara 0f (lemah) sampai 1f (kuat)
        val signalLevel = (value / 100f).coerceIn(0f, 1f)
        val barTop = height * (1 - signalLevel)

        val gradientColors: IntArray
        val colorPositions: FloatArray

        when {
            signalLevel <= 0.33f -> {
                // Sinyal lemah → hijau
                gradientColors = intArrayOf(Color.GREEN, Color.GREEN)
                colorPositions = floatArrayOf(0f, 1f)
            }
            signalLevel <= 0.66f -> {
                // Sinyal sedang → hijau ke kuning
                gradientColors = intArrayOf(Color.GREEN, Color.YELLOW)
                colorPositions = floatArrayOf(0f, 1f)
            }
            else -> {
                // Sinyal kuat → kuning ke merah
                gradientColors = intArrayOf(Color.YELLOW, Color.RED)
                colorPositions = floatArrayOf(0f, 1f)
            }
        }

        val gradient = LinearGradient(
            0f, barTop,
            0f, bottom,
            gradientColors,
            colorPositions,
            Shader.TileMode.CLAMP
        )
        fillPaint.shader = gradient

        // Gambar border
        canvas.drawRect(
            left.toFloat(), 0f,
            right.toFloat(), bottom,
            borderPaint
        )

        // Gambar isi bar
        canvas.drawRect(
            left.toFloat(), barTop,
            right.toFloat(), bottom,
            fillPaint
        )
    }
}

