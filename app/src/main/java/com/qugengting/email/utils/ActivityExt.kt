package com.qugengting.email.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.inputmethod.InputMethodManager

/**
 * Description: Activity相关
 * Create by xuruibin
 * 2021/1/29
 */

fun Activity.hideInput() {
    val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val v = currentFocus
    if (null != v) {
        imm.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}

inline fun <reified T> Activity.start(block: Intent.() -> Unit) {
    val intent = Intent(this, T::class.java)
    intent.block()
    startActivity(intent)
}

