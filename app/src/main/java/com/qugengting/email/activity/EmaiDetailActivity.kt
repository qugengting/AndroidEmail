package com.qugengting.email.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.qugengting.email.R
import com.qugengting.email.adapter.AttachRecvAdapter
import com.qugengting.email.bean.MailBean
import com.qugengting.email.constants.ConstantsKeys
import com.qugengting.email.constants.MailConstants
import com.qugengting.email.constants.ReplyType
import com.qugengting.email.eventbus.MailStatusEvent
import com.qugengting.email.helper.MailHelper
import com.qugengting.email.utils.*
import com.qugengting.email.utils.systembar.StatusBarUtils
import kotlinx.android.synthetic.main.activity_email_detail.*
import org.greenrobot.eventbus.EventBus
import org.litepal.LitePal

/**
 * Created by xuruibin on 2018/4/17.
 * 描述：邮件详情界面
 */
class EmaiDetailActivity : EmailAttachmentBaseActivity(), View.OnClickListener, MailHelper {

    private lateinit var attachAdapter: AttachRecvAdapter
    private var position = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtils.setDefault(this)
        setContentView(R.layout.activity_email_detail)
        initView()
        uid = intent.getLongExtra("uid", -1)
        position = intent.getIntExtra("position", -1)
        mailBean = LitePal.where("account = ? and uid = ?"
                , MailConstants.MAIL_ACCOUNT, uid.toString()).findFirst(MailBean::class.java)
        content = mailBean.content
        tvSender1.text = mailBean.sender
        tvSender2.text = mailBean.sender
        var sendtime = DateUtils.longToString(mailBean.sendTime, "yyyy-MM-dd==HH:mm")
        val a = sendtime.split("==".toRegex()).toTypedArray()
        val a1 = a[0].split("-".toRegex()).toTypedArray()
        sendtime = (a1[0] + "年" + a1[1] + "月" + a1[2] + "日" + DateUtils.dayForWeek(mailBean.sendTime)
                + " " + a[1])
        tvSendTime.text = sendtime
        tvRecvs.text = mailBean.receiveTo.getMailSimpleNames()
        if (TextUtils.isEmpty(mailBean.receiveCc)) {
            lyCC.visibility = View.GONE
        } else {
            tvCcs.text = mailBean.receiveCc.getMailSimpleNames()
        }
        tvTitle.text = mailBean.title

        if (mailBean.fileFlag == 0) {
            //该邮件没有需要处理的内容（包括附件和正文中的图片等），不用再去获取
            webView.loadData(content, "text/html; charset=UTF-8", null)
        } else {
            initMailWithAttachment()
        }
    }

    override fun showAttachment() {
        if (mailBean.attachFlag == 1) { //有附件，显示附件
            ivAttachMark.visibility = View.VISIBLE
            tvAttachNum.text = "${mailBean.attachList.size}"
            lyAttach.visibility = View.VISIBLE
            attachAdapter = AttachRecvAdapter(this)
            attachAdapter.setOnAttachClickListener(object : AttachRecvAdapter.OnAttachClickListener {
                override fun onClick(position: Int) {
                    openAttachment(attachAdapter.getDataList()[position])
                }
            })
            rvAttach.adapter = attachAdapter
            rvAttach.layoutManager = LinearLayoutManager(this)
            attachAdapter.setDataList(mailBean.attachList)
            tvAttachLabel.text = String.format(getString(R.string.mail_attach_label), mailBean.attachList.size)

            EventBus.getDefault().post(MailStatusEvent())//通知首页附件状态更新
        }
    }

    private fun initView() {
        webView = findViewById(R.id.webview_mail_detail)
        lyDetailDelete.setOnClickListener(this)
        lyDetailBack.setOnClickListener(this)
        tvLookDetail.setOnClickListener(this)
        tvHideDetail.setOnClickListener(this)
        tvReply.setOnClickListener(this)
        tvReplyAll.setOnClickListener(this)
        tvTransmit.setOnClickListener(this)
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    companion object {
        private val TAG = EmaiDetailActivity::class.java.simpleName
    }

    private fun deleteMail() {
        val progressDialog = ProgressDialog(this@EmaiDetailActivity)
        progressDialog.setMessage(getString(R.string.mail_please_wait))
        progressDialog.show()
        //RecyclerView关于notifyItemRemoved的那点小事 参考：http://blog.csdn.net/jdsjlzx/article/details/52131528
        exec({
            deleteMail(mailBean)
        }, {
            progressDialog.dismiss()
            if (it) {
                //通知首页更新数据
                EventBus.getDefault().post(MailStatusEvent())
                finish()
            } else {
                Utils.makeText(this@EmaiDetailActivity, R.string.mail_delete_fail_hint)
            }
        })
        //且如果想让侧滑菜单同时关闭，需要同时调用 ((CstSwipeDelMenu) holder.itemView).quickClose();
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            //返回上一页
            R.id.lyDetailBack -> {
                setResult(Activity.RESULT_OK)
                finish()
            }
            //删除
            R.id.lyDetailDelete -> {
                Utils.showDialog(this, getString(R.string.mail_delete_title), getString(R.string.mail_delete_prompt), getString(R.string.mail_delete_confirm),
                        DialogInterface.OnClickListener { _, _ ->
                            //从首页列表点击进来的，由首页自己去做删除处理
                            if (position != -1) {
                                val i = Intent()
                                i.putExtra("position", position)
                                setResult(ConstantsKeys.DELETE_RESULT_CODE, i)
                                finish()
                            }
                            //从搜索页点进来的
                            else {
                                deleteMail()
                            }
                        }, getString(R.string.mail_cancel), null, 1)
            }
            //查看邮件头信息详情
            R.id.tvLookDetail -> {
                lyBrief.visibility = View.GONE
                lyDetail.visibility = View.VISIBLE
            }
            //隐藏邮件头信息详情
            R.id.tvHideDetail -> {
                lyBrief.visibility = View.VISIBLE
                lyDetail.visibility = View.GONE
            }
            //回复
            R.id.tvReply -> start<EmailReplyActivity> {
                putExtra("uid", uid)
                putExtra(ReplyType.REPLY_TYPE, ReplyType.REPLY_ONLY)
            }
            //回复全部
            R.id.tvReplyAll -> start<EmailReplyActivity> {
                putExtra("uid", uid)
                putExtra(ReplyType.REPLY_TYPE, ReplyType.REPLY_ALL)
            }
            //转发
            R.id.tvTransmit -> start<EmailReplyActivity> {
                putExtra("uid", uid)
                putExtra(ReplyType.REPLY_TYPE, ReplyType.REPLY_TRANSMIT)
            }
        }
    }
}