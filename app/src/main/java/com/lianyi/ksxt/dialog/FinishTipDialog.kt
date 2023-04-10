package com.lianyi.ksxt.dialog

import android.content.Context
import com.lianyi.ksxt.R
import com.lianyi.ksxt.databinding.DialogFinishTipBinding
import com.lxj.xpopup.core.CenterPopupView

class FinishTipDialog(content:Context,var confirmListener:()->Unit):CenterPopupView(content) {
    private lateinit var binding: DialogFinishTipBinding
    override fun getImplLayoutId() = R.layout.dialog_finish_tip
    override fun onCreate() {
        super.onCreate()
        binding = DialogFinishTipBinding.bind(popupImplView)
        binding.tvCancel.setOnClickListener { dismiss() }
        binding.tvConfirm.setOnClickListener { confirmListener.invoke() }
    }
}