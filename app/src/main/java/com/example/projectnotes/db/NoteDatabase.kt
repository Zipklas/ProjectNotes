package com.example.projectnotes.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.projectnotes.activities.MainActivity
import com.example.projectnotes.model.Note


@Database(
    entities = [Note::class],
    version = 1,
    exportSchema = false,
)
abstract class NoteDatabase:RoomDatabase() {
    abstract fun getNoteDao(): NoteDao
    companion object{
        @Volatile
        private var instance:NoteDatabase?=null
        private val LOCK= Any()

        operator fun invoke(context: MainActivity)= instance ?: synchronized(LOCK){
            instance ?: createDatabase(context).also {
                instance=it
            }
        }






        private fun createDatabase(context :Context)= Room.databaseBuilder(
            context.applicationContext,
            NoteDatabase::class.java,"note_database"
        ).build()
    }








}