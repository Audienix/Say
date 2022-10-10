package com.twain.say.timer

import android.util.Log
import android.widget.TextView

class SayTimer(private var duration: Long) {
    companion object {
        /** Set debug mode to print debug log messages in the logcat
         * @param debugMode the boolean status to set the debug mode
         */
        private var isDebugMode: Boolean = false
    }
    /**
     * Fetch the set duration of the timer
     *
     * @return the set duration of the timer in milliseconds
     * @since 1.2
     */
    /**
     * Used to set the duration of the timer.
     * Note : Should only be used once before starting the timer or it may have unintended consequences.
     *
     * @param duration the duration in milliseconds for which you want to set the timer
     * @since 1.2
     */
    private var textView: TextView?
    private val stopwatch: SayStopwatch = SayStopwatch()
    private var onTickListener: OnTimerTickListener? = null
    /** Returns true if debug mode is enabled
     * @return the current boolean status of debug mode
     * @since 1.2
     */


    /**
     * Returns true if the timer has been started
     *
     * @return true if the timer has been started
     * @since 1.2
     */
    val isStarted: Boolean
        get() = stopwatch.isStarted

    /**
     * Returns true if the timer is paused
     *
     * @return true if the timer is paused
     * @since 1.2
     */
    val isPaused: Boolean
        get() = stopwatch.isPaused

    /**
     * Get the remaining time of the timer in milliseconds
     *
     * @return the remaining time of the timer in milliseconds
     * @since 1.2
     */
    val remainingTime: Long
        get() = duration - stopwatch.elapsedTime

    /**
     * Get the start time in long
     *
     * @return the timestamp when the timer was started
     * @since 1.2
     */
    val start: Long
        get() = stopwatch.start

    /**
     * Used to set the textView which is auto-updated every clock tick.
     *
     * @param textView the textView to update every clock tick.
     * @since 1.2
     */
    fun setTextView(textView: TextView?) {
        this.textView = textView
    }

    /**
     * Used to set an OnTickListener to listen to clock events
     *
     * @param onTickListener an instance of Timer.OnTickListener
     * @see OnTimerTickListener
     *
     * @since 1.2
     */
    fun setOnTickListener(onTickListener: OnTimerTickListener?) {
        this.onTickListener = onTickListener
    }

    /**
     * Used to start the timer.
     * Should only be called if the timer is not already started.
     *
     * @since 1.2
     */
    fun start() {
        if (duration > 0) stopwatch.start() else throw IllegalStateException("Duration not set")
    }

    /**
     * Used to stop the timer.
     * Should only be called if the timer is already started.
     *
     * @since 1.2
     */
    fun stop() {
        if (stopwatch.isStarted)
            stopwatch.stop()
    }

    /**
     * Used to pause the timer.
     * Should only be called if the timer is currently running
     *
     * @since 1.2
     */
    fun pause() {
        stopwatch.pause()
    }

    /**
     * Used to resume a paused timer.
     * Should only be used after a pause call.
     *
     * @since 1.2
     */
    fun resume() {
        stopwatch.resume()
    }

    init {
        textView = null
        stopwatch.setOnTickListener(object : OnStopwatchTickListener {
            override fun onStopwatchTick(stopwatch: SayStopwatch?) {
                if (isDebugMode) Log.d(
                    "TIMER",
                    "; Duration : $duration Elapsed : ${stopwatch!!.elapsedTime}; Remaining : ${duration - stopwatch.elapsedTime}"
                )
                if (onTickListener != null) onTickListener!!.onTimerTick(this@SayTimer)
                if (stopwatch?.elapsedTime!! >= duration) {
                    textView?.text = stopwatch.getFormattedTime(0)
                    stopwatch.stop()
                    if (onTickListener != null) onTickListener!!.onTimerCompletion(this@SayTimer)
                } else {
                    if (textView != null) textView?.text =
                        stopwatch.getFormattedTime(duration - stopwatch.elapsedTime)
                }
            }
        })
        onTickListener = null
        isDebugMode = true
    }
}
