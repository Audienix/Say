package com.twain.say.helper

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.DatePicker
import android.widget.TimePicker
import com.twain.say.utils.currentDate
import java.util.*

class DateTimePicker(private val context: Context, private val dismissListener: (dateTime : Calendar) -> Unit) :
    TimePickerDialog.OnTimeSetListener,
    DatePickerDialog.OnDateSetListener {
    private var selectedDateTime: Calendar = currentDate()
    private val currentDateTime = currentDate()

    fun pickDate() {
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)

        //Get yesterday's date
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, 0)

        val datePickerDialog =
            DatePickerDialog(context, this, startYear, startMonth, startDay)
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        selectedDateTime.set(Calendar.HOUR_OF_DAY, p1)
        selectedDateTime.set(Calendar.MINUTE, p2)
        if (selectedDateTime.timeInMillis <= currentDate().timeInMillis) {
            selectedDateTime.run {
                set(Calendar.DAY_OF_MONTH, currentDateTime.get(Calendar.DAY_OF_MONTH) + 1)
                set(Calendar.YEAR, currentDateTime.get(Calendar.YEAR))
                set(Calendar.MONTH, currentDateTime.get(Calendar.MONTH))
            }
        }
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        selectedDateTime.set(p1, p2, p3)
        val hourOfDay = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val minuteOfDay = currentDateTime.get(Calendar.MINUTE)
        val timePickerDialog =
            TimePickerDialog(context, this, hourOfDay, minuteOfDay, false)

        timePickerDialog.setOnDismissListener {
            dismissListener(selectedDateTime)
        }
        timePickerDialog.show()
    }
}