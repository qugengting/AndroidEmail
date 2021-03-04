package com.qugengting.email.activity

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.permissionx.guolindev.PermissionX
import com.qugengting.email.R
import com.qugengting.email.adapter.AttachSendAdapter
import com.qugengting.email.bean.AttachBean
import com.qugengting.email.bean.MailBean
import com.qugengting.email.constants.MailConstants
import com.qugengting.email.constants.ReplyType
import com.qugengting.email.utils.*
import com.qugengting.email.utils.Utils.*
import com.qugengting.email.utils.systembar.StatusBarUtils
import com.qugengting.email.widget.flowlayout.FlowLayout
import com.qugengting.email.widget.flowlayout.TagAdapter
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_email_reply.*
import org.jsoup.Jsoup
import org.litepal.LitePal
import java.io.*
import java.util.*
import javax.activation.*
import javax.mail.*
import javax.mail.internet.*

/**
 * Created by xuruibin on 2018/4/23.
 * 描述：云邮回复界面
 */

const val PICK_FILE = 1
const val PICK_IMAGE = 2

class EmailReplyActivity : EmailAttachmentBaseActivity(), View.OnClickListener {
    private var type: String = ""
    private lateinit var mInflater: LayoutInflater
    private lateinit var attachAdapter: AttachSendAdapter
    private var mContent: String = ""
    private var isCreate = false

    /**
     * TODO 注意，后续通过外部添加的收件人（抄送也是）应该把名称和地址加入到该集合当中
     * 键为收件人名称，值为收件人地址
     */
    private val recvMap: MutableMap<String, String> = mutableMapOf()
    private val ccMap: MutableMap<String, String> = mutableMapOf()

    /**
     * 新添加的正文图片
     */
    private val newImgList = arrayListOf<String>()

    /**
     * 发送邮件的uid，用于标记该邮件的文件夹（放置备份的附件）
     * 因为附件名称有可能重名，必须分开放
     */
    private val sendUid = System.currentTimeMillis()

    /**
     * 要发送的邮件存放附件的根路径
     */
    private var folderPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtils.setDefault(this)
        setContentView(R.layout.activity_email_reply)
        mInflater = LayoutInflater.from(this)
        initView()
        folderPath = "${getExternalFilesDir("Mail")}/${MailConstants.MAIL_NAME}/${sendUid}_F/"
        isCreate = intent.getBooleanExtra("isCreate", false)
        if (!isCreate) {
            initMail()
        }
    }

    private fun initView() {
        webView = findViewById(R.id.webView_reply)
        lyWriteBack.setOnClickListener(this)
        tvMailSend.setOnClickListener(this)
        addReceiver.setOnClickListener(this)
        addReceiveCC.setOnClickListener(this)
        addAttach.setOnClickListener(this)
        insertImg.setOnClickListener(this)
        flReceiver.setAttachLabel(false)
        flReceiver.setAdapter(recvAdapter)
        flCC.setAttachLabel(false)
        flCC.setAdapter(ccAdapter)
        attachAdapter = AttachSendAdapter(this)
        attachAdapter.setOnDelListener(object : AttachSendAdapter.OnAttachSwipeListener {
            override fun onRemove(position: Int) {
                val file = File(attachAdapter.getDataList()[position].path)
                file.delete()
                attachAdapter.remove(position)
            }

        })
        rvAttach.layoutManager = LinearLayoutManager(this)
        rvAttach.adapter = attachAdapter

        initEdit()
    }

    private fun initEdit() {
        editor.setEditorHeight(80)
        editor.setEditorFontSize(15)
        editor.setEditorFontColor(Color.BLACK)
        //mEditor.setEditorBackgroundColor(Color.BLUE);
        //mEditor.setBackgroundColor(Color.BLUE);
        //mEditor.setBackgroundResource(R.drawable.bg);
        //mEditor.setEditorBackgroundColor(Color.BLUE);
        //mEditor.setBackgroundColor(Color.BLUE);
        //mEditor.setBackgroundResource(R.drawable.bg);
        editor.setPadding(0, 0, 0, 0)
        //mEditor.setBackground("https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg");
        //mEditor.setBackground("https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg");
        editor.setPlaceholder(getString(R.string.mail_write_content_hint))
        //mEditor.setInputEnabled(false);
        editor.setOnTextChangeListener { text ->
            //<img src="cid:/storage/emulated/0/Pictures/1593659038445.jpg" alt=" 1612159652694" width="320"><br>哦哦哦
            Log.e(TAG, "web : $text")
            mContent = text
        }

        action_undo.setOnClickListener(this)
        action_redo.setOnClickListener(this)
        action_insert_image.setOnClickListener(this)
        action_bold.setOnClickListener(this)
        action_italic.setOnClickListener(this)
        action_underline.setOnClickListener(this)
        action_heading1.setOnClickListener(this)
        action_heading2.setOnClickListener(this)
        action_heading3.setOnClickListener(this)
        action_heading4.setOnClickListener(this)
        action_heading5.setOnClickListener(this)
        action_heading6.setOnClickListener(this)
        action_indent.setOnClickListener(this)
        action_outdent.setOnClickListener(this)
        action_align_left.setOnClickListener(this)
        action_align_center.setOnClickListener(this)
        action_align_right.setOnClickListener(this)
        action_blockquote.setOnClickListener(this)
        action_insert_bullets.setOnClickListener(this)
        action_insert_numbers.setOnClickListener(this)
    }

    private val recvAdapter: TagAdapter = object : TagAdapter(arrayOf()) {
        override fun getView(parent: FlowLayout, position: Int, s: String): View {
            val tv = mInflater.inflate(R.layout.tv_item, parent, false) as TextView
            tv.text = s
            return tv
        }

        override fun getLabelView(parent: FlowLayout): View {
            //如果设置flowLayout.setAttachLabel(false);该标签将不显示
            return mInflater.inflate(R.layout.tv_label, parent, false) as TextView
        }

        override fun getInputView(parent: FlowLayout): View {
            return mInflater.inflate(R.layout.edt, parent, false)
        }
    }
    private val ccAdapter: TagAdapter = object : TagAdapter(arrayOf()) {
        override fun getView(parent: FlowLayout, position: Int, s: String): View {
            val tv = mInflater.inflate(R.layout.tv_item, parent, false) as TextView
            tv.text = s
            return tv
        }

        override fun getLabelView(parent: FlowLayout): View {
            //如果设置flowLayout.setAttachLabel(false);该标签将不显示
            return mInflater.inflate(R.layout.tv_label, parent, false) as TextView
        }

        override fun getInputView(parent: FlowLayout): View {
            return mInflater.inflate(R.layout.edt, parent, false)
        }
    }

    override fun showAttachment() {}
    private fun initMail() {
        val intent = intent
        uid = intent.getLongExtra("uid", 0)
        type = intent.getStringExtra(ReplyType.REPLY_TYPE)
        mailBean = LitePal.where("account = ? and uid = ?"
                , MailConstants.MAIL_ACCOUNT, uid.toString()).findFirst(MailBean::class.java)
        content = mailBean.content

        lyOriMailLabel.visibility = View.VISIBLE
        webView.visibility = View.VISIBLE
        lyOriMailHead.visibility = View.VISIBLE
        tvOriMailSender.text = mailBean.sender
        tvOriMailRecvs.text = mailBean.receiveTo.getMailSimpleNames()
        if (!mailBean.receiveCc.isNullOrBlank()) {
            lyOriMailCC.visibility = View.VISIBLE
            tvOriMailCC.text = mailBean.receiveCc.getMailSimpleNames()
        }
        tvOriMailDate.text = DateUtils.longToString(mailBean.sendTime, "yyyy-MM-dd HH:mm:ss")
        tvOriMailTitle.text = mailBean.title

        when (type) {
            //转发
            ReplyType.REPLY_TRANSMIT -> {
                tvWriteTitle.text = getString(R.string.mail_transmit)
                edtTitle.setText(String.format(getString(R.string.mail_transmit_title), mailBean.title))

                //转发要把原邮件附件带上
                val attachList = mailBean.attachList
                for (item in attachList) {
                    copyFile(item.path, item.name)
                }
            }
            //回复
            ReplyType.REPLY_ONLY -> {
                tvWriteTitle.text = getString(R.string.mail_reply)
                edtTitle.setText(String.format(getString(R.string.mail_reply_title), mailBean.title))
                if (mailBean.type == 0) {
                    //收件人就是原邮件的发件人
                    flReceiver.addItem(mailBean.sender)
                    recvMap[mailBean.sender] = mailBean.senderAddress
                } else if (mailBean.type == 1) {
                    //收件人还是原来的收件人
                    val allRecv = mailBean.receiveTo
                    val recvs = allRecv.split("#")
                    for (item in recvs) {
                        val names = item.split("=")
                        recvMap[names[0]] = names[1]
                        flReceiver.addItem(names[0])
                    }
                }

            }
            //回复全部
            ReplyType.REPLY_ALL -> {
                tvWriteTitle.text = getString(R.string.mail_reply_all)
                edtTitle.setText(String.format(getString(R.string.mail_reply_title), mailBean.title))

                //填充收件人列表
                var allRecv = mailBean.receiveTo
                if (mailBean.type == 0) {
                    //收件人加上原邮件的发件人
                    flReceiver.addItem(mailBean.sender)
                    recvMap[mailBean.sender] = mailBean.senderAddress
                    //把自己移除
                    allRecv = allRecv.replace(MailConstants.MAIL_NAME + "=" + MailConstants.MAIL_ACCOUNT + "#", "")
                    //自己有可能在最后一位，没有"#"分隔符
                    allRecv = allRecv.replace(MailConstants.MAIL_NAME + "=" + MailConstants.MAIL_ACCOUNT, "")
                }
                if (allRecv.isNotBlank()) {
                    val otherRecvs = allRecv.split("#")
                    for (item in otherRecvs) {
                        val names = item.split("=")
                        recvMap[names[0]] = names[1]
                        flReceiver.addItem(names[0])
                    }
                }

                //填充抄送列表
                var allCC = mailBean.receiveCc
                //把自己移除
                allCC = allCC.replace(MailConstants.MAIL_NAME + "=" + MailConstants.MAIL_ACCOUNT + "#", "")
                //有可能自己在最后一位，没有"#"分隔符
                allCC = allCC.replace(MailConstants.MAIL_NAME + "=" + MailConstants.MAIL_ACCOUNT, "")
                if (allCC.isNotBlank()) {
                    val otherCCs = allCC.split("#")
                    for (item in otherCCs) {
                        val names = item.split("=")
                        ccMap[names[0]] = names[1]
                        flCC.addItem(names[0])
                    }
                }
            }
        }
        initMailWithAttachment()
    }

    private fun removeAttachment() {
        //把没发送出去的附件删除掉
        val parentAttachFile = File(folderPath)
        parentAttachFile.deleteAll()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.lyWriteBack -> {
                hideInput()
                removeAttachment()
                finish()
            }
            R.id.tvMailSend -> sendMail()
            R.id.addAttach -> {
                pick(PICK_FILE)
            }
            R.id.insertImg -> pick(PICK_IMAGE)
            R.id.action_undo -> editor.undo()
            R.id.action_redo -> editor.redo()
            R.id.action_insert_image -> pick(PICK_IMAGE)
            R.id.action_bold -> editor.setBold()
            R.id.action_italic -> editor.setItalic()
            R.id.action_underline -> editor.setUnderline()
            R.id.action_heading1 -> editor.setHeading(1)
            R.id.action_heading2 -> editor.setHeading(2)
            R.id.action_heading3 -> editor.setHeading(3)
            R.id.action_heading4 -> editor.setHeading(4)
            R.id.action_heading5 -> editor.setHeading(5)
            R.id.action_heading6 -> editor.setHeading(6)
            R.id.action_indent -> editor.setIndent()
            R.id.action_outdent -> editor.setOutdent()
            R.id.action_align_left -> editor.setAlignLeft()
            R.id.action_align_center -> editor.setAlignCenter()
            R.id.action_align_right -> editor.setAlignRight()
            R.id.action_blockquote -> editor.setBlockquote()
        }
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == 0) {
//            for (result in grantResults) {
//                if (result != PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "You must allow all the permissions.", Toast.LENGTH_SHORT).show()
//                    finish()
//                }
//            }
//            pickFile()
//        }
//    }

    private fun pick(code: Int) {
        PermissionX.init(this)
                .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onExplainRequestReason { deniedList ->
                    showRequestReasonDialog(deniedList, "即将重新申请的权限是程序必须依赖的权限", "我已明白", "取消")
                }
                .onForwardToSettings { deniedList ->
                    showForwardToSettingsDialog(deniedList, "您需要去应用程序设置当中手动开启权限", "我已明白", "取消")
                }
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        if (code == PICK_FILE) {
                            pickFile()
                        } else {
                            pickImage()
                        }
                    } else {
                        Toast.makeText(this, "您拒绝了如下权限：$deniedList", Toast.LENGTH_SHORT).show()
                    }
                }

    }

    private fun pickFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, PICK_FILE)
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/jpeg"
        }
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_FILE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val uri = data.data
                    if (uri != null) {
                        val fileName = getFileNameByUri(uri)
                        copyUriToExternalFilesDir(uri, fileName)
                    }
                }
            }
            PICK_IMAGE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val uri = data.data
                    if (uri != null) {
                        val path = UriUtils.getFileAbsolutePath(this, uri)
                        // path example : storage/emulated/0/Pictures/1593659038445.jpg
//                        val index = path.lastIndexOf('/')
//                        val name = path.substring(index + 1)
                        editor.insertImage("cid:$path")
                        newImgList.add(path)
                    }
                }
            }
        }
    }

    private fun getFileNameByUri(uri: Uri): String {
        var fileName = System.currentTimeMillis().toString()
        val cursor = contentResolver.query(uri, null, null, null, null)
        if (cursor != null && cursor.count > 0) {
            cursor.moveToFirst()
            fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
            cursor.close()
        }
        return fileName
    }

    private fun copyUriToExternalFilesDir(uri: Uri, fileName: String) {
        var size: Long = 0
        exec({
            val inputStream = contentResolver.openInputStream(uri)
            val parent = File(folderPath)
            if (!parent.exists()) {
                parent.mkdirs()
            }
            if (inputStream != null) {
                val file = File("$folderPath/$fileName")
                val fos = FileOutputStream(file)
                val bis = BufferedInputStream(inputStream)
                val bos = BufferedOutputStream(fos)
                val byteArray = ByteArray(1024)
                var bytes = bis.read(byteArray)
                while (bytes > 0) {
                    bos.write(byteArray, 0, bytes)
                    size += bytes
                    bos.flush()
                    bytes = bis.read(byteArray)
                }
                bos.close()
                fos.close()
            }
            fileName
        }, {
            val path = "$folderPath$fileName"
            val bean = AttachBean()
            bean.name = fileName
            bean.path = path
            bean.size = FileUtils.getFileSize(size)
            attachAdapter.add(bean)
        })
    }

    /**
     * 将原邮件的附件复制到新邮件的附件目录当中
     */
    private fun copyFile(oriPath: String, fileName: String) {
        var size: Long = 0
        exec({
            val oriFile = File(oriPath)
            if (!oriFile.exists()) {
                //如果原邮件附件不存在且属于收件箱，需要重新下载
                if (mailBean.type == 0) {
                    val b = getAttachment(fileName)
                    if (b) {
                        size = startCopyFile(oriFile, fileName)
                        true
                    } else {
                        makeText(this@EmailReplyActivity, String.format(getString(R.string.mail_this_attachment_download_fail), fileName))
                        false
                    }
                } else {
                    false
                }
            } else {
                size = startCopyFile(oriFile, fileName)
                true
            }
        }, {
            if (it) {
                val path = "$folderPath$fileName"
                val bean = AttachBean()
                bean.name = fileName
                bean.path = path
                bean.size = FileUtils.getFileSize(size)
                attachAdapter.add(bean)
            }
        })
    }

    private fun startCopyFile(oriFile: File, fileName: String): Long {
        var size = 0L
        val inputStream = FileInputStream(oriFile)
        val parent = File(folderPath)
        if (!parent.exists()) {
            parent.mkdirs()
        }
        val file = File("$folderPath/$fileName")
        if (file.exists()) {
            file.delete()
        }
        val fos = FileOutputStream(file)
        val bis = BufferedInputStream(inputStream)
        val bos = BufferedOutputStream(fos)
        val byteArray = ByteArray(1024)
        var bytes = bis.read(byteArray)
        while (bytes > 0) {
            bos.write(byteArray, 0, bytes)
            size += bytes
            bos.flush()
            bytes = bis.read(byteArray)
        }
        bos.close()
        fos.close()
        return size
    }

    /**
     * 发送邮件
     */
    private fun sendMail() {
        hideInput()
        //标题或内容不能为空
        if (edtTitle.editableText.toString().isBlank() || mContent.isBlank()) {
            makeText(this, getString(R.string.mail_not_null_warn))
            return
        }
        //收件人不能为空
        if (recvAdapter.datas.isEmpty()) {
            makeText(this, getString(R.string.mail_no_recv_warn))
            return
        }
        dialog = ProgressDialog(this)
        dialog.setMessage(getString(R.string.mail_sending_wait))
        dialog.show()
        Observable.create(ObservableOnSubscribe { emitter: ObservableEmitter<String?> ->
            val result = sendHtmlMail()
            emitter.onNext(result)
            emitter.onComplete()
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<String?> {
                    override fun onSubscribe(d: Disposable) {
                        compositeDisposable.add(d)
                    }

                    override fun onNext(value: String?) {
                        if (value.equals(OK)) {
                            makeText(this@EmailReplyActivity, getString(R.string.mail_send_success_hint))
                            finish()
                        } else if (value.equals(ERR)) {
                            makeText(this@EmailReplyActivity, getString(R.string.mail_send_fail_hint))
                        }
                    }

                    override fun onError(e: Throwable) {
                        dialog.dismiss()
                        e.printStackTrace()
                    }

                    override fun onComplete() {
                        dialog.dismiss()
                    }
                })
    }

    private fun sendHtmlMail(): String {
        val props = Properties()
        // 开启debug调试
//        props.setProperty("mail.debug", "true");
        // 设置邮件服务器主机名
        props["mail.smtp.host"] = MailConstants.MAIL_SMTP_HOST
        // 发送邮件协议名称
        props.setProperty("mail.transport.protocol", "smtp")
        // 设置邮件服务器端口号
//        props.setProperty("mail.smtp.port", MailConstants.MAIL_SMTP_PORT);
        try {
            val session = Session.getInstance(props)
            val msg: Message = MimeMessage(session)
            //设置主体
            msg.subject = edtTitle.editableText.toString()
            //设置收件人列表
            val receiverAddress: MutableList<InternetAddress> = ArrayList()
            for (name in recvAdapter.datas) {
                //手动输入的收件人地址，没有包含收件人名称，自动将地址赋值给名称
                val address = if (recvMap[name].isNullOrEmpty()) {
                    name
                } else {
                    recvMap[name]!!
                }
                receiverAddress.add(InternetAddress(address, name))
            }
            val receivers = receiverAddress.toTypedArray()
            msg.setRecipients(Message.RecipientType.TO, receivers)
            //设置抄送列表
            val ccAddress: MutableList<InternetAddress> = ArrayList()
            for (name in ccAdapter.datas) {
                val address = if (ccMap[name].isNullOrEmpty()) {
                    name
                } else {
                    recvMap[name]!!
                }
                ccAddress.add(InternetAddress(address, name))
            }
            val ccs = ccAddress.toTypedArray()
            msg.setRecipients(Message.RecipientType.CC, ccs)
            msg.setFrom(InternetAddress(MailConstants.MAIL_ACCOUNT, MailConstants.MAIL_NAME))

            var sendContent = getContent()
            val mainPart: Multipart = MimeMultipart()

            var hasSomePart = imgList.isNotEmpty()

            val map = mutableMapOf<String, String>()
            if (newImgList.isNotEmpty()) {
                for (path in newImgList) {
                    if (sendContent.contains(path)) {
                        hasSomePart = true
                        //类似：_qugting@6b4c25——警告，不要带中文！！！不要带中文
                        val cid = "_qugting@${UUID.randomUUID().toString().substring(0, 6)}.png"
                        sendContent = sendContent.replace(path, cid)
                        map[path] = cid
                        //需要把图片备份到默认文件夹里
                        exec({
                            FileUtils.copyFile(path, contentImgPath + cid, false)
                        }, {
                            Log.e(TAG, "图片备份到：$contentImgPath$cid, 结果：$it")
                        })
                    }
                }
            }
            val textBody = MimeBodyPart().apply {
                //width="320"只是为了在手机上能显示出来，添加是在rich_editor.js实现的，发送时不应该带出去
                //但是发件箱进入邮件详情时又得重新排版，所以不改了，默认就有width="320"了
                sendContent = sendContent.replace("width=\"320\"", "")
                setContent(sendContent, "text/html; charset=utf-8")
            }
            //设置正文
            var partIndex = 0
            mainPart.addBodyPart(textBody, partIndex++)
            //设置已有的正文图片（回复的，转发的等）
            for (fileName in imgList) {
                val f = contentImgPath + fileName
                // 创建一新的MimeBodyPart
                val mdp = MimeBodyPart()
                //得到文件数据源
                val fds = FileDataSource(f)
                //得到附件本身并值入BodyPart
                mdp.dataHandler = DataHandler(fds)
                //得到文件名同样值入BodyPart
                val tempFileName = MimeUtility.encodeText(fileName)
                mdp.fileName = tempFileName
                mdp.contentID = tempFileName
                mainPart.addBodyPart(mdp, partIndex++)
            }
            //设置新添加的正文图片
            for (filePathName in map.keys) {
                val mdp = MimeBodyPart()
                val fds = FileDataSource(filePathName)
                mdp.dataHandler = DataHandler(fds)
                val tempFileName = MimeUtility.encodeText(map[filePathName])
                mdp.fileName = tempFileName
                mdp.contentID = tempFileName
                //Content-Disposition: attachment; filename="_qugting@8d11ff"
                mainPart.addBodyPart(mdp, partIndex++)
            }
            //设置附件
            hasSomePart = hasSomePart || attachAdapter.getDataList().isNotEmpty()
            for (bean in attachAdapter.getDataList()) {
                val name = MimeUtility.encodeText(bean.name)
                //附件
                val attach = MimeBodyPart()
                //把文件，添加到附件中
                //数据源
                val ds: DataSource = FileDataSource(File(bean.path))
                //数据处理器
                //数据处理器
                val dh = DataHandler(ds)
                //设置附件的数据
                attach.dataHandler = dh
                //设置附件的文件名
                attach.fileName = name
                attach.disposition = "attachment"
                //把附件加入到 MIME消息体中
                mainPart.addBodyPart(attach, partIndex++)
            }

            msg.setContent(mainPart)
            val mc = CommandMap.getDefaultCommandMap() as MailcapCommandMap
            mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html")
            mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml")
            mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain")
            mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed")
            mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822")
            CommandMap.setDefaultCommandMap(mc)
            val transport = session.transport
            //            transport.connect("smtp.qq.com", "**发送人的邮箱地址**", "**你的邮箱密码或者授权码**");
            transport.connect(MailConstants.MAIL_SMTP_HOST, MailConstants.MAIL_ACCOUNT, MailConstants.MAIL_PWD)
            //            transport.sendMessage(msg, new Address[]{new InternetAddress("**接收人的邮箱地址**")});
            receiverAddress.addAll(ccAddress)
            val allReceivers = receiverAddress.toTypedArray()
            transport.sendMessage(msg, allReceivers)
            transport.close()

            //保存发件信息
            MailBean().apply {
                account = MailConstants.MAIL_ACCOUNT
                type = 1
                sender = MailConstants.MAIL_NAME
                senderAddress = MailConstants.MAIL_ACCOUNT
                title = edtTitle.editableText.toString()
                //收件人
                var sb = StringBuilder()
                for (name in recvAdapter.datas) {
                    //手动输入的收件人地址，没有包含收件人名称，自动将地址赋值给名称
                    val address = if (recvMap[name].isNullOrEmpty()) {
                        name
                    } else {
                        recvMap[name]!!
                    }
                    sb.append(name)
                    sb.append("=")
                    sb.append(address)
                    sb.append("#")
                }
                receiveTo = sb.deleteCharAt(sb.length - 1).toString()
                //抄送
                sb = StringBuilder()
                for (name in ccAdapter.datas) {
                    val address = if (ccMap[name].isNullOrEmpty()) {
                        name
                    } else {
                        recvMap[name]!!
                    }
                    sb.append(name)
                    sb.append("=")
                    sb.append(address)
                    sb.append("#")
                }
                receiveCc = if (sb.isNotBlank()) sb.deleteCharAt(sb.length - 1).toString() else ""
                sendTime = System.currentTimeMillis()
                content = sendContent
                val regex = "<(img|IMG)(.*?)(/>|></img>|>)"
                var preview = sendContent.replace(regex.toRegex(), getString(R.string.mail_img))
                val regex2 = "<style>(.*?)(/>|</style>)"
                preview = preview.replace(regex2.toRegex(), "").replace("\n", "")
                preview = preview.filterHtml(100)
                previewContent = preview
                uuid = sendUid.toString()
                if (attachAdapter.getDataList().isNotEmpty()) {
                    attachFlag = 1
                    for (bean in attachAdapter.getDataList()) {
                        bean.uuid = uuid
                        bean.save()
                    }
                } else {
                    attachFlag = 0
                }
                uid = sendUid
                fileFlag = if (hasSomePart) 1 else 0
                downloadFlag = 1
                save()
            }
        } catch (e: MessagingException) {
            e.printStackTrace()
            return ERR
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            return ERR
        }
        return OK
    }

    private fun getContent(): String {
        val builder = StringBuilder()
        if (isCreate) {
            return builder
                    .append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=GB18030\">")
                    .append("</head><body><div>")
                    .append(mContent)
                    .append("<div/><br>")
                    .append("<div style=\"font-size:14px;color:#878D94;margin-top:48px;\"><span>")
                    .append(getString(R.string.mail_editor_card))
                    .append("</span><br></div>")
                    .append("<br></body></html>")
                    .toString()
        } else {
            builder.append(mContent)
                    .append("<br><div style=\"font-size:14px;color:#878D94;margin-top:48px;\"><span>")
                    .append(getString(R.string.mail_editor_card))
                    .append("</span><br></div>")
                    .append("<div style=\"font-size:10px;color:#878D94;margin-top:12px;\"><span>")
                    .append(getString(R.string.mail_ori_label))
                    .append(getString(R.string.mail_ori))
                    .append(getString(R.string.mail_ori_label))
                    .append("</span></div>")
            //添加旧邮件的头信息
            builder.append("<div style=\"background-color:#F4F4F4;font-size:12px;color:#878D94;padding:12px;border-radius: 8px; margin-top:4px;margin-bottom:16px\"><span>发件人： ")
                    .append(mailBean.sender)
                    .append("</span><br>")
                    .append("<span>收件人： ")
                    .append(mailBean.receiveTo.getMailSimpleNames())
                    .append("</span><br>")
                    .append(if (mailBean.receiveCc.isNullOrBlank())
                        "" else
                        "<span>抄送： " + mailBean.receiveCc.getMailSimpleNames() + "</span><br>")
                    .append("<span>日期： ")
                    .append(DateUtils.longToString(mailBean.sendTime, "yyyy-MM-dd HH:mm:ss"))
                    .append("</span><br>")
                    .append("<span>主题： ")
                    .append(mailBean.title)
                    .append("</span><br></div>")
            // 设置html内容，先添加div到最开始的地方
            val doc = Jsoup.parse(content)
            val div = doc.select("div").first()
            div.prepend(builder.toString()) //在div前添加html内容
            Log.e(TAG, "发件内容：$doc")
            return doc.toString()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        removeAttachment()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (compositeDisposable != null) {
            compositeDisposable.clear()
        }
    }

    companion object {
        private val TAG = EmailReplyActivity::class.java.simpleName
    }
}