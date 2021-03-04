package com.qugengting.email.activity

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.qugengting.email.R
import com.qugengting.email.adapter.SearchHisAdapter
import com.qugengting.email.bean.SearchHisBean
import com.qugengting.email.constants.MailConstants
import com.qugengting.email.eventbus.MailSearchEvent
import com.qugengting.email.fragment.*
import com.qugengting.email.utils.setListener
import com.qugengting.email.utils.systembar.StatusBarUtils
import kotlinx.android.synthetic.main.activity_email_search.*
import org.greenrobot.eventbus.EventBus
import org.litepal.LitePal
import java.util.*

/**
 * Created by xuruibin on 2018/5/23.
 * 描述：
 */
class EmailSearchActivity : BaseActivity() {

    private lateinit var hisAdapter: SearchHisAdapter
    private lateinit var hisList: MutableList<SearchHisBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtils.setDefault(this)
        setContentView(R.layout.activity_email_search)

        val fragmentList: MutableList<BaseFragment> = ArrayList()
        val searchAllFragment = SearchAllFragment()
        val searchReceiverFragment = SearchReceiverFragment()
        val searchSenderFragment = SearchSenderFragment()
        val searchTitleFragment = SearchTitleFragment()
        searchAllFragment.fragmentTitle = getString(R.string.mail_search_all)
        searchReceiverFragment.fragmentTitle = getString(R.string.mail_search_recv)
        searchSenderFragment.fragmentTitle = getString(R.string.mail_search_send)
        searchTitleFragment.fragmentTitle = getString(R.string.mail_search_subject)
        fragmentList.add(searchAllFragment)
        fragmentList.add(searchReceiverFragment)
        fragmentList.add(searchSenderFragment)
        fragmentList.add(searchTitleFragment)

        val adapter = BaseFragmentAdapter(supportFragmentManager, fragmentList)
        vpSearch.adapter = adapter
        vpSearch.offscreenPageLimit = 4
        adapter.notifyDataSetChanged()
        tabSearch.tabMode = TabLayout.MODE_FIXED
        tabSearch.setSelectedTabIndicatorHeight(0)
        tabSearch.setupWithViewPager(vpSearch)

        edtSearch.setListener {
            if (it > 0) {
                rvHis.visibility = View.GONE
                val key = edtSearch.text.toString()
                EventBus.getDefault().post(MailSearchEvent(key))

                var old = LitePal.where("mailAccount = ? and key = ?"
                        , MailConstants.MAIL_ACCOUNT, key).findFirst(SearchHisBean::class.java)
                if (old != null) {
                    old.time = System.currentTimeMillis()
                } else {
                    old = SearchHisBean(MailConstants.MAIL_ACCOUNT, key, System.currentTimeMillis())
                }
                old.save()
                resetHisData()
            } else {
                rvHis.visibility = View.VISIBLE
            }
        }
        tvCancle.setOnClickListener {
            hideInput()
            finish()
        }

        hisList = LitePal.where("mailAccount = ?", MailConstants.MAIL_ACCOUNT)
                .order("time desc").limit(5).find(SearchHisBean::class.java)
        hisAdapter = SearchHisAdapter(this@EmailSearchActivity)
        rvHis.adapter = hisAdapter
        rvHis.layoutManager = LinearLayoutManager(this)
        hisAdapter.setDataList(hisList)
        hisAdapter.setOnDelListener(object : SearchHisAdapter.OnSearchHisListener {
            override fun onDel(position: Int) {
                val bean = hisList[position]
                bean.delete()
                resetHisData()
            }

            override fun onItemClick(position: Int) {
                val bean = hisList[position]
                edtSearch.setText(bean.key)
                edtSearch.setSelection(bean.key.length)
            }

        })
    }

    private fun resetHisData() {
        hisList = LitePal.where("mailAccount = ?", MailConstants.MAIL_ACCOUNT)
                .order("time desc").limit(5).find(SearchHisBean::class.java)
        hisAdapter.setDataList(hisList)
    }
}