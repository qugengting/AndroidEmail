package com.qugengting.email.adapter

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.qugengting.email.R
import com.qugengting.email.widget.flowlayout.FlowLayout
import com.qugengting.email.widget.flowlayout.TagAdapter

/**
 * @author:xuruibin

 * @date:2021/3/10
 *
 * Description:用于显示收件人和抄送人的适配器
 */
class FlowLabelAdapter(private val layoutInflater: LayoutInflater) : TagAdapter(arrayOf()) {
    override fun getView(parent: FlowLayout, position: Int, s: String): View {
        val tv = layoutInflater.inflate(R.layout.tv_item, parent, false) as TextView
        tv.text = s
        return tv
    }

    override fun getLabelView(parent: FlowLayout): View {
        //如果设置flowLayout.setAttachLabel(false);该标签将不显示
        return layoutInflater.inflate(R.layout.tv_label, parent, false) as TextView
    }

    override fun getInputView(parent: FlowLayout): View {
        return layoutInflater.inflate(R.layout.edt, parent, false)
    }
}