package com.launcher.views.colorSeekBar

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.R
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

//https://github.com/rtugeek/ColorSeekBar
// NOT in priority
class ColorSeekBar : View {
    private val mCachedColors: MutableList<Int> = ArrayList()
    private val colorPaint = Paint()
    private val alphaThumbGradientPaint = Paint()
    private val alphaBarPaint = Paint()
    private val mDisabledPaint = Paint()
    private val thumbGradientPaint = Paint()
    private var mColorSeeds = intArrayOf(
        -0x1,
        -0x66ff01,
        -0xffff01,
        -0xff0100,
        -0xff0001,
        -0x10000,
        -0xff01,
        -0x9a00,
        -0x100,
        -0x707374,
        -0x1000000 // 0xFFEAFEAF
    )
    private var mAlpha = 0
    private var mOnColorChangeLister: OnColorChangeListener? = null
    private var mContext: Context? = null
    private var mIsShowAlphaBar = false
    private var isVertical = false
    private var mMovingColorBar = false
    private var mMovingAlphaBar = false
    private var mTransparentBitmap: Bitmap? = null
    private var mColorRect: RectF? = null
    private var thumbHeight = 20
    private var mThumbRadius = 0f
    private var barHeight = 2
    private var mColorRectPaint: Paint? = null
    private var realLeft = 0
    private var realRight = 0
    private var mBarWidth = 0
    private var maxValue = 0
    private var mAlphaRect: RectF? = null
    private var mColorBarPosition = 0
    private var mAlphaBarPosition = 0
    private var mDisabledColor = 0
    private var barMargin = 5
    private var mAlphaMinPosition = 0
    private var mAlphaMaxPosition = 255
    private var mBarRadius = 0
    private var mColorsToInvoke = -1
    private var mInit = false

    /**
     * @return
     */
    @get:Deprecated("use {@link #setOnInitDoneListener(OnInitDoneListener)} instead.")
    var isFirstDraw = true
        private set
    private var mShowThumb = true
    private var mOnInitDoneListener: OnInitDoneListener? = null

    constructor(context: Context) : super(context) {
        init(context, null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs, defStyleAttr, 0)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Suppress("unused")
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun init(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) {
        applyStyle(context, attrs, defStyleAttr, defStyleRes)
    }

    @Suppress("unused")
    fun applyStyle(resId: Int) {
        applyStyle(context, null, 0, resId)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var mViewWidth = widthMeasureSpec
        var mViewHeight = heightMeasureSpec
        val widthSpeMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpeMode = MeasureSpec.getMode(heightMeasureSpec)
        val barHeight = if (mIsShowAlphaBar) barHeight * 2 else barHeight
        val thumbHeight = if (mIsShowAlphaBar) thumbHeight * 2 else thumbHeight
        if (isVertical) {
            if (widthSpeMode == MeasureSpec.AT_MOST || widthSpeMode == MeasureSpec.UNSPECIFIED) {
                mViewWidth = thumbHeight + barHeight + barMargin
                setMeasuredDimension(mViewWidth, mViewHeight)
            }
        } else {
            if (heightSpeMode == MeasureSpec.AT_MOST || heightSpeMode == MeasureSpec.UNSPECIFIED) {
                mViewHeight = thumbHeight + barHeight + barMargin
                setMeasuredDimension(mViewWidth, mViewHeight)
            }
        }
    }

    private fun applyStyle(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) {
        mContext = context
        //get attributes
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.ColorSeekBar,
            defStyleAttr,
            defStyleRes
        )
        val colorsId = a.getResourceId(R.styleable.ColorSeekBar_colorSeeds, 0)
        maxValue = a.getInteger(R.styleable.ColorSeekBar_maxPosition, 100)
        mColorBarPosition = a.getInteger(R.styleable.ColorSeekBar_colorBarPosition, 0)
        mAlphaBarPosition =
            a.getInteger(R.styleable.ColorSeekBar_alphaBarPosition, mAlphaMinPosition)
        mDisabledColor = a.getInteger(R.styleable.ColorSeekBar_disabledColor, Color.GRAY)
        isVertical = a.getBoolean(R.styleable.ColorSeekBar_isVertical, false)
        mIsShowAlphaBar = a.getBoolean(R.styleable.ColorSeekBar_showAlphaBar, false)
        mShowThumb = a.getBoolean(R.styleable.ColorSeekBar_showAlphaBar, true)
        val backgroundColor = a.getColor(R.styleable.ColorSeekBar_bgColor, Color.TRANSPARENT)
        barHeight = a.getDimension(R.styleable.ColorSeekBar_barHeight, dp2px(2f).toFloat()).toInt()
        mBarRadius = a.getDimension(R.styleable.ColorSeekBar_barRadius, 0f).toInt()
        thumbHeight =
            a.getDimension(R.styleable.ColorSeekBar_thumbHeight, dp2px(30f).toFloat()).toInt()
        barMargin = a.getDimension(R.styleable.ColorSeekBar_barMargin, dp2px(5f).toFloat()).toInt()
        a.recycle()
        mDisabledPaint.isAntiAlias = true
        mDisabledPaint.color = mDisabledColor
        if (colorsId != 0) {
            mColorSeeds = getColorsById(colorsId)
        }
        setBackgroundColor(backgroundColor)
    }

