package com.example.projectnotes.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.projectnotes.ViewModel.NoteActivityViewModel
import com.example.projectnotes.ViewModel.NoteActivityViewModelFactory
import com.example.projectnotes.databinding.ActivityMainBinding
import com.example.projectnotes.db.NoteDatabase
import com.example.projectnotes.repository.NoteRepository

class MainActivity : AppCompatActivity() {

    lateinit var noteActivityViewModel: NoteActivityViewModel
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding= ActivityMainBinding.inflate(layoutInflater)
        try{
            setContentView(binding.root)
            val noteRepository = NoteRepository(NoteDatabase(this))
            val noteRepositoryViewModelFactory = NoteActivityViewModelFactory(noteRepository)
            noteActivityViewModel=ViewModelProvider(this,noteRepositoryViewModelFactory)[NoteActivityViewModel::class.java]
        }catch (e:Exception)
        {
            Log.d("Knigi","huinya ne rabotaet")
        }

    }
}