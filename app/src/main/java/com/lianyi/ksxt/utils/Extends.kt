package com.lianyi.ksxt.utils

import android.app.Activity
import android.widget.Toast

fun Activity.toast(message:String?) {
    message?.let {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }
}

fun Long.toFormatTime(): String {
    val hour = this / (60 * 60 * 1000)
    val minute = (this-hour*60 * 60 * 1000) / (60 * 1000)
    val second = (this-hour*60 * 60 * 1000 - minute * 60 * 1000) / 1000
    return hour.toPreAppendZeroString()+":"+minute.toPreAppendZeroString()+":"+second.toPreAppendZeroString()
}

private fun Long.toPreAppendZeroString():String{
    if (this == 0L) return "00"
    if (this < 10) return "0$this"
    return this.toString()
}