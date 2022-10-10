package com.twain.say.timer

import android.os.Handler
import android.util.Log
import android.widget.TextView
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class SayStopwatch {
    private val splits: LinkedList<Split>
    private var textView: TextView?

    /**
     * Returns the clock time (in milliseconds) when the stopwatch was started
     *
     * @return time when the stopwatch was started in milliseconds.
     * @since 1.0
     */
    var start: Long
        private set
    private var current: Long

    /**
     * Gets the current elapsed time the stopwatch has been running for in milliseconds
     *
     * @return the time in milliseconds the stopwatch has been running for.
     * @since 1.0
     */
    var elapsedTime: Long
        private set
    private var lapTime: Long

    /**
     * Returns true if the stopwatch has started
     *
     * @return true if the stopwatch has been started by calling start(). False otherwise
     * @since 1.0
     */
    var isStarted: Boolean
        private set

    /**
     * Returns true if the stopwatch is paused
     *
     * @return true if the stopwatch is paused. False otherwise
     * @since 1.0
     */
    var isPaused: Boolean
        private set
    private var logEnabled: Boolean
    private var onTickListener: OnStopwatchTickListener?
    /**
     * Returns the currently set clock delay
     *
     * @return currently set clock delay in milliseconds (default: 100ms)
     * @since 1.0
     */
    /**
     * Set a custom clock delay to increase/decrease update frequency.
     * Clock delay is the delay in between each successive clock update.
     *
     * @param clockDelay clock delay in milliseconds (default : 100ms)
     * @see Thread.sleep
     * @since 1.0
     */
    private var clockDelay: Long
    private val handler: Handler

    /**
     * The runnable used to call the thread.
     *
     * @since 1.1
     */
    private val runnable = Runnable { this.run() }

    /**
     * Get a list of all splits that have been created.
     *
     * @return all splits created with split method.
     * @see Split
     *
     * @since 1.0
     */
    fun getSplits(): LinkedList<Split> {
        return splits
    }

    /**
     * Set whether to print debug logs or not. If enabled, it will log each time the time is updated.
     *
     * @param debugMode desired debugging status
     * @since 1.0
     */
    fun setDebugMode(debugMode: Boolean) {
        logEnabled = debugMode
    }

    /**
     * Allows you to set a textView where the stopwatch time is displayed.
     * If not provided, or set to null, you need to manually display the time.
     *
     * @param textView the textView where you want to display the stopwatch time. Can be null.
     * @since 1.0
     */
    fun setTextView(textView: TextView?) {
        this.textView = textView
    }

    /**
     * Set an OnTickListener to listen for clock changes.
     *
     * @param onTickListener a reference to the interface implementation.
     * @since 1.0
     */
    fun setOnTickListener(onTickListener: OnStopwatchTickListener?) {
        this.onTickListener = onTickListener
    }

    /**
     * Starts the stopwatch at the current time. Cannot be called again without calling stop() first.
     *
     * @throws IllegalStateException if the stopwatch has already been started.
     * @see .stop
     * @see .isStarted
     * @since 1.0
     */
    fun start() {
        check(!isStarted) { "Already Started" }
        isStarted = true
        isPaused = false
        start = System.currentTimeMillis()
        current = System.currentTimeMillis()
        lapTime = 0
        elapsedTime = 0
        splits.clear()
        handler.post(runnable)
    }

    /**
     * Stops the stopwatch. Stopwatch cannot be resumed from current time later.
     *
     * @throws IllegalStateException if stopwatch has not been started yet.
     * @see .start
     * @see .isStarted
     * @since 1.0
     */
    fun stop() {
        check(isStarted) { "Not Started" }
        updateElapsed(System.currentTimeMillis())
        isStarted = false
        isPaused = false
        handler.removeCallbacks(runnable)
    }

    /**
     * Pauses the stopwatch. Using this allows you to resume the stopwatch from the current state.
     *
     * @throws IllegalStateException if stopwatch is already paused or not started yet.
     * @see .resume
     * @see .isPaused
     * @since 1.0
     */
    fun pause() {
        check(!isPaused) { "Already Paused" }
        check(isStarted) { "Not Started" }
        updateElapsed(System.currentTimeMillis())
        isPaused = true
        handler.removeCallbacks(runnable)
    }

    /**
     * Used to resume the stopwatch from the current time after being paused.
     *
     * @throws IllegalStateException if stopwatch is not paused or not started yet.
     * @see .pause
     * @see .isPaused
     * @since 1.0
     */
    fun resume() {
        check(isPaused) { "Not Paused" }
        check(isStarted) { "Not Started" }
        isPaused = false
        current = System.currentTimeMillis()
        handler.post(runnable)
    }

    /**
     * Creates a new split/lap at the current time. Can even be called when stopwatch is paused.
     *
     * @throws IllegalStateException if stopwatch is not started yet
     * @see .getSplits
     * @since 1.0
     */
    fun split() {
        check(isStarted) { "Not Started" }
        val split = Split(elapsedTime, lapTime)
        lapTime = 0
        if (logEnabled) Log.d(
            "STOPWATCH",
            "split at " + split.splitTime.toString() + ". Lap = " + split.lapTime
        )
        splits.add(split)
    }

    /**
     * Updates the time in elapsed and lap time and then updates the current time.
     *
     * @param time Current time in millis. Passing any other value may result in odd behaviour
     * @since 1.1
     */
    private fun updateElapsed(time: Long) {
        elapsedTime += time - current
        lapTime += time - current
        current = time
    }

    /**
     * The main thread responsible for updating and displaying the time
     *
     * @since 1.1
     */
    private fun run() {
        if (!isStarted || isPaused) {
            handler.removeCallbacks(runnable)
            return
        }
        updateElapsed(System.currentTimeMillis())
        handler.postDelayed(runnable, clockDelay)
        if (logEnabled) Log.d(
            "STOPWATCH",
            (elapsedTime / 1000).toString() + " seconds, " + elapsedTime % 1000 + " milliseconds"
        )
        if (onTickListener != null) onTickListener!!.onStopwatchTick(this)
        if (textView != null) {
            val displayTime = getFormattedTime(elapsedTime)
            textView!!.text = displayTime
        }
    }

    /**
     * Formats time in either of the following formats depending on time passed - SS.ss, MM:SS.ss, HH:MM:SS.
     *
     * @param elapsedTime time in milliseconds which has to be formatted
     * @return formatted time in String form
     * @since 1.1
     */
    fun getFormattedTime(elapsedTime: Long): String {
        val displayTime = StringBuilder()
//        val milliseconds = (elapsedTime % 1000 / 10).toInt()
        val seconds = (elapsedTime / 1000 % 60).toInt()
        val minutes = (elapsedTime / (60 * 1000) % 60).toInt()
        val hours = (elapsedTime / (60 * 60 * 1000)).toInt()
        val f: NumberFormat = DecimalFormat("00")
//        if (minutes == 0) displayTime.append(f.format(seconds.toLong())).append('.')
//            .append(f.format(milliseconds.toLong()))
        if (hours == 0)
            displayTime.append(f.format(hours.toLong())).append(":").append(f.format(minutes.toLong())).append(":").append(f.format(seconds.toLong()))
//            .append('.')
//            .append(f.format(milliseconds.toLong()))
        else displayTime.append(hours).append(":").append(f.format(minutes.toLong())).append(":").append(f.format(seconds.toLong()))
        return displayTime.toString()
    }

    /**
     * The default constructor should be called to create an object to call functions accordingly.
     *
     * @since 1.0
     */
    init {
        start = System.currentTimeMillis()
        current = System.currentTimeMillis()
        elapsedTime = 0
        isStarted = false
        isPaused = false
        logEnabled = false
        splits = LinkedList<Split>()
        textView = null
        lapTime = 0
        onTickListener = null
        clockDelay = 100
        handler = Handler()
    }
}