package com.qugengting.email.adapter

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.qugengting.email.R
import com.qugengting.email.bean.AttachBean
import com.qugengting.email.utils.DrawableUtils

class AttachRecvAdapter(context: Context) : ListBaseAdapter<AttachBean>(context) {

    override fun getLayoutId(): Int {
        return R.layout.list_item_attach_recv
    }

    override fun onBindItemHolder(holder: SuperViewHolder, position: Int) {
        holder.getView<TextView>(R.id.tvAttachName).text = getDataList()[position].name
        holder.getView<TextView>(R.id.tvAttachSize).text = getDataList()[position].size
        holder.getView<ImageView>(R.id.ivAttach).setImageResource(DrawableUtils.getFileIcon(getDataList()[position].name))
        val contentView = holder.getView<View>(R.id.layout_item)
        contentView.setOnClickListener {
            mOnAttachClickListener?.onClick(position)
        }
    }

    /**
     * 和Activity通信的接口
     */
    interface OnAttachClickListener {
        fun onClick(position: Int)
    }

    private var mOnAttachClickListener: OnAttachClickListener? = null
    fun setOnAttachClickListener(onAttachClickListener: OnAttachClickListener?) {
        mOnAttachClickListener = onAttachClickListener
    }
}