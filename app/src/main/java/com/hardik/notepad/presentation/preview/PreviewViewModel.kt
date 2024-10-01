package com.hardik.notepad.presentation.preview

import androidx.lifecycle.ViewModel
import com.hardik.notepad.common.Constants.BASE_TAG
import com.hardik.notepad.domain.repository.NoteRepository
import com.hardik.notepad.presentation.MainViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PreviewViewModel @Inject constructor(private val noteRepository: NoteRepository) : MainViewModel(noteRepository =  noteRepository) {
    private val tag = BASE_TAG + PreviewViewModel::class.java.simpleName
}