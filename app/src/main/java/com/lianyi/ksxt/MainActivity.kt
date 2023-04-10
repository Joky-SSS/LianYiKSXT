package com.lianyi.ksxt

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ApiUtils.Api
import com.lianyi.ksxt.api.ApiService
import com.lianyi.ksxt.bean.State
import com.lianyi.ksxt.bean.Token
import com.lianyi.ksxt.bean.UserPadInfo
import com.lianyi.ksxt.databinding.ActivityMainBinding
import com.lianyi.ksxt.dialog.FinishTipDialog
import com.lianyi.ksxt.utils.Constants
import com.lianyi.ksxt.utils.PlayerUtil
import com.lianyi.ksxt.utils.toFormatTime
import com.lianyi.ksxt.utils.toast
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.LoadingPopupView
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.tbruyelle.rxpermissions2.RxPermissions
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import rxhttp.toAwait
import rxhttp.wrapper.param.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var state = State.NOT_CONFIRM
    private var userPadInfo: UserPadInfo = UserPadInfo()

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        RxPermissions(this).request(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).subscribe {
            if (it) {
                val padCode = AppHolder.getPadCode()
                binding.tvNum.text = "编码：$padCode"
                login()
            }
        }
    }

    private fun initView() {
        PlayerUtil.initPlayer(binding.videoPlayerTop)
        PlayerUtil.initPlayer(binding.videoPlayerBottom)
        binding.tvButton.setOnClickListener { buttonClick() }
    }

    /**
     * 登录
     */
    private fun login() {
        lifecycleScope.launch {
            val padCode = AppHolder.getPadCode()
            RxHttp.postForm(ApiService.API_LOGIN)
                .add("clinetId", "123456")
                .toFlow<Token>()
                .collectLatest { token ->
                    MMKV.defaultMMKV().encode(Constants.TOKEN, token)
                    getVideoUrl()
                    getScenesStatus()
                    startHeartBeat()
                }
        }
    }

    private fun startHeartBeat() {
        lifecycleScope.launch {
            val padCode = AppHolder.getPadCode()
            repeat(Int.MAX_VALUE) {
                RxHttp.postForm(ApiService.API_HEART_BEAT)
                    .add("padId", "123456")
                    .toFlow<Void>(false).collect()
                delay(60 * 1000)
            }
        }
    }

    /**
     * 获取视频播放地址
     */
    private fun getVideoUrl() {
        lifecycleScope.launch {
            val padCode = AppHolder.getPadCode()
            RxHttp.get(ApiService.API_GET_VIDEO_URL)
                .add("padId", "123456")
                .toFlow<String>()
                .collectLatest { url ->
                    binding.videoPlayerTop.setUp(url, true, "")
                    binding.videoPlayerTop.startPlayLogic()
                }
        }
    }

    private val dispatcher = Dispatchers.IO
    private val statusFlow = channelFlow {
        while (isActive) {
            val padCode = AppHolder.getPadCode()
            try {
                val status = RxHttp.get(ApiService.API_GET_SCENES_STATUS)
                    .add("padId", "123456")
                    .toAwait<Int>().await()
                send(status)
            } catch (e: Exception) {
            }
            delay(5000)
        }
    }.flowOn(dispatcher)

    /**
     * 轮询考试开启状态
     */
    private fun getScenesStatus() {
        lifecycleScope.launch {
            statusFlow.collectLatest { status ->
                if (status == 1) {
                    dispatcher.cancel()
                    getUserPadInfo()
                }
            }
        }
    }

    /**
     * 获取学生信息
     */
    private fun getUserPadInfo() {
        val padCode = AppHolder.getPadCode()
        lifecycleScope.launch {
            RxHttp.get(ApiService.API_GET_USER_PAD_INFO)
                .toFlowResponse<UserPadInfo>()
                .onStart { showLoading() }
                .onCompletion { hideLoading() }
                .catch {
                    toast(it.message)
                }.collectLatest {
                    setUserInfo(it)
                }
        }
    }

    /**
     * 更新界面学生信息
     */
    private fun setUserInfo(userPadInfo: UserPadInfo?) {
        userPadInfo?.let {
            this.userPadInfo = it
            binding.tvArea.text = it.roomName
            binding.tvTableNum.text = it.seatSeg
            binding.tvSubject.text = it.subjectName
            binding.tvTopic.text = it.question
            binding.tvLicense.text = it.studentCode
            binding.tvName.text = it.studentName
            binding.tvSchool.text = it.schoolName
            updateButton(it.confirm, it.examinationStatus)
        }
    }

    /**
     * 更新按钮状态
     */
    private fun updateButton(confirmState: Int, examinationStatus: Int) {
        when (examinationStatus) {
            Constants.UNSTART -> {
                if (confirmState == Constants.UNCONFIRM) {
                    state = State.NOT_CONFIRM
                    //未确认
                    binding.tvButton.text = "确认信息"
                    binding.tvButton.setBackgroundResource(R.color.color_button_submit)
                    binding.layoutConfirmTip.visibility = View.VISIBLE
                    binding.layoutTimeTip.visibility = View.INVISIBLE
                } else if (confirmState == Constants.CONFIRMED) {
                    state = State.CONFIRMED
                    //已确认
                    binding.tvButton.text = "我已核对信息无误"
                    binding.tvButton.setBackgroundResource(R.color.color_button_gray)
                    binding.layoutConfirmTip.visibility = View.INVISIBLE
                    binding.layoutTimeTip.visibility = View.INVISIBLE
                }
            }
            Constants.STARTED -> {
                state = State.EXAM
                binding.tvButton.text = "结束考试"
                binding.tvButton.setBackgroundResource(R.color.color_button_red)
                binding.layoutConfirmTip.visibility = View.INVISIBLE
                binding.layoutTimeTip.visibility = View.VISIBLE
                binding.tvLabelTime.setTextColor(getColor(R.color.color_button_red))
                binding.tvTime.setTextColor(getColor(R.color.color_button_red))
                binding.tvLabelTime.text = "考试时间："
                starTimeCount()
            }
            Constants.FINISH -> {
                state = State.FINISHED
                binding.tvButton.text = "已结束考试"
                binding.tvButton.setBackgroundResource(R.color.color_button_gray)
                binding.layoutConfirmTip.visibility = View.INVISIBLE
                binding.layoutTimeTip.visibility = View.VISIBLE
                binding.tvLabelTime.setTextColor(getColor(R.color.text_color_01))
                binding.tvTime.setTextColor(getColor(R.color.text_color_01))
                binding.tvLabelTime.text = "考试时长："
                finishTimeCount()
            }
        }
    }

    /**
     * 计算考试时间
     */
    private val countDispatcher = Dispatchers.Default
    private fun starTimeCount() {
        val timeStr = userPadInfo!!.realBeginTime
        val startTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).parse(timeStr).time
        val timeEmitter = channelFlow {
            while (isActive) {
                send(System.currentTimeMillis() - startTime)
                delay(1000)
            }
        }.flowOn(countDispatcher)

        lifecycleScope.launch {
            timeEmitter.collectLatest { second ->
                binding.tvTime.text = second.toFormatTime()
            }
        }
    }

    /**
     * 停止计算考试时间
     */
    private fun finishTimeCount() {
        countDispatcher.cancel()
    }

    /**
     * 确认信息弹窗
     */
    private fun showConfirmTip() {
        XPopup.Builder(this).asConfirm("", "确认信息无误") {
            confirmInfo()
        }.show()
    }

    /**
     * 确认信息
     */
    private fun confirmInfo() {
        lifecycleScope.launch {
            val padCode = AppHolder.getPadCode()
            RxHttp.postForm(ApiService.API_CONFIRM_INFO)
                .add("padid","123456")
                .toFlow<Void>()
                .collectLatest {
                    updateButton(Constants.CONFIRMED,Constants.UNSTART)
                }
        }

    }

    /**
     * 结束考试弹窗
     */
    private fun showFinishTip() {
        XPopup.Builder(this).asCustom(FinishTipDialog(this) {
            finishExam()
        }).show()
    }

    /**
     * 结束考试
     */
    private fun finishExam() {
        lifecycleScope.launch {
            val padCode = AppHolder.getPadCode()
            //sceneId 场次id ，studentId 学生id
            RxHttp.postForm(ApiService.API_FINISH_EXAM)
                .add("sceneId", userPadInfo!!.sceneId)
                .add("studentId", userPadInfo!!.studentId)
                .toFlow<Void>()
                .collectLatest {
                    updateButton(Constants.CONFIRMED, Constants.FINISH)
                }
        }
    }

    /**
     * 按钮点击事件
     */
    private fun buttonClick() {
        when (state) {
            State.NOT_CONFIRM -> {
                showConfirmTip()
            }
            State.CONFIRMED -> {

            }
            State.EXAM -> {
                showFinishTip()
            }
            State.FINISHED -> {

            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.videoPlayerTop.onVideoPause()
        binding.videoPlayerBottom.onVideoPause()
    }

    override fun onResume() {
        super.onResume()
        binding.videoPlayerTop.onVideoResume()
        binding.videoPlayerBottom.onVideoResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }

    private var loadingDialog: LoadingPopupView? = null

    private fun showLoading() {
        if (loadingDialog == null) {
            loadingDialog = XPopup.Builder(this)
                .dismissOnBackPressed(false)
                .dismissOnTouchOutside(false)
                .isDestroyOnDismiss(false)
                .asLoading()
        }
        loadingDialog?.show()
    }

    private fun hideLoading() {
        loadingDialog?.dismiss()
    }

    private inline fun <reified T> RxHttpFormParam.toFlow(toastError: Boolean = true): Flow<T> {
        return toFlowResponse<T>()
            .onStart { showLoading() }
            .onCompletion { hideLoading() }
            .catch {
                if (toastError) toast(it.message)
            }
    }

    private inline fun <reified T> RxHttpNoBodyParam.toFlow(toastError: Boolean = true): Flow<T> {
        return toFlowResponse<T>()
            .onStart { showLoading() }
            .onCompletion { hideLoading() }
            .catch {
                if (toastError) toast(it.message)
            }
    }
}