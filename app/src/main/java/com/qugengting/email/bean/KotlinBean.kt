package com.qugengting.email.bean

import org.litepal.crud.LitePalSupport

/**
 * @author:xuruibin

 * @date:2021/1/22
 *
 * Description:
 */

data class SearchHisBean(val mailAccount: String, val key: String, var time: Long) : LitePalSupport()//搜索历史

/**
 * 是否已加载完最早的邮件
 * loadFlag: 0表示还没加载到；1表示已加载到
 */
data class MailLoadFlag(val account: String, val loadFlag: String) : LitePalSupport()

