package com.twain.say.data.database

import androidx.room.TypeConverter
import java.util.*

class Converters {

    @TypeConverter
    fun fromTimeStamp(value: Long) = Date(value)

    @TypeConverter
    fun dateToTimeStamp(date: Date) = date.time
}