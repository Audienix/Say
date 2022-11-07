package com.twain.say.ui.home.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.twain.say.R
import com.twain.say.databinding.ItemVoiceNoteBinding
import com.twain.say.helper.DateTimePicker
import com.twain.say.ui.home.model.Note
import com.twain.say.ui.home.viewmodel.HomeViewModel
import com.twain.say.utils.ReminderAvailableState
import com.twain.say.utils.formatReminderDate

class NoteListAdapter(
    val context: Context,
    val viewModel: HomeViewModel,
    val noteEditClickListener: (View, Note, Boolean) -> Unit
) : ListAdapter<Note, NoteListAdapter.NoteListViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteListViewHolder {
        val binding =
            ItemVoiceNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteListViewHolder(private val binding: ItemVoiceNoteBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        fun bind(note: Note) {
            binding.note = note
            binding.apply {
                reminderAvailableState = viewModel.reminderAvailableState
                reminderCompletionState = viewModel.reminderCompletionState
            }
            binding.btnPlay.setOnClickListener(this)
            binding.tvNoteMenu.setOnClickListener(this)
            binding.tvSetReminder.setOnClickListener(this)
            binding.tvReminderTime.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            if (p0?.id == R.id.btn_play) {
                noteEditClickListener(p0, getItem(adapterPosition), true)
            } else if (p0?.id == R.id.tv_note_menu)
                noteEditClickListener(p0, getItem(adapterPosition), false)
            else if (p0?.id == R.id.tv_set_reminder || p0?.id == R.id.tv_reminder_time) {
                val dateTimePicker = DateTimePicker(context) { selectedDateTime ->
                    binding.note?.reminder = selectedDateTime.timeInMillis
                    viewModel.reminderAvailableState.set(ReminderAvailableState.HAS_REMINDER)
                    binding.tvReminderTime.text = formatReminderDate(selectedDateTime.timeInMillis)
                }

                if (binding.note?.reminder == null)
                    dateTimePicker.pickDate()
//                else
//                    openEditReminderDialog()
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note) = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Note, newItem: Note) = oldItem == newItem
        }
    }
}