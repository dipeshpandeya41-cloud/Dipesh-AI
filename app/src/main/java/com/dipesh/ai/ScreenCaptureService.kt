package com.dipesh.ai

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import java.io.ByteArrayOutputStream

class ScreenCaptureService : Service() {
    private var projection: MediaProjection? = null
    private var reader: ImageReader? = null
    private var display: VirtualDisplay? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotification()
        val code = intent?.getIntExtra("resultCode", 0) ?: 0
        val data = if (Build.VERSION.SDK_INT >= 33)
            intent?.getParcelableExtra("data", Intent::class.java)
        else @Suppress("DEPRECATION") (intent?.getParcelableExtra("data") as? Intent)
        if (code == 0 || data == null) { stopSelf(); return START_NOT_STICKY }
        try {
            val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            projection = manager.getMediaProjection(code, data)
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealMetrics(metrics)
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            val density = metrics.densityDpi
            reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
            projection?.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() { stopCapture() }
            }, Handler(Looper.getMainLooper()))
            display = projection?.createVirtualDisplay(
                "DipeshAIScreenCapture", width, height, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                reader?.surface, null, Handler(Looper.getMainLooper())
            )
            Handler(Looper.getMainLooper()).postDelayed({ captureFrame() }, 900)
        } catch (_: Exception) {
            sendBroadcast(Intent(ACTION_RESULT).setPackage(packageName).putExtra("error", "स्क्रीन कैप्चर शुरू नहीं हो पाया।"))
            stopCapture()
        }
        return START_NOT_STICKY
    }

    private fun createNotification() {
        val channelId = "dipesh_screen_capture"
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId, "Screen capture", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification = if (Build.VERSION.SDK_INT >= 26)
            Notification.Builder(this, channelId).setContentTitle("DIPESH AI")
                .setContentText("तुम्हारी अनुमति से स्क्रीन कैप्चर हो रहा है")
                .setSmallIcon(android.R.drawable.ic_menu_camera).build()
        else @Suppress("DEPRECATION") (Notification.Builder(this)
            .setContentTitle("DIPESH AI").setContentText("Screen capture active")
            .setSmallIcon(android.R.drawable.ic_menu_camera).build())
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(2, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        } else startForeground(2, notification)
    }

    private fun captureFrame() {
        try {
            val image = reader?.acquireLatestImage()
            if (image == null) {
                sendBroadcast(Intent(ACTION_RESULT).setPackage(packageName).putExtra("error", "स्क्रीन फ्रेम नहीं मिला।"))
                stopCapture()
                return
            }
            image.use {
                val plane = it.planes[0]
                val buffer = plane.buffer
                val pixelStride = plane.pixelStride
                val rowStride = plane.rowStride
                val rowPadding = rowStride - pixelStride * it.width
                val full = Bitmap.createBitmap(
                    it.width + rowPadding / pixelStride, it.height, Bitmap.Config.ARGB_8888
                )
                full.copyPixelsFromBuffer(buffer)
                val cropped = Bitmap.createBitmap(full, 0, 0, it.width, it.height)
                val bytes = ByteArrayOutputStream().use { out ->
                    cropped.compress(Bitmap.CompressFormat.JPEG, 75, out)
                    out.toByteArray()
                }
                sendBroadcast(Intent(ACTION_RESULT).setPackage(packageName).putExtra("image", bytes))
                full.recycle()
                cropped.recycle()
            }
        } catch (_: Exception) {
            sendBroadcast(Intent(ACTION_RESULT).setPackage(packageName).putExtra("error", "स्क्रीन इमेज पढ़ने में समस्या हुई।"))
        }
        stopCapture()
    }

    private fun stopCapture() {
        try { display?.release() } catch (_: Exception) {}
        try { reader?.close() } catch (_: Exception) {}
        try { projection?.stop() } catch (_: Exception) {}
        display = null
        reader = null
        projection = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() { stopCapture(); super.onDestroy() }

    companion object {
        const val ACTION_RESULT = "com.dipesh.ai.SCREEN_CAPTURE_RESULT"
    }
}
