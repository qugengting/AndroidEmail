package com.qugengting.email.adapter

import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.qugengting.email.R
import com.qugengting.email.bean.SearchHisBean

class SearchHisAdapter(context: Context) : ListBaseAdapter<SearchHisBean>(context) {
    override fun getLayoutId(): Int {
        return R.layout.list_item_search_history
    }

    override fun onBindItemHolder(holder: SuperViewHolder, position: Int) {
        val contentView = holder.getView<View>(R.id.layout_item)
        val tvHis = holder.getView<TextView>(R.id.tvHis)
        tvHis.text = getDataList()[position].key
        val lyDelete = holder.getView<RelativeLayout>(R.id.lyHisDelete)
        lyDelete.setOnClickListener {
            //如果删除时，不使用mAdapter.notifyItemRemoved(pos)，则删除没有动画效果，
            //且如果想让侧滑菜单同时关闭，需要同时调用 ((CstSwipeDelMenu) holder.itemView).quickClose();
            //((CstSwipeDelMenu) holder.itemView).quickClose();
            mOnSearchHisListener?.onDel(position)
        }
        //注意事项，设置item点击，不能对整个holder.itemView设置咯，只能对第一个子View，即原来的content设置，这算是局限性吧。
        contentView.setOnClickListener {
            mOnSearchHisListener?.onItemClick(position)
        }
    }

    /**
     * 和Activity通信的接口
     */
    interface OnSearchHisListener {
        fun onDel(position: Int)
        fun onItemClick(position: Int)
    }

    private var mOnSearchHisListener: OnSearchHisListener? = null
    fun setOnDelListener(onSearchHisListener: OnSearchHisListener?) {
        mOnSearchHisListener = onSearchHisListener
    }
}