    /**
     * @param id color array resource
     * @return
     */
    private fun getColorsById(id: Int): IntArray {
        return if (isInEditMode) {
            val s = context.resources.getStringArray(id)
            val colors = IntArray(s.size)
            for (j in s.indices) {
                colors[j] = Color.parseColor(s[j])
            }
            colors
        } else {
            val typedArray = context.resources.obtainTypedArray(id)
            val colors = IntArray(typedArray.length())
            for (j in 0 until typedArray.length()) {
                colors[j] = typedArray.getColor(j, Color.BLACK)
            }
            typedArray.recycle()
            colors
        }
    }

    private fun init() {

        //init size
        mThumbRadius = (thumbHeight / 2).toFloat()
        val mPaddingSize = mThumbRadius.toInt()
        val viewBottom = height - paddingBottom - mPaddingSize
        val viewRight = width - paddingRight - mPaddingSize
        //init left right top bottom
        realLeft = paddingLeft + mPaddingSize
        realRight = if (isVertical) viewBottom else viewRight
        val realTop = paddingTop + mPaddingSize
        mBarWidth = realRight - realLeft

        //init rect
        mColorRect = RectF(
            realLeft.toFloat(),
            realTop.toFloat(),
            realRight.toFloat(),
            (realTop + barHeight).toFloat()
        )

        //init paint
        val mColorGradient = LinearGradient(
            0f,
            0f,
            mColorRect?.width() ?: 0f,
            0f,
            mColorSeeds,
            null,
            Shader.TileMode.CLAMP
        )
        mColorRectPaint = Paint()
        mColorRectPaint?.shader = mColorGradient
        mColorRectPaint?.isAntiAlias = true
        cacheColors()
        setAlphaValue()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mTransparentBitmap = if (isVertical) {
            Bitmap.createBitmap(h, w, Bitmap.Config.ARGB_4444)
        } else {
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444)
        }
        mTransparentBitmap?.eraseColor(Color.TRANSPARENT)
        init()
        mInit = true
        if (mColorsToInvoke != -1) {
            color = mColorsToInvoke
        }
    }

    private fun cacheColors() {
        //if the view's size hasn't been initialized. do not cache.
        if (mBarWidth < 1) {
            return
        }
        mCachedColors.clear()
        for (i in 0..maxValue) {
            mCachedColors.add(pickColor(i))
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        if (isVertical) {
            canvas.rotate(-90f)
            canvas.translate(-height.toFloat(), 0f)
            canvas.scale(-1f, 1f, (height / 2).toFloat(), (width / 2).toFloat())
        }
        val colorPosition = mColorBarPosition.toFloat() / maxValue * mBarWidth
        colorPaint.isAntiAlias = true
        val color = if (isEnabled) getColor(false) else mDisabledColor
        val colorStartTransparent =
            Color.argb(mAlphaMaxPosition, Color.red(color), Color.green(color), Color.blue(color))
        val colorEndTransparent =
            Color.argb(mAlphaMinPosition, Color.red(color), Color.green(color), Color.blue(color))
        colorPaint.color = color
        val toAlpha = intArrayOf(colorStartTransparent, colorEndTransparent)
        //clear
        mTransparentBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }

        //draw color bar
        mColorRect?.let { colorRect ->
            (if (isEnabled) mColorRectPaint else mDisabledPaint)?.let { paint ->
                canvas.drawRoundRect(
                    colorRect,
                    mBarRadius.toFloat(),
                    mBarRadius.toFloat(),
                    paint
                )
            }
        }
        //draw color bar thumb
        if (mShowThumb) {
            val thumbX = colorPosition + realLeft
            val thumbY = mColorRect!!.top + mColorRect!!.height() / 2
            canvas.drawCircle(thumbX, thumbY, (barHeight / 2 + 5).toFloat(), colorPaint)

            //draw color bar thumb radial gradient shader
            val thumbShader =
                RadialGradient(thumbX, thumbY, mThumbRadius, toAlpha, null, Shader.TileMode.MIRROR)
            thumbGradientPaint.isAntiAlias = true
            thumbGradientPaint.shader = thumbShader
            canvas.drawCircle(thumbX, thumbY, (thumbHeight / 2).toFloat(), thumbGradientPaint)
        }
        if (mIsShowAlphaBar) {
            //init rect
            val top = (thumbHeight + mThumbRadius + barHeight + barMargin).toInt()
            mAlphaRect = RectF(
                realLeft.toFloat(),
                top.toFloat(),
                realRight.toFloat(),
                (top + barHeight).toFloat()
            )
            //draw alpha bar
            alphaBarPaint.isAntiAlias = true
            val alphaBarShader = LinearGradient(
                0f,
                0f,
                mAlphaRect?.width() ?: 0f,
                0f,
                toAlpha,
                null,
                Shader.TileMode.CLAMP
            )
            alphaBarPaint.shader = alphaBarShader
            mAlphaRect?.let {
                canvas.drawRect(it, alphaBarPaint)
            }

            //draw alpha bar thumb
            if (mShowThumb) {
                val alphaPosition =
                    (mAlphaBarPosition - mAlphaMinPosition).toFloat() / (mAlphaMaxPosition - mAlphaMinPosition) * mBarWidth
                val alphaThumbX = alphaPosition + realLeft
                val alphaThumbY = mAlphaRect!!.top + mAlphaRect!!.height() / 2
                canvas.drawCircle(
                    alphaThumbX,
                    alphaThumbY,
                    (barHeight / 2 + 5).toFloat(),
                    colorPaint
                )

                //draw alpha bar thumb radial gradient shader
                val alphaThumbShader = RadialGradient(
                    alphaThumbX,
                    alphaThumbY,
                    mThumbRadius,
                    toAlpha,
                    null,
                    Shader.TileMode.MIRROR
                )
                alphaThumbGradientPaint.isAntiAlias = true
                alphaThumbGradientPaint.shader = alphaThumbShader
                canvas.drawCircle(
                    alphaThumbX,
                    alphaThumbY,
                    (thumbHeight / 2).toFloat(),
                    alphaThumbGradientPaint
                )
            }
        }
        if (isFirstDraw) {
            mOnColorChangeLister?.onColorChangeListener(
                mColorBarPosition,
                mAlphaBarPosition,
                this.color
            )
            isFirstDraw = false
            mOnInitDoneListener?.done()
        }
        super.onDraw(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return true
        }
        val x = if (isVertical) event.y else event.x
        val y = if (isVertical) event.x else event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> if (isOnBar(mColorRect, x, y)) {
                mMovingColorBar = true
                val value = (x - realLeft) / mBarWidth * maxValue
                colorBarPosition = value.toInt()
            } else if (mIsShowAlphaBar) {
                if (isOnBar(mAlphaRect, x, y)) {
                    mMovingAlphaBar = true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                parent.requestDisallowInterceptTouchEvent(true)
                if (mMovingColorBar) {
                    val value = (x - realLeft) / mBarWidth * maxValue
                    colorBarPosition = value.toInt()
                } else if (mIsShowAlphaBar) {
                    if (mMovingAlphaBar) {
                        val value =
                            (x - realLeft) / mBarWidth.toFloat() * (mAlphaMaxPosition - mAlphaMinPosition) + mAlphaMinPosition
                        mAlphaBarPosition = value.toInt()
                        if (mAlphaBarPosition < mAlphaMinPosition) {
                            mAlphaBarPosition = mAlphaMinPosition
                        } else if (mAlphaBarPosition > mAlphaMaxPosition) {
                            mAlphaBarPosition = mAlphaMaxPosition
                        }
                        setAlphaValue()
                    }
                }
                if ((mMovingAlphaBar || mMovingColorBar)) {
                    mOnColorChangeLister?.onColorChangeListener(
                        mColorBarPosition,
                        mAlphaBarPosition,
                        color
                    )
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                mMovingColorBar = false
                mMovingAlphaBar = false
            }
            else -> {}
        }
        return true
    }

    @Suppress("unused")
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
    }

    /***
     *
     * alphaMaxPosition <= 255 && > alphaMinPosition
     */
    @Suppress("unused")
    var alphaMaxPosition: Int
        get() = mAlphaMaxPosition
        set(alphaMaxPosition) {
            mAlphaMaxPosition = alphaMaxPosition
            if (mAlphaMaxPosition > 255) {
                mAlphaMaxPosition = 255
            } else if (mAlphaMaxPosition <= mAlphaMinPosition) {
                mAlphaMaxPosition = mAlphaMinPosition + 1
            }
            if (mAlphaBarPosition > mAlphaMinPosition) {
                mAlphaBarPosition = mAlphaMaxPosition
            }
            invalidate()
        }

    /***
     *
     * alphaMinPosition >=0 && < alphaMaxPosition
     */
    @Suppress("unused")
    var alphaMinPosition: Int
        get() = mAlphaMinPosition
        set(alphaMinPosition) {
            mAlphaMinPosition = alphaMinPosition
            if (mAlphaMinPosition >= mAlphaMaxPosition) {
                mAlphaMinPosition = mAlphaMaxPosition - 1
            } else if (mAlphaMinPosition < 0) {
                mAlphaMinPosition = 0
            }
            if (mAlphaBarPosition < mAlphaMinPosition) {
                mAlphaBarPosition = mAlphaMinPosition
            }
            invalidate()
        }

    /**
     * @param r
     * @param x
     * @param y
     * @return whether MotionEvent is performing on bar or not
     */
    private fun isOnBar(r: RectF?, x: Float, y: Float): Boolean {
        return r!!.left - mThumbRadius < x && x < r.right + mThumbRadius && r.top - mThumbRadius < y && y < r.bottom + mThumbRadius
    }

    /**
     * @param value
     * @return color
     */
    private fun pickColor(value: Int): Int {
        return pickColor(value.toFloat() / maxValue * mBarWidth)
    }

    /**
     * @param position
     * @return color
     */
    private fun pickColor(position: Float): Int {
        val unit = position / mBarWidth
        if (unit <= 0.0) {
            return mColorSeeds[0]
        }
        if (unit >= 1) {
            return mColorSeeds[mColorSeeds.size - 1]
        }
        var colorPosition = unit * (mColorSeeds.size - 1)
        val i = colorPosition.toInt()
        colorPosition -= i.toFloat()
        val c0 = mColorSeeds[i]
        val c1 = mColorSeeds[i + 1]

        // mAlpha = mix(Color.alpha(c0), Color.alpha(c1), colorPosition);
        val mRed = mix(Color.red(c0), Color.red(c1), colorPosition)
        val mGreen = mix(Color.green(c0), Color.green(c1), colorPosition)
        val mBlue = mix(Color.blue(c0), Color.blue(c1), colorPosition)
        return Color.rgb(mRed, mGreen, mBlue)
    }

    /**
     * @param start
     * @param end
     * @param position
     * @return
     */
    private fun mix(start: Int, end: Int, position: Float): Int {
        return start + (position * (end - start)).roundToInt()
    }

    /**
     * Set color,the mCachedColors must contains the specified color, if not ,invoke setColorBarPosition(0);
     *
     * color
     */
    var color: Int
        get() = getColor(mIsShowAlphaBar)
        set(color) {
            val withoutAlphaColor =
                Color.rgb(Color.red(color), Color.green(color), Color.blue(color))
            if (mInit) {
                val value = mCachedColors.indexOf(withoutAlphaColor)
                alphaValue = Color.alpha(color)
                colorBarPosition = value
            } else {
                mColorsToInvoke = color
            }
        }

    /**
     * withAlpha
     * @return
     */
    private fun getColor(withAlpha: Boolean): Int {
        //pick mode
        if (mColorBarPosition >= mCachedColors.size) {
            val color = pickColor(mColorBarPosition)
            return if (withAlpha) {
                color
            } else {
                Color.argb(
                    alphaValue,
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                )
            }
        }

        //cache mode
        val color = mCachedColors[mColorBarPosition]
        return if (withAlpha) {
            Color.argb(
                alphaValue,
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            )
        } else color
    }

    @Suppress("unused")
    var alphaBarPosition: Int
        get() = mAlphaBarPosition
        set(value) {
            mAlphaBarPosition = value
            setAlphaValue()
            invalidate()
        }

    // invalidate();
    private var alphaValue: Int
        get() = mAlpha
        private set(value) {
            mAlpha = value
            mAlphaBarPosition = 255 - mAlpha
            // invalidate();
        }

    /**
     * @param onColorChangeListener
     */
    fun setOnColorChangeListener(onColorChangeListener: OnColorChangeListener?) {
        mOnColorChangeLister = onColorChangeListener
    }

    private fun dp2px(dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * Set colors by resource id. The resource's type must be ArrayRes
     *
     * @param resId
     */
    @Suppress("unused")
    fun setColorSeeds(resId: Int) {
        setColorSeeds(getColorsById(resId))
    }

    private fun setColorSeeds(colors: IntArray) {
        mColorSeeds = colors
        init()
        invalidate()
        mOnColorChangeLister?.onColorChangeListener(
            mColorBarPosition,
            mAlphaBarPosition,
            color
        )
    }

    /**
     * @param color
     * @return the color's position in the bar, if not in the bar ,return -1;
     */
    @Suppress("unused")
    fun getColorIndexPosition(color: Int): Int {
        return mCachedColors.indexOf(
            Color.argb(
                255,
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            )
        )
    }

    @Suppress("unused")
    val colors: List<Int>
        get() = mCachedColors

    //    public void setVertical(boolean vertical) {
    //        mIsVertical = vertical;
    //        refreshLayoutParams();
    //        invalidate();
    //    }
    var isShowAlphaBar: Boolean
        get() = mIsShowAlphaBar
        set(show) {
            mIsShowAlphaBar = show
            refreshLayoutParams()
            invalidate()
            mOnColorChangeLister?.onColorChangeListener(
                mColorBarPosition,
                mAlphaBarPosition,
                color
            )
        }

    private fun refreshLayoutParams() {
        layoutParams = layoutParams
    }

    /**
     * @param px
     */
    @Suppress("unused")
    fun setBarHeightPx(px: Int) {
        barHeight = px
        refreshLayoutParams()
        invalidate()
    }

    private fun setAlphaValue() {
        mAlpha = 255 - mAlphaBarPosition
    }

    fun setMaxPosition(value: Int) {
        maxValue = value
        invalidate()
        cacheColors()
    }

    /**
     * set margin between bars
     *
     * @param mBarMargin
     */
    @Suppress("unused")
    fun setBarMarginPx(mBarMargin: Int) {
        barMargin = mBarMargin
        refreshLayoutParams()
        invalidate()
    }

    @Suppress("unused")
    fun setPosition(colorBarPosition: Int, alphaBarPosition: Int) {
        mColorBarPosition = colorBarPosition
        mColorBarPosition = min(mColorBarPosition, maxValue)
        mColorBarPosition = max(mColorBarPosition, 0)
        mAlphaBarPosition = alphaBarPosition
        setAlphaValue()
        invalidate()
        mOnColorChangeLister?.onColorChangeListener(
            mColorBarPosition,
            mAlphaBarPosition,
            color
        )
    }

    @Suppress("unused")
    fun setOnInitDoneListener(listener: OnInitDoneListener?) {
        mOnInitDoneListener = listener
    }

    /**
     * set thumb's height by pixels
     *
     * @param px
     */
    @Suppress("unused")
    fun setThumbHeightPx(px: Int) {
        thumbHeight = px
        mThumbRadius = (thumbHeight / 2).toFloat()
        refreshLayoutParams()
        invalidate()
    }

    /**
     * @param dp
     */
    fun setBarHeight(dp: Float) {
        barHeight = dp2px(dp)
        refreshLayoutParams()
        invalidate()
    }

    /**
     * set thumb's height by dpi
     *
     * @param dp
     */
    @Suppress("unused")
    fun setThumbHeight(dp: Float) {
        thumbHeight = dp2px(dp)
        mThumbRadius = (thumbHeight / 2).toFloat()
        refreshLayoutParams()
        invalidate()
    }

    /**
     * set margin between bars
     *
     * @param mBarMargin
     */
    @Suppress("unused")
    fun setBarMargin(mBarMargin: Float) {
        barMargin = dp2px(mBarMargin)
        refreshLayoutParams()
        invalidate()
    }

    @Suppress("unused")
    val colorBarValue: Float
        get() = mColorBarPosition.toFloat()

    /**
     * Set the value of color bar, if out of bounds , it will be 0 or maxValue;
     *
     * value
     */
    private var colorBarPosition: Int
        get() = mColorBarPosition
        set(value) {
            mColorBarPosition = value
            mColorBarPosition = min(mColorBarPosition, maxValue)
            mColorBarPosition = max(mColorBarPosition, 0)
            invalidate()
            mOnColorChangeLister?.onColorChangeListener(
                mColorBarPosition,
                mAlphaBarPosition,
                color
            )
        }

    @Suppress("unused")
    var disabledColor: Int
        get() = mDisabledColor
        set(disabledColor) {
            mDisabledColor = disabledColor
            mDisabledPaint.color = disabledColor
        }

    @Suppress("unused")
    var isShowThumb: Boolean
        get() = mShowThumb
        set(showThumb) {
            mShowThumb = showThumb
            invalidate()
        }

    /**
     * Set bar radius with px unit
     *
     * barRadiusInPx
     */
    @Suppress("unused")
    var barRadius: Int
        get() = mBarRadius
        set(barRadiusInPx) {
            mBarRadius = barRadiusInPx
            invalidate()
        }

    interface OnColorChangeListener {
        /**
         * @param colorBarPosition between 0-maxValue
         * @param alphaBarPosition between 0-255
         * @param color            return the color contains alpha value whether showAlphaBar is true or without alpha value
         */
        fun onColorChangeListener(colorBarPosition: Int, alphaBarPosition: Int, color: Int)
    }

    interface OnInitDoneListener {
        fun done()
    }
}
