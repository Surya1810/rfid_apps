package com.partnership.bjbdocumenttrackerreader.ui.search.radar.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View
import androidx.annotation.IntDef
import kotlin.math.min
import androidx.core.graphics.withTranslation

open class RadarBackgroundView : View {
    private val TAG = "RadarView"
    private var mLinePaint: Paint? = null
    private var mCirclePaint: Paint? = null
    private var mCirclePaint_Blue: Paint? = null
    private var mSectorPaint: Paint? = null
    private var mText1Paint: Paint? = null
    private var mText2Paint: Paint? = null
    private var mShader: Shader? = null
    private var matrix: Matrix? = null

    var isStart: Boolean = false
    private var viewSize = 0 // 控件尺寸
    private var startAngle = StartAngle(0) // 旋转效果起始角度
    private var textLen = 0f
    private var len30 = 0 //角度30刻度线条长度
    private var len5 = 0 //角度5刻度线条长度

    @IntDef(*[CLOCK_WISE, ANTI_CLOCK_WISE])
    annotation class RADAR_DIRECTION

    //设定雷达扫描方向
    private var direction = DEFAULT_DIERCTION

    private var threadRunning = true

    constructor(context: Context?) : super(context) {
        initPaint()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initPaint()
    }


    private fun initPaint() {
        setBackgroundColor(Color.TRANSPARENT)

        //宽度=5，抗锯齿，描边效果的白色画笔
        mLinePaint = Paint()
        mLinePaint!!.strokeWidth = 5f
        mLinePaint!!.isAntiAlias = true
        mLinePaint!!.style = Paint.Style.STROKE
        mLinePaint!!.color = Color.WHITE

        //宽度=5，抗锯齿，描边效果的浅绿色画笔
        mCirclePaint = Paint()
        mCirclePaint!!.strokeWidth = 5f
        mCirclePaint!!.isAntiAlias = true
        mCirclePaint!!.style = Paint.Style.FILL
        mCirclePaint!!.color = -0x67000000

        //宽度=5，抗锯齿，描边效果的浅蓝色画笔
        mCirclePaint_Blue = Paint()
        mCirclePaint_Blue!!.strokeWidth = 5f
        mCirclePaint_Blue!!.isAntiAlias = true
        mCirclePaint_Blue!!.style = Paint.Style.FILL
        mCirclePaint_Blue!!.color = -0x6d4b0c

        //暗绿色的画笔
        mSectorPaint = Paint()
        mSectorPaint!!.color = -0x62ff0100
        mSectorPaint!!.isAntiAlias = true

        //        mShader = new SweepGradient(viewSize / 2, viewSize / 2, Color.TRANSPARENT, Color.GREEN);
//        mPaintSector.setShader(mShader);

        // 方位（N、E、S、W）画笔
        mText1Paint = Paint()
        mText1Paint!!.color = Color.WHITE
        mText1Paint!!.isAntiAlias = true
        mText1Paint!!.textAlign = Paint.Align.CENTER
        mText1Paint!!.textSize = 40f

        // 数字画笔
        mText2Paint = Paint()
        mText2Paint!!.color = Color.WHITE
        mText2Paint!!.isAntiAlias = true
        mText2Paint!!.textAlign = Paint.Align.CENTER
        mText2Paint!!.textSize = 30f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getSize(widthMeasureSpec)
        val height = getSize(heightMeasureSpec)
        viewSize = min(width.toDouble(), height.toDouble()).toInt()
        textLen = mText1Paint!!.measureText("W")
        len30 = viewSize / 20
        len5 = viewSize / 40
        setMeasuredDimension(viewSize, viewSize)
    }

    private fun getSize(measureSpec: Int): Int {
        var mySize = 100 // 默认值100
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)

        if (mode == MeasureSpec.UNSPECIFIED) mySize = 100
        else if (mode == MeasureSpec.AT_MOST) mySize = size
        else if (mode == MeasureSpec.EXACTLY) mySize = size

        return mySize
    }


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val r = (viewSize / 2.0).toFloat()
        canvas.drawCircle(r, r, (viewSize / 2).toFloat(), mCirclePaint!!)
        canvas.drawCircle(r, r, (viewSize / 2).toFloat(), mLinePaint!!)
        canvas.drawCircle(r, r, (viewSize / 3).toFloat(), mLinePaint!!)
        canvas.drawCircle(r, r, (viewSize / 6).toFloat(), mLinePaint!!)

        //绘制两条十字线
        canvas.drawLine(r, len30 + textLen, r, viewSize - len30 - textLen, mLinePaint!!)
        canvas.drawLine(len30 + textLen, r, viewSize - len30 - textLen, r, mLinePaint!!)

        canvas.drawCircle(r, r, (viewSize / 6).toFloat(), mCirclePaint_Blue!!)

        // 绘制刻度
        canvas.withTranslation(r, r) {
            for (i in 0..359) {
                if (i % 30 == 0) {
                    drawLine(0f, -r, 0f, -r + len30, mLinePaint!!)
                    if (i % 90 != 0) {
                        drawText(
                            i.toString(), 0f, -r + len30 + mText1Paint!!.measureText("0"),
                            mText2Paint!!
                        )
                    }
                } else if (i % 5 == 0) {
                    drawLine(0f, -r, 0f, -r + len5, mLinePaint!!)
                }
                rotate(1f)
            }

            drawText("0", 0f, -r + len30 + textLen - 4, mText1Paint!!)
            rotate(90f)
            drawText("90", 0f, -r + len30 + textLen - 4, mText1Paint!!)
            rotate(90f)
            drawText("180", 0f, -r + len30 + textLen - 4, mText1Paint!!)
            rotate(90f)
            drawText("270", 0f, -r + len30 + textLen - 4, mText1Paint!!)
        }

        mShader = SweepGradient(r, r, Color.TRANSPARENT, Color.GREEN)
        mSectorPaint!!.shader = mShader //
        matrix = Matrix() // 根据matrix中设定角度，不断绘制shader,呈现出一种扇形扫描效果
        matrix!!.preRotate((direction * startAngle.angle).toFloat(), r, r) // 设定旋转角度,制定进行转转操作的圆心
        canvas.concat(matrix)
        canvas.drawCircle(r, r, r, mSectorPaint!!)
        super.onDraw(canvas)
    }


    fun setDirection(@RADAR_DIRECTION direction: Int) {
        require(!(direction != CLOCK_WISE && direction != ANTI_CLOCK_WISE)) { "Use @RADAR_DIRECTION constants only!" }
        this.direction = direction
    }

    fun setStartAngle(startAngle: StartAngle) {
        this.startAngle = startAngle
        this.invalidate()
    }

    fun start() {
        val mThread: ScanThread = ScanThread(this)
        mThread.name = "radar"
        mThread.start()
        threadRunning = true
        isStart = true
    }

    fun stop() {
        if (isStart) {
            threadRunning = false
            isStart = false
        }
    }

    class StartAngle(//封装成对象，使引用者也能检测到其变化，用于状态保存
        var angle: Int
    )

    protected inner class ScanThread(private val view: RadarBackgroundView) : Thread() {
        override fun run() {
            while (threadRunning) {
                if (isStart) {
                    view.post {
                        startAngle.angle = (startAngle.angle + 1) % 360
                        //                            matrix = new Matrix();
                        //                            //设定旋转角度,制定进行转转操作的圆心
                        //                            // matrix.postRotate(start, viewSize / 2, viewSize / 2);
                        //                            // matrix.setRotate(start,viewSize/2,viewSize/2);
                        //                            float r = (float) (viewSize / 2.0);
                        //                            matrix.preRotate(direction * startAngle.angle, r, r);
                        view.invalidate()
                    }
                    try {
                        sleep(5)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    companion object {
        const val CLOCK_WISE: Int = 1
        const val ANTI_CLOCK_WISE: Int = -1

        //默认为顺时针呢
        private const val DEFAULT_DIERCTION = CLOCK_WISE
    }
}