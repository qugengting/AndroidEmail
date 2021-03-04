package com.qugengting.email.utils

import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.widget.AppCompatEditText
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File

/**
 * @author:xuruibin

 * @date:2020/11/23
 *
 * Description:高阶函数定义文件
 */

/**
 * 使用RxJava异步执行任务，并返回相应的值
 *
 * @param block 要执行的任务
 * @param rtn 任务中的返回值
 * @return
 */
fun <T, R> T.exec(block: T.() -> R, rtn: (R) -> Unit): T {
    Observable.create<R> { it.onNext(block()) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { rtn(it) }
    return this
}

fun <T, R> T.trycatch(block: T.() -> R?): R? {
    return try {
        block()
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }
}

fun String.withoutAddress() : String {
    val regex = "<(.*?)>"
    return this.replace(regex.toRegex(), ";")
}

//将格式：qugengting=qugengting@sina.com#qu=qu@sina.com 转换为格式：qugengting;qu;
fun String.getMailSimpleNames() : String {
    if (isNullOrBlank()) {
        return ""
    }
    val s = this.split("#")
    val sb = StringBuilder()
    for (item in s) {
        val names = item.split("=")
        sb.append(names[0]).append(";")
    }
    return sb.toString()
}

fun String.filterHtml(length: Int) : String {
    if (isNullOrBlank()) {
        return ""
    }
    // 去掉所有html元素,
    // 去掉所有html元素,
    var str: String = replace("\\&[a-zA-Z]{1,10};".toRegex(), "").replace(
            "<[^>]*>".toRegex(), "")
    str = str.replace("[(/>)<]".toRegex(), "")
    val len = str.length
    if (len <= length) {
        return str
    } else {
        str = str.substring(0, length)
        str += "......"
    }
    return str
}

fun File.deleteAll() {
    if (exists()) {
        val list = listFiles()
        for (child in list) {
            child.delete()
        }
        delete()
    }
}

fun AppCompatEditText.setListener(block: (Int) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun afterTextChanged(s: Editable) = Unit
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            block(s.length)
        }
    })
}

//fun main() {
//    val numbers = mutableListOf("one", "one", "four", "four")
//    val mutableListIterator = numbers.listIterator()
//
//    mutableListIterator.next()
//    mutableListIterator.remove()
//    mutableListIterator.next()
//    mutableListIterator.add("two")
//    mutableListIterator.next()
//    mutableListIterator.set("three")
//    println(numbers)
//}

//fun main() {
//    val words = "The quick brown fox jumps over the lazy dog".split(" ")
//    // 将列表转换为序列
//    val wordsSequence = words.asSequence()
//
//    val lengthsSequence = wordsSequence.filter { println("filter: $it"); it.length > 3 }
//            .map { println("map length: ${it.length}"); it.length }
//            .take(4)
//
//    println("Lengths of first 4 words longer than 3 chars")
//    // 末端操作：以列表形式获取结果。
//    println(lengthsSequence.toList())
//}

fun main() {
    val s = "123#"
    val ss = s.split("#")
    println(ss.size)
}
