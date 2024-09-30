package com.example.alarmavisual.widget

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import com.example.alarmavisual.MainActivity
import com.example.alarmavisual.R
import com.example.alarmavisual.alarm.CustomAlarmManager
import com.example.alarmavisual.alarm.Alarm
import java.text.SimpleDateFormat
import java.util.*

class AlarmWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        //Toast.makeText(context, "Widget actualizado", Toast.LENGTH_SHORT).show()

        // Actualizar cada widget con la alarma más cercana
        appWidgetIds.forEach { appWidgetId ->
            val alarmManager = CustomAlarmManager(context)

            // Obtener alarmas de Firestore de manera asíncrona
            alarmManager.getAlarmsFromFirestore(context) { alarms ->
                val nextAlarm = getNextAlarm(alarms)
                //Toast.makeText(context, "getNextAlarm: $nextAlarm", Toast.LENGTH_SHORT).show()
                updateAppWidget(context, appWidgetManager, appWidgetId, nextAlarm)
                // Configurar actualización cada 15 minutos
                setAlarmForNextUpdate(context)
            }
        }
    }

    private fun getNextAlarm(alarms: List<Alarm>): Alarm? {
        return alarms.filter { it.active }
            .minByOrNull { it.getNextTriggerTime() }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        alarm: Alarm?
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        if (alarm != null) {
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            views.setTextViewText(R.id.widget_alarm_time, dateFormat.format(Date(alarm.getNextTriggerTime())))
            views.setTextViewText(R.id.widget_alarm_label, alarm.label)

            // Set an intent to launch the MainActivity when clicked
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent: PendingIntent = TaskStackBuilder.create(context).run {
                addNextIntentWithParentStack(intent)
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)
        } else {
            views.setTextViewText(R.id.widget_alarm_time, "No hay alarmas")
            views.setTextViewText(R.id.widget_alarm_label, "")
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun setAlarmForNextUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmWidgetProvider::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + AlarmManager.INTERVAL_FIFTEEN_MINUTES
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)

        //Toast.makeText(context, "Widget actualizado cada 15 min", Toast.LENGTH_SHORT).show()
    }
}