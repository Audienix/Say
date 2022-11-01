package com.twain.say.ui.home.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.twain.say.R
import com.twain.say.R.*
import com.twain.say.data.model.AlertDialogDetails
import com.twain.say.databinding.FragmentHomeBinding
import com.twain.say.ui.common.AlertDialogFragment
import com.twain.say.ui.home.model.Note
import com.twain.say.ui.home.repository.HomeRepository
import com.twain.say.ui.home.viewmodel.HomeViewModel
import com.twain.say.utils.Extensions.statusBarColorFromResource
import com.twain.say.utils.UIState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var repository: HomeRepository
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var adapter: NoteListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.uiState = viewModel.uiState

        binding.apply {
//            btnSettings.setOnClickListener { navController.navigate(R.id.action_homeFragment_to_settingsFragment) }
            fabAddNote.setOnClickListener {
                val action =
                    HomeFragmentDirections.actionHomeFragmentToEditNoteFragment(Note())
                navController.navigate(action)
            }

            setUpRecyclerView(recyclerViewNotes)

            val bottomSheetBehavior =
                BottomSheetBehavior.from(bottomSheetDialogLayout.bottomSheetDialog)
            bottomSheetBehavior.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN)
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                    Unused
                }
            })
            bottomSheetDialogLayout.linearLayoutCompat.setOnClickListener {
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                else
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query != null) {
                        getItemsFromDb(query)
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText != null) {
                        getItemsFromDb(newText)
                    }
                    return true
                }

            })
        }
        viewModel.apply {
            notes.observe(viewLifecycleOwner) {
                if (it != null && it.isNotEmpty())
                    uiState.set(UIState.HAS_DATA)
                else
                    uiState.set(UIState.EMPTY)
                adapter.submitList(it)
            }
        }
    }

    private fun setUpRecyclerView(recyclerViewNotes: RecyclerView) {
        adapter = NoteListAdapter(requireActivity()) { view: View, note: Note, _: Int ->
            showNoteActionMenu(view, note)
        }
        val layoutManager =
            GridLayoutManager(requireContext(), 1)
        recyclerViewNotes.also {
            it.layoutManager = layoutManager
            it.adapter = adapter
        }
    }

    private fun showNoteActionMenu(view: View, note: Note) {
        val popupMenu = PopupMenu(requireActivity(), view)
        popupMenu.menuInflater.inflate(menu.menu_note_card, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_show_info -> {}
                R.id.action_edit
                -> {
                    val action =
                        HomeFragmentDirections.actionHomeFragmentToEditNoteFragment(note)
                    navController.navigate(action)
                }
                R.id.action_delete -> {launchDeleteNoteDialog(note)}
            }
            true
        }
        popupMenu.show()
    }

    override fun onResume() {
        super.onResume()
        statusBarColorFromResource(color.translucent_white)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun launchDeleteNoteDialog(note: Note) {
        val alertDlg = AlertDialogDetails(
            drawable.ic_alert_warning,
            resources.getString(string.delete_note),
            "${requireContext().getString(string.confirm_deletion)} '${note.title}?'",
            resources.getString(string.yes),
            resources.getString(string.no),
            true
        )
        AlertDialogFragment(requireContext()).show(alertDlg) { dialog, response ->
            if(response == AlertDialogFragment.ResponseType.YES){
                viewModel.deleteNote(note)
                lifecycleScope.launch(Dispatchers.IO) { File(note.filePath).delete() }
                dialog.dismiss()
            }
            else if(response == AlertDialogFragment.ResponseType.NO){
                dialog.dismiss()
            }
        }
    }

    private fun getItemsFromDb(searchText: String) {
        val searchQuery = "%$searchText%"

        viewModel.searchNotes(query = searchQuery).observe(this) { list ->
            adapter.submitList(list)

        }

    }
}