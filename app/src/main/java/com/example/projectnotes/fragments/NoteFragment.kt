package com.example.projectnotes.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.projectnotes.Adapters.RcNoteAdapter
import com.example.projectnotes.R
import com.example.projectnotes.ViewModel.NoteActivityViewModel
import com.example.projectnotes.ViewModel.NoteActivityViewModelFactory
import com.example.projectnotes.activities.MainActivity
import com.example.projectnotes.databinding.FragmentNoteBinding
import com.example.projectnotes.db.NoteDatabase
import com.example.projectnotes.repository.NoteRepository
import com.example.projectnotes.utils.SwipeToDelete
import com.example.projectnotes.utils.hidekeyboard
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class NoteFragment : Fragment(R.layout.fragment_note) {


    private lateinit var noteBinding: FragmentNoteBinding
    private lateinit var rcAdapter:RcNoteAdapter
    lateinit var noteActivityViewModel: NoteActivityViewModel
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        exitTransition=MaterialElevationScale(false).apply {
            duration=350
        }
        enterTransition=MaterialElevationScale(true).apply {
            duration=350
        }


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteBinding = FragmentNoteBinding.bind(view)
        val activity=activity as MainActivity
        val navController = Navigation.findNavController(view)
        requireView().hidekeyboard()
        CoroutineScope(Dispatchers.Main).launch {
           // activity.window.statusBarColor= Color.WHITE
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.statusBarColor=Color.parseColor("#9E9D9D")
        }

        noteBinding.addNoteFab.setOnClickListener {
            noteBinding.appBarLayout.visibility = View.VISIBLE
            navController.navigate(NoteFragmentDirections.actionNoteFragmentToSaveAndUpdateFragment())

        }
        noteBinding.innerFab.setOnClickListener {
            noteBinding.appBarLayout.visibility = View.VISIBLE
            navController.navigate(NoteFragmentDirections.actionNoteFragmentToSaveAndUpdateFragment())

        }

        recyclerViewDisplay()
        SwipeToDelete(noteBinding.RcNote)

        noteBinding.search.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                noteBinding.noData.isVisible=false
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0.toString().isNotEmpty())
                {
                    val text=p0.toString()
                    val query="%$text%"
                    if(query.isNotEmpty())
                    {
                        noteActivityViewModel.searchNote(query).observe(viewLifecycleOwner)
                        {
                            rcAdapter.submitList(it)
                        }
                    }
                    else{
                        observerDataChanges()
                    }
                }
                else{
                    observerDataChanges()
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
        noteBinding.search.setOnEditorActionListener{v,actionId, _->
            if(actionId==EditorInfo.IME_ACTION_SEARCH)
            {
                v.clearFocus()
                requireView().hidekeyboard()
            }
            return@setOnEditorActionListener true

        }
        noteBinding.RcNote.setOnScrollChangeListener { _, scrollX, scrollY, _, OldScrollY ->

            when{
                scrollY>OldScrollY->{
                    noteBinding.chatFabText.isVisible=false
                }
                scrollX==scrollY->{
                    noteBinding.chatFabText.isVisible=true
                }
                else->
                {
                    noteBinding.chatFabText.isVisible=true
                }
            }


        }
    }

    private fun SwipeToDelete(rcNote: RecyclerView) {
            
        val swipeToDeleteCallback= object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
               val position=viewHolder.absoluteAdapterPosition
                val note=rcAdapter.currentList[position]
                var actionBtnTapped = false
                noteActivityViewModel.deleteNote(note)
                noteBinding.search.apply {
                    hidekeyboard()
                    clearFocus()
                }
                if(noteBinding.search.text.toString().isEmpty())
                {
                    observerDataChanges()
                }
                val snackbar= Snackbar.make(
                    requireView(), "Заметка удалена",Snackbar.LENGTH_LONG
                ).addCallback(object :BaseTransientBottomBar.BaseCallback<Snackbar>(){
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                    }

                    override fun onShown(transientBottomBar: Snackbar?) {
                        transientBottomBar?.setAction("Отменить"){
                            noteActivityViewModel.saveNote(note)
                            actionBtnTapped=true
                            noteBinding.noData.isVisible=false
                        }
                        super.onShown(transientBottomBar)
                    }
                }).apply {
                    animationMode=Snackbar.ANIMATION_MODE_FADE
                    setAnchorView(R.id.add_note_fab)
                }
                snackbar.setActionTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.yellowOrange
                    )
                )
                snackbar.show()
            }
        }
        val itemTouchHelper=ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(rcNote)
    }
        
        
        


    private fun observerDataChanges() {
            noteActivityViewModel.getAllNote().observe(viewLifecycleOwner){ list->
                noteBinding.noData.isVisible=list.isEmpty()
                rcAdapter.submitList(list)

            }
    }

    private fun recyclerViewDisplay() {
        when(resources.configuration.orientation)
        {
            Configuration.ORIENTATION_PORTRAIT->setUpRecyclerView(2)
            Configuration.ORIENTATION_LANDSCAPE->setUpRecyclerView(3)
        }
    }

    private fun setUpRecyclerView(spanCount: Int) {


        noteBinding.RcNote.apply {
            layoutManager=StaggeredGridLayoutManager(spanCount,StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
            rcAdapter= RcNoteAdapter()
            rcAdapter.stateRestorationPolicy=RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            adapter=rcAdapter
            postponeEnterTransition(300L,TimeUnit.MILLISECONDS)
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }
        observerDataChanges()
    }
}

