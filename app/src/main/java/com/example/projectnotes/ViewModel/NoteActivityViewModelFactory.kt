package com.example.projectnotes.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.projectnotes.repository.NoteRepository

class NoteActivityViewModelFactory(private val repository: NoteRepository):
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NoteActivityViewModel(repository) as T
        }
    }