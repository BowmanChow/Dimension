package com.bowman.dimension.ui.anniversary

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bowman.dimension.R
import com.bowman.dimension.ThemeColor
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AnniversaryFragment : Fragment() {

    private lateinit var dashboardViewModel: AnniversaryViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
                ViewModelProvider(this).get(AnniversaryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_anniversary, container, false)
        val addButton: FloatingActionButton = root.findViewById(R.id.addAnni)
        addButton.backgroundTintList = ColorStateList.valueOf(ThemeColor.theme_color)
        addButton.imageTintList = ColorStateList.valueOf(Color.WHITE)
        return root
    }
}