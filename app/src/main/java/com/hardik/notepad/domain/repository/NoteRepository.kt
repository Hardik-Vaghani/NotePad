package com.hardik.notepad.domain.repository

import androidx.lifecycle.LiveData
import com.hardik.notepad.domain.model.Note

interface NoteRepository {
    suspend fun getAllNotes(): LiveData<List<Note>>
    suspend fun addNote(note: Note)
    suspend fun addNotes(notes: List<Note>)
    suspend fun updateNote(id: String, newTitle: String, newSubject: String, newContent: String, createdTime: String, updatedTime: String)
}