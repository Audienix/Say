package com.twain.say.timer

import androidx.annotation.RestrictTo

class Split
/**
 * Constructor to create a Split object.
 * @param splitTime the time in milliseconds for which stopwatch has been running
 * @param lapTime the time in milliseconds since the last split/lap
 * @since 1.0
 */ @RestrictTo(RestrictTo.Scope.LIBRARY) constructor(
    /** Gets the split time in milliseconds
     * @return the time in milliseconds since the stopwatch was running at the instant this split was created.
     * @see SayStopwatch.split
     * @since 1.0
     */
    val splitTime: Long,
    /**
     * Gets the lap time in milliseconds
     * @return the time in milliseconds between this and the last split/lap
     * @see SayStopwatch.split
     * @since 1.0
     */
    val lapTime: Long
)