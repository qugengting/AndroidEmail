package com.qugengting.email.adapter

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.qugengting.email.R
import com.qugengting.email.activity.BaseActivity
import com.qugengting.email.activity.EmaiDetailActivity
import com.qugengting.email.bean.MailBean
import com.qugengting.email.utils.DateUtils
import com.qugengting.email.widget.SwipeMenuView
import java.util.*

class RecvAdapter(context: Context) : ListBaseAdapter<MailBean>(context) {
    override fun getLayoutId(): Int {
        return R.layout.list_item_recv
    }

    override fun onBindItemHolder(holder: SuperViewHolder, position: Int) {
        val contentView = holder.getView<View>(R.id.layout_item)
        val tvSender = holder.getView<TextView>(R.id.tv_mail_sender)
        val tvTitle = holder.getView<TextView>(R.id.tv_mail_title)
        val tvContent = holder.getView<TextView>(R.id.tv_mail_content)
        val tvTime = holder.getView<TextView>(R.id.tv_mail_send_time)
        val ivRead = holder.getView<View>(R.id.iv_mail_read)
        val ivAttachment = holder.getView<ImageView>(R.id.iv_mail_attach)
        val btnDelete = holder.getView<Button>(R.id.btn_delete)
        tvSender.text = getDataList()[position].sender
        tvTitle.text = getDataList()[position].title
        tvContent.text = getDataList()[position].previewContent
//        tvSender.setTextColor(if (getDataList()[position].readFlag == 1) Color.parseColor("#888888") else Color.parseColor("#000000"))
        tvTime.text = DateUtils.getFormatDate(Date(getDataList()[position].sendTime))
        ivRead.visibility = if (getDataList()[position].readFlag == 1) View.GONE else View.VISIBLE
        ivAttachment.visibility = if (getDataList()[position].attachFlag == 0) View.GONE else View.VISIBLE
        //这句话关掉IOS阻塞式交互效果 并依次打开左滑右滑
        (holder.itemView as SwipeMenuView).setIos(false).isLeftSwipe = true
        btnDelete.setOnClickListener {
            //如果删除时，不使用mAdapter.notifyItemRemoved(pos)，则删除没有动画效果，
            //且如果想让侧滑菜单同时关闭，需要同时调用 ((CstSwipeDelMenu) holder.itemView).quickClose();
            //((CstSwipeDelMenu) holder.itemView).quickClose();
            mOnSwipeListener?.onDel(position)
        }
        //注意事项，设置item点击，不能对整个holder.itemView设置咯，只能对第一个子View，即原来的content设置，这算是局限性吧。
        contentView.setOnClickListener {
            val intent = Intent(mContext, EmaiDetailActivity::class.java)
            val bean = getDataList()[position]
            bean.readFlag = 1
            bean.save()
            intent.putExtra("uid", getDataList()[position].uid)
            intent.putExtra("position", position)
            (mContext as BaseActivity).startActivityForResult(intent, 1)
        }
    }

    /**
     * 和Activity通信的接口
     */
    interface OnRecvSwipeListener {
        fun onDel(position: Int)
    }

    private var mOnSwipeListener: OnRecvSwipeListener? = null
    fun setOnDelListener(onSwipeListener: OnRecvSwipeListener?) {
        mOnSwipeListener = onSwipeListener
    }
}