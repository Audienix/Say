package com.twain.say.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class AlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val noteId = intent.getIntExtra("noteId", 0)
        val noteTitle = intent.getStringExtra("noteTitle")
        val noteDescription = intent.getStringExtra("noteDescription")
        val noteColor = intent.getIntExtra("noteColor", -1)
        val noteLastModificationDate = intent.getLongExtra("noteLastModificationDate", -1L)
        val noteSize = intent.getStringExtra("noteSize")
        val noteAudioLength = intent.getLongExtra("noteAudioLength", 0L)
        val noteFilePath = intent.getStringExtra("noteFilePath")
        val noteStarted = intent.getBooleanExtra("noteStarted", false)
        val noteReminder = intent.getLongExtra("noteReminder", -1L)

        val data = Data.Builder()
        data.apply {
            putInt("noteId", noteId)
            putString("noteTitle", noteTitle)
            putString("noteDescription", noteDescription)
            putInt("noteColor", noteColor)
            putLong("noteLastModificationDate", noteLastModificationDate)
            putString("noteSize", noteSize)
            putLong("noteAudioLength", noteAudioLength)
            putString("noteFilePath", noteFilePath)
            putBoolean("noteStarted", noteStarted)
            putLong("noteReminder", noteReminder)
        }
        val notificationRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
        notificationRequest
            .setInitialDelay(10000, TimeUnit.MILLISECONDS).setInputData(data.build())
        WorkManager.getInstance(context).beginUniqueWork(
            "Audio Notes notification work",
            ExistingWorkPolicy.REPLACE,
            notificationRequest.build()
        ).enqueue()
    }
}