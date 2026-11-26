package com.dipesh.ai

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.AlarmManager
import org.json.JSONArray
import android.os.Build

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            restorePendingReminders(context)
            return
        }
        if (intent.action == ACTION_SNOOZE) {
            snoozeReminder(context, intent.getIntExtra(EXTRA_ID, 0))
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
        val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_ID, id)
        }
        val snoozePending = PendingIntent.getBroadcast(context, id + 1000000, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = builder
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText("Your scheduled reminder")
            .setContentIntent(contentIntent)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze 10 min", snoozePending)
            .setAutoCancel(true)
            .build()
        manager.notify(id, notification)
        scheduleNextDailyOccurrence(context, id)
    }


    companion object {
        const val ACTION_SNOOZE = "com.dipesh.ai.ACTION_SNOOZE"
        const val EXTRA_TITLE = "reminder_title"
        const val EXTRA_ID = "reminder_id"

        private fun snoozeReminder(context: Context, reminderId: Int) {
            if (reminderId == 0) return
            val preferences = context.getSharedPreferences("dipesh_ai", Context.MODE_PRIVATE)
            val tasks = try { JSONArray(preferences.getString("local_tasks", "[]")) } catch (_: Exception) { JSONArray() }
            val newDue = System.currentTimeMillis() + 10L * 60L * 1000L
            var title = "DIPESH AI reminder"
            var found = false
            for (index in 0 until tasks.length()) {
                val task = tasks.optJSONObject(index) ?: continue
                if (task.optInt("reminder_id", 0) == reminderId && !task.optBoolean("done")) {
                    task.put("due_at", newDue)
                    title = task.optString("title", title)
                    found = true
                    break
                }
            }
            if (!found) return
            preferences.edit().putString("local_tasks", tasks.toString()).apply()
            val alarmIntent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_ID, reminderId)
            }
            val pending = PendingIntent.getBroadcast(context, reminderId, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, newDue, pending)
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(reminderId)
        }

        private fun scheduleNextDailyOccurrence(context: Context, reminderId: Int) {
            val preferences = context.getSharedPreferences("dipesh_ai", Context.MODE_PRIVATE)
            val tasks = try { JSONArray(preferences.getString("local_tasks", "[]")) } catch (_: Exception) { JSONArray() }
            val nextDue = System.currentTimeMillis() + 24L * 60L * 60L * 1000L
            var title = "DIPESH AI reminder"
            var found = false
            for (index in 0 until tasks.length()) {
                val task = tasks.optJSONObject(index) ?: continue
                if (task.optInt("reminder_id", 0) == reminderId && task.optBoolean("repeat_daily") && !task.optBoolean("done")) {
                    task.put("due_at", nextDue)
                    title = task.optString("title", title)
                    found = true
                    break
                }
            }
            if (!found) return
            preferences.edit().putString("local_tasks", tasks.toString()).apply()
            val nextIntent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_ID, reminderId)
            }
            val pending = PendingIntent.getBroadcast(context, reminderId, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextDue, pending)
        }

        fun restorePendingReminders(context: Context) {
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

    }
}
