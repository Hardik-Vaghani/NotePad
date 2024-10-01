package com.hardik.notepad.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.hardik.notepad.common.Constants.BASE_TAG
import com.hardik.notepad.domain.model.Note
import com.hardik.notepad.domain.model.RealmLiveData
import com.hardik.notepad.domain.repository.NoteRepository
import com.hardik.notepad.extra_class.UniqueIdGenerator
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject


class NoteRepositoryImpl @Inject constructor(private val realm: Realm, private val uniqueIdGenerator: UniqueIdGenerator) : NoteRepository {

    private val tag = BASE_TAG + NoteRepositoryImpl::class.java.simpleName

    override suspend fun getAllNotes(): LiveData<List<Note>> {
        return CoroutineScope(Dispatchers.Main).async {
        val notes: RealmResults<Note> = realm.where(Note::class.java).findAll()
        RealmLiveData(notes)
        }.await()
    }

    override suspend fun addNote(note: Note) {
        withContext(Dispatchers.Main){
            realm.executeTransaction { r ->
                realm.copyToRealmOrUpdate(note)
            }
        }
    }

    override suspend fun addNotes(notes: List<Note>) {
        withContext(Dispatchers.Main) {
            realm.executeTransaction { r ->
                notes.forEach { note ->
                    realm.copyToRealmOrUpdate(note)
                }
            }
        }
    }

    override suspend fun updateNote(id: String, newTitle: String, newSubject: String, newContent: String, createdTime: String, updatedTime: String) {
        withContext(Dispatchers.Main) {
            realm.executeTransaction { r ->
                val note = realm.where(Note::class.java).equalTo("id", id).findFirst()
                if (note != null) {
                    // Update the note's fields
                    note.title = newTitle
                    note.subject = newSubject
                    note.content = newContent
                    note.created_time = createdTime
                    note.updated_time = updatedTime

                    // Update the note in the Realm database
                    realm.copyToRealmOrUpdate(note)
                } else {
                    // Handle the case where the note is not found, if necessary
                    // For example, you could throw an exception or log an error
                    throw IllegalArgumentException("Note with ID $id not found.")
                }
            }
        }
    }







    fun getAllSubjectsFromNotes(): LiveData<List<String>> {
        val mediatorLiveData = MediatorLiveData<List<String>>()

        // Initialize RealmResults
        val notes: RealmResults<Note> = realm.where(Note::class.java).findAll()

        // Observe RealmResults changes
        mediatorLiveData.addSource(RealmLiveData(notes)) { notesList ->
            val subjectList = notesList.map { it.subject }.distinct()
            mediatorLiveData.postValue(subjectList)
        }
        return mediatorLiveData
    }

    fun getAllNotesOfWhichSameSubject(): LiveData<List<Note>> {
        val mediatorLiveData = MediatorLiveData<List<Note>>()

        // Query all notes
        val notes: RealmResults<Note> = realm.where(Note::class.java).findAll()

        // Create a RealmLiveData to observe changes in notes
        val realmLiveData = RealmLiveData(notes)
        mediatorLiveData.addSource(realmLiveData) { notesList ->
            // Group notes by subject and filter subjects with more than one note
            val groupedNotes = notesList
                .groupBy { it.subject }
                .filter { (_, notes) -> notes.size > 1 }
                .flatMap { (_, notes) -> notes }

            // Post the filtered notes to LiveData
            mediatorLiveData.postValue(groupedNotes)
        }

        return mediatorLiveData
    }

    fun getAllNotesOfWhichSameSubject(subjectName: String): LiveData<List<Note>> {
        val mediatorLiveData = MediatorLiveData<List<Note>>()

        // Query all notes
        val notes: RealmResults<Note> = realm.where(Note::class.java).findAll()

        // Create a RealmLiveData to observe changes in notes
        val realmLiveData = RealmLiveData(notes)
        mediatorLiveData.addSource(realmLiveData) { notesList ->
            // Filter notes by the provided subject name
            val filteredNotes = notesList.filter { it.subject == subjectName }

            // Post the filtered notes to LiveData
            mediatorLiveData.postValue(filteredNotes)
        }

        return mediatorLiveData
    }

}
