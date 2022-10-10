package com.twain.say.ui.home.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.twain.say.utils.colors
import com.twain.say.utils.currentDate
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "notes_table")
data class Note(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var title: String = String(),
    var description: String = String(),
    var color: Int = colors.random(),
    var lastModificationDate: Long = currentDate().timeInMillis,
    var size: String = String(),
    var audioLength: Long = -1L,
    var filePath: String = String(),
    var started: Boolean = false,
    var reminder: Long? = null
) : Parcelable