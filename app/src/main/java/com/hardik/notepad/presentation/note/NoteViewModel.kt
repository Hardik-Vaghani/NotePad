package com.hardik.notepad.presentation.note

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.notepad.common.Constants.BASE_TAG
import com.hardik.notepad.domain.model.Note
import com.hardik.notepad.domain.repository.NoteRepository
import com.hardik.notepad.presentation.MainViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class NoteViewModel @Inject constructor(private val noteRepository: NoteRepository) : MainViewModel(noteRepository =  noteRepository) {
    private val tag = BASE_TAG + NoteViewModel::class.java.simpleName


    fun addNote(note: Note = Note()) {
        viewModelScope.launch {
            noteRepository.addNote(note)
        }
    }

    fun upsertNote(id: String, newTitle: String, newSubject: String, newContent: String, createdTime: String, updatedTime: String) {
        viewModelScope.launch {
            noteRepository.updateNote(id, newTitle, newSubject, newContent, createdTime, updatedTime)
        }
    }
}