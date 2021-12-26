package com.bowman.dimension.ui.diary

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.bowman.dimension.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class DiaryFragment : Fragment(), OnRefreshListener {

    private lateinit var homeViewModel: DiaryViewModel
    private var isRefresh = false //是否刷新中

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(DiaryViewModel::class.java)
        diaryFragment = this
        val root = inflater.inflate(R.layout.fragment_diary, container, false)
        val mSwipeLayout =
                root.findViewById<View>(R.id.swipeLayout_diary) as SwipeRefreshLayout?
        mSwipeLayout?.setOnRefreshListener(this)
        mSwipeLayout?.setDistanceToTriggerSync(200)
        Event.initEvents(requireActivity())
        adapter = EventAdapter(
                requireActivity(),  // Context上下文
                R.layout.event,  // 子项布局id
                Event.eventList
        ) // 数据
        val listView =
                root.findViewById<View>(R.id.diary_listView) as ListView?
        listView?.adapter = adapter
        val addButton: FloatingActionButton = root.findViewById(R.id.addDiary)
        addButton.backgroundTintList = ColorStateList.valueOf(ThemeColor.theme_color)
        addButton.imageTintList = ColorStateList.valueOf(Color.WHITE)
        return root
    }

    companion object {
        private lateinit var adapter: EventAdapter
        private lateinit var diaryFragment: DiaryFragment
        fun refreshListView() {
            adapter.clear()
            adapter.addAll(Event.eventList)
            adapter.notifyDataSetChanged()
        }
        fun editEvent(view: View) {
            val intent = Intent(diaryFragment.requireActivity(), EventEditorActivity::class.java)
            diaryFragment.requireActivity().startActivity(intent)
        }
    }

    open override fun onRefresh(): Unit {
        if (!isRefresh) {
            isRefresh = true
            //模拟加载网络数据，这里设置4秒，正好能看到4色进度条
            CloudFileTransfer.getFileList(requireActivity(), "diary/"
            ) { list ->
                Event.beginDownloadEvent(requireActivity(), list)
            }
            //显示或隐藏刷新进度条
                Snackbar.make(requireActivity().window.decorView, "Refreshing", Snackbar.LENGTH_SHORT)
                    .show()
                Handler().postDelayed({
                    (requireActivity().findViewById<View>(R.id.swipeLayout_diary) as SwipeRefreshLayout).isRefreshing =
                        false
                    isRefresh = false
                }, 2000)
            }
        }
}
