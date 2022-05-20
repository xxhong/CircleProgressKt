package com.github.lzyzsd.circleprogresskt

import android.content.Context
import kotlin.jvm.JvmOverloads
import android.graphics.RectF
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.text.TextUtils
import android.os.Parcelable
import android.os.Bundle
import android.util.AttributeSet
import android.view.View

/**
 * Created by bruce on 11/4/14.
 */
class CircleProgress @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var textPaint: Paint? = null
    private val rectF = RectF()
    private var textSize = 0f
    private var textColor = 0
    private var progress = 0
    private var text: String? = null
    var max = 0
        set(max) {
            if (max > 0) {
                field = max
                invalidate()
            }
        }
    private var finishedColor = 0
    private var unfinishedColor = 0
    private var prefixText = ""
    private var suffixText = "%"
    private val default_finished_color = Color.rgb(66, 145, 241)
    private val default_unfinished_color = Color.rgb(204, 204, 204)
    private val default_text_color = Color.WHITE
    private val default_max = 100
    private val default_text_size: Float
    private val min_size: Int
    private val paint = Paint()
    protected fun initByAttributes(attributes: TypedArray) {
        finishedColor = attributes.getColor(
            R.styleable.CircleProgress_circle_finished_color,
            default_finished_color
        )
        unfinishedColor = attributes.getColor(
            R.styleable.CircleProgress_circle_unfinished_color,
            default_unfinished_color
        )
        textColor =
            attributes.getColor(R.styleable.CircleProgress_circle_text_color, default_text_color)
        textSize =
            attributes.getDimension(R.styleable.CircleProgress_circle_text_size, default_text_size)
        max = attributes.getInt(R.styleable.CircleProgress_circle_max, default_max)
        setProgress(attributes.getInt(R.styleable.CircleProgress_circle_progress, 0))
        if (attributes.getString(R.styleable.CircleProgress_circle_prefix_text) != null) {
            setPrefixText(attributes.getString(R.styleable.CircleProgress_circle_prefix_text)!!)
        }
        if (attributes.getString(R.styleable.CircleProgress_circle_suffix_text) != null) {
            setSuffixText(attributes.getString(R.styleable.CircleProgress_circle_suffix_text)!!)
        }
    }

    protected fun initPainters() {
        textPaint = TextPaint()
        textPaint?.setColor(textColor)
        textPaint?.setTextSize(textSize)
        textPaint?.setAntiAlias(true)
        paint.isAntiAlias = true
    }

    override fun invalidate() {
        initPainters()
        super.invalidate()
    }

    fun getProgress(): Int {
        return progress
    }

    fun setProgress(progress: Int) {
        this.progress = progress
        if (this.progress > max) {
            this.progress %= max
        }
        invalidate()
    }

    fun setDefaultText() {
        text = null
        invalidate()
    }

    /**
     * Setting Central Text to custom String
     */
    fun setText(text: String?) {
        this.text = text
        invalidate()
    }

    fun getText(): String {
        return text ?: progress.toString()
    }

    fun getTextSize(): Float {
        return textSize
    }

    fun setTextSize(textSize: Float) {
        this.textSize = textSize
        this.invalidate()
    }

    fun getTextColor(): Int {
        return textColor
    }

    fun setTextColor(textColor: Int) {
        this.textColor = textColor
        this.invalidate()
    }

    fun getFinishedColor(): Int {
        return finishedColor
    }

    fun setFinishedColor(finishedColor: Int) {
        this.finishedColor = finishedColor
        this.invalidate()
    }

    fun getUnfinishedColor(): Int {
        return unfinishedColor
    }

    fun setUnfinishedColor(unfinishedColor: Int) {
        this.unfinishedColor = unfinishedColor
        this.invalidate()
    }

    fun getPrefixText(): String {
        return prefixText
    }

    fun setPrefixText(prefixText: String) {
        this.prefixText = prefixText
        this.invalidate()
    }

    fun getSuffixText(): String {
        return suffixText
    }

    fun setSuffixText(suffixText: String) {
        this.suffixText = suffixText
        this.invalidate()
    }

    val drawText: String
        get() = getPrefixText() + getText() + getSuffixText()

    override fun getSuggestedMinimumHeight(): Int {
        return min_size
    }

    override fun getSuggestedMinimumWidth(): Int {
        return min_size
    }

    val progressPercentage: Float
        get() = getProgress() / max.toFloat()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        rectF[0f, 0f, MeasureSpec.getSize(widthMeasureSpec).toFloat()] =
            MeasureSpec.getSize(heightMeasureSpec).toFloat()
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        val yHeight = getProgress() / max.toFloat() * height
        val radius = width / 2f
        val angle = (Math.acos(((radius - yHeight) / radius).toDouble()) * 180 / Math.PI).toFloat()
        val startAngle = 90 + angle
        val sweepAngle = 360 - angle * 2
        paint.color = getUnfinishedColor()
        canvas.drawArc(rectF, startAngle, sweepAngle, false, paint)
        canvas.save()
        canvas.rotate(180f, (width / 2).toFloat(), (height / 2).toFloat())
        paint.color = getFinishedColor()
        canvas.drawArc(rectF, 270 - angle, angle * 2, false, paint)
        canvas.restore()

        // Also works.
//        paint.setColor(getFinishedColor());
//        canvas.drawArc(rectF, 90 - angle, angle * 2, false, paint);
        val text = drawText
        if (!TextUtils.isEmpty(text)) {
            val textHeight = textPaint!!.descent() + textPaint!!.ascent()
            canvas.drawText(
                text,
                (width - textPaint!!.measureText(text)) / 2.0f,
                (width - textHeight) / 2.0f,
                textPaint!!
            )
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState())
        bundle.putInt(INSTANCE_TEXT_COLOR, getTextColor())
        bundle.putFloat(INSTANCE_TEXT_SIZE, getTextSize())
        bundle.putInt(INSTANCE_FINISHED_STROKE_COLOR, getFinishedColor())
        bundle.putInt(INSTANCE_UNFINISHED_STROKE_COLOR, getUnfinishedColor())
        bundle.putInt(INSTANCE_MAX, max)
        bundle.putInt(INSTANCE_PROGRESS, getProgress())
        bundle.putString(INSTANCE_SUFFIX, getSuffixText())
        bundle.putString(INSTANCE_PREFIX, getPrefixText())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            val bundle = state
            textColor = bundle.getInt(INSTANCE_TEXT_COLOR)
            textSize = bundle.getFloat(INSTANCE_TEXT_SIZE)
            finishedColor = bundle.getInt(INSTANCE_FINISHED_STROKE_COLOR)
            unfinishedColor = bundle.getInt(INSTANCE_UNFINISHED_STROKE_COLOR)
            initPainters()
            max = bundle.getInt(INSTANCE_MAX)
            setProgress(bundle.getInt(INSTANCE_PROGRESS))
            prefixText = bundle.getString(INSTANCE_PREFIX).toString()
            suffixText = bundle.getString(INSTANCE_SUFFIX).toString()
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE))
            return
        }
        super.onRestoreInstanceState(state)
    }

    companion object {
        private const val INSTANCE_STATE = "saved_instance"
        private const val INSTANCE_TEXT_COLOR = "text_color"
        private const val INSTANCE_TEXT_SIZE = "text_size"
        private const val INSTANCE_FINISHED_STROKE_COLOR = "finished_stroke_color"
        private const val INSTANCE_UNFINISHED_STROKE_COLOR = "unfinished_stroke_color"
        private const val INSTANCE_MAX = "max"
        private const val INSTANCE_PROGRESS = "progress"
        private const val INSTANCE_SUFFIX = "suffix"
        private const val INSTANCE_PREFIX = "prefix"
    }

    init {
        default_text_size = Utils.sp2px(resources, 18f)
        min_size = Utils.dp2px(resources, 100f).toInt()
        val attributes =
            context.theme.obtainStyledAttributes(attrs, R.styleable.CircleProgress, defStyleAttr, 0)
        initByAttributes(attributes)
        attributes.recycle()
        initPainters()
    }
}