package com.qugengting.email.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter
import com.github.jdsjlzx.recyclerview.ProgressStyle
import com.qugengting.email.R
import com.qugengting.email.adapter.RecvAdapter
import com.qugengting.email.adapter.RecvAdapter.OnRecvSwipeListener
import com.qugengting.email.bean.MailBean
import com.qugengting.email.bean.MailLoadFlag
import com.qugengting.email.constants.ConstantsKeys
import com.qugengting.email.constants.MailConstants
import com.qugengting.email.eventbus.MailStatusEvent
import com.qugengting.email.helper.MailHelper
import com.qugengting.email.utils.*
import com.qugengting.email.utils.systembar.StatusBarUtils
import com.qugengting.email.widget.PopupDialog
import com.qugengting.email.widget.PopupView
import com.sun.mail.imap.IMAPFolder
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_email_main.*
import kotlinx.android.synthetic.main.include_email_head_side.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jsoup.Jsoup
import org.litepal.LitePal
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.activation.CommandMap
import javax.activation.MailcapCommandMap
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeUtility

/**
 * 云邮首页
 */
class MainActivity : BaseActivity(), View.OnClickListener, MailHelper {

    private lateinit var adapter: RecvAdapter
    private lateinit var mLRecyclerViewAdapter: LRecyclerViewAdapter
    private lateinit var list: MutableList<MailBean>
    private var compositeDisposable: CompositeDisposable? = null
    private lateinit var dialog: PopupDialog
    private lateinit var popupView: PopupView
    private var updateResult = false
    private var lastUpdateTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtils.setDefault(this)
        System.setProperty("mail.mime.splitlongparameters", "false")
        EventBus.getDefault().register(this)
        setContentView(R.layout.activity_email_main)
        tvAccount.text = MailConstants.MAIL_ACCOUNT
        tvMailAccount.text = MailConstants.MAIL_ACCOUNT
        initView()
        initToolBar()
    }

    private fun initView() {
        list = LitePal.where("account = ? and type = ?"
                , MailConstants.MAIL_ACCOUNT, "0").order("sendTime desc").find(MailBean::class.java)
        adapter = RecvAdapter(this)
        adapter.setDataList(list)
        adapter.setOnDelListener(object : OnRecvSwipeListener {
            override fun onDel(position: Int) {
                deleteMail(position)
            }
        })
        mLRecyclerViewAdapter = LRecyclerViewAdapter(adapter)
        rvRecv.adapter = mLRecyclerViewAdapter
        rvRecv.layoutManager = LinearLayoutManager(this)
        rvRecv.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader)
        rvRecv.setArrowImageView(R.drawable.ic_pulltorefresh_arrow)
        rvRecv.setOnRefreshListener { getNewMail() }
        rvRecv.setLoadMoreEnabled(true)
        rvRecv.setOnLoadMoreListener {
            val loadFlagBean = LitePal.where("account = ?", MailConstants.MAIL_ACCOUNT).findFirst(MailLoadFlag::class.java)
            //先判断是否已加载最早的邮件，避免每次请求都要去连接邮箱服务器
            if (loadFlagBean != null && loadFlagBean.loadFlag == "1") {
                rvRecv.setNoMore(true)
            } else {
                getOldMail()
            }
        }
        rvRecv.refresh()
        edtSearch.inputType = InputType.TYPE_NULL //禁止弹出软键盘

        edtSearch.setOnClickListener(this)
        lySideMenu.setOnClickListener(this)
        allRead.setOnClickListener(this)
        tvRecv.setOnClickListener(this)
        tvSend.setOnClickListener(this)
        lyCreateNew.setOnClickListener(this)

        dialog = PopupDialog(this, 0)
    }

    private fun initToolBar() {
        barMain.setTitleLayoutClickListener { showDialog() }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.lySideMenu -> layoutDrawer.openDrawer(GravityCompat.START)
            R.id.allRead -> markAllRead()
            R.id.tvRecv -> layoutDrawer.closeDrawer(GravityCompat.START)
            R.id.tvSend -> start<EmailSendListActivity> {}
            R.id.edtSearch -> start<EmailSearchActivity> {}
            R.id.lyCreateNew -> start<EmailReplyActivity> { putExtra("isCreate", true) }
        }
    }

    private fun deleteMail(position: Int) {
        val progressDialog = ProgressDialog(this@MainActivity)
        progressDialog.setMessage(getString(R.string.mail_please_wait))
        progressDialog.show()
        //RecyclerView关于notifyItemRemoved的那点小事 参考：http://blog.csdn.net/jdsjlzx/article/details/52131528
        //因为要删除对应附件，而附件有可能在邮件详情界面更新，所以需要重新从数据库取最新更新数据
        val bean = LitePal.where("account = ? and uid = ?", MailConstants.MAIL_ACCOUNT
                , adapter.getDataList()[position].uid.toString()).findFirst(MailBean::class.java)
        exec({
            deleteMail(bean)
        }, {
            progressDialog.dismiss()
            if (it) {
                adapter.remove(position)
                updateTitle()
            } else {
                Utils.makeText(this@MainActivity, R.string.mail_delete_fail_hint)
            }
        })
        //且如果想让侧滑菜单同时关闭，需要同时调用 ((CstSwipeDelMenu) holder.itemView).quickClose();
    }

    private fun showDialog() {
        popupView = PopupView(this)
        popupView.setOnClickListener1 { v ->
            when (v.tag as String) {
                PopupView.TAG_I -> {
                    titleSelect = ITEM_UNREAD
                }
                PopupView.TAG_II -> {
                    titleSelect = ITEM_READ
                }
                PopupView.TAG_III -> {
                    titleSelect = ITEM_ALL
                }
            }
            dialog.dismiss()
            showData()
            updateTitle()
        }
        popupView.setTitle(String.format(getString(R.string.mail_recv_num), list.size))
        dialog.setCancelable(true)
        dialog.setContentView(popupView)
        dialog.setOnCancelListener { barMain.setTitleImageDrawableDown(true) }
        val manager = this.windowManager
        val outMetrics = DisplayMetrics()
        manager.defaultDisplay.getMetrics(outMetrics)
        val width = outMetrics.widthPixels
        val window = dialog.window
        val lp = window?.attributes
        window?.setGravity(Gravity.START or Gravity.TOP)
        lp?.x = 0
//        lp.y = barMain.height
        lp?.y = 0
        lp?.width = width
        window?.attributes = lp
        dialog.show()
    }

    private fun showData() {
        when (titleSelect) {
            ITEM_ALL -> {
                list = LitePal.where("account = ? and type = ?", MailConstants.MAIL_ACCOUNT, "0")
                        .order("sendTime desc").find(MailBean::class.java)
            }
            ITEM_UNREAD -> {
                list = LitePal.where("account = ? and type = ? and readFlag = ?", MailConstants.MAIL_ACCOUNT, "0", "0")
                        .order("sendTime desc").find(MailBean::class.java)
            }
            ITEM_READ -> {
                list = LitePal.where("account = ? and type = ? and readFlag = ?", MailConstants.MAIL_ACCOUNT, "0", "1")
                        .order("sendTime desc").find(MailBean::class.java)
            }
            //滚回到顶部
        }
        adapter.setDataList(list)
        adapter.notifyDataSetChanged()
        rvRecv.smoothScrollToPosition(0) //滚回到顶部
    }

    private var titleSelect = 0
    private fun updateTitle() {
        barMain.setTitle(String.format(getString(R.string.mail_recv_num), list.size))
//        when (titleSelect) {
//            ITEM_ALL -> {
//                barMain.setTitle("收件箱(" + list.size + "封)")
//            }
//            ITEM_UNREAD -> {
//                barMain.setTitle("收件箱(未读" + list.size + "封)")
//            }
//            ITEM_READ -> {
//                barMain.setTitle("收件箱(已读" + list.size + "封)")
//            }
//        }
    }//滚回到顶部

    private fun getNewMail() {
        compositeDisposable = CompositeDisposable()
        Observable.create<String> { emitter ->
            val result = getMails()
            list.sort()
            emitter.onNext(result)
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<String> {
                    override fun onSubscribe(d: Disposable) {
                        compositeDisposable?.add(d)
                    }

                    override fun onNext(value: String) {
                        rvRecv.refreshComplete(REQUEST_COUNT)
                        if (value == "ok") {
                            titleSelect = ITEM_ALL
                            showData()
                            updateTitle()
                            lastUpdateTime = System.currentTimeMillis()
                            tvUpdateStatus.text = String.format(getString(R.string.mail_update), DateUtils.getFormatDate(Date(lastUpdateTime)))
                        } else {
                            updateResult = false
                            tvUpdateStatus.text = getString(R.string.mail_update_fail)
                        }
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        rvRecv.refreshComplete(REQUEST_COUNT)
                        adapter.notifyDataSetChanged()
                        rvRecv.smoothScrollToPosition(0) //滚回到顶部
                    }

                    override fun onComplete() {}
                })
    }

    private fun getOldMail() {
        compositeDisposable = CompositeDisposable()
        Observable.create<String> { emitter ->
            val result = getMails(true)
            emitter.onNext(result)
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<String> {
                    override fun onSubscribe(d: Disposable) {
                        compositeDisposable?.add(d)
                    }

                    override fun onNext(value: String) {
                        rvRecv.refreshComplete(REQUEST_COUNT)
                        if (value == "ok") {
                            if (titleSelect != ITEM_ALL) {
                                titleSelect = ITEM_ALL
                                showData()
                            } else {
                                adapter.notifyDataSetChanged()
                            }
                            updateTitle()
                        } else {
                            updateResult = false
                            tvUpdateStatus.text = getString(R.string.mail_loadmore_fail)
                        }
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        rvRecv.refreshComplete(REQUEST_COUNT)
                    }

                    override fun onComplete() {}
                })
    }

    private fun markAllRead() {
        for (bean in list) {
            bean.readFlag = 1
        }
        val mailBean = MailBean()
        mailBean.readFlag = 1
        mailBean.updateAll()
        adapter.notifyDataSetChanged()
    }

    /**
     * 更新为已读
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateStatus(event: MailStatusEvent) {
        if (event.type == MailStatusEvent.TYPE_READ) {
            //从本页进入详情页收到的通知
            if (event.position != -1) {
                val mailbean = adapter.getDataList()[event.position]
                mailbean.readFlag = 1
                adapter.notifyItemChanged(event.position)
            }
            //通过搜索界面进入详情页收到的通知
            else {
                list = LitePal.where("account = ? and type = ?"
                        , MailConstants.MAIL_ACCOUNT, "0").order("sendTime desc").find(MailBean::class.java)
                adapter.setDataList(list)
                updateTitle()
            }
        } else if (event.type == MailStatusEvent.TYPE_DELETE) {
            list = LitePal.where("account = ? and type = ?"
                    , MailConstants.MAIL_ACCOUNT, "0").order("sendTime desc").find(MailBean::class.java)
            adapter.setDataList(list)
            updateTitle()
        }
    }

    override fun onResume() {
        super.onResume()
        if (lastUpdateTime != 0L && updateResult) {
            tvUpdateStatus.text = String.format(getString(R.string.mail_update), DateUtils.getFormatDate(Date(lastUpdateTime)))
        }
    }

    override fun onBackPressed() {
        when {
            layoutDrawer.isDrawerOpen(GravityCompat.START) -> {
                layoutDrawer.closeDrawer(GravityCompat.START)
            }
            dialog.isShowing -> {
                dialog.dismiss()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        compositeDisposable?.clear()
    }

    private fun getMails(isOld: Boolean = false): String {
        val props = Properties()
        props.setProperty("mail.transport.protocol", "imap") // 使用的协议（JavaMail规范要求）
        props.setProperty("mail.imap.host", MailConstants.MAIL_IMAP_HOST) //
        props.setProperty("mail.imap.connectiontimeout", "5000") //设置连接超时时间
        /*  The default IMAP implementation in JavaMail is very slow to download large attachments.
            Reason for this is that, by default, it uses attachmentPath small 16K fetch buffer size.
            You can increase this buffer size using the “mail.imap.fetchsize” system property
            For example:
        */props.setProperty("mail.imap.fetchsize", "1000000")
        //加入以下设置，附件下载速度更是快了10倍
        props.setProperty("mail.imap.partialfetch", "false")

        // 获取连接
        try {
            val mc = CommandMap.getDefaultCommandMap() as MailcapCommandMap
            mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html")
            mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml")
            mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain")
            mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed")
            mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822")
            CommandMap.setDefaultCommandMap(mc)
            val session = Session.getDefaultInstance(props)
            session.debug = false
            // 获取Store对象
            val store = session.getStore("imap")
            store.connect(MailConstants.MAIL_IMAP_HOST, MailConstants.MAIL_ACCOUNT, MailConstants.MAIL_PWD)

            // 通过POP3协议获得Store对象调用这个方法时，邮件夹名称只能指定为"INBOX"
            val folder = store.getFolder("INBOX") // 获得用户的邮件帐户
            folder.open(Folder.READ_WRITE) // 设置对邮件帐户的访问权限
            var index = folder.messageCount
            val inbox = folder as IMAPFolder
            var number = 0
            //每次最多拉取20条
            while (index >= 1 && number < 20) {
                //拉取到最早一条时，本地数据库进行标记，可以避免重复连接邮箱服务器进行不必要的加载更多
                if (index == 1) {
                    var loadFlag = LitePal.where("account = ?", MailConstants.MAIL_ACCOUNT)
                            .findFirst(MailLoadFlag::class.java)
                    if (loadFlag == null) {
                        loadFlag = MailLoadFlag(MailConstants.MAIL_ACCOUNT, "1")
                        loadFlag.save()
                    }
                }
                val message = folder.getMessage(index)
                Log.e(TAG, "=====================>> $index <<=====================")
                val uid = inbox.getUID(message)
                var mailBean = LitePal.where("account = ? and uid = ?"
                        , MailConstants.MAIL_ACCOUNT, uid.toString()).findFirst(MailBean::class.java)
                if (mailBean != null) {
                    //邮件已存在，如果是刷新最新的邮件，直接跳出，如果是获取更早的邮件，则进行下一个判断
                    if (isOld) {
                        index--
                        continue
                    } else {
                        break
                    }
                }
                mailBean = MailBean()
                val isRead = isRead(message)
                var isContainAttach = isContainAttach(message)
                mailBean.fileFlag = if (isContainAttach) 1 else 0
                mStringBufferContent = StringBuffer()
                try {
                    getMailContent(message, index) //获取邮件内容
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e(TAG, "=====================>> " + "解析错误！！！" + " <<=====================")
                    index--
                    number++
                    continue
                }
                //判断邮件正文有几处img，img的src会以附件形式发送，这里用img数量比较附件数量就可以获取真正的附件数量
                val doc = Jsoup.parse(mStringBufferContent.toString())
                val pngs = doc.select("img")
                if (pngs != null) {
                    isContainAttach = hookNum > pngs.size
                }
                hookNum = 0
                val headerInfo = getHeaderInfo(message)
                var date = message.sentDate
                if (date == null) {
                    date = message.receivedDate
                    if (date == null) {
                        date = Date()
                    }
                }
                Log.e(TAG, "=====================>>开始显示邮件内容<<=====================")
                Log.e(TAG, "发送人: ${headerInfo[0]}<${headerInfo[1]}>")
                Log.e(TAG, "收件人：${headerInfo[2]}")
                Log.e(TAG, "抄送：${headerInfo[3]}")
                Log.e(TAG, "主题: ${getSubject(message)}")
                Log.e(TAG, "内容: $mStringBufferContent")
                Log.e(TAG, "发送时间: " +
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date))
                Log.e(TAG, "是否有附件: " + if (isContainAttach) "有附件" else "无附件")
                //                Log.e(TAG, "附件名称：" + (isContainAttach ? mAttachmentName : "无"));
                Log.e(TAG, "=====================>>结束显示邮件内容<<=====================")
                val regex = "<(img|IMG)(.*?)(/>|></img>|>)"
                var previewContent = mStringBufferContent.toString().replace(regex.toRegex(), getString(R.string.mail_img))
                val regex2 = "<style>(.*?)(/>|</style>)"
                previewContent = previewContent.replace(regex2.toRegex(), "")
                previewContent = previewContent.filterHtml(100)
                val uuid = System.currentTimeMillis()
                mailBean.uuid = uuid.toString()
                mailBean.account = MailConstants.MAIL_ACCOUNT
                mailBean.type = 0
                mailBean.uid = uid
                mailBean.sender = headerInfo[0]
                mailBean.senderAddress = headerInfo[1]
                mailBean.receiveTo = headerInfo[2]
                mailBean.receiveCc = headerInfo[3]
                mailBean.title = getSubject(message)
                mailBean.sendTime = date.time
                mailBean.content = mStringBufferContent.toString()
                mailBean.previewContent = previewContent
                mailBean.attachFlag = if (isContainAttach) 1 else 0
                mailBean.readFlag = if (isRead) 1 else 0
                mailBean.downloadFlag = 0
                mailBean.save()
                list.add(mailBean)
                index--
                number++
            }
            folder.close(false) // 关闭邮件夹对象
            store.close() // 关闭连接对象
        } catch (e: Exception) {
            Log.e("receiveEmail", e.message)
            e.printStackTrace()
            return "error"
        }
        return "ok"
    }

    @Throws(MessagingException::class)
    private fun isRead(message: Message): Boolean {
        val flags = message.flags.systemFlags
        for (f in flags) {
            if (f == Flags.Flag.SEEN) return true
        }
        return false
    }

    private lateinit var mStringBufferContent: StringBuffer //存放邮件内容

    /**
     * 获取邮件内容
     *
     * @param part：Part
     */
    @Throws(Exception::class)
    private fun getMailContent(part: Part, index: Int) {
        //判断邮件类型,不同类型操作不同
        if (part.isMimeType("text/plain")) {
            //全部按html处理，注释掉此处
//            mStringBufferContent.append((String) part.getContent());
        } else if (part.isMimeType("text/html")) {
            val content: String
            content = try {
                part.content as String
            } catch (e: Exception) {
                //java.io.UnsupportedEncodingException: gb2312rn
                //火星文等需要处理
                val `is` = part.inputStream
                inputStream2String(`is`)
            }
            mStringBufferContent.append(content)
        } else if (part.isMimeType("multipart/*")) {
            val multipart = part.content as Multipart
            val counts = multipart.count
            for (i in 0 until counts) {
                getMailContent(multipart.getBodyPart(i), index)
            }
        } else if (part.isMimeType("message/rfc822")) {
            getMailContent(part.content as Part, index)
        } else {
        }
    }

    private var hookNum = 0

    /**
     * 判断此邮件是否包含附件
     *
     * @param part：Part
     * @return 是否包含附件
     */
    @Throws(Exception::class)
    private fun isContainAttach(part: Part): Boolean {
        var attachflag = false
        if (part.isMimeType("multipart/*")) {
            val mp = part.content as Multipart
            for (i in 0 until mp.count) {
                val mpart = mp.getBodyPart(i)
                val disposition = mpart.disposition
                if (disposition != null && (disposition == Part.ATTACHMENT || disposition == Part.INLINE)) {
                    attachflag = true
                    hookNum += 1
                } else if (mpart.isMimeType("multipart/*")) {
                    attachflag = isContainAttach(mpart as Part)
                } else {
                    val contype = mpart.contentType
                    val lowerContype = contype.toLowerCase(Locale.ROOT)
                    if (lowerContype.contains("application") && lowerContype.contains("name")) {
                        attachflag = true
                        hookNum += 1
                    } else if (lowerContype.contains("image/jpeg")) {
                        attachflag = true
                        hookNum += 1
                    }
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            attachflag = isContainAttach(part.content as Part)
        }
        return attachflag
    }

    /**
     * 获得发件人和收件人等信息
     *
     * @param message：Message
     */
    @Throws(Exception::class)
    private fun getHeaderInfo(message: Message): Array<String?> {
        val address = message.from as Array<InternetAddress>
        var from = address[0].address
        if (from == null) {
            from = ""
        }
        var personal = address[0].personal
        if (personal == null) personal = ""
        val receiveTo = StringBuilder()
        val receTo = message.getRecipients(Message.RecipientType.TO) as Array<InternetAddress>
        if (receTo.isNotEmpty()) {
            for (address1 in receTo) {
                receiveTo.append(if (TextUtils.isEmpty(address1.personal)) "unknown" else address1.personal + "=" + address1.address + "#")
            }
        }
        val receiveCc = StringBuilder()
        var recvCc = message.getRecipients(Message.RecipientType.CC)
        if (recvCc != null && recvCc.isNotEmpty()) {
            recvCc = recvCc as Array<InternetAddress>
            for (address1 in recvCc) {
                receiveCc.append(if (TextUtils.isEmpty(address1.personal)) "unknown" else address1.personal + "=" + address1.address + "#")
            }
        }
        val info = arrayOfNulls<String>(4)
        info[0] = personal
        info[1] = from
        info[2] = receiveTo.deleteCharAt(receiveTo.length - 1).toString()
        info[3] = if (receiveCc.isBlank()) "" else receiveCc.deleteCharAt(receiveCc.length - 1).toString()
        return info
    }

    /**
     * 获得邮件主题
     *
     * @param message：Message
     * @return 邮件主题
     */
    @Throws(Exception::class)
    private fun getSubject(message: Message): String {
        var subject = ""
        if (message.subject != null) {
            subject = MimeUtility.decodeText(message.subject) // 将邮件主题解码
        }
        return subject
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            adapter.notifyDataSetChanged()
        } else if (resultCode == ConstantsKeys.DELETE_RESULT_CODE) {
            val pos = data!!.getIntExtra("position", -1)
            deleteMail(pos)
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName

        /**
         * 每一页展示多少条数据，注意，这里不要随意乱改，会影响到加载更多的UI效果。。。
         * 好在这个数据不需要怎么改
         */
        private const val REQUEST_COUNT = 10
        private const val ITEM_ALL = 100
        private const val ITEM_UNREAD = 101
        private const val ITEM_READ = 102

        @Throws(Exception::class)
        fun inputStream2String(`in`: InputStream): String {
            val out = StringBuilder()
            val b = ByteArray(4096)
            var n: Int
            while (`in`.read(b).also { n = it } != -1) {
                out.append(String(b, 0, n))
            }
            return out.toString()
        }
    }
}