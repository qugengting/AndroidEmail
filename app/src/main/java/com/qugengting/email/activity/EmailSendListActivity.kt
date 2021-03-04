package com.qugengting.email.activity

import android.os.Bundle
import android.text.InputType
import androidx.recyclerview.widget.LinearLayoutManager
import com.qugengting.email.R
import com.qugengting.email.adapter.SendAdapter
import com.qugengting.email.bean.MailBean
import com.qugengting.email.constants.MailConstants
import com.qugengting.email.utils.start
import com.qugengting.email.utils.systembar.StatusBarUtils
import kotlinx.android.synthetic.main.activity_email_send_list.*
import org.litepal.LitePal

/**
 * 发件箱列表界面
 * create by xuruibin in 2021.1.31
 */
class EmailSendListActivity : BaseActivity() {
    private lateinit var adapter: SendAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                bean.delete()
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

    companion object {
        private val TAG = EmailSendListActivity::class.java.simpleName
    }
}