package com.twain.say.ui.home.data.dao

import androidx.room.*
import com.twain.say.ui.home.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioNotesDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes_table WHERE id = :noteId")
    fun getNote(noteId: Int): Flow<Note>

    @Query("SELECT * FROM notes_table ORDER BY id DESC")
    fun getAllNotes(): Flow<List<Note>>
}