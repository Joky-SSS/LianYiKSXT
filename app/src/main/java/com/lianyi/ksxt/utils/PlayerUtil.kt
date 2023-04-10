package com.lianyi.ksxt.utils

import android.view.View
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.model.VideoOptionModel
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer


object PlayerUtil {
    fun initPlayer(videoPlayer :StandardGSYVideoPlayer) {
        var videoOptionModel =
            VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "udp")
        val list: MutableList<VideoOptionModel> = ArrayList()
        list.add(videoOptionModel)
        videoOptionModel =
            VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_flags", "prefer_tcp")
        list.add(videoOptionModel)
        videoOptionModel = VideoOptionModel(
            IjkMediaPlayer.OPT_CATEGORY_FORMAT,
            "allowed_media_types", "video"
        ) //根据媒体类型来配置

        list.add(videoOptionModel)
        videoOptionModel = VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 20000)
        list.add(videoOptionModel)
        videoOptionModel = VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "buffer_size", 1316)
        list.add(videoOptionModel)
        videoOptionModel = VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "infbuf", 1) // 无限读

        list.add(videoOptionModel)
        videoOptionModel =
            VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100)
        list.add(videoOptionModel)
        videoOptionModel = VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 10240)
        list.add(videoOptionModel)
        videoOptionModel = VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1)
        list.add(videoOptionModel)
        //  关闭播放器缓冲，这个必须关闭，否则会出现播放一段时间后，一直卡主，控制台打印 FFP_MSG_BUFFERING_START
        videoOptionModel =
            VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0)
        list.add(videoOptionModel)
        GSYVideoManager.instance().optionModelList = list
        //增加title
        videoPlayer.titleTextView.visibility = View.GONE
        //设置返回键
        videoPlayer.backButton.visibility = View.GONE
        //设置全屏按键功能,这是使用的是选择屏幕，而不是全屏
        videoPlayer.fullscreenButton.visibility = View.GONE
        //是否可以滑动调整
        videoPlayer.setIsTouchWiget(false)
        videoPlayer.startButton.visibility = View.GONE
    }
}