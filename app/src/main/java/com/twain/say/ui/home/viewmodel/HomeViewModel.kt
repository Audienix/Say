package com.twain.say.ui.home.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.twain.say.ui.home.model.Note
import com.twain.say.ui.home.repository.HomeRepository
import com.twain.say.utils.ReminderAvailableState
import com.twain.say.utils.ReminderCompletionState
import com.twain.say.utils.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val homeRepository: HomeRepository) : ViewModel() {

    val uiState = ObservableField(UIState.LOADING)
    val reminderAvailableState = ObservableField(ReminderAvailableState.NO_REMINDER)
    val reminderCompletionState = ObservableField(ReminderCompletionState.ONGOING)

    private val _notes = MutableLiveData<List<Note>?>()
    lateinit var notes: LiveData<List<Note>>

    init {
        getNotes()
    }

    private fun getNotes() {
        notes = homeRepository.allNotes.asLiveData()
    }

    fun insertNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            homeRepository.insertNote(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            homeRepository.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            homeRepository.deleteNote(note)
        }
    }

    fun getNote(noteId: Int): LiveData<Note> = homeRepository.getNote(noteId).asLiveData()

    fun searchNotes(query: String) : LiveData<List<Note>> {
        return homeRepository.searchNote(query).asLiveData()
    }
}