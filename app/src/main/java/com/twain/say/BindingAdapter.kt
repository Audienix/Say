package com.twain.say

import android.graphics.Paint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.twain.say.ui.home.model.Note
import com.twain.say.ui.home.view.NoteListAdapter

@BindingAdapter("listItems")
fun bindItemRecyclerView(recyclerView: RecyclerView, data: List<Note>?) {
    val adapter = recyclerView.adapter as NoteListAdapter
    adapter.submitList(data)
}

@BindingAdapter("visible")
fun View.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("editNoteVisible")
fun View.setEditNoteVisible(audioLength: Long) {
    visibility = when (audioLength) {
        0L -> View.GONE
        else -> View.VISIBLE
    }
}

@BindingAdapter("timerVisible")
fun View.setTimerVisible(audioLength: Long) {
    visibility = when (audioLength) {
        0L -> View.GONE
        else -> View.VISIBLE
    }
}

@BindingAdapter("strikeThrough")
fun strikeThrough(textView: TextView, strikeThrough: Boolean) {
    if (strikeThrough) {
        textView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
    } else {
        textView.paintFlags = 0
    }
}

@BindingAdapter("timeText")
fun TextView.timeText(value: Long) {
    text = if (value == -1L)
        "00:00:00"
    else
        String.format("%02d:%02d:%02d", value / 3600, (value % 3600) / 60, value % 60)
}

@BindingAdapter("image")
fun ImageView.loadImage(image: Int) {
    this.load(image)
}

@BindingAdapter("sizeText")
fun TextView.sizeText(value: String) {
    text = "${value}MB"
}