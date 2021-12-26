package com.bowman.dimension

import android.app.Activity
import java.io.File
import java.io.FileOutputStream

class ThemeColor {
    companion object {
        var theme_color = R.color.purple_200
        fun init(activity: Activity) {
            val themeFile = File(activity.filesDir, "theme.txt")
            if (!themeFile.exists())
            {
                val outputStream = FileOutputStream(themeFile)
                outputStream.write(theme_color.toString().toByteArray())
                outputStream.close()
            }
            theme_color = themeFile.readLines()[0].toInt()
        }
        fun setTheme(color: Int, activity: Activity) {
            theme_color = color
            val themeFile = File(activity.filesDir, "theme.txt")
            val outputStream = FileOutputStream(themeFile)
            outputStream.write(theme_color.toString().toByteArray())
            outputStream.close()
        }
    }
}