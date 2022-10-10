package com.twain.say.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.twain.say.ui.home.data.dao.AudioNotesDAO
import com.twain.say.ui.home.model.Note

@Database(entities = [Note::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AudioNotesDatabase : RoomDatabase() {

    abstract fun audioNotesDao(): AudioNotesDAO
}