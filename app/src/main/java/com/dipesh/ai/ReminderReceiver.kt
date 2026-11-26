package com.dipesh.ai

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.app.AlarmManager
import org.json.JSONArray
import android.os.Build

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            restorePendingReminders(context)
            return
        }
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "DIPESH AI reminder"
        val id = intent.getIntExtra(EXTRA_ID, 1)
        val channelId = "dipesh_ai_reminders"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId, "DIPESH AI reminders", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }
        val openApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(context, id, openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        @Suppress("DEPRECATION")
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(context, channelId)
            else Notification.Builder(context)
        val notification = builder
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText("Your scheduled reminder")
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        manager.notify(id, notification)
    }

    private fun restorePendingReminders(context: Context) {
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) return
        val preferences = context.getSharedPreferences("dipesh_ai", Context.MODE_PRIVATE)
        val tasks = try { JSONArray(preferences.getString("local_tasks", "[]")) } catch (_: Exception) { JSONArray() }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()
        for (index in 0 until tasks.length()) {
            val task = tasks.optJSONObject(index) ?: continue
            val dueAt = task.optLong("due_at", 0L)
            val reminderId = task.optInt("reminder_id", 0)
            if (task.optBoolean("done") || dueAt <= now || reminderId == 0) continue
            val reminderIntent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra(EXTRA_TITLE, task.optString("title", "DIPESH AI reminder"))
                putExtra(EXTRA_ID, reminderId)
            }
            val pending = PendingIntent.getBroadcast(context, reminderId, reminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueAt, pending)
        }
    }

    companion object {
        const val EXTRA_TITLE = "reminder_title"
        const val EXTRA_ID = "reminder_id"
    }
}
