package com.dipesh.ai

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.SystemClock
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

/** Animated JARVIS-style HUD. Uses no sensors, network, or extra permissions. */
class JarvisHudView(context: Context) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cyan = 0xFF43E8FF.toInt()
    private val green = 0xFF55F2B0.toInt()
    private val muted = 0xFF24566A.toInt()
    private val density = resources.displayMetrics.density

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h * .46f
        val r = minOf(w * .29f, h * .36f)
        canvas.drawColor(0xFF081522.toInt())
        p.style = Paint.Style.STROKE
        p.strokeWidth = 1.2f * density
        p.color = muted
        for (i in 1..4) canvas.drawCircle(cx, cy, r * i / 4f, p)
        val seconds = SystemClock.uptimeMillis() / 1000f
        p.color = cyan
        p.strokeWidth = 2f * density
        canvas.drawArc(RectF(cx-r,cy-r,cx+r,cy+r), seconds*34f, 92f, false, p)
        p.color = green
        canvas.drawArc(RectF(cx-r*.82f,cy-r*.82f,cx+r*.82f,cy+r*.82f), -seconds*57f, 54f, false, p)
        p.color = muted
        for (i in 0 until 48) {
            val a = Math.PI * 2 * i / 48
            val inner = r * .91f
            val outer = r * if (i % 4 == 0) 1.12f else 1.04f
            canvas.drawLine(cx + cos(a).toFloat()*inner, cy + sin(a).toFloat()*inner,
                cx + cos(a).toFloat()*outer, cy + sin(a).toFloat()*outer, p)
        }
        p.style = Paint.Style.FILL
        p.color = cyan
        canvas.drawCircle(cx, cy, 4f*density, p)
        p.color = 0xFFBCEFFF.toInt()
        p.textAlign = Paint.Align.CENTER
        p.textSize = 12f*density
        canvas.drawText("DIPESH AI", cx, h-20f*density, p)
        p.color = 0xFF6F9FB3.toInt()
        p.textSize = 8f*density
        canvas.drawText("PERSONAL INTELLIGENCE SYSTEM", cx, h-7f*density, p)
        postInvalidateDelayed(40)
    }
}
