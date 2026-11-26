package com.dipesh.ai

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sin

/** Shared colours for the holographic look. Pure drawing code: no sensors, network or permissions. */
internal object Holo {
    const val BG = 0xFF03060D.toInt()
    const val TEXT = 0xFFDDF8FF.toInt()
    const val TEXT_DIM = 0xFF7DB2C4.toInt()
    const val GREEN = 0xFF4DFFB0.toInt()
    const val AMBER = 0xFFFFC857.toInt()
    const val RED = 0xFFFF6B7A.toInt()
    const val MAGENTA = 0xFFE64DFF.toInt()
    const val GOLD = 0xFFFFB85C.toInt()

    val accents = intArrayOf(0xFF2EE6FF.toInt(), 0xFFB388FF.toInt(), 0xFF4DFFB0.toInt())
    val accentNames = arrayOf("Cyan hologram (default)", "Violet hologram", "Emerald hologram")

    @Volatile
    var accent: Int = accents[0]

    fun withAlpha(color: Int, alpha: Int): Int =
        Color.argb(alpha.coerceIn(0, 255), Color.red(color), Color.green(color), Color.blue(color))

    fun blend(from: Int, to: Int, amount: Float): Int {
        val t = amount.coerceIn(0f, 1f)
        return Color.argb(
            (Color.alpha(from) + (Color.alpha(to) - Color.alpha(from)) * t).toInt(),
            (Color.red(from) + (Color.red(to) - Color.red(from)) * t).toInt(),
            (Color.green(from) + (Color.green(to) - Color.green(from)) * t).toInt(),
            (Color.blue(from) + (Color.blue(to) - Color.blue(from)) * t).toInt()
        )
    }
}

/** Glass panel: dark translucent fill, soft glow outline and bright corner brackets. */
internal class HoloPanelDrawable(private val density: Float, private val cornerDp: Float = 10f) : Drawable() {
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val rect = RectF()
    private val arc = RectF()
    private val bracket = Path()

    override fun draw(canvas: Canvas) {
        val b = bounds
        if (b.width() <= 0 || b.height() <= 0) return
        val inset = 2f * density
        rect.set(b.left + inset, b.top + inset, b.right - inset, b.bottom - inset)
        val r = cornerDp * density
        val accent = Holo.accent

        fill.shader = LinearGradient(
            0f, rect.top, 0f, rect.bottom,
            intArrayOf(0xEB0B2238.toInt(), 0xEB050E1C.toInt()), null, Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, r, r, fill)

        stroke.strokeWidth = 4f * density
        stroke.color = Holo.withAlpha(accent, 26)
        canvas.drawRoundRect(rect, r, r, stroke)
        stroke.strokeWidth = 1f * density
        stroke.color = Holo.withAlpha(accent, 120)
        canvas.drawRoundRect(rect, r, r, stroke)

        stroke.strokeWidth = 2.2f * density
        stroke.color = accent
        val len = 18f * density
        val l = rect.left
        val t = rect.top
        val rr = rect.right
        val bb = rect.bottom
        bracket.reset()
        bracket.moveTo(l, t + len)
        bracket.lineTo(l, t + r)
        arc.set(l, t, l + 2 * r, t + 2 * r)
        bracket.arcTo(arc, 180f, 90f)
        bracket.lineTo(l + len, t)

        bracket.moveTo(rr - len, t)
        bracket.lineTo(rr - r, t)
        arc.set(rr - 2 * r, t, rr, t + 2 * r)
        bracket.arcTo(arc, 270f, 90f)
        bracket.lineTo(rr, t + len)

        bracket.moveTo(rr, bb - len)
        bracket.lineTo(rr, bb - r)
        arc.set(rr - 2 * r, bb - 2 * r, rr, bb)
        bracket.arcTo(arc, 0f, 90f)
        bracket.lineTo(rr - len, bb)

        bracket.moveTo(l + len, bb)
        bracket.lineTo(l + r, bb)
        arc.set(l, bb - 2 * r, l + 2 * r, bb)
        bracket.arcTo(arc, 90f, 90f)
        bracket.lineTo(l, bb - len)
        canvas.drawPath(bracket, stroke)
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

/** Animated voice-activity bars (decorative; does not use the microphone). */
internal class WaveformView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeCap = Paint.Cap.ROUND }
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
        val t = SystemClock.uptimeMillis() / 1000f
        val bars = 44
        val step = w / bars
        paint.strokeWidth = (step * 0.45f).coerceAtLeast(1.5f * density)
        val cy = h / 2f
        for (i in 0 until bars) {
            val pos = i / (bars - 1f)
            val envelope = Math.pow(sin(Math.PI * pos), 0.7).toFloat()
            val wobble = abs(sin(t * 1.9 + i * 0.37) * sin(t * 0.8 + i * 0.19)).toFloat()
            val amp = (0.12f + 0.88f * wobble) * envelope
            val half = max(1.5f * density, amp * h * 0.46f)
            paint.color = Holo.blend(Holo.accent, Holo.MAGENTA, pos)
            val x = step * i + step / 2f
            canvas.drawLine(x, cy - half, x, cy + half, paint)
        }
        if (attached) postInvalidateDelayed(40L)
    }
}

/** Small area/line chart drawn from real values supplied by the caller. */
internal class SparklineView(context: Context, private val values: FloatArray, private val lineColor: Int) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val density = resources.displayMetrics.density
    private val line = Path()
    private val area = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f || values.size < 2) return
        val padX = 4f * density
        val padTop = 6f * density
        val padBottom = 6f * density
        val plotW = w - padX * 2
        val plotH = h - padTop - padBottom

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Holo.withAlpha(Holo.accent, 40)
        for (i in 0..3) {
            val y = padTop + plotH * i / 3f
            canvas.drawLine(padX, y, w - padX, y, paint)
        }

        val peak = max(values.maxOrNull() ?: 0f, 1f)
        fun xAt(i: Int) = padX + plotW * i / (values.size - 1f)
        fun yAt(v: Float) = padTop + plotH * (1f - v / peak)

        line.reset()
        area.reset()
        line.moveTo(xAt(0), yAt(values[0]))
        for (i in 1 until values.size) {
            val midX = (xAt(i - 1) + xAt(i)) / 2f
            line.cubicTo(midX, yAt(values[i - 1]), midX, yAt(values[i]), xAt(i), yAt(values[i]))
        }
        area.addPath(line)
        area.lineTo(xAt(values.size - 1), padTop + plotH)
        area.lineTo(xAt(0), padTop + plotH)
        area.close()

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            0f, padTop, 0f, padTop + plotH,
            Holo.withAlpha(lineColor, 110), Holo.withAlpha(lineColor, 0), Shader.TileMode.CLAMP
        )
        canvas.drawPath(area, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4.5f * density
        paint.color = Holo.withAlpha(lineColor, 40)
        canvas.drawPath(line, paint)
        paint.strokeWidth = 1.8f * density
        paint.color = lineColor
        canvas.drawPath(line, paint)

        paint.style = Paint.Style.FILL
        for (i in values.indices) {
            if (values[i] > 0f) canvas.drawCircle(xAt(i), yAt(values[i]), 2.6f * density, paint)
        }
    }
}

/** Thin glowing progress bar. */
internal class GlowBarView(context: Context, private val progress: Float) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private val density = resources.displayMetrics.density

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        val r = h / 2f
        rect.set(0f, 0f, w, h)
        paint.shader = null
        paint.color = Holo.withAlpha(Holo.accent, 38)
        canvas.drawRoundRect(rect, r, r, paint)
        val filled = (w * progress.coerceIn(0f, 1f)).coerceAtLeast(if (progress > 0f) h else 0f)
        if (filled <= 0f) return
        rect.set(0f, 0f, filled, h)
        paint.shader = LinearGradient(
            0f, 0f, filled, 0f, Holo.accent, Holo.MAGENTA, Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rect, r, r, paint)
        paint.shader = null
        paint.color = Holo.withAlpha(Holo.accent, 60)
        canvas.drawCircle(filled - r, h / 2f, r + 1.5f * density, paint)
    }
}
