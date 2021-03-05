package com.qugengting.email.adapter

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.qugengting.email.R
import com.qugengting.email.activity.BaseActivity
import com.qugengting.email.activity.EmaiDetailActivity
import com.qugengting.email.bean.MailBean
import com.qugengting.email.utils.DateUtils
import com.qugengting.email.utils.start
import java.util.*

class SearchResultAdapter(context: Context) : ListBaseAdapter<MailBean>(context) {
    override fun getLayoutId(): Int {
        return R.layout.list_item_searchresult
    }

    override fun onBindItemHolder(holder: SuperViewHolder, position: Int) {
        val contentView = holder.getView<View>(R.id.layout_item)
        val tvSender = holder.getView<TextView>(R.id.tv_mail_sender)
        val tvTitle = holder.getView<TextView>(R.id.tv_mail_title)
        val tvContent = holder.getView<TextView>(R.id.tv_mail_content)
        val tvTime = holder.getView<TextView>(R.id.tv_mail_send_time)
        val ivRead = holder.getView<View>(R.id.iv_mail_read)
        val ivAttachment = holder.getView<ImageView>(R.id.iv_mail_attach)
        tvSender.text = getDataList()[position].sender
        tvTitle.text = getDataList()[position].title
        tvContent.text = getDataList()[position].previewContent
//        tvSender.setTextColor(if (getDataList()[position].readFlag == 1) Color.parseColor("#888888") else Color.parseColor("#000000"))
        tvTime.text = DateUtils.getFormatDate(Date(getDataList()[position].sendTime))
        ivRead.visibility = if (getDataList()[position].readFlag == 1) View.GONE else View.VISIBLE
        ivAttachment.visibility = if (getDataList()[position].attachFlag == 0) View.GONE else View.VISIBLE
        //注意事项，设置item点击，不能对整个holder.itemView设置咯，只能对第一个子View，即原来的content设置，这算是局限性吧。
        contentView.setOnClickListener {
            (mContext as BaseActivity).start<EmaiDetailActivity> {
                putExtra("uid", getDataList()[position].uid)
            }
        }
    }

}