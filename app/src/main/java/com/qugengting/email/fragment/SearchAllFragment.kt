package com.qugengting.email.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qugengting.email.R
import com.qugengting.email.bean.MailBean
import com.qugengting.email.constants.MailConstants
import com.qugengting.email.eventbus.MailSearchEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal

class SearchAllFragment : SearchBaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_search, container, false)
            rvSearch = view.findViewById(R.id.rvSearch)
            initView()
            EventBus.getDefault().register(this)
        }
        return view
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateData(event: MailSearchEvent) {
        val searchKey = event.searchKey
        key = "%$searchKey%"
        resetData()
    }

    override fun resetData() {
        list = LitePal.where("account = ? and sender like ? or senderAddress like ? or receiveTo like ? or title like ?"
                , MailConstants.MAIL_ACCOUNT, key, key, key, key).order("sendTime desc").find(MailBean::class.java)
        adapter.setDataList(list)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

}