package com.twain.say.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION
import android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_ALL
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.twain.say.MainActivity
import com.twain.say.R
import com.twain.say.ui.home.model.Note

class NotificationWorker(private val appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {

        val noteId = inputData.getInt("noteId", 0)
        val noteTitle = inputData.getString("noteTitle")
        val noteDescription = inputData.getString("noteDescription")
        val noteColor = inputData.getInt("noteColor", -1)
        val noteLastModificationDate = inputData.getLong("noteLastModificationDate", -1L)
        val noteSize = inputData.getString("noteSize")
        val noteAudioLength = inputData.getLong("noteAudioLength", 0L)
        val noteFilePath = inputData.getString("noteFilePath")
        val noteStarted = inputData.getBoolean("noteStarted", false)
        val noteReminder = inputData.getLong("noteReminder", -1L)

        val note = Note(
            noteId,
            noteTitle!!,
            noteDescription!!,
            noteColor,
            noteLastModificationDate,
            noteSize!!,
            noteAudioLength,
            noteFilePath!!,
            noteStarted,
            noteReminder
        )
        notifyUser(appContext, note)

        return Result.success()
    }

    private fun notifyUser(context: Context, note: Note) {

        val channelId = context.getString(R.string.channelId)

        val defaultSoundUri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val audioAttributes = AudioAttributes.Builder().setUsage(USAGE_NOTIFICATION_RINGTONE)
            .setContentType(CONTENT_TYPE_SONIFICATION).build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val pendingIntent =
            NavDeepLinkBuilder(context)
                .setGraph(R.navigation.nav_graph)
//                .setDestination(
//                    R.id.editNoteFragment,
//                    EditNoteFragmentArgs(note).toBundle()
//                )
                .setComponentName(MainActivity::class.java)
                .createPendingIntent()

        val notificationBuilder =
            NotificationCompat.Builder(context, context.getString(R.string.channelId))
                .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                .setContentTitle(note.title)
                .setContentText("Hi there, it's time to check out ${note.title}")
                .setColor(ResourcesCompat.getColor(context.resources, R.color.primary, null))
                .setSound(defaultSoundUri)
                .setDefaults(DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setPriority(PRIORITY_HIGH)
                .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(channelId)
            val channel = NotificationChannel(
                channelId,
                "Audio Notes",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            channel.setSound(defaultSoundUri, audioAttributes)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(note.id, notificationBuilder.build())
    }
}