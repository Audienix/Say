package com.twain.say.ui.home.view

import android.graphics.Color
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.twain.say.R
import com.twain.say.R.*
import com.twain.say.data.model.AlertDialogDetails
import com.twain.say.databinding.BsheetDialogNowPlayingBinding
import com.twain.say.databinding.FragmentHomeBinding
import com.twain.say.helper.AudioRecorder
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
class HomeFragment : Fragment(){

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var audioRecorder: AudioRecorder? = null

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

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query != null) {
                        getSearchedItemsFromDb(query)
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText != null) {
                        getSearchedItemsFromDb(newText)
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
        adapter =
            NoteListAdapter(requireActivity()) { view: View, note: Note, playAudioNote: Boolean ->
                if (playAudioNote)
                    showAudioPlayerBottomSheet(note)
                else
                    showNoteActionMenu(view, note)
            }
        val layoutManager =
            GridLayoutManager(requireContext(), 1)
        recyclerViewNotes.also {
            it.layoutManager = layoutManager
            it.adapter = adapter
        }
    }

    private fun showAudioPlayerBottomSheet(_note: Note) {
        val bindingView = BsheetDialogNowPlayingBinding.inflate(layoutInflater)
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bindingView.apply {
            note = _note
            btnPlay.setOnClickListener {
                if (audioRecorder == null) {
                    audioRecorder = AudioRecorder(
                        requireContext(),
                        btnPlay,
                        tvTimer,
                        _note
                    )
                }
                audioRecorder?.manageExistingAudioRecording()
            }
            ivClose.setOnClickListener { bottomSheetDialog.dismiss() }
        }
        bottomSheetDialog.dismissWithAnimation = true
        bottomSheetDialog.edgeToEdgeEnabled
        bottomSheetDialog.setContentView(bindingView.root)
        bottomSheetDialog.setCanceledOnTouchOutside(false)
        bottomSheetDialog.setOnDismissListener {
            audioRecorder?.cleanupResource()
            audioRecorder = null
        }
        bottomSheetDialog.show()

    }

    private fun showNoteActionMenu(view: View, note: Note) {
        val popupMenu = PopupMenu(requireActivity(), view)
        popupMenu.menuInflater.inflate(menu.menu_note_card, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit
                -> {
                    val action =
                        HomeFragmentDirections.actionHomeFragmentToEditNoteFragment(note)
                    navController.navigate(action)
                }
                R.id.action_delete -> {
                    launchDeleteNoteDialog(note)
                }
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
            if (response == AlertDialogFragment.ResponseType.YES) {
                viewModel.deleteNote(note)
                lifecycleScope.launch(Dispatchers.IO) { File(note.filePath).delete() }
                dialog.dismiss()
            } else if (response == AlertDialogFragment.ResponseType.NO) {
                dialog.dismiss()
            }
        }
    }

    private fun getSearchedItemsFromDb(searchText: String) {
        val searchQuery = "%$searchText%"

        viewModel.searchNotes(query = searchQuery).observe(this) { list ->
            if (list.isNotEmpty()) {
                adapter.submitList(list)
                binding.recyclerViewNotes.visibility = View.VISIBLE

                binding.ivEmptyNotes.visibility = View.GONE
                binding.tvEmptyNotes.visibility = View.GONE
            } else {
                binding.recyclerViewNotes.visibility = View.GONE

                binding.tvEmptyNotes.text = resources.getString(string.no_notes_list)
                binding.tvEmptyNotes.setTextColor(Color.RED)
                binding.ivEmptyNotes.setImageResource(drawable.ic_no_records)
                binding.ivEmptyNotes.visibility = View.VISIBLE
                binding.tvEmptyNotes.visibility = View.VISIBLE
            }
        }
    }
}