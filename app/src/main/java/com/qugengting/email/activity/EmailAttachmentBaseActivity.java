package com.qugengting.email.activity;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.qugengting.email.R;
import com.qugengting.email.bean.AttachBean;
import com.qugengting.email.bean.MailBean;
import com.qugengting.email.constants.MailConstants;
import com.qugengting.email.utils.FileUtils;
import com.qugengting.email.utils.Utils;
import com.sun.mail.imap.IMAPFolder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.litepal.LitePal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeUtility;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.richeditor.RichEditor;

/**
 * Created by xuruibin on 2018/4/24.
 * 描述：带附件/正文图片的activity基类
 */

public abstract class EmailAttachmentBaseActivity extends BaseActivity {
    private static final String TAG = EmailAttachmentBaseActivity.class.getSimpleName();
    private static final String NEED_RE_DOWNLOAD = "need_re_download";
    protected RichEditor webView;
    protected long uid;
    protected static final String OK = "ok";
    protected static final String ERR = "err";
    protected String attachRootPath;
    protected ProgressDialog dialog;
    protected CompositeDisposable compositeDisposable;
    protected MailBean mailBean;
    protected String content;
    /**
     * 是否编辑模式
     */
    protected boolean isEdit = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);// 设置默认键盘不弹出
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 设置设备为竖屏模式
        attachRootPath = getExternalFilesDir("Mail") + File.separator + MailConstants.MAIL_NAME + File.separator;
        contentImgPath = attachRootPath + "contentImg" + File.separator;
        File contentImgFile = new File(contentImgPath);
        if (!contentImgFile.exists()) {
            contentImgFile.mkdirs();
        }
        compositeDisposable = new CompositeDisposable();
    }

    /**
     * 正文图片名称列表
     */
    protected List<String> imgList = new ArrayList<>();

    /**
     * 文件名称列表
     */
    protected List<String> fileNames = new ArrayList<>();
    /**
     * 真正的附件存放路径
     */
    protected String attachRealPath;
    /**
     * 正文图片存放的路径
     */
    protected String contentImgPath;

    protected void initMailWithAttachment() {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
        }
        dialog.setMessage(getString(R.string.mail_please_wait));
        dialog.show();
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                attachRealPath = attachRootPath + uid + "_F" + File.separator;
                //刚下载完的附件（包含正文图片）都在 attachOriPath 路径下
                if (mailBean.getDownloadFlag() == 0) {//还没下载正文图片要先下载
                    getAllMailFile();
                }
                File file = new File(contentImgPath);
                if (file.exists() && file.isDirectory()) {
                    File[] files = file.listFiles();
                    for (File file1 : files) {
                        fileNames.add(file1.getName());
                    }
                }
                //遍历找出正文里面的图片名称，如<div><img src="cid:80E3B4F6@9AD8D94B.89A4D55A.jpg"</div> 并加入集合
                Document doc = Jsoup.parse(content);
                Elements imgElements = doc.select("img");
                for (Element element : imgElements) {
                    String imgUrl = element.attr("src");
                    if (imgUrl.contains("cid:")) {
                        imgUrl = imgUrl.substring(4);
                    }
                    for (int i = 0; i < fileNames.size(); i++) {
                        if (imgUrl.equals(fileNames.get(i))) {
                            imgList.add(imgUrl);
                            //图片宽度设置为适配手机屏幕的320
                            element.attr("width", "320");
                        }
                    }
                }
                //邮件有正文图片，但缓存里找不到，说明缓存被清空了，需要重新下载
                if (imgElements.size() > 0 && imgList.size() < imgElements.size() && mailBean.getType() == 0) {
                    mailBean.setDownloadFlag(0);
                    mailBean.save();
                    Log.e(TAG, "邮件图片被删除，需要重新下载");
                    emitter.onNext(NEED_RE_DOWNLOAD);
                } else {
                    mailBean.setDownloadFlag(1);
                    boolean b = mailBean.save();
                    Log.e(TAG, "邮件状态更新是否成功：" + b);
                    String newContent = doc.toString();
                    for (String imgName : imgList) {
                        newContent = newContent.replace(imgName, contentImgPath + imgName);
                    }
                    Log.e(TAG, "newContent: " + newContent);
                    emitter.onNext(newContent);
                }
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(String value) {
                        dialog.dismiss();
                        if (value.equals(NEED_RE_DOWNLOAD)) {
                            initMailWithAttachment();
                        } else {
                            if (isEdit) {
                                webView.setHtml(value);
                            } else {
                                webView.loadData(value, "text/html; charset=UTF-8", null);
                            }
                            showAttachment();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dialog.dismiss();
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        dialog.dismiss();
                    }
                });
    }

    /**
     * 打开附件
     * @param bean 附件实体类
     */
    protected void openAttachment(final AttachBean bean) {
        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.mail_please_wait));
        dialog.show();
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) {
                File file = new File(bean.getPath());
                if (file.exists()) {
                    emitter.onNext(true);
                } else {
                    boolean b = false;
                    if (mailBean.getType() == 0) {
                        b = getAttachment(bean.getName());
                    }
                    //发件箱的附件被删除，无法从服务器恢复
                    if (mailBean.getType() == 1) {
                        b = false;
                    }
                    emitter.onNext(b);
                }
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(Boolean value) {
                        if (value) {
                            Utils.makeText(EmailAttachmentBaseActivity.this, "文件路径是: " + bean.getPath());
                        } else {
                            if (mailBean.getType() == 0) {
                                Utils.makeText(EmailAttachmentBaseActivity.this, getString(R.string.mail_attachment_download_fail));
                            } else if (mailBean.getType() == 1) {
                                Utils.makeText(EmailAttachmentBaseActivity.this, getString(R.string.mail_attachment_cache_no_exitst));
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dialog.dismiss();
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        dialog.dismiss();
                    }
                });
    }

    /**
     * 显示附件
     */
    protected abstract void showAttachment();

    /**
     * 获取邮件所有文件信息（如果是正文图片则直接下载）
     */
    protected void getAllMailFile() {
        try {
            Properties props = new Properties();
            props.setProperty("mail.transport.protocol", "imap"); // 使用的协议（JavaMail规范要求）
            props.setProperty("mail.smtp.host", MailConstants.MAIL_IMAP_HOST); // 发件人的邮箱的 SMTP服务器地址
            props.setProperty("mail.imap.connectiontimeout", "5000");//设置连接超时时间
            /*  The default IMAP implementation in JavaMail is very slow to download large attachments.
                Reason for this is that, by default, it uses attachmentPath small 16K fetch buffer size.
                You can increase this buffer size using the “mail.imap.fetchsize” system property
                For example:
            */
            props.setProperty("mail.imap.fetchsize", "1000000");
            //加入以下设置，附件下载速度更是快了10倍
            props.setProperty("mail.imap.partialfetch", "false");
            // 获取连接
            MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
            mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
            mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
            mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
            mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
            mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
            CommandMap.setDefaultCommandMap(mc);

            Session session = Session.getDefaultInstance(props);

            session.setDebug(false);
            // 获取Store对象
            Store store = session.getStore("imap");
            store.connect(MailConstants.MAIL_IMAP_HOST, MailConstants.MAIL_ACCOUNT, MailConstants.MAIL_PWD);
            // 通过POP3协议获得Store对象调用这个方法时，邮件夹名称只能指定为"INBOX"
            Folder folder = store.getFolder("INBOX");// 获得用户的邮件帐户
            folder.open(Folder.READ_ONLY); // 设置对邮件帐户的访问权限
            IMAPFolder inbox = (IMAPFolder) folder;
            Message message = inbox.getMessageByUID(uid);
            getFile(message, contentImgPath);
            folder.close(false);// 关闭邮件夹对象
            store.close(); // 关闭连接对象}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected boolean getAttachment(String attachName) {
        try {
            Properties props = new Properties();
            props.setProperty("mail.transport.protocol", "imap"); // 使用的协议（JavaMail规范要求）
            props.setProperty("mail.smtp.host", MailConstants.MAIL_IMAP_HOST); // 发件人的邮箱的 SMTP服务器地址
            props.setProperty("mail.imap.connectiontimeout", "5000");//设置连接超时时间
            /*  The default IMAP implementation in JavaMail is very slow to download large attachments.
                Reason for this is that, by default, it uses attachmentPath small 16K fetch buffer size.
                You can increase this buffer size using the “mail.imap.fetchsize” system property
                For example:
            */
            props.setProperty("mail.imap.fetchsize", "1000000");
            //加入以下设置，附件下载速度更是快了10倍
            props.setProperty("mail.imap.partialfetch", "false");
            // 获取连接
            MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
            mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
            mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
            mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
            mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
            mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
            CommandMap.setDefaultCommandMap(mc);

            Session session = Session.getDefaultInstance(props);

            session.setDebug(false);
            // 获取Store对象
            Store store = session.getStore("imap");
            store.connect(MailConstants.MAIL_IMAP_HOST, MailConstants.MAIL_ACCOUNT, MailConstants.MAIL_PWD);
            // 通过POP3协议获得Store对象调用这个方法时，邮件夹名称只能指定为"INBOX"
            Folder folder = store.getFolder("INBOX");// 获得用户的邮件帐户
            folder.open(Folder.READ_ONLY); // 设置对邮件帐户的访问权限
            IMAPFolder inbox = (IMAPFolder) folder;
            Message message = inbox.getMessageByUID(uid);
            downloadAttach(message, attachName);
            folder.close(false);// 关闭邮件夹对象
            store.close(); // 关闭连接对象}
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 处理邮件所带文件
     *
     * @param part：Part
     * @param contentImgPath：邮件正文图片存放路径
     */
    private void getFile(Part part, String contentImgPath) throws Exception {
        String fileName = "";
        //保存附件到服务器本地
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart mpart = mp.getBodyPart(i);
                //API地址：https://docs.oracle.com/javaee/5/api/javax/mail/internet/MimeBodyPart.html
                String disposition = mpart.getDisposition();
                //1
                if ((disposition != null) && ((disposition.equals(Part.ATTACHMENT)) || (disposition.equals(Part.INLINE)))) {
                    //如果是正文图片，新浪、网易等邮件filename并不等于cid:后面的名称
                    //只有Content-ID才是最可靠的判断方法，所以优先取用Content-ID的值
                    //Content-ID: <part1.60190c8f26c9473_202102>
                    //Content-Type: image/jpeg; name="=?GBK?B?uL28/jEuanBn?="
                    //Content-Disposition: attachment; filename="=?GBK?B?uL28/jEuanBn?="
                    String contentID = ((MimeBodyPart) mpart).getContentID();
                    if (contentID != null) {
                        fileName = contentID.replaceAll("<", "")
                                .replaceAll(">", "");
                        if (fileName != null) {
                            fileName = MimeUtility.decodeText(fileName);
                            Log.e(TAG, "正文图片：" + fileName);
                            InputStream inputStream = mpart.getInputStream();
                            saveFile(fileName, inputStream, contentImgPath);
                        }
                    } else {
                        fileName = mpart.getFileName();
                        if (fileName != null) {
                            fileName = MimeUtility.decodeText(fileName);
                            Log.e(TAG, "附件名称：" + fileName);
                            long attachSize = (long) (mpart.getSize() * 72.97 / 100);
                            String path = attachRealPath + fileName;
                            AttachBean attachBean = LitePal.where("path = ?", path).findFirst(AttachBean.class);
                            if (attachBean == null) {
                                attachBean = new AttachBean();
                                attachBean.setName(fileName);
                                attachBean.setPath(attachRealPath + fileName);
                                attachBean.setSize(FileUtils.getFileSize(attachSize));
                                attachBean.setUuid(mailBean.getUuid());
                                attachBean.save();
                            }
                            //附件不下载，用户点击打开才下载
//                            InputStream inputStream = mpart.getInputStream();
//                            saveFile(fileName, inputStream, attachRealPath);
                        }
                    }
                } else if (mpart.isMimeType("multipart/*")) {//2
                    getFile(mpart, contentImgPath);
                } else {//3
                    fileName = mpart.getFileName();
                    if (fileName != null) {
                        //不可使用getFileName方法获取正文图片名称，因在foxmail当中不匹配
                        //QQ邮件不会设置正文图片的这些头信息：
                        //Content-Disposition: attachment; filename=
                        //所以没有跳入1判断条件里面，这里处理的都是正文图片
                        fileName = ((MimeBodyPart) mpart).getContentID().replaceAll("<", "")
                                .replaceAll(">", "");
                        fileName = MimeUtility.decodeText(fileName);
                        Log.e(TAG, "正文图片：" + fileName);
                        InputStream inputStream = mpart.getInputStream();
                        saveFile(fileName, inputStream, contentImgPath);
                    }
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            getFile((Part) part.getContent(), contentImgPath);
        }
    }

    /**
     * 下载指定附件
     *
     * @param part：Part
     * @param attachName：附件名称
     */
    private void downloadAttach(Part part, String attachName) throws Exception {
        String fileName = "";
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart mpart = mp.getBodyPart(i);
                //API地址：https://docs.oracle.com/javaee/5/api/javax/mail/internet/MimeBodyPart.html
                String disposition = mpart.getDisposition();
                //1
                if ((disposition != null) && ((disposition.equals(Part.ATTACHMENT)) || (disposition.equals(Part.INLINE)))) {
                    String contentID = ((MimeBodyPart) mpart).getContentID();
                    if (TextUtils.isEmpty(contentID)) {
                        fileName = mpart.getFileName();
                        if (fileName != null) {
                            fileName = MimeUtility.decodeText(fileName);
                            Log.e(TAG, "附件名称：" + fileName);
                            if (fileName.equals(attachName)) {
                                InputStream inputStream = mpart.getInputStream();
                                saveFile(fileName, inputStream, attachRealPath);
                            }
                        }
                    }
                } else if (mpart.isMimeType("multipart/*")) {//2
                    downloadAttach(mpart, attachName);
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            downloadAttach((Part) part.getContent(), attachName);
        }
    }

    /**
     * 保存邮件文件到指定目录里
     *
     * @param fileName：文件名称
     * @param in：文件输入流
     * @param filePath：邮件文件存放基路径
     */
    private void saveFile(String fileName, InputStream in, String filePath) throws Exception {
        File storeFile = new File(filePath);
        if (!storeFile.exists()) {
            storeFile.mkdirs();
        }
        OutputStream o;
        File file = new File(filePath + File.separator + fileName);
        if (file.exists()) {
            Log.e(TAG, "附件" + fileName + "已存在");
            return;
        }
        o = new FileOutputStream(file);
        byte[] data = new byte[1024];
        int i;
        long a = System.currentTimeMillis();
        while ((i = in.read(data)) != -1) {
            o.write(data, 0, i);
        }
        long b = System.currentTimeMillis();
        Log.e(TAG, "下载附件结束, 所用时间：" + (b - a) + "毫秒");
        o.flush();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
    }
}
