package com.hardik.notepad.presentation.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hardik.notepad.common.Constants.BASE_TAG
import com.hardik.notepad.domain.model.Note
import com.hardik.notepad.domain.repository.NoteRepository
import com.hardik.notepad.presentation.MainViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val noteRepository: NoteRepository) : MainViewModel(noteRepository =  noteRepository) {
    private val TAG = BASE_TAG + HomeViewModel::class.java.simpleName

    fun addNotes(notes: List<Note> = listOf(Note())) {
        viewModelScope.launch() {
            noteRepository.addNotes(notes)
        }
    }

    // LiveData to handle notes based on the selected subject, use in home index
    private var _notesOfSameSubject = MutableLiveData<List<Note>>()
    val notesOfSameSubject: LiveData<List<Note>> get() = _notesOfSameSubject
    fun setNotesOfSameSubject(subject: String) {
        viewModelScope.launch(Dispatchers.Main){
            Log.i(TAG, "setNotesOfSameSubject: subject: $subject")
            notes.observeForever{
                notes ->
                if (subject == "All"){
                    _notesOfSameSubject.postValue(notes)
                }else{
                    val filteredNotes = notes.filter { it.subject == subject }
                    _notesOfSameSubject.postValue(filteredNotes)
                }
            }
        }
    }
    /*  viewModelScope.launch(Dispatchers.Main){
      notes.observeForever { notes ->
          // If "All" is selected, show all notes, else filter by the selected subject
//            selectedSubject.observeForever { subjectOfNote ->
//                val noteList = if (subjectOfNote == "All") {
//                    notes //Post the all notes to MediatorLiveData
//                } else {
//                    val filteredNotes = notes.filter { it.subject == subjectOfNote }
//                    filteredNotes // Post the filtered notes to MediatorLiveData
//                }
//                _notesOfSameSubject.postValue(noteList)
//            }
          val noteList = if (selectedSubject.value == "All") {
              notes //Post the all notes to MediatorLiveData
          } else {
              val filteredNotes = notes.filter { it.subject == selectedSubject.value }
              filteredNotes // Post the filtered notes to MediatorLiveData
          }
          _notesOfSameSubject.postValue(noteList)
      }
  }
      MediatorLiveData<List<Note>>().apply {
          addSource(notes) { notesList ->
              // Filter notes by the selected subject

              // If "All" is selected, show all notes, else filter by the selected subject
              value = if (selectedSubject.value == "All") {
                  notesList //Post the all notes to MediatorLiveData
              } else {
                  val filteredNotes = notesList.filter { it.subject == selectedSubject.value }
                  filteredNotes // Post the filtered notes to MediatorLiveData
              }
              _notesOfSameSubject.postValue(value)
          }
      }*/


}