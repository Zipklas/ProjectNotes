package com.example.projectnotes.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.TextView
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter
import com.example.projectnotes.R
import com.example.projectnotes.databinding.NoteItemLayoutBinding
import com.example.projectnotes.fragments.NoteFragment
import com.example.projectnotes.fragments.NoteFragmentDirections
import com.example.projectnotes.model.Note
import com.example.projectnotes.utils.hidekeyboard
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import org.commonmark.node.SoftLineBreak

class RcNoteAdapter :ListAdapter <Note,RcNoteAdapter.NotesViewHolder>(DiffUtilCallback()) {

    inner class NotesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contentBinding = NoteItemLayoutBinding.bind(itemView)
        val title: MaterialTextView = contentBinding.noteItemTitle
        val content: TextView = contentBinding.noteContentItem
        val date: MaterialTextView = contentBinding.noteData
        val parent: MaterialCardView = contentBinding.noteItemLayoutParent
        val markwon = Markwon.builder(itemView.context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(itemView.context))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                    super.configureVisitor(builder)
                    builder.on(
                        SoftLineBreak::class.java
                    ) { visitor, _, -> visitor.forceNewLine() }
                }
            })
            .build()


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        return NotesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.note_item_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        getItem(position).let { note1 ->
            holder.apply {
                parent.transitionName="recycler_view_${note1?.id}"
                title.text = note1.title
                markwon.setMarkdown(content, note1.content)
                date.text = note1.date
                parent.setBackgroundColor(note1.color)
                itemView.setOnClickListener {

                    val action=NoteFragmentDirections.actionNoteFragmentToSaveAndUpdateFragment(note1)
                    val extras= FragmentNavigatorExtras(parent to "recycler_view_${note1?.id}")
                    it.hidekeyboard()
                    Navigation.findNavController(it).navigate(action,extras)


                }
                content.setOnClickListener {
                    val action=NoteFragmentDirections.actionNoteFragmentToSaveAndUpdateFragment(note1)
                    val extras= FragmentNavigatorExtras(parent to "recycler_view_${note1?.id}")
                    it.hidekeyboard()
                    Navigation.findNavController(it).navigate(action, extras)
                    
                }
            }
        }
    }


}

