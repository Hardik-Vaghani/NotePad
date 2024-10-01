package com.hardik.notepad.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hardik.notepad.common.Constants.BASE_TAG
import com.hardik.notepad.common.Constants.NEW_NOTE
import com.hardik.notepad.domain.model.Note
import com.hardik.notepad.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class MainViewModel @Inject constructor(private val noteRepository: NoteRepository) : ViewModel() {
    private val tag = BASE_TAG + MainViewModel::class.java.simpleName

    // LiveData to expose the list of all notes
    //val notes: LiveData<List<Note>> = noteRepository.getAllNotes() //LiveData fetch from the noteRepository, and store in live data
    private var _notes = MutableLiveData<List<Note>>()
    // Expose as LiveData to other components
    val notes: LiveData<List<Note>> get() = _notes

    init {
        viewModelScope.launch {
            reFreshNote()
        }
    }
    // Update _notes when data changes from repository
    suspend fun reFreshNote(){
        // Observe changes from repository and update _notes accordingly
        viewModelScope.launch  (Dispatchers.Main){
            noteRepository.getAllNotes().observeForever { updatedNotes ->
                _notes.postValue(updatedNotes)
                Log.d(tag, "Updated notes: $updatedNotes")
                setUpSubjectsOfNotes()
            }
        }.join()
//        setNotesOfWhichSameSubject("All")
    }

    // LiveData to expose the list of unique subjects
    private var _subjectsOfNotes = MutableLiveData<List<String>>()
    val subjectsOfNotes: LiveData<List<String>> get() = _subjectsOfNotes
    private fun setUpSubjectsOfNotes() = viewModelScope.launch(Dispatchers.Main) {
        notes.observeForever{ notes ->
            // Transform notesList into a distinct list of subjects
            val subjectList = notes.map { it.subject }.distinct()
            val modifiedSubjectList = listOf("All") + subjectList// + listOf("Unknown subject")
            _subjectsOfNotes.postValue(modifiedSubjectList)
        }
//        val list = MediatorLiveData<List<String>>().apply {
//            // Add a source to observe changes in notes LiveData
//            addSource(notes) { notesList ->
//                // Transform notesList into a distinct list of subjects
//                val subjectList = notesList.map { it.subject }.distinct()
//                Log.d(tag, "subjectList: $subjectList")
//                // Post the transformed list to MediatorLiveData
//                value = listOf("All") + subjectList// + listOf("Unknown subject")
//            }
//        }.value
//        _subjectsOfNotes.postValue(list)
    }


    /**-----------------here performed internal operations-----------------------*/

    // MutableLiveData for the selected subject with a default value
    private var _selectedSubject = MutableLiveData<String>("All")
    val selectedSubject: LiveData<String> get() = _selectedSubject
    fun setSelectedSubject(subject: String = "") = viewModelScope.launch(Dispatchers.Main){// Method to update the selected subject
        _selectedSubject.postValue(subject)
    }

    // LiveData to handle the selected note, set in home and use in Preview & NotePad
    private val _selectedNote = MutableLiveData<Note>()
    val selectedNote: LiveData<Note> get() = _selectedNote
    fun setSelectedNote(note: Note?) {
        _selectedNote.postValue(note)
    }

    private val _updateNote = MutableLiveData<Note>()
    val updateNote: LiveData<Note> get() = _updateNote
    fun updateNote(note: Note?) {
        _updateNote.postValue(note)
    }

    // this is useful for edit or new notes
    private val _isNewNote = MutableLiveData<Boolean>(NEW_NOTE)
    val isNewNote: LiveData<Boolean> get() = _isNewNote
    fun setIsNewNote(isNewNote: Boolean) {
        _isNewNote.postValue(isNewNote)
    }


    /*
    // LiveData to expose the list of all notes
    private var _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> get() = _notes

    init {
        getAllNotes()
    }

    fun getAllNotes() {
        MediatorLiveData<List<Note>>().apply {
            addSource(noteRepository.getAllNotes()){
                _notes.postValue( it?: emptyList() )
                Log.d(tag, "getAllNotes: $it")
            }
        }
    }
    */
}