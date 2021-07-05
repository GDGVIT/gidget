package com.dscvit.gidget.common

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.dscvit.gidget.widget.GidgetWidget
import java.util.Calendar

class AppWidgetAlarm {
    companion object {
        private const val alarmID = 0
        private const val intervalMillis: Long = 300000

        fun startGidgetRefresh(context: Context) {
            try {
                val calendar: Calendar = Calendar.getInstance()
                calendar.add(Calendar.MILLISECOND, intervalMillis.toInt())

                val alarmIntent =
                    Intent(context.applicationContext, GidgetWidget::class.java).let { intent ->
                        intent.action = Utils.getOnRefreshButtonClicked()
                        PendingIntent.getBroadcast(context.applicationContext, 0, intent, 0)
                    }
                with(context.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager) {
                    setRepeating(
                        AlarmManager.RTC,
                        calendar.timeInMillis,
                        intervalMillis,
                        alarmIntent
                    )
                }
            } catch (e: Throwable) {
                println(e.message)
            }
        }

        fun stopGidgetRefresh(context: Context) {
            try {
                val alarmIntent = Intent(context.applicationContext, GidgetWidget::class.java)
                alarmIntent.action = Utils.getOnRefreshButtonClicked()
                val pendingIntent = PendingIntent.getBroadcast(context.applicationContext, alarmID, alarmIntent, 0)
                val alarmManager = context.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(pendingIntent)
            } catch (e: Exception) {
                println(e.message)
            }
        }

//        fun isGidgetRefreshOn(context: Context): Boolean {
//            return try {
//                val alarmIntent = Intent(context.applicationContext, GidgetWidget::class.java)
//                val alarmManager = context.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//                alarmManager.nextAlarmClock.showIntent.cancel()
//                PendingIntent.getBroadcast(context.applicationContext, 0, alarmIntent, 0) != null
//            } catch (e: Throwable) {
//                println(e.message)
//                false
//            }
//        }
    }
}
