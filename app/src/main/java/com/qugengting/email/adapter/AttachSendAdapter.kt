package com.qugengting.email.adapter

import android.content.Context
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.qugengting.email.R
import com.qugengting.email.bean.AttachBean
import com.qugengting.email.utils.DrawableUtils
import com.qugengting.email.widget.SwipeMenuView

class AttachSendAdapter(context: Context) : ListBaseAdapter<AttachBean>(context) {

    override fun getLayoutId(): Int {
        return R.layout.list_item_attach_send
    }

    override fun onBindItemHolder(holder: SuperViewHolder, position: Int) {
        holder.getView<TextView>(R.id.tvAttachName).text = getDataList()[position].name
        holder.getView<TextView>(R.id.tvAttachSize).text = getDataList()[position].size
        holder.getView<ImageView>(R.id.ivAttach).setImageResource(DrawableUtils.getFileIcon(getDataList()[position].name))
        //这句话关掉IOS阻塞式交互效果 并依次打开左滑右滑
        (holder.itemView as SwipeMenuView).setIos(false).isLeftSwipe = true
        holder.getView<Button>(R.id.btn_attach_delete).setOnClickListener {
            //如果删除时，不使用mAdapter.notifyItemRemoved(pos)，则删除没有动画效果，
            //且如果想让侧滑菜单同时关闭，需要同时调用 ((CstSwipeDelMenu) holder.itemView).quickClose();
            //((CstSwipeDelMenu) holder.itemView).quickClose();
            mOnSwipeListener?.onRemove(position)
        }
    }

    /**
     * 和Activity通信的接口
     */
    interface OnAttachSwipeListener {
        fun onRemove(position: Int)
    }

    private var mOnSwipeListener: OnAttachSwipeListener? = null
    fun setOnDelListener(onAttachSwipeListener: OnAttachSwipeListener?) {
        mOnSwipeListener = onAttachSwipeListener
    }
}