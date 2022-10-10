package com.twain.say.timer

/**
 * Listener interface to listen for important clock events with the timer.
 *
 * @since 1.2
 */
interface OnTimerTickListener {
    /**
     * Called every clock cycle update
     *
     * @param timer the timer instance which has been updated
     * @since 1.2
     */
    fun onTimerTick(timer: SayTimer?)

    /**
     * Called when the timer has been completed (ran its full duration)
     *
     * @param timer the timer instance which has completed
     * @since 1.2
     */
    fun onTimerCompletion(timer: SayTimer?)
}
