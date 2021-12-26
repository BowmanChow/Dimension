package com.bowman.dimension

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bowman.dimension.ui.diary.DiaryFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.text.ParseException
import java.util.*


class EventEditorActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_editor)
        val toolbar : androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar3)
        toolbar.setBackgroundColor(ThemeColor.theme_color)
        window.statusBarColor = ThemeColor.theme_color
        val addButton: FloatingActionButton = findViewById(R.id.addButton)
        addButton.backgroundTintList = ColorStateList.valueOf(ThemeColor.theme_color)
        addButton.imageTintList = ColorStateList.valueOf(Color.WHITE)
        val deleteButton: FloatingActionButton = findViewById(R.id.deleteButton)
        deleteButton.backgroundTintList = ColorStateList.valueOf(ThemeColor.theme_color)
        deleteButton.imageTintList = ColorStateList.valueOf(Color.WHITE)
        // Get the Intent that started this activity and extract the string
        val intent: Intent = intent
        (findViewById<View>(R.id.eventNameEdit) as EditText).setText(Data.EventName)
        (findViewById<View>(R.id.eventDateEdit) as TextView).text = Event.sdf.format(
            Data.EventDate
        )
        (findViewById<View>(R.id.eventLocationEdit) as EditText).setText(Data.Location)
        val content = SpannableString(Data.Description)
        //        content.setSpan(new UnderlineSpan(), 0, Data.Description.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        (findViewById<View>(R.id.eventDescription) as EditText).setText(content)
    }

    override fun finish() {
        super.finish()
        DiaryFragment.refreshListView()
    }

    @SuppressLint("SetTextI18n")
    fun selectDate(view: View?) {
        var date: Date? = Date()
        try {
            date = Event.sdf.parse(
                (findViewById<View>(R.id.eventDateEdit) as TextView).text
                    .toString()
            )
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val cal = Calendar.getInstance()
        cal.time = date
        val datePickerDialog = DatePickerDialog(
            this,
            AlertDialog.THEME_HOLO_LIGHT,
            OnDateSetListener { view1: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                (findViewById<View>(
                    R.id.eventDateEdit
                ) as TextView).text = year.toString() + " - " + (month + 1) + " - " + dayOfMonth
            },
            cal[Calendar.YEAR],
            cal[Calendar.MONTH],
            cal[Calendar.DAY_OF_MONTH]
        )
        datePickerDialog.show()
    }

    @Throws(ParseException::class)
    fun addEvent(view: View) {
        val date = Event.sdf.parse(
            (findViewById<View>(R.id.eventDateEdit) as TextView).text.toString()
        )
        if (Data.isAdd) {
            if (Event.findEvent(date!!) != null) {
                Snackbar.make(view, "已存在当前日期事件，无法添加", Snackbar.LENGTH_SHORT).show()
                return
            }
        } else if (Data.EventDate != date) {
            if (Event.findEvent(date!!) != null) {
                Snackbar.make(view, "已存在当前日期事件，无法添加", Snackbar.LENGTH_SHORT).show()
                return
            } else {
                Event.removeEvent(MainActivity.getInstance(), Data.EventDate)
            }
        } else {
            Event.removeEvent(MainActivity.getInstance(), Data.EventDate, false)
        }
        val event = Event(
            (findViewById<View>(R.id.eventNameEdit) as EditText).text.toString(),
            date,
            (findViewById<View>(R.id.eventLocationEdit) as EditText).text
                .toString(),
            (findViewById<View>(R.id.eventDescription) as EditText).text
                .toString()
        )
        Event.addEvent(MainActivity.getInstance(), event)

//        Snackbar.make(view, Integer.toString(Event.eventList.size()), Snackbar.LENGTH_SHORT).show();
        finish()
    }

    fun removeEvent(view: View?) {
        if (Data.isAdd) {
            finish()
            return
        }
        Event.removeEvent(this, Data.EventDate)
        finish()
    }

    object Data {
        var EventName = ""
        var EventDate = Date()
        var Location = ""
        var Description = ""
        var isAdd = false
    }
}
