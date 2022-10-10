package com.twain.say.di

import android.content.Context
import androidx.room.Room
import com.twain.say.data.database.AudioNotesDatabase
import com.twain.say.ui.home.data.dao.AudioNotesDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun provideAudioNotesDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, AudioNotesDatabase::class.java, "say_database.db")
            .fallbackToDestructiveMigration()
            .build()

    @Singleton
    @Provides
    fun provideNoteDao(database: AudioNotesDatabase): AudioNotesDAO {
        return database.audioNotesDao()
    }
}