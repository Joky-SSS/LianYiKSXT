package com.lianyi.ksxt

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
import rxhttp.asFlow
import rxhttp.repeat
import rxhttp.toAwait
import rxhttp.wrapper.param.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var state = State.NOT_START
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
                    startHeartBeat()
//                    getScenesStatus()
                }
        }
    }

    private fun startHeartBeat() {
        lifecycleScope.launch {
            val padCode = AppHolder.getPadCode()
            RxHttp.postForm(ApiService.API_HEART_BEAT)
                .add("padId", "123456")
                .toAwait<String>()
                .repeat(Long.MAX_VALUE, 60 * 1000)
                .asFlow().collect()
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

    /**
     * 轮询考试开启状态
     */
    private fun getScenesStatus() {
        lifecycleScope.launch {
            val padCode = AppHolder.getPadCode()
            try {
                val status = RxHttp.get(ApiService.API_GET_SCENES_STATUS)
                    .add("padId", "123456")
                    .toAwait<Int>()
                    .asFlow().collectLatest {
                        if (it == 1) {
                            getUserPadInfo()
                            getVideoUrl()
                        } else {
                            toast("暂无考试，请等待考场管理员开启考试")
                        }
                    }
            } catch (e: Exception) {
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
                .add("padId", "123456")
                .toFlow<UserPadInfo>()
                .collectLatest {
                    setUserInfo(it)
                }
        }
    }

    private fun repeatRequestUserInfo() {
        val padCode = AppHolder.getPadCode()
        lifecycleScope.launch {
            RxHttp.get(ApiService.API_GET_USER_PAD_INFO)
                .add("padId", "123456")
                .toAwait<UserPadInfo>()
                .repeat(Long.MAX_VALUE, 5 * 1000) { userPadInfo ->
                    userPadInfo.realBeginTime.isNullOrBlank()
                }.asFlow()
                .collectLatest {
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
        val timeStr = userPadInfo.realBeginTime ?: return
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
                .add("padId", "123456")
                .toFlow<String>()
                .collectLatest {
                    updateButton(Constants.CONFIRMED, Constants.UNSTART)
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
                .toFlow<String>()
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
            State.NOT_START -> {
                getScenesStatus()
            }
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
        return toAwait<T>()
            .asFlow()
            .onStart { showLoading() }
            .onCompletion { hideLoading() }
            .catch {
                if (toastError) toast(it.message)
            }
    }

    private inline fun <reified T> RxHttpNoBodyParam.toFlow(toastError: Boolean = true): Flow<T> {
        return toAwait<T>()
            .asFlow()
            .onStart { showLoading() }
            .onCompletion { hideLoading() }
            .catch {
                if (toastError) toast(it.message)
            }
    }
}