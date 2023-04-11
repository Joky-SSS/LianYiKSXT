package com.lianyi.ksxt.utils

object Constants {
    const val TOKEN = "token"
    const val AUTHORIZATION = "Authorization"

    //学生信息确认状态 0未确认 1已确认,
    const val UNCONFIRM = 0;
    const val CONFIRMED = 1;

    //考生考试状态 11 已开启，已开始 0 未开启 1开启，未开始 2已结束,
    const val EXAM = 11;
    const val UNSTART = 0;
    const val STARTED = 1;
    const val FINISH = 2;
}