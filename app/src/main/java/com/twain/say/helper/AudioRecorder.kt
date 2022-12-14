package com.twain.say.helper

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import com.twain.say.R
import com.twain.say.constants.IntegerConstants
import com.twain.say.constants.StringConstants
import com.twain.say.timer.OnStopwatchTickListener
import com.twain.say.timer.OnTimerTickListener
import com.twain.say.timer.SayStopwatch
import com.twain.say.timer.SayTimer
import com.twain.say.ui.home.model.Note
import com.twain.say.utils.Extensions.showToast
import com.twain.say.utils.filePath
import com.twain.say.utils.roundOffDecimal
import java.io.File
import java.io.IOException

class AudioRecorder(
    private val context: Context,
    private val btnRecord: AppCompatImageView,
    private val tvTimer: TextView,
    private val note: Note,
    private val seekbar: SeekBar? = null
) :
    OnStopwatchTickListener,
    OnTimerTickListener {

    private val logTag = this::class.qualifiedName
    private var sayTimer: SayTimer? = null
    private var isPlayingRecord = false
    private var isRecording = false
    private var sayStopwatch: SayStopwatch? = null
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var mediaRecorder: MediaRecorder

    init {
        sayStopwatch = SayStopwatch()
        sayStopwatch!!.setOnTickListener(this)
        sayStopwatch!!.setTextView(tvTimer)
        seekbar?.setOnSeekBarChangeListener( object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
    }

    override fun onStopwatchTick(stopwatch: SayStopwatch?) {
        tvTimer.text = stopwatch?.elapsedTime.toString()
    }

    override fun onTimerTick(timer: SayTimer?) {
        seekbar?.progress = mediaPlayer.currentPosition
    }

    override fun onTimerCompletion(timer: SayTimer?) {
//        btnRecord.setBackgroundResource(R.drawable.ic_circle)
//        ViewCompat.setBackgroundTintList(
//            btnRecord,
//            ColorStateList.valueOf(note.color)
//        )
        btnRecord.setImageDrawable(
            ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.ic_baseline_play_recording,
                null
            )
        ).run {
            isPlayingRecord = false
            stopPlayingRecording()
        }
    }

    fun startRecording() {
        val filePath = filePath(context as Activity)
        val fileName =
            "${System.currentTimeMillis()}${StringConstants.AUDIO_RECORDING_FILE_EXTENSION}"
        note.filePath = "$filePath/$fileName"
        context.getString(R.string.started_recording)
            .showToast(context, Toast.LENGTH_SHORT)

//            .startFormat("MM:SS")
//            .onTick { time -> binding.tvTimer.text = time }
//            .changeFormatWhen(1, TimeUnit.HOURS, "HH:MM:SS")
//            .build()

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile("$filePath/$fileName")
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
                start()
                sayStopwatch!!.start()
                isRecording = true
            } catch (e: IOException) {
                context.getString(R.string.error_occurred)
                    .showToast(context, Toast.LENGTH_SHORT)
            }
        }
    }

    fun stopRecording() {
        mediaRecorder.apply {
            if(isRecording) {
                stop()
                release()
                isRecording = false
            }
        }
        sayStopwatch?.apply {
            stop()
            note.audioLength = sayStopwatch!!.elapsedTime / 1000
        }
        if (note.audioLength <= 0)
            return
        val file = File(note.filePath)
        val fileByte = (file.readBytes().size.toDouble() / 1048576.00)
        val fileSize = roundOffDecimal(fileByte)
        note.size = fileSize
        context.getString(R.string.stopped_recording)
            .showToast(context, Toast.LENGTH_SHORT)
    }

    private fun startPlayingRecording() {
        val recordingDuration = note.audioLength * IntegerConstants.TIME_DURATION
        sayTimer = SayTimer(recordingDuration)
        sayTimer!!.setTextView(tvTimer)
        sayTimer!!.setOnTickListener(this)
        mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.apply {
                val file = File(note.filePath)
                setDataSource(file.absolutePath)
                prepare()
                if (seekbar != null)
                    seekbar.max = duration
                start()
            }
            if (!sayTimer!!.isStarted) sayTimer!!.start()

            context.getString(R.string.started_playing_recording)
                .showToast(context, Toast.LENGTH_SHORT)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("TAG", "startPlayingRecording: ${e.localizedMessage}")
            context.getString(R.string.error_occurred)
                .showToast(context, Toast.LENGTH_SHORT)
        }
    }

    private fun pausePlayingRecording() {
        mediaPlayer.pause()
        if (!sayTimer!!.isPaused && sayTimer!!.isStarted) sayTimer!!.pause()
    }

    private fun resumePlayingRecording() {
        mediaPlayer.start()
        if (sayTimer!!.isPaused) sayTimer!!.resume()
    }

    private fun stopPlayingRecording() {
        mediaPlayer.apply {
            stop()
            release()
        }
        if (sayTimer!!.isStarted) sayTimer!!.stop()
        sayTimer = null
        context.getString(R.string.stopped_playing_recording)
            .showToast(context, Toast.LENGTH_SHORT)
    }

    private fun finishStopWatch() {
        sayStopwatch?.apply {
            if (isStarted || isPaused)
                stop()
        }
        sayStopwatch = null
    }

    private fun stopTimer() {
        if (sayTimer?.isStarted == true) sayTimer?.stop()
        sayTimer = null
    }

    fun cleanupResource() {
        try {
            mediaRecorder.apply {
                stop()
                release()
            }
            mediaPlayer.apply {
                if (isPlaying) {
                    stop()
                    release()
                }
            }
            stopTimer()
            finishStopWatch()
        } catch (illegalStateException: IllegalStateException) {
            illegalStateException.message?.let { Log.d(logTag, it) }
            Log.d(logTag, illegalStateException.stackTraceToString())
        }
        catch (exception: Exception)
        {}
    }

    fun manageExistingAudioRecording() {
        if (!isPlayingRecord) {
            btnRecord.setImageDrawable(
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.ic_baseline_pause_recording, null
                )
            )
                .run {
                    isPlayingRecord = true
                    if (sayTimer == null)
                        startPlayingRecording()
                    else
                        resumePlayingRecording()
                }
        } else {
//            btnRecord.setBackgroundResource(R.drawable.ic_circle)
//            ViewCompat.setBackgroundTintList(
//                btnRecord,
//                ColorStateList.valueOf(note.color)
//            )
            btnRecord.setImageDrawable(
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.ic_baseline_play_recording, null
                )
            )
                .run {
                    isPlayingRecord = false
                    if (sayTimer?.remainingTime != 0L)
                        pausePlayingRecording()
                    else
                        stopPlayingRecording()
                }
        }
    }
}