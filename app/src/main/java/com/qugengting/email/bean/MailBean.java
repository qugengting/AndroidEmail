package com.qugengting.email.bean;


import androidx.annotation.NonNull;

import org.litepal.LitePal;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuruibin on 2018/4/17.
 * 描述：
 */

public class MailBean extends LitePalSupport implements Comparable {
    @Column(unique = true)
    private String uuid;//数据库唯一标识
    private String account;//邮箱账号
    /**
     * 发件人
     */
    private String sender;
    private String senderAddress;
    private String receiveTo;//格式为：qugengting=qugengting@sina.com#qu=qu@sina.com，便于拆分
    private String receiveCc;
    private String title;
    private String content;
    private String previewContent;//预览的正文内容，纯文本格式，去掉html元素，只截取前100，用于首页显示
    private long sendTime;
    private int readFlag;//0代表未读，1代表已读
    private int attachFlag;//0代表没附件，1代表有附件
    private int fileFlag;//0代表没内容需要下载，1代表有内容需要下载（包括附件和图片）
    private int downloadFlag;//0代表还没下载，1代表已下载
    private long uid;
    private int type;//0代表收件箱，1代表发件箱
//    private ArrayList<String> attachList;//附件列表，element是APP私有目录下的文件名称，包含后缀

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPreviewContent() {
        return previewContent;
    }

    public void setPreviewContent(String previewContent) {
        this.previewContent = previewContent;
    }

    public List<AttachBean> getAttachList() {
        return LitePal.where("uuid = ?", uuid).find(AttachBean.class);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getFileFlag() {
        return fileFlag;
    }

    public void setFileFlag(int fileFlag) {
        this.fileFlag = fileFlag;
    }

    public int getDownloadFlag() {
        return downloadFlag;
    }

    public void setDownloadFlag(int downloadFlag) {
        this.downloadFlag = downloadFlag;
    }

    public String getReceiveTo() {
        return receiveTo;
    }

    public void setReceiveTo(String receiveTo) {
        this.receiveTo = receiveTo;
    }

    public String getReceiveCc() {
        return receiveCc;
    }

    public void setReceiveCc(String receiveCc) {
        this.receiveCc = receiveCc;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public int getAttachFlag() {
        return attachFlag;
    }

    public void setAttachFlag(int attachFlag) {
        this.attachFlag = attachFlag;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public int getReadFlag() {
        return readFlag;
    }

    public void setReadFlag(int readFlag) {
        this.readFlag = readFlag;
    }

    @Override
    public int compareTo(@NonNull Object another) {
        if (another instanceof MailBean) {
            if (this.sendTime > ((MailBean) another).sendTime) {
                return -1;
            } else {
                return 1;
            }
        }
        return -1;
    }
}
