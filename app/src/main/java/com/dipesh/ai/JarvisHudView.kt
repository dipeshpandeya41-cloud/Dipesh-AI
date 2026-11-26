package com.dipesh.ai

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.LinearGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.SystemClock
import android.view.View
import java.util.Random
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Animated holographic orb with orbital rings, light beam and glowing platform.
 * Purely decorative drawing: it uses no sensors, network, or additional permissions.
 */
internal class JarvisHudView(context: Context) : View(context) {
    private class Particle(val angle: Float, val dist: Float, val speed: Float, val size: Float, val phase: Float)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val density = resources.displayMetrics.density
    private val path = Path()
    private val oval = RectF()
    private var attached = false

    private val random = Random(11L)
    private val particles = List(64) {
        Particle(
            angle = random.nextFloat() * 6.2832f,
            dist = 0.75f + random.nextFloat() * 1.15f,
            speed = (random.nextFloat() - 0.5f) * 0.5f,
            size = 0.7f + random.nextFloat() * 1.6f,
            phase = random.nextFloat() * 6.2832f
        )
    }
    private val risers = List(16) {
        Particle(
            angle = random.nextFloat() * 6.2832f,
            dist = random.nextFloat(),
            speed = 0.12f + random.nextFloat() * 0.22f,
            size = 0.8f + random.nextFloat() * 1.3f,
            phase = random.nextFloat()
        )
    }

    private var cx = 0f
    private var cy = 0f
    private var radius = 0f
    private var baseY = 0f
    private var halo: Shader? = null
    private var body: Shader? = null
    private var beam: Shader? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attached = true
        invalidate()
    }

    override fun onDetachedFromWindow() {
        attached = false
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        buildShaders()
    }

    private fun buildShaders() {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        cx = w / 2f
        cy = h * 0.40f
        radius = min(w * 0.25f, h * 0.27f)
        baseY = h * 0.85f
        val accent = Holo.accent
        halo = RadialGradient(
            cx, cy, radius * 2.4f,
            intArrayOf(Holo.withAlpha(accent, 80), Holo.withAlpha(accent, 22), 0),
            floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP
        )
        body = RadialGradient(
            cx - radius * 0.22f, cy - radius * 0.28f, radius * 1.3f,
            intArrayOf(
                0xEBF2FFFF.toInt(),
                Holo.withAlpha(accent, 215),
                Holo.withAlpha(Holo.blend(accent, 0xFF0A2A80.toInt(), 0.65f), 225),
                0xF0040A1E.toInt()
            ),
            floatArrayOf(0f, 0.32f, 0.72f, 1f), Shader.TileMode.CLAMP
        )
        beam = LinearGradient(
            0f, cy + radius, 0f, baseY,
            Holo.withAlpha(accent, 0), Holo.withAlpha(accent, 120), Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        if (halo == null) buildShaders()
        val t = SystemClock.uptimeMillis() / 1000f
        val d = density
        val accent = Holo.accent
        val r = radius

        // soft halo behind everything
        paint.style = Paint.Style.FILL
        paint.shader = halo
        canvas.drawCircle(cx, cy, r * 2.4f, paint)
        paint.shader = null

        // light beam from the platform up to the orb
        path.reset()
        path.moveTo(cx - r * 0.45f, cy + r * 0.9f)
        path.lineTo(cx + r * 0.45f, cy + r * 0.9f)
        path.lineTo(cx + r * 1.25f, baseY)
        path.lineTo(cx - r * 1.25f, baseY)
        path.close()
        paint.shader = beam
        canvas.drawPath(path, paint)
        paint.shader = null

        drawPlatform(canvas, t)

        // back half of the orbital rings
        drawRing(canvas, t, -14f, r * 1.62f, r * 0.30f, Holo.GOLD, 0.9f, false)
        drawRing(canvas, t, 9f, r * 1.38f, r * 0.22f, accent, -1.3f, false)

        // sphere body
        paint.style = Paint.Style.FILL
        paint.shader = body
        canvas.drawCircle(cx, cy, r, paint)
        paint.shader = null

        // swirling energy ribbons, clipped to the sphere
        canvas.save()
        path.reset()
        path.addCircle(cx, cy, r * 0.97f, Path.Direction.CW)
        canvas.clipPath(path)
        paint.style = Paint.Style.STROKE
        for (k in 0 until 7) {
            val color = when (k % 3) {
                0 -> accent
                1 -> Holo.MAGENTA
                else -> Holo.GOLD
            }
            val amp = r * (0.16f + 0.05f * (k % 3))
            val yOff = (k - 3) * r * 0.27f
            val speed = 0.55f + 0.14f * k
            path.reset()
            var x = cx - r
            var first = true
            val stepX = r / 22f
            while (x <= cx + r) {
                val y = cy + yOff + amp * sin((x - cx) / r * (2.2f + 0.28f * k) + t * speed + k)
                if (first) { path.moveTo(x, y); first = false } else path.lineTo(x, y)
                x += stepX
            }
            paint.strokeWidth = (1.2f + (k % 3) * 0.6f) * d
            paint.color = Holo.withAlpha(color, 150)
            canvas.drawPath(path, paint)
        }
        canvas.restore()

        // bright core
        paint.style = Paint.Style.FILL
        val pulse = 1f + 0.12f * sin(t * 3f)
        for (i in 3 downTo 1) {
            paint.color = Holo.withAlpha(0xFFFFFFFF.toInt(), 28 + (4 - i) * 45)
            canvas.drawCircle(cx, cy, r * 0.075f * i * pulse, paint)
        }

        // glassy rim
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.6f * d
        paint.color = Holo.withAlpha(accent, 170)
        canvas.drawCircle(cx, cy, r, paint)
        paint.strokeWidth = 5f * d
        paint.color = Holo.withAlpha(accent, 28)
        canvas.drawCircle(cx, cy, r + 2f * d, paint)

        // front half of the rings
        drawRing(canvas, t, -14f, r * 1.62f, r * 0.30f, Holo.GOLD, 0.9f, true)
        drawRing(canvas, t, 9f, r * 1.38f, r * 0.22f, accent, -1.3f, true)

        drawParticles(canvas, t)
        drawTitle(canvas, w, h)

        if (attached) postInvalidateDelayed(40L)
    }

    private fun drawRing(canvas: Canvas, t: Float, rotation: Float, rx: Float, ry: Float, color: Int, speed: Float, front: Boolean) {
        canvas.save()
        canvas.rotate(rotation, cx, cy)
        oval.set(cx - rx, cy - ry, cx + rx, cy + ry)
        paint.style = Paint.Style.STROKE
        paint.shader = null
        // the ring is split into a back arc (behind the orb) and a front arc
        val start = if (front) 0f else 180f
        paint.strokeWidth = 4.5f * density
        paint.color = Holo.withAlpha(color, 30)
        canvas.drawArc(oval, start, 180f, false, paint)
        paint.strokeWidth = 1.5f * density
        paint.color = Holo.withAlpha(color, if (front) 230 else 120)
        canvas.drawArc(oval, start, 180f, false, paint)
        val a = t * speed
        val sinA = sin(a)
        if ((sinA >= 0f) == front) {
            paint.style = Paint.Style.FILL
            paint.color = 0xFFFFFFFF.toInt()
            canvas.drawCircle(cx + cos(a) * rx, cy + sinA * ry, 3f * density, paint)
            paint.color = Holo.withAlpha(color, 90)
            canvas.drawCircle(cx + cos(a) * rx, cy + sinA * ry, 6f * density, paint)
        }
        canvas.restore()
    }

    private fun drawPlatform(canvas: Canvas, t: Float) {
        val d = density
        val accent = Holo.accent
        val colors = intArrayOf(accent, Holo.GOLD, Holo.MAGENTA, accent, 0xFFBFFBFF.toInt())

        // dark slab under the rings
        // keep the whole platform inside the view on narrow screens
        val pr = min(radius, width * 0.46f / 2.15f)
        val slabRx = pr * 2.15f
        oval.set(cx - slabRx, baseY - slabRx * 0.11f, cx + slabRx, baseY + slabRx * 0.17f)
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.color = 0xE6070F1C.toInt()
        canvas.drawOval(oval, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * d
        paint.color = Holo.withAlpha(accent, 90)
        canvas.drawOval(oval, paint)

        for (i in 0 until 5) {
            val rx = pr * (1.95f - i * 0.36f)
            val ry = rx * 0.17f
            oval.set(cx - rx, baseY - ry, cx + rx, baseY + ry)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = (1.2f + (4 - i) * 0.1f) * d
            paint.color = Holo.withAlpha(colors[i], 85)
            canvas.drawOval(oval, paint)
            val dir = if (i % 2 == 0) 1f else -1f
            paint.strokeWidth = 2.4f * d
            paint.color = Holo.withAlpha(colors[i], 235)
            canvas.drawArc(oval, t * 45f * dir + i * 70f, 64f, false, paint)
        }
        paint.style = Paint.Style.FILL
        paint.color = Holo.withAlpha(0xFFFFFFFF.toInt(), 90)
        canvas.drawCircle(cx, baseY, 3f * d, paint)

        // sparkles rising along the beam
        for (p in risers) {
            val prog = (t * p.speed + p.phase) % 1f
            val x = cx + sin(p.angle + t * 0.6f) * radius * 0.75f * (1f - prog * 0.5f)
            val y = baseY - prog * (baseY - cy - radius * 0.9f)
            val a = (1f - prog) * 210f
            paint.color = Holo.withAlpha(accent, a.toInt())
            canvas.drawCircle(x, y, p.size * density, paint)
        }
    }

    private fun drawParticles(canvas: Canvas, t: Float) {
        paint.style = Paint.Style.FILL
        paint.shader = null
        for (p in particles) {
            val ang = p.angle + t * p.speed
            val dist = radius * p.dist
            val x = cx + cos(ang) * dist * 1.18f
            val y = cy + sin(ang) * dist * 0.80f
            val alpha = 60 + (190 * abs(sin(t * 1.6f + p.phase))).toInt()
            paint.color = Holo.withAlpha(if (p.size > 1.8f) 0xFFFFFFFF.toInt() else Holo.accent, alpha)
            canvas.drawCircle(x, y, p.size * density, paint)
        }
    }

    private fun drawTitle(canvas: Canvas, w: Float, h: Float) {
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.letterSpacing = 0.3f
        paint.textSize = 20f * density
        paint.color = Holo.withAlpha(Holo.accent, 70)
        canvas.drawText("DIPESH AI", w / 2f + 1f * density, 29f * density + 1f * density, paint)
        paint.color = 0xFFE6FBFF.toInt()
        canvas.drawText("DIPESH AI", w / 2f, 29f * density, paint)
        paint.typeface = Typeface.DEFAULT
        paint.letterSpacing = 0.22f
        paint.textSize = 9f * density
        paint.color = Holo.TEXT_DIM
        canvas.drawText("PERSONAL INTELLIGENCE SYSTEM", w / 2f, h - 8f * density, paint)
        paint.letterSpacing = 0f
        paint.textAlign = Paint.Align.LEFT
    }
}
