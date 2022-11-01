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
import com.twain.say.helper.AudioRecorder
import com.twain.say.ui.home.model.Note

class NoteListAdapter(
    val context: Context,
    val noteEditClickListener: (View, Note, Int) -> Unit
) : ListAdapter<Note, NoteListAdapter.NoteListViewHolder>(DIFF_CALLBACK) {

    private var audioRecorder: AudioRecorder? = null
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
        private lateinit var note: Note
        fun bind(note: Note) {
            binding.note = note
            this.note = note
            binding.btnPlay.setOnClickListener(this)
            binding.tvNoteMenu.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            if (p0?.id == R.id.btn_play) {
                if (audioRecorder == null)
                    audioRecorder = AudioRecorder(
                        context,
                        binding.btnPlay,
                        binding.tvRecordLength,
                        this.note
                    )
                audioRecorder?.manageExistingAudioRecording()
            } else if (p0?.id == R.id.tv_note_menu)
                noteEditClickListener(p0, getItem(adapterPosition), adapterPosition)
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note) = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Note, newItem: Note) = oldItem == newItem
        }
    }
}