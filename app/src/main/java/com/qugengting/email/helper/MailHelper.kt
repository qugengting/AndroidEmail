package com.qugengting.email.helper

import android.content.Context
import android.util.Log
import com.qugengting.email.activity.EmailAttachmentBaseActivity
import com.qugengting.email.bean.AttachBean
import com.qugengting.email.bean.MailBean
import com.qugengting.email.constants.MailConstants
import com.sun.mail.imap.IMAPFolder
import org.litepal.LitePal
import java.io.File
import java.util.*
import javax.activation.CommandMap
import javax.activation.MailcapCommandMap
import javax.mail.Flags
import javax.mail.Folder
import javax.mail.Session

/**
 * @author:xuruibin

 * @date:2021/2/4
 *
 * Description:
 */
interface MailHelper {

    fun deleteMail(mailBean: MailBean): Boolean {
        //已发的邮件直接删除
        if (mailBean.type == 1) {
            deleteAttach(mailBean)
            mailBean.delete()
            return true
        }
        try {
            val props = Properties()
            props.setProperty("mail.transport.protocol", "imap") // 使用的协议（JavaMail规范要求）
            props.setProperty("mail.smtp.host", MailConstants.MAIL_IMAP_HOST) // 发件人的邮箱的 SMTP服务器地址
            props.setProperty("mail.imap.connectiontimeout", "5000") //设置连接超时时间
            /*  The default IMAP implementation in JavaMail is very slow to download large attachments.
                Reason for this is that, by default, it uses attachmentPath small 16K fetch buffer size.
                You can increase this buffer size using the “mail.imap.fetchsize” system property
                For example:
            */props.setProperty("mail.imap.fetchsize", "1000000")
            //加入以下设置，附件下载速度更是快了10倍
            props.setProperty("mail.imap.partialfetch", "false")
            // 获取连接
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
            val inbox = folder as IMAPFolder
            val message = inbox.getMessageByUID(mailBean.uid)
            message.setFlag(Flags.Flag.DELETED, true)
            folder.close(true) // 关闭邮件夹对象
            store.close() // 关闭连接对象
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        deleteAttach(mailBean)
        mailBean.delete()
        return true
    }

    fun deleteAttach(mailBean: MailBean) {
        val list = mailBean.attachList
        var parent: File? = null
        for (item in list) {
            val file = File(item.path)
            if (file.exists()) {
                parent = file.parentFile
                //删除附件
                file.delete()
            }
            //删除附件实体类
            val attachBean: AttachBean? = LitePal.where("path = ?", item.path).findFirst(AttachBean::class.java)
            attachBean?.delete()
        }
        //删除父目录
        parent?.apply {
            delete()
        }
    }

    fun setMailSeenFlag(mails: List<MailBean>): Boolean {
        try {
            val props = Properties()
            props.setProperty("mail.transport.protocol", "imap") // 使用的协议（JavaMail规范要求）
            props.setProperty("mail.smtp.host", MailConstants.MAIL_IMAP_HOST) // 发件人的邮箱的 SMTP服务器地址
            props.setProperty("mail.imap.connectiontimeout", "5000") //设置连接超时时间
            /*  The default IMAP implementation in JavaMail is very slow to download large attachments.
                Reason for this is that, by default, it uses attachmentPath small 16K fetch buffer size.
                You can increase this buffer size using the “mail.imap.fetchsize” system property
                For example:
            */props.setProperty("mail.imap.fetchsize", "1000000")
            //加入以下设置，附件下载速度更是快了10倍
            props.setProperty("mail.imap.partialfetch", "false")
            // 获取连接
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
            val inbox = folder as IMAPFolder
            for (mailBean in mails) {
                val message = inbox.getMessageByUID(mailBean.uid)
                message.setFlag(Flags.Flag.SEEN, true)
            }
            folder.close(true) // 关闭邮件夹对象
            store.close() // 关闭连接对象
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        for (mailBean in mails) {
            mailBean.readFlag = 1
            mailBean.save()
        }
        return true
    }
}