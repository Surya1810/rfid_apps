package com.partnership.bjbdocumenttrackerreader.ui.search.radar.view


import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.partnership.bjbdocumenttrackerreader.R
import com.rscja.deviceapi.entity.RadarLocationEntity

class RadarView : ConstraintLayout {
    private var radarBackgroundView: RadarBackgroundView? = null
    private var radarPanelView: RadarPanelView? = null

    constructor(context: Context?) : super(context!!)

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        LayoutInflater.from(context).inflate(R.layout.radar_view, this, true)

        radarBackgroundView = findViewById(R.id._radarBackgroundView)
        radarPanelView = findViewById(R.id._labelPanelView)

        //        // 获取自定义属性
//        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RadarView);
//        int image = typedArray.getResourceId(R.styleable.RadarView_center_image, R.drawable.phone); //设置默认图片
//        final float scale = context.getResources().getDisplayMetrics().density;
//        int width = typedArray.getDimensionPixelSize(R.styleable.RadarView_image_width, (int) (45 * scale + 0.5f));     //默认尺寸为45dp
//        int height = typedArray.getDimensionPixelSize(R.styleable.RadarView_image_height, (int) (103 * scale + 0.5f));  //默认尺寸为103dp
//
//        ImageView imageView = findViewById(R.id._centerImage);
//        imageView.setBackground(context.getResources().getDrawable(image));
//
//        LayoutParams params = (LayoutParams) imageView.getLayoutParams();
//        params.width = width;
//        params.height = height;
//        imageView.setLayoutParams(params);
//
//        typedArray.recycle();
    }

    /**
     * 设置雷达扫描动画的方向
     *
     * @param direction CLOCK_WISE:顺时针; ANTI_CLOCK_WISE:逆时针
     */
    fun setDirection(@RadarBackgroundView.RADAR_DIRECTION direction: Int) {
        require(direction == CLOCK_WISE || direction == ANTI_CLOCK_WISE) {
            "Use @RADAR_DIRECTION constants only!"
        }
        radarBackgroundView?.setDirection(direction)
    }

    /**
     * 设置雷达扫描动画的起始角度
     *
     * @param startAngle 起始角度
     */
    fun setStartAngle(startAngle: RadarBackgroundView.StartAngle?) {
        if (startAngle != null) {
            radarBackgroundView!!.setStartAngle(startAngle)
        }
        radarBackgroundView!!.invalidate()
    }

    /**
     * 打开雷达扫描动画
     */
    fun startRadar() {
        radarBackgroundView!!.start()
    }

    /**
     * 关闭雷达扫描动画
     */
    fun stopRadar() {
        radarBackgroundView!!.stop()
    }

    /**
     * 绑定标签数据
     *
     * @param TagList   标签集合
     * @param targetTag 目标标签
     */
    fun bindingData(TagList: List<RadarLocationEntity>?, targetTag: String?, showAllTag: Boolean = false) {
        radarPanelView!!.bindingData(TagList, targetTag!!, showAllTag)
        //        this.invalidate();
    }

    /**
     * 清空所有标签
     */
    fun clearPanel() {
        radarPanelView!!.clearPanel()
    }

    fun setRotation(angle: Int) {
        radarBackgroundView!!.rotation = angle.toFloat()
        radarPanelView!!.rotation = angle.toFloat()
    }

    companion object {
        const val CLOCK_WISE: Int = 1
        const val ANTI_CLOCK_WISE: Int = -1
    }
}