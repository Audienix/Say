package com.twain.say.ui.home.repository

import com.twain.say.ui.home.data.dao.AudioNotesDAO
import com.twain.say.ui.home.model.Note
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(private val audioNotesDAO: AudioNotesDAO) {

    val allNotes: Flow<List<Note>> = audioNotesDAO.getAllNotes()

    suspend fun insertNote(note: Note) {
        audioNotesDAO.insertNote(note)
    }

    suspend fun updateNote(note: Note) {
        audioNotesDAO.updateNote(note)
    }

    suspend fun deleteNote(note: Note) {
        audioNotesDAO.deleteNote(note)
    }

    fun getNote(noteId: Int): Flow<Note> = audioNotesDAO.getNote(noteId)
}