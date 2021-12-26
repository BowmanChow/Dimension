package com.bowman.dimension

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.bowman.dimension.ui.diary.DiaryFragment
import com.google.gson.Gson
import com.tencent.cos.xml.model.tag.ListBucket.Contents
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class Event constructor(
    val name: String,
    date: Date,
    val location: String,
    val description: String,
    val modifiable: Boolean = true
) : Serializable {

    private val date: String = sdf.format(date)

    fun getDate(): Date {
        return sdf.parse(date)
    }

    companion object {
        private var events = TreeMap<Date, Event>()
        val sdf = SimpleDateFormat("yyyy - MM - dd")
        fun initEvents(context: Context) {
            val date = sdf.parse("2021 - 1 - 3")
            events[date] = Event(
                "量子力学考试",
                date!!,
                "清华六教C300",
                "",
                false
            )
            val diaryFiles =
                getLocalDiaryFiles(context) ?: return
            for (diaryFile in diaryFiles) {
                Log.d(ContentValues.TAG, diaryFile.path)
                val event = Gson().fromJson(
                    String(Files.readAllBytes(Paths.get(diaryFile.path))),
                    Event::class.java
                )
                events[sdf.parse(event.date)] = event
            }
        }

        fun addEvent(activity: Activity, event: Event) {
            try {
                events[sdf.parse(event.date)] = event
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            uploadEvent(activity, event)
        }

        fun createDir(context: Context) {
            val file = File(context.filesDir, "diary")
            if (!file.exists()) {
                file.mkdirs()
            }
        }

        private fun getLocalDiaryFiles(context: Context): Array<File> {
            createDir(context)
            val diaryDir = File(context.filesDir, "diary")
            return diaryDir.listFiles()
        }

        private fun uploadEvent(activity: Activity, event: Event) {
            val fileName = "diary/" + event.date + ".json"
            val file =
                File(activity.filesDir, fileName)
            try {
                val outputStream = FileOutputStream(file)
                outputStream.write(Gson().toJson(event).toByteArray())
                outputStream.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            CloudFileTransfer.transfer(
                CloudFileConstants.TransferType.UPLOAD,
                activity,
                fileName
            ) {}
        }

        fun beginDownloadEvent(activity: Activity, cloudDiaryList: List<Contents>) {
            val localDiaryFiles =
                getLocalDiaryFiles(activity)
            for (localDiaryFile in localDiaryFiles) {
                localDiaryFile.delete()
            }
            val cloudFileNames =
                arrayOfNulls<String>(cloudDiaryList.size)
            for (i in cloudDiaryList.indices) {
                cloudFileNames[i] = cloudDiaryList[i].key
            }
            CloudFileTransfer.batchDownload(activity, cloudFileNames) {
                downloadEventsComplete(activity)
            }
        }

        fun downloadEventsComplete(activity: Activity) {
            events = TreeMap()
            initEvents(activity)
            activity.runOnUiThread(Runnable { DiaryFragment.refreshListView() })
        }

        val eventList: List<Event>
            get() {
                val list =
                    ArrayList(events.values)
                list.reverse()
                return list
            }

        fun removeEvent(activity: Activity, date: Date?, isDeleteCloudFile: Boolean = true) {
            events.remove(date)
            val fileName = "diary/" + sdf.format(date) + ".json"
            File(activity.filesDir, fileName).delete()
            if (isDeleteCloudFile) {
                CloudFileTransfer.deleteFile(activity, fileName)
            }
        }

        fun findEvent(date: Date): Event? {
            return events[date]
        }
    }

}
