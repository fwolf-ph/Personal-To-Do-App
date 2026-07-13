package com.example.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Vibrator
import android.os.VibrationEffect
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class TimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("todo_settings", Context.MODE_PRIVATE)
        val isRunning = prefs.getBoolean("timer_is_running", false)
        if (!isRunning) return

        val secondsAtStart = prefs.getInt("timer_seconds_at_start", 0)
        val lastAccumulated = prefs.getInt("timer_accumulated_stats_seconds", 0)
        
        // Add final delta of focus time to daily stats
        val finalDelta = maxOf(0, secondsAtStart - lastAccumulated)
        if (finalDelta > 0) {
            val cal = java.util.Calendar.getInstance()
            val key = "focus_sec_${cal.get(java.util.Calendar.YEAR)}_${cal.get(java.util.Calendar.DAY_OF_YEAR)}"
            val currentSec = prefs.getInt(key, 0)
            prefs.edit().putInt(key, currentSec + finalDelta).apply()
        }

        // Reset timer states in persistent preferences
        prefs.edit()
            .putBoolean("timer_is_running", false)
            .putInt("timer_accumulated_stats_seconds", 0)
            .putInt("timer_paused_left", -1)
            .apply()

        // Play notification ringtone
        try {
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, notificationUri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Vibrate the device
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(1000)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Post a system notification
        showNotification(context)
    }

    private fun showNotification(context: Context) {
        val channelId = "focus_timer_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Fokus-Timer",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Benachrichtigungen für abgelaufene Fokus-Timer"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Fokus-Timer beendet")
            .setContentText("Gut gemacht! Der Fokus-Timer ist abgelaufen.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(1001, notification)
    }
}
