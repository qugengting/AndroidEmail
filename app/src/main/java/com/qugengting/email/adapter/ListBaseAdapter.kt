package com.qugengting.email.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * 封装adapter（注意：仅供参考，根据需要选择使用demo中提供的封装adapter）
 * @param <T>
</T> */
abstract class ListBaseAdapter<T>(protected var mContext: Context) : RecyclerView.Adapter<SuperViewHolder>() {
    private val mInflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var mDataList: MutableList<T> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuperViewHolder {
        val itemView = mInflater.inflate(getLayoutId(), parent, false)
        return SuperViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SuperViewHolder, position: Int) {
        onBindItemHolder(holder, position)
    }

    //局部刷新关键：带payload的这个onBindViewHolder方法必须实现
    override fun onBindViewHolder(holder: SuperViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            onBindItemHolder(holder, position, payloads)
        }
    }

    abstract fun getLayoutId(): Int
    abstract fun onBindItemHolder(holder: SuperViewHolder, position: Int)
    private fun onBindItemHolder(holder: SuperViewHolder, position: Int, payloads: List<Any>?) {}
    override fun getItemCount(): Int {
        return mDataList.size
    }

    fun setDataList(list: MutableList<T>) {
        mDataList = list
        notifyDataSetChanged()
    }

    fun getDataList(): List<T> {
        return mDataList
    }

    fun addAll(list: List<T>) {
        val lastIndex = mDataList.size
        if (mDataList.addAll(list)) {
            notifyItemRangeInserted(lastIndex, list.size)
        }
    }

    fun add(e: T) {
        mDataList.add(e)
        notifyDataSetChanged()
    }

    fun remove(position: Int) {
        mDataList.removeAt(position)
        notifyItemRemoved(position)
        if (position != mDataList.size) { // 如果移除的是最后一个，忽略
            notifyItemRangeChanged(position, mDataList.size - position)
        }
    }

    fun clear() {
        mDataList.clear()
        notifyDataSetChanged()
    }

}