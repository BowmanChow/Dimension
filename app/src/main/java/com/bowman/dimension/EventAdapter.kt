package com.bowman.dimension

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.bowman.dimension.ui.diary.DiaryFragment
import java.text.ParseException
import java.text.SimpleDateFormat


class EventAdapter(
    context: Context,
    private val resourceId: Int,
    objects: List<Event>
) :
    ArrayAdapter<Event>(context, resourceId, objects) {
    private val ft = SimpleDateFormat("yyyy-MM-dd")
    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val event = getItem(position)
        val view: View
        val viewHolder: ViewHolder
        if (convertView == null) {
            // inflate出子项布局，实例化其中的图片控件和文本控件
            view = LayoutInflater.from(context).inflate(resourceId, null)
            viewHolder = ViewHolder(
                view.findViewById<View>(R.id.eventName) as TextView,
                view.findViewById<View>(R.id.eventDate) as TextView,
                view.findViewById<View>(R.id.eventCard) as CardView
            )
            viewHolder.EventCard.isClickable = true
            viewHolder.EventCard.setOnClickListener {
                var date = ft.parse(viewHolder.EventDate!!.text.toString())
//                Snackbar.make(view, date.toString(), Snackbar.LENGTH_SHORT).show()
                val e = Event.findEvent(date)
                if (!e!!.modifiable) return@setOnClickListener
                EventEditorActivity.Data.EventName = e.name
                try {
                    EventEditorActivity.Data.EventDate = e.getDate()
                } catch (parseException: ParseException) {
                    parseException.printStackTrace()
                }
                EventEditorActivity.Data.Location = e.location
                EventEditorActivity.Data.Description = e.description
                EventEditorActivity.Data.isAdd = false
                DiaryFragment.editEvent(view)
            }
            // 缓存图片控件和文本控件的实例
            view.tag = viewHolder
        } else {
            view = convertView
            // 取出缓存
            viewHolder = view.tag as ViewHolder
        }

        // 直接使用缓存中的图片控件和文本控件的实例
        viewHolder.EventName.text = event!!.name
        try {
            viewHolder.EventDate!!.text = ft.format(event!!.getDate())
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return view
    }

    // 内部类
    internal inner class ViewHolder constructor(
        val EventName: TextView,
        val EventDate: TextView,
        val EventCard: CardView
    )

}
