package com.qugengting.email.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.recyclerview.widget.LinearLayoutManager
import com.qugengting.email.R
import com.qugengting.email.adapter.SendAdapter
import com.qugengting.email.bean.MailBean
import com.qugengting.email.constants.ConstantsKeys
import com.qugengting.email.constants.MailConstants
import com.qugengting.email.eventbus.MailStatusEvent
import com.qugengting.email.helper.MailHelper
import com.qugengting.email.utils.start
import com.qugengting.email.utils.systembar.StatusBarUtils
import kotlinx.android.synthetic.main.activity_email_send_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal

/**
 * 发件箱列表界面
 * create by xuruibin in 2021.1.31
 */
class EmailSendListActivity : BaseActivity(), MailHelper {
    private lateinit var adapter: SendAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        StatusBarUtils.setDefault(this)
        System.setProperty("mail.mime.splitlongparameters", "false")
        setContentView(R.layout.activity_email_send_list)
        initView()
    }

    private fun initView() {
        val list = LitePal.where(
                "account = ? and type = ?", MailConstants.MAIL_ACCOUNT, "1")
                .order("sendTime desc").find(MailBean::class.java)
        adapter = SendAdapter(this)
        adapter.setOnDelListener(object : SendAdapter.OnSendSwipeListener {
            override fun onDel(position: Int) {
                val bean = adapter.getDataList()[position]
                adapter.remove(position)
                deleteMail(bean)
            }
        })
        adapter.setDataList(list)
        rv_mail_send.layoutManager = LinearLayoutManager(this)
        rv_mail_send.adapter = adapter
        edtSearch.inputType = InputType.TYPE_NULL //禁止弹出软键盘
        edtSearch.setOnClickListener {
            start<EmailSearchActivity> {}
        }
    }

    /**
     * 通过搜索进入到邮件详情，并删除某邮件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateStatus(event: MailStatusEvent) {
        if (event.type == MailStatusEvent.TYPE_DELETE) {
            val list = LitePal.where(
                    "account = ? and type = ?", MailConstants.MAIL_ACCOUNT, "1")
                    .order("sendTime desc").find(MailBean::class.java)
            adapter.setDataList(list)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //邮件详情页操作删除邮件，由此进行处理
        if (resultCode == ConstantsKeys.DELETE_RESULT_CODE) {
            val pos = data!!.getIntExtra("position", -1)
            val mailBean = adapter.getDataList()[pos]
            adapter.remove(pos)
            deleteMail(mailBean)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    companion object {
        private val TAG = EmailSendListActivity::class.java.simpleName
    }
}