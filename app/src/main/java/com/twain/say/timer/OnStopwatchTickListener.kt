package com.twain.say.timer

/**
 * Interface to listen for stopwatch tick events every time clock is updated.
 */
interface OnStopwatchTickListener {
    /**
     * Called every time the clock 'ticks'. The stopwatch ticks after a delay of 100ms (or as specified).
     * @since 1.2
     * @param stopwatch Reference to the currently calling stopwatch.
     */
    fun onStopwatchTick(stopwatch: SayStopwatch?)
}