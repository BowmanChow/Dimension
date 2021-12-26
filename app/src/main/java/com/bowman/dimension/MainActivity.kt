package com.bowman.dimension

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory.decodeStream
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bowman.dimension.ui.diary.DiaryFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.*
import java.util.*


@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar : androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)
        ThemeColor.init(this)
        setThemeColor()
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            1
        )
        var day: Long = (Date().time - Event.sdf.parse("2021 - 1 - 3")
                .time) / (24 * 60 * 60 * 1000) + 1
        (findViewById<View>(R.id.loveDays) as TextView).text = day.toString() + ""
        val photoFile = File(this.filesDir, "background_photo.jpg")
        if (photoFile.exists())
        {
            val inputStream = FileInputStream(photoFile)
            val bitmap = decodeStream(inputStream)
            val imageView: ImageView = findViewById(R.id.imageView)
            imageView.setImageBitmap(bitmap)
            inputStream.close()
        }
        mainActivity = this
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.change_theme -> {
                changeTheme()
                true
            }
            R.id.change_background -> {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, 1)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun changeTheme() {
        class Listener : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                ThemeColor.setTheme(color, this@MainActivity)
                this@MainActivity.setThemeColor()
            }
            override fun onCancel(dialog: AmbilWarnaDialog?) {
            }
        }
        val dialog = AmbilWarnaDialog(this, ThemeColor.theme_color, Listener())
        dialog.show()
    }

    private fun setThemeColor() {
        val toolbar : androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        toolbar.setBackgroundColor(ThemeColor.theme_color)
        window.statusBarColor = ThemeColor.theme_color
        navView.itemIconTintList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(ThemeColor.theme_color, Color.DKGRAY))
        navView.itemTextColor = navView.itemIconTintList
        val addButton: FloatingActionButton? = findViewById(R.id.addDiary)
        addButton?.backgroundTintList = ColorStateList.valueOf(ThemeColor.theme_color)
    }

    @Throws(IOException::class)
    fun copyStream(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(1024)
        var bytesRead: Int = 0
        while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
        }
    }
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                1 -> if (data != null) {
                    val uri: Uri? = data.data
//                    val path = uri?.path
//                    val file = File(path?.substring(path?.indexOf("/storage")))
                    val inputStream1 = contentResolver.openInputStream(uri!!);
                    val photoFile = File(this.filesDir, "background_photo.jpg")
                    val bitmap = decodeStream(inputStream1)
                    val fileOutputStream = FileOutputStream(photoFile)
                    val inputStream2 = contentResolver.openInputStream(uri!!);
                    copyStream(inputStream2!!, fileOutputStream)
                    fileOutputStream.close()
                    val imageView: ImageView = findViewById(R.id.imageView)
                    imageView.setImageBitmap(bitmap)
                    inputStream1!!.close()
                    inputStream2.close()
                }
            }
        }
    }

    fun addEvent(view: View) {
        EventEditorActivity.Data.EventName = ""
        EventEditorActivity.Data.EventDate = Date()
        EventEditorActivity.Data.Location = ""
        EventEditorActivity.Data.Description = ""
        EventEditorActivity.Data.isAdd = true
        DiaryFragment.editEvent(view)
    }

    companion object {
        private lateinit var mainActivity: MainActivity
        fun getInstance(): MainActivity {
            return mainActivity
        }
    }

}