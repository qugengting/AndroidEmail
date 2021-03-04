package com.qugengting.email.adapter

import android.util.SparseArray
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * ViewHolder基类
 */
class SuperViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val views: SparseArray<View> = SparseArray()
    fun <T : View> getView(viewId: Int): T {
        var view = views[viewId]
        if (view == null) {
            view = itemView.findViewById(viewId)
            views.put(viewId, view)
        }
        return view as T
    }

}