package com.khair.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.res.ResourcesCompat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    companion object {
        private const val DEFAULT_PIE_TITLE = "title"
        private const val DEFAULT_PIE_BORDER_COLOR = Color.WHITE
        private const val DEFAULT_PIE_BORDER_SIZE = 0.0f
        private const val DEFAULT_IS_ORDERED = false
        private const val DEFAULT_IS_DRAW_VALUES = false
        private const val DEFAULT_VALUES_TEXT_COLOR = Color.WHITE
        private const val DEFAULT_VALUES_TEXT_SIZE = 24.0f
    }

    private var size = 300.0f
    private val fullCircleAngle = 360f
    private var startAngle = 270.0f
    private var sweepAngle = 0.0f
    private var valuesSize = 0
    var valuePairs: ArrayList<Int>? = null
        set(value) {
            valuesSize = value?.let {
                it
                    .reduce{ i1: Int, i2:Int -> i1 + i2 }
            } ?: 0
            field = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val offsetBetweenTitleAndDiagram = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        16.0f,
        resources.displayMetrics)
    private var diagramCenterOffset = 0.0f

    var title = DEFAULT_PIE_TITLE
    var pieBorderSize = DEFAULT_PIE_BORDER_SIZE
        set(value) {
            val temp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                resources.displayMetrics
            )
            borderPaint.strokeWidth = temp
            field = temp
        }
    var pieBorderColor = DEFAULT_PIE_BORDER_COLOR
        set(value) {
            borderPaint.color = value
            field = value
        }
    var isDrawValues = DEFAULT_IS_DRAW_VALUES
    var isOrdered = DEFAULT_IS_ORDERED
    var valuesTextColor = DEFAULT_VALUES_TEXT_COLOR
        set(value) {
            textPaint.color = value
            field = value
        }
    var valuesTextSize = DEFAULT_VALUES_TEXT_SIZE
        set(value) {
            val temp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                value,
                resources.displayMetrics
            )
            textPaint.textSize = temp
            field = temp
        }

    init {
        setupAttributes(attrs)
        setupPaints()
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.PieChartView, 0, 0)
        title = typedArray.getString(R.styleable.PieChartView_title) ?: DEFAULT_PIE_TITLE
        pieBorderSize = typedArray.getDimension(R.styleable.PieChartView_pieBorderSize,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_PIE_BORDER_SIZE,
                resources.displayMetrics)
        )
        pieBorderColor = typedArray.getColor(R.styleable.PieChartView_pieBorderColor, DEFAULT_PIE_BORDER_COLOR)
        isDrawValues = typedArray.getBoolean(R.styleable.PieChartView_isDrawValues, DEFAULT_IS_DRAW_VALUES)
        isOrdered = typedArray.getBoolean(R.styleable.PieChartView_isOrdered, DEFAULT_IS_ORDERED)
        valuesTextColor = typedArray.getColor(R.styleable.PieChartView_valuesTextColor, DEFAULT_VALUES_TEXT_COLOR)
        valuesTextSize = typedArray.getDimension(R.styleable.PieChartView_valuesTextSize,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                DEFAULT_VALUES_TEXT_SIZE,
                resources.displayMetrics)
        )
        typedArray.recycle()
    }

    private fun setupPaints() {
        paint.apply {
            color = DEFAULT_PIE_BORDER_COLOR
            style = Paint.Style.FILL
        }
        borderPaint.apply {
            color = pieBorderColor
            strokeWidth = pieBorderSize
            style = Paint.Style.STROKE
        }
        textPaint.apply {
            color = valuesTextColor
            textSize = valuesTextSize
            style = Paint.Style.FILL_AND_STROKE
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        size = min(measuredWidth, measuredHeight).toFloat()
        diagramCenterOffset = 0.0f
        if (title != DEFAULT_PIE_TITLE)
            diagramCenterOffset = textPaint.fontMetrics.ascent * -1.0f + offsetBetweenTitleAndDiagram
        if (valuesSize != 0 && valuePairs?.size ?: 0 > 0)
            setMeasuredDimension(size.toInt(), (size + diagramCenterOffset).toInt())
        else
            setMeasuredDimension(0, 0)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if(title != DEFAULT_PIE_TITLE)
            drawTitle(canvas)
        valuePairs?.let {
            if(isOrdered)
                it.sortWith(Comparator { p0, p1 -> p0 - p1 })
            for ((counter, value) in it.withIndex()) {
                paint.color = colorForChart(counter)
                sweepAngle = fullCircleAngle * value / valuesSize
                drawPieChart(canvas)
                if(pieBorderSize > 0.0f)
                    drawStroke(canvas)
                if(isDrawValues)
                    drawTextInsideRectangle(canvas, value.toString())
                startAngle = (startAngle + sweepAngle) % fullCircleAngle
            }
        }
    }

    private fun drawTitle(canvas: Canvas?) {
        val center = size/2
        val xCenter = center
        val yCenter = 0.0f + paddingTop
        val xOffset = textPaint.measureText(title) * 0.5f
        val yOffset = textPaint.fontMetrics.ascent * -0.8f
        val textX = xCenter - xOffset
        val textY = yCenter + yOffset
        canvas?.drawText(title, textX, textY, textPaint)
    }

    private fun drawPieChart(canvas: Canvas?) {
        canvas?.drawArc(0f + paddingStart, 0f + paddingTop + diagramCenterOffset, size - paddingEnd,
            size - paddingBottom + diagramCenterOffset, startAngle, sweepAngle, true, paint)
    }

    private fun drawStroke(canvas: Canvas?) {
        canvas?.drawArc(0f + paddingStart, 0f + paddingTop + diagramCenterOffset, size - paddingEnd,
            size - paddingBottom + diagramCenterOffset, startAngle, sweepAngle, true, borderPaint)
    }

    private fun drawTextInsideRectangle(canvas: Canvas?, str: String) {
        val center = size/2
        val radius = size/2 - max(max(paddingStart, paddingEnd), max(paddingTop, paddingBottom))
        val angle = (startAngle + sweepAngle / 2) % fullCircleAngle
        val xCenter = center + radius * 2 * cos(angle * Math.PI / 180) / 3
        val yCenter = center + radius * 2 * sin(angle * Math.PI / 180) / 3 + diagramCenterOffset
        val xOffset = textPaint.measureText(str) * 0.5f
        val yOffset = textPaint.fontMetrics.ascent * -0.4f
        val textX = xCenter - xOffset
        val textY = yCenter + yOffset
        canvas?.drawText(str, textX.toFloat(), textY.toFloat(), textPaint)
    }

    private fun colorForChart(counter: Int): Int = when(counter) {
        0 -> ResourcesCompat.getColor(resources, android.R.color.holo_green_light, null)
        1 -> ResourcesCompat.getColor(resources, android.R.color.holo_blue_light, null)
        2 -> ResourcesCompat.getColor(resources, android.R.color.holo_red_light, null)
        3 -> ResourcesCompat.getColor(resources, android.R.color.holo_orange_light, null)
        4 -> ResourcesCompat.getColor(resources, android.R.color.holo_purple, null)
        5 -> Color.BLUE
        6 -> Color.RED
        7 -> Color.YELLOW
        8 -> Color.GREEN
        else -> Color.BLUE
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        var viewState = state
        if (viewState is Bundle) {
            viewState = viewState.getParcelable("superState")
        }
        super.onRestoreInstanceState(viewState)
    }
}