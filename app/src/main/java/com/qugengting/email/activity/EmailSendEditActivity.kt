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
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.permissionx.guolindev.PermissionX
import com.qugengting.email.R
import com.qugengting.email.adapter.AttachSendAdapter
import com.qugengting.email.adapter.FlowLabelAdapter
import com.qugengting.email.bean.AttachBean
import com.qugengting.email.bean.MailBean
import com.qugengting.email.constants.MailConstants
import com.qugengting.email.utils.*
import com.qugengting.email.utils.Utils.makeText
import com.qugengting.email.utils.systembar.StatusBarUtils
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_email_re_edit.*
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import org.litepal.LitePal
import java.io.*
import java.util.*
import javax.activation.*
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Multipart
import javax.mail.Session
import javax.mail.internet.*

/**
 * Created by xuruibin on 2021/3/9.
 * 描述：云邮已发邮件再次编辑界面
 */

class EmailSendEditActivity : EmailAttachmentBaseActivity(), View.OnClickListener {
    private lateinit var mInflater: LayoutInflater
    private lateinit var recvAdapter: FlowLabelAdapter
    private lateinit var ccAdapter: FlowLabelAdapter
    private lateinit var attachAdapter: AttachSendAdapter
    private var mContent: String = ""

    /**
     * TODO 注意，后续通过外部添加的收件人（抄送也是）应该把名称和地址加入到该集合当中
     * 键为收件人名称，值为收件人地址
     */
    private val recvMap: MutableMap<String, String> = mutableMapOf()
    private val ccMap: MutableMap<String, String> = mutableMapOf()

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
        setContentView(R.layout.activity_email_re_edit)
        mInflater = LayoutInflater.from(this)
        recvAdapter = FlowLabelAdapter(mInflater)
        ccAdapter = FlowLabelAdapter(mInflater)
        initView()
        initEdit()
        folderPath = "${getExternalFilesDir("Mail")}/${MailConstants.MAIL_NAME}/${sendUid}_F/"
        initMail()
    }

    private fun initView() {
        webView = findViewById(R.id.editor)
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
//        editor.setPlaceholder(getString(R.string.mail_write_content_hint))
        editor.setInputEnabled(true)
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

    override fun showAttachment() {
        mContent = webView.html
    }

    private fun initMail() {
        isEdit = true
        val intent = intent
        uid = intent.getLongExtra("uid", 0)
        mailBean = LitePal.where("account = ? and uid = ?"
                , MailConstants.MAIL_ACCOUNT, uid.toString()).findFirst(MailBean::class.java)
        content = mailBean.content
        val doc = Jsoup.parseBodyFragment(content)
        content = doc.body().toString()

        edtTitle.setText(mailBean.title)
        //填充收件人列表
        val allRecv = mailBean.receiveTo
        if (allRecv.isNotBlank()) {
            val otherRecvs = allRecv.split("#")
            for (item in otherRecvs) {
                val names = item.split("=")
                recvMap[names[0]] = names[1]
                flReceiver.addItem(names[0])
            }
        }
        //填充抄送列表
        val allCC = mailBean.receiveCc
        if (allCC.isNotBlank()) {
            val otherCCs = allCC.split("#")
            for (item in otherCCs) {
                val names = item.split("=")
                ccMap[names[0]] = names[1]
                flCC.addItem(names[0])
            }
        }

        //把原邮件附件带上
        val attachList = mailBean.attachList
        for (item in attachList) {
            copyFile(item.path, item.name)
        }

        initMailWithAttachment()
//        webView.html = content
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
                        //类似：_qugting@6b4c25——警告，不要带中文！！！不要带中文
                        val cid = "_qugting@${UUID.randomUUID().toString().substring(0, 6)}.png"
                        //需要把图片备份到默认文件夹里
                        exec({
                            FileUtils.copyFile(path, "$contentImgPath$cid", false)
                        }, {
                            Log.e(TAG, "图片备份到：$contentImgPath$cid, 结果：$it")
                            editor.insertImage("cid:$contentImgPath$cid")
                        })
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
                false
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
                            makeText(this@EmailSendEditActivity, getString(R.string.mail_send_success_hint))
                            finish()
                        } else if (value.equals(ERR)) {
                            makeText(this@EmailSendEditActivity, getString(R.string.mail_send_fail_hint))
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

            var hasSomePart = false

            val map = mutableMapOf<String, String>()
            //找到正文图片，将所有的src对应值替换并取出来放入map供后面封装part
            val doc = Jsoup.parse(sendContent)
            val imgElements: Elements = doc.select("img")
            for (element in imgElements) {
                var imgUrl = element.attr("src")
                if (imgUrl.contains("cid:")) {
                    hasSomePart = true
                    //imgUrl类似cid:storage/emulated/0/Android/data/com.qugengting.email/file/email/contentImg/xxx.png
                    imgUrl = imgUrl.substring(4)
                    //imgUrl类似storage/emulated/0/Android/data/com.qugengting.email/file/email/contentImg/xxx.png
                    val index = imgUrl.lastIndexOf('/')
                    val name = imgUrl.substring(index + 1)
                    //name类似xxx.png
                    sendContent = sendContent.replace(imgUrl, name)
                    map[imgUrl] = name
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
            //设置正文图片
            for (filePathName in map.keys) {
                val mdp = MimeBodyPart()
                val fds = FileDataSource(filePathName)
                mdp.dataHandler = DataHandler(fds)
                val tempFileName = MimeUtility.encodeText(map[filePathName])
                mdp.fileName = tempFileName
                mdp.contentID = tempFileName
                //Content-Disposition: attachment; filename="_qugting@8d11ff.png"
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
            transport.connect(MailConstants.MAIL_SMTP_HOST, MailConstants.MAIL_ACCOUNT, MailConstants.MAIL_PWD)
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
                readFlag = 1
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
        return StringBuilder()
                .append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=GB18030\">")
                .append("</head><body>")
                .append(mContent)
                .append("</body></html>")
                .toString()
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