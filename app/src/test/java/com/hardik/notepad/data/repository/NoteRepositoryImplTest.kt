package com.hardik.notepad.data.repository

import com.hardik.notepad.domain.model.Note
import com.hardik.notepad.domain.repository.NoteRepository
import io.realm.Realm
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.nio.file.Files.delete
class NoteRepositoryImplTest {

/*
    @Test
    fun testGetNoteById() = runBlockingTest {
        val note = Note(
            id = 1,
            subject = "Subject",
            title = "Title",
            description = "Description",
            isPin = true,
            created_time = "2024-07-30",
            updated_time = "2024-07-30",
            device_id = "Device123"
        )

        // Configure mock behavior
        `when`(realm.query<Note>("id == $0", 1).first().find()).thenReturn(note)

        val result = noteRepository.getNoteById(1)

        assertNotNull(result)
        assertEquals(note, result)
    }

    @Test
    fun testGetAllNotes() = runBlockingTest {
        val notes = listOf(
            Note(
                id = 1,
                subject = "Subject",
                title = "Title",
                description = "Description",
                isPin = true,
                created_time = "2024-07-30",
                updated_time = "2024-07-30",
                device_id = "Device123"
            )
        )

        // Configure mock behavior
        `when`(realm.query<Note>().find()).thenReturn(notes)

        val result = noteRepository.getAllNotes("title")

        assertEquals(notes, result)
    }

    @Test
    fun testDeleteNoteById() = runBlockingTest {
        val note = Note(
            id = 1,
            subject = "Subject",
            title = "Title",
            description = "Description",
            isPin = true,
            created_time = "2024-07-30",
            updated_time = "2024-07-30",
            device_id = "Device123"
        )

        // Configure mock behavior
        `when`(realm.query<Note>("id == $0", 1).first().find()).thenReturn(note)

        noteRepository.deleteNoteById(1)

        verify(realm).write { delete(note) }
    }

    @Test
    fun testSearchNotesByTitle() = runBlockingTest {
        val notes = listOf(
            Note(
                id = 1,
                subject = "Subject",
                title = "Title",
                description = "Description",
                isPin = true,
                created_time = "2024-07-30",
                updated_time = "2024-07-30",
                device_id = "Device123"
            )
        )

        // Configure mock behavior
        `when`(realm.query<Note>("title == $0", "Title").find()).thenReturn(notes)

        val result = noteRepository.searchNotesByTitle("Title")

        assertEquals(notes, result)
    }*/
}
