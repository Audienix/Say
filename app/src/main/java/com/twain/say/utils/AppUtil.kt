package com.twain.say.utils

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.twain.say.R
import com.twain.say.helper.AlertReceiver
import com.twain.say.ui.home.model.Note
import com.vmadalin.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

val colors = listOf(
    "#FFCDD2", //Red 100
    "#F8BBD0", // Pink 100
    "#E1BEE7", // Purple 100
    "#D1C4E9", // Deep Purple 100
    "#C5CAE9", // Indigo 100
    "#BBDEFB", // Blue 100
    "#B3E5FC", // Light Blue 100
    "#B2EBF2", // Cyan 100
    "#B2DFDB", // Teal 100
    "#C8E6C9", // Green 100
    "#DCEDC8", // Light Green 100
    "#F0F4C3", // Lime 100
    "#FFF9C4", // Yellow 100
    "#FFECB3", // Amber 100
    "#FFE0B2", // Orange 100
    "#FFCCBC", // Deep Orange 100
    "#D7CCC8", // Brown 100
    "#F5F5F5", // Gray 100
    "#CFD8DC"  // Blue Gray 100
)

fun filePath(activity: Activity) = activity.getExternalFilesDir("/")?.absolutePath

fun hasPermission(context: Context, permission: String) =
    EasyPermissions.hasPermissions(context, permission)

fun requestPermission(activity: Activity, message: String, requestCode: Int, permission: String) {
    EasyPermissions.requestPermissions(activity, message, requestCode, permission)
}

fun currentDate(): Calendar = Calendar.getInstance()

fun formatDate(date: Long, context: Context): String {
    val now = Date()
    val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - date)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - date)
    val hours = TimeUnit.MILLISECONDS.toHours(now.time - date)
    val days = TimeUnit.MILLISECONDS.toDays(now.time - date)

    return when {
        seconds < 60 -> context.getString(R.string.just_now)
        minutes == 1L -> context.getString(R.string.a_minute_ago)
        minutes in 2..59L -> "$minutes ${context.getString(R.string.minutes_ago)}"
        hours == 1L -> context.getString(R.string.an_hour_ago)
        hours in 2..23 -> "$hours ${context.getString(R.string.hours_ago)}"
        days == 1L -> context.getString(R.string.a_day_ago)
        else -> formatSimpleDate(date)
    }
}

fun formatReminderDate(date: Long): String =
    SimpleDateFormat("dd MMM, yyyy h:mm a", Locale.getDefault()).format(date)

fun formatSimpleDate(date: Long): String =
    SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)

fun formatDateOnly(date: Long): String =
    SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(date)

fun formatTime(date: Long): String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)

fun startAlarm(context: Context, time: Long, note: Note) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlertReceiver::class.java)
    intent.apply {
        putExtra("noteId", note.id)
        putExtra("noteTitle", note.title)
        putExtra("noteDescription", note.description)
        putExtra("noteColor", note.color)
        putExtra("noteLastModificationDate", note.lastModificationDate)
        putExtra("noteSize", note.size)
        putExtra("noteAudioLength", note.audioLength)
        putExtra("noteFilePath", note.filePath)
        putExtra("noteStarted", note.started)
        putExtra("noteReminder", note.reminder)
    }
    val pendingIntent =
        PendingIntent.getBroadcast(context, note.id, intent, PendingIntent.FLAG_IMMUTABLE)
    alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent)
    Log.d("TAG", "startAlarm: Alarm started")
}

fun cancelAlarm(context: Context, noteId: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlertReceiver::class.java)
    val pendingIntent =
        PendingIntent.getBroadcast(context, noteId, intent, PendingIntent.FLAG_IMMUTABLE)
    alarmManager.cancel(pendingIntent)
    Log.d("TAG", "cancelAlarm: Alarm canceled")
}

fun roundOffDecimal(number: Double): String {
    return "%.2f".format(number)
}

fun hideKeyboard(context: Context, view: View) {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}