package com.qugengting.email.constants;

/**
 * Created by xuruibin on 2018/4/24.
 * 描述：
 */

public class MailConstants {
    //这里是邮箱地址，本APP使用的是QQ测试账号，需要自行替换
    public static final String MAIL_ACCOUNT = "375144541@qq.com";
    //本人邮箱发送出去带的名称，如果是QQ邮箱，可以设置成自己的QQ昵称
    public static final String MAIL_NAME = "375144541";
    //参考：https://service.mail.qq.com/cgi-bin/help?subtype=1&&no=1001256&&id=28
    //邮箱第三方登录授权码或密码，需要自行替换，参考上面链接获取
    public static final String MAIL_PWD = "xxxxxxxxxxxxxx";

    public static final String MAIL_SMTP_HOST = "smtp.qq.com";

    public static final String MAIL_IMAP_HOST = "imap.qq.com";
}
