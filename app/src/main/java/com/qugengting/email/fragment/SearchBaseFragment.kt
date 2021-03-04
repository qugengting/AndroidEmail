package com.qugengting.email.fragment

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qugengting.email.adapter.SearchResultAdapter
import com.qugengting.email.bean.MailBean
import java.util.*

abstract class SearchBaseFragment : BaseFragment() {
    protected lateinit var rvSearch: RecyclerView
    protected lateinit var adapter: SearchResultAdapter
    protected lateinit var list: MutableList<MailBean>
    protected var key = ""

    fun initView() {
        list = ArrayList()
        adapter = SearchResultAdapter(activity!!)
        rvSearch.adapter = adapter
        rvSearch.layoutManager = LinearLayoutManager(activity)
        adapter.setDataList(list)
    }

    override fun onResume() {
        super.onResume()
        resetData()
    }

    abstract fun resetData()
}