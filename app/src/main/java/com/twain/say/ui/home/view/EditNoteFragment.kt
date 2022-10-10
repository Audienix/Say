package com.twain.say.ui.home.view

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.twain.say.MainActivity
import com.twain.say.R
import com.twain.say.databinding.DialogEditReminderBinding
import com.twain.say.databinding.FragmentEditNoteBinding
import com.twain.say.helper.AudioRecorder
import com.twain.say.ui.home.model.Note
import com.twain.say.ui.home.viewmodel.HomeViewModel
import com.twain.say.utils.*
import com.twain.say.utils.Extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


@AndroidEntryPoint
class EditNoteFragment : Fragment(), View.OnClickListener, TimePickerDialog.OnTimeSetListener,
    DatePickerDialog.OnDateSetListener {

    private var _binding: FragmentEditNoteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private val args: EditNoteFragmentArgs by navArgs()
    private var file: File? = null
    private var isRecording = false

    private lateinit var _note: Note
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var navController: NavController

    private var pickedDateTime: Calendar? = null
    private val currentDateTime by lazy { currentDate() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditNoteBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboard(requireContext(), binding.root)

        _note = args.note
        navController = Navigation.findNavController(view)
        audioRecorder = AudioRecorder(
            requireActivity(),
            binding.btnRecord,
            binding.tvTimer,
            _note
        )

        binding.lifecycleOwner = this
        binding.apply {
            note = _note
            uiState = viewModel.uiState
            reminderAvailableState = viewModel.reminderAvailableState
            reminderCompletionState = viewModel.reminderCompletionState
        }

        binding.apply {
            btnBack.setOnClickListener {
                navController.navigate(R.id.action_editNoteFragment_to_homeFragment)
            }
            cardAddReminder.setOnClickListener {
                if (viewModel.reminderAvailableState.get() == ReminderAvailableState.NO_REMINDER)
                    pickDate()
                else
                    openEditReminderDialog()
            }
            btnDelete.setOnClickListener { launchDeleteNoteDialog(requireContext()) }
            btnRecord.setOnClickListener(this@EditNoteFragment)
            fabSaveNote.setOnClickListener(this@EditNoteFragment)

            if (args.note.id == 0) {
                btnRecord.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_baseline_voice, null
                    )
                )
                viewModel.uiState.set(UIState.EMPTY)
                file = null
            } else {
                setup()
            }
        }
    }

    private fun setup() {
        binding.apply {
            btnRecord.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_baseline_play_recording, null
                )
            )
            lifecycleScope.launch(Dispatchers.IO) {
                file = File(_note.filePath)
            }
            viewModel.apply {
                uiState.set(UIState.HAS_DATA)
                getNote(args.note.id).observe(viewLifecycleOwner) {
                    if (it.reminder != null) {
                        reminderAvailableState.set(ReminderAvailableState.HAS_REMINDER)
                        if (currentDate().timeInMillis > args.note.reminder!!) {
                            reminderCompletionState.set(ReminderCompletionState.COMPLETED)
                        } else {
                            reminderCompletionState.set(ReminderCompletionState.ONGOING)
                        }
                    } else {
                        reminderAvailableState.set(ReminderAvailableState.NO_REMINDER)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
//        if (args.note.id == 0)
//            binding.etNoteTitle.apply {
//                requestFocus()
//                showKeyboardFor(requireContext())
//            }
        updateStatusBarColor(requireActivity(), binding.note!!.color)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioRecorder.cleanupResource()
        _binding = null
    }

    override fun onClick(p0: View?) {
        binding.apply {
            if (args.note.id == 0) {
                onClickWhenIdIsZero(p0)
            } else {
                onClickWhenIdIsNotZero(p0)
            }
        }
    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        pickedDateTime!!.set(Calendar.HOUR_OF_DAY, p1)
        pickedDateTime!!.set(Calendar.MINUTE, p2)
        if (pickedDateTime!!.timeInMillis <= currentDate().timeInMillis) {
            pickedDateTime!!.run {
                set(Calendar.DAY_OF_MONTH, currentDateTime.get(Calendar.DAY_OF_MONTH) + 1)
                set(Calendar.YEAR, currentDateTime.get(Calendar.YEAR))
                set(Calendar.MONTH, currentDateTime.get(Calendar.MONTH))
            }
        }
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        pickedDateTime = currentDate()
        pickedDateTime!!.set(p1, p2, p3)
        val hourOfDay = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val minuteOfDay = currentDateTime.get(Calendar.MINUTE)
        val timePickerDialog =
            TimePickerDialog(requireContext(), this, hourOfDay, minuteOfDay, false)

        timePickerDialog.setOnDismissListener {
            _note.reminder = pickedDateTime!!.timeInMillis
            viewModel.reminderAvailableState.set(ReminderAvailableState.HAS_REMINDER)
            binding.tvReminderDate.text = formatReminderDate(pickedDateTime!!.timeInMillis)
        }
        timePickerDialog.show()
    }

    private fun onClickWhenIdIsNotZero(p0: View?) {
        binding.apply {
            when (p0) {
                btnRecord -> {
                   audioRecorder.manageExistingAudioRecording()
                }
                fabSaveNote -> {
                    val note = _note.copy(
                        title = etNoteTitle.text.toString().trim(),
                        description = etNoteDescription.text.toString().trim()
                            .ifEmpty { resources.getString(R.string.no_description) },
                        lastModificationDate = currentDate().timeInMillis
                    )
                    if (note.title.isNotBlank()) {
                        viewModel.updateNote(note)
                        if (pickedDateTime?.timeInMillis != null && pickedDateTime?.timeInMillis != currentDateTime.timeInMillis)
                            startAlarm(requireContext(), pickedDateTime!!.timeInMillis, note)
                        navController.navigate(R.id.action_editNoteFragment_to_homeFragment)
                    } else {
                        requireContext().getString(R.string.title_required)
                            .showToast(requireContext(), Toast.LENGTH_SHORT)
                        etNoteTitle.requestFocus()
                    }
                }
            }
        }
    }

    private fun onClickWhenIdIsZero(p0: View?) {
        val context = requireContext()
        binding.apply {
            when (p0) {
                btnRecord -> {
                    if (!isRecording) {
                        if (hasPermission(context, Manifest.permission.RECORD_AUDIO)) {
                            btnRecord.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.ic_baseline_recording_voice,
                                    null
                                )
                            )
                                .run {
                                    isRecording = true
                                    audioRecorder.startRecording()
                                }
                        } else
                            requestPermission(
                                requireActivity(),
                                context.getString(R.string.permission_required),
                                MainActivity.RECORD_AUDIO_PERMISSION_CODE,
                                Manifest.permission.RECORD_AUDIO
                            )
                    } else {
//                        btnRecord.setBackgroundResource(R.drawable.ic_circle)
//                        ViewCompat.setBackgroundTintList(
//                            btnRecord,
//                            ColorStateList.valueOf(_note.color)
//                        )
                        btnRecord.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                resources, R.drawable.ic_baseline_voice, null
                            )
                        )
                            .run {
                                isRecording = false
                                audioRecorder.stopRecording()
                            }
                    }
                }
                fabSaveNote -> {
                    if (etNoteTitle.text.toString().isNotBlank()) {
                        audioRecorder.stopRecording()
                        if (_note.audioLength <= 0) {
                            context.getString(R.string.record_note_before_saving)
                                .showToast(context, Toast.LENGTH_SHORT)
                            return
                        }
                        val note = _note.copy(
                            title = etNoteTitle.text.toString().trim(),
                            description = etNoteDescription.text.toString().trim()
                        )
                        viewModel.insertNote(note)
                        context.getString(R.string.note_saved)
                            .showToast(requireContext(), Toast.LENGTH_SHORT)
                        if (pickedDateTime?.timeInMillis != null && pickedDateTime!!.timeInMillis <= currentDateTime.timeInMillis)
                            startAlarm(context, pickedDateTime!!.timeInMillis, note)
                        navController.navigate(R.id.action_editNoteFragment_to_homeFragment)
                    } else {
                        context.getString(R.string.title_required)
                            .showToast(requireContext(), Toast.LENGTH_SHORT)
                        etNoteTitle.requestFocus()
                    }
                }
            }
        }
    }

    private fun pickDate() {
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)

        //Get yesterday's date
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, 0)

        val datePickerDialog =
            DatePickerDialog(requireContext(), this, startYear, startMonth, startDay)
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun openEditReminderDialog() {
        val view = DialogEditReminderBinding.inflate(layoutInflater)
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        view.apply {
            note = _note
            btnDeleteReminder.setOnClickListener {
                viewModel.reminderCompletionState.set(ReminderCompletionState.COMPLETED)
                viewModel.reminderAvailableState.set(ReminderAvailableState.NO_REMINDER)
                _note.reminder = null
                cancelAlarm(requireContext(), _note.id)
                bottomSheetDialog.dismiss()
            }
            btnModifyReminder.setOnClickListener {
                bottomSheetDialog.dismiss()
                pickDate()
            }
        }
        bottomSheetDialog.edgeToEdgeEnabled
        bottomSheetDialog.setContentView(view.root)
        bottomSheetDialog.show()
    }

    private fun launchDeleteNoteDialog(context: Context) {
        val materialDialog = MaterialAlertDialogBuilder(context)
        materialDialog.apply {
            setTitle(context.getString(R.string.delete_note))
            setMessage("${context.getString(R.string.confirm_deletion)} '${_note.title}?'")
            setNegativeButton(context.getString(R.string.no)) { dialog, _ -> dialog?.dismiss() }
            setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                viewModel.deleteNote(_note)
                lifecycleScope.launch(Dispatchers.IO) { file?.delete() }
                navController.navigate(R.id.action_editNoteFragment_to_homeFragment)
            }
            show()
        }
    }
}