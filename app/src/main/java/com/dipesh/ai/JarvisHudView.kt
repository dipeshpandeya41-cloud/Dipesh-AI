package com.dipesh.ai

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.SystemClock
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

/** Lightweight animated HUD; it uses no sensors, network, or additional permissions. */
internal class JarvisHudView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val density = resources.displayMetrics.density
    private var attached = false

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attached = true
        invalidate()
    }

    override fun onDetachedFromWindow() {
        attached = false
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        val cx = w / 2f
        val cy = h * 0.45f
        val radius = minOf(w * 0.22f, h * 0.36f)
        canvas.drawColor(0xFF081522.toInt())

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * density
        paint.color = 0xFF24566A.toInt()
        for (i in 1..4) canvas.drawCircle(cx, cy, radius * i / 4f, paint)

        val seconds = SystemClock.uptimeMillis() / 1000f
        paint.color = 0xFF43E8FF.toInt()
        paint.strokeWidth = 2f * density
        val outer = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        canvas.drawArc(outer, seconds * 34f, 92f, false, paint)
        val innerRadius = radius * 0.82f
        canvas.drawArc(
            RectF(cx - innerRadius, cy - innerRadius, cx + innerRadius, cy + innerRadius),
            -seconds * 57f, 54f, false, paint
        )

        paint.color = 0xFF24566A.toInt()
        for (i in 0 until 48) {
            val angle = Math.PI * 2.0 * i / 48.0
            val inner = radius * 0.91f
            val outerTick = radius * if (i % 4 == 0) 1.12f else 1.04f
            canvas.drawLine(
                cx + cos(angle).toFloat() * inner,
                cy + sin(angle).toFloat() * inner,
                cx + cos(angle).toFloat() * outerTick,
                cy + sin(angle).toFloat() * outerTick,
                paint
            )
        }

        paint.style = Paint.Style.FILL
        paint.color = 0xFF43E8FF.toInt()
        canvas.drawCircle(cx, cy, 4f * density, paint)
        paint.textAlign = Paint.Align.CENTER
        paint.color = 0xFFBCEFFF.toInt()
        paint.textSize = 11f * density
        canvas.drawText("DIPESH AI", cx, h - 18f * density, paint)
        paint.color = 0xFF6F9FB3.toInt()
        paint.textSize = 8f * density
        canvas.drawText("PERSONAL INTELLIGENCE SYSTEM", cx, h - 5f * density, paint)

        if (attached) postInvalidateDelayed(40L)
    }
}
