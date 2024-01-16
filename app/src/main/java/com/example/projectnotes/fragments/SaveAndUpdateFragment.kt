package com.example.projectnotes.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.projectnotes.R
import com.example.projectnotes.ViewModel.NoteActivityViewModel
import com.example.projectnotes.activities.MainActivity
import com.example.projectnotes.databinding.BottomSheetLayoutBinding
import com.example.projectnotes.databinding.FragmentSaveAndUpdateBinding
import com.example.projectnotes.model.Note
import com.example.projectnotes.utils.hidekeyboard
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.transition.MaterialContainerTransform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class SaveAndUpdateFragment : Fragment(R.layout.fragment_save_and_update) {

    private lateinit var navController: NavController
    private lateinit var contentBinding: FragmentSaveAndUpdateBinding
    private var note: Note? = null
    private var color = -1
    private lateinit var result:String
    private val noteActivityViewModel: NoteActivityViewModel by activityViewModels()
    private val currentData = SimpleDateFormat.getInstance().format(Date())
    private val job = CoroutineScope(Dispatchers.Main)
    private val args: SaveAndUpdateFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animation = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment
            scrimColor = Color.TRANSPARENT
            duration = 300L
        }
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentBinding = FragmentSaveAndUpdateBinding.bind(view)
        navController = Navigation.findNavController(view)
        val activity = activity as MainActivity

        contentBinding.backBtn.setOnClickListener {
            requireView().hidekeyboard()
            navController.popBackStack()
        }
        contentBinding.lastEdited.text = getString(R.string.edited_on,SimpleDateFormat.getDateInstance().format(Date()))
        contentBinding.saveNote.setOnClickListener {
            saveNote()

        }
        ViewCompat.setTransitionName(
            contentBinding.noteContentFragmentParent,
            "recycler_view_${args.note1?.id}"
        )
        try {
            contentBinding.etNoteContent.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    contentBinding.bottomBar.visibility = View.VISIBLE
                    contentBinding.etNoteContent.setStylesBar(contentBinding.styleBar)
                } else contentBinding.bottomBar.visibility = View.GONE
            }
        } catch (e: Throwable) {
            Log.d("Tag", e.stackTraceToString())
        }

        contentBinding.fabColorPick.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(
                requireContext(),
                R.style.BottomSheetDialogTheme
            )
            val bottomSheetView: View = layoutInflater.inflate(
                R.layout.bottom_sheet_layout,
                null
            )
            with(bottomSheetDialog)
            {
                setContentView(bottomSheetView)
                show()
            }
            val bottomSheetBinding = BottomSheetLayoutBinding.bind(bottomSheetView)
            bottomSheetBinding.apply {
                ColorPicker.apply {
                    setSelectedColor(color)
                    setOnColorSelectedListener { value ->
                        color = value
                        contentBinding.apply {
                            noteContentFragmentParent.setBackgroundColor(color)
                            toolbarFragmentNoteContent.setBackgroundColor(color)
                            bottomBar.setBackgroundColor(color)
                            activity.window.statusBarColor = color
                        }
                        bottomSheetBinding.bottomSheetParent.setCardBackgroundColor(color)
                    }
                }
                bottomSheetParent.setCardBackgroundColor(color)
            }
            bottomSheetView.post {
                bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        setUpNote()


    }

    private fun setUpNote() {
        val note=args.note1
        val title = contentBinding.etTitle
        val content= contentBinding.etNoteContent
        val lastEdited= contentBinding.lastEdited

        if(note==null)
        {
            contentBinding.lastEdited.text = getString(R.string.edited_on,SimpleDateFormat.getDateInstance().format(Date()))
        }
        if(note!=null)
        {
            title.setText(note.title)
            content.renderMD(note.content)
            lastEdited.text=getString(R.string.edited_on,note.date)
            color=note.color
            contentBinding.apply {
                job.launch {
                    delay(10)
                    noteContentFragmentParent.setBackgroundColor(color)

                }
                toolbarFragmentNoteContent.setBackgroundColor(color)
                bottomBar.setBackgroundColor(color)
            }
            activity?.window?.statusBarColor=note.color
        }
    }


    private fun saveNote() {
        if (contentBinding.etNoteContent.text.toString().isEmpty() ||
            contentBinding.etTitle.text.toString().isEmpty()
        ) {
            Toast.makeText(activity, "Что-то осталость пустым", Toast.LENGTH_LONG).show()
        } else {
            note = args.note1
            when (note) {
                null -> {
                    noteActivityViewModel.saveNote(
                        Note(
                            0,
                            contentBinding.etTitle.text.toString(),
                            contentBinding.etNoteContent.getMD(),
                            currentData,
                            color
                        )
                    )
                    result = "Note Saved"
                    setFragmentResult(
                        "key",
                        bundleOf("bundleKey" to result)
                    )
                    navController.navigate(SaveAndUpdateFragmentDirections.actionSaveAndUpdateFragmentToNoteFragment())
                }
                else -> {
                    updateNote()
                    navController.popBackStack()
                }

            }
        }


    }

    private fun updateNote() {
        if(note!=null)
        {
            noteActivityViewModel.updateNote(
                Note(
                    note!!.id,
                    contentBinding.etTitle.text.toString(),
                    contentBinding.etNoteContent.getMD(),
                    currentData,
                    color
                )
            )
        }
    }
}