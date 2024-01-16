package com.example.projectnotes.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Query
import com.example.projectnotes.model.Note
import com.example.projectnotes.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteActivityViewModel(private val repository: NoteRepository):ViewModel() {

    fun saveNote(newNote: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.addNote(newNote)
    }

    fun updateNote(existingNote: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateNote(existingNote)
    }
    fun deleteNote(existingNote: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteNote(existingNote)
    }

    fun searchNote(query: String):LiveData<List<Note>>{
        return repository.searchNote(query)
    }

    fun getAllNote():LiveData<List<Note>> = repository.getNote()

}