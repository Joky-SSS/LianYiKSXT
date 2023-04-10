package com.lianyi.ksxt.api

import rxhttp.wrapper.annotation.DefaultDomain

object ApiService {
    //默认接口地址
    @DefaultDomain //设置为默认域名
    const val API_BASE = "http://192.168.2.179:8088/"
    //登录接口
    const val API_LOGIN = "exam/sys/iportal/users/clinet/login"
    //获取视频地址
    const val API_GET_VIDEO_URL = "monitor/cameras/hk/examination/url"
    //获取学生信息
    const val API_GET_USER_PAD_INFO = "exam/base/students/seat/info"
    //获取场次状态
    const val API_GET_SCENES_STATUS = "exam/site/scenes/status"
    //结束考试
    const val API_FINISH_EXAM = "exam/base/central /student/end"
    //确认信息
    const val API_CONFIRM_INFO = "exam/base/students/info/confirm"
    //心跳接口
    const val API_HEART_BEAT = "exam/external/pad/heart"
}