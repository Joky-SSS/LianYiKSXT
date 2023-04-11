package com.lianyi.ksxt.dialog

import android.content.Context
import com.lianyi.ksxt.R
import com.lianyi.ksxt.databinding.DialogFinishTipBinding
import com.lxj.xpopup.core.CenterPopupView

class TipDialog(
    context: Context,
    private val content: String = "",
    var confirmListener: () -> Unit
) :
    CenterPopupView(context) {
    private lateinit var binding: DialogFinishTipBinding
    override fun getImplLayoutId() = R.layout.dialog_finish_tip
    override fun onCreate() {
        super.onCreate()
        binding = DialogFinishTipBinding.bind(popupImplView)
        if (content.isNotEmpty()) binding.tvContent.text = content
        binding.tvCancel.setOnClickListener { dismiss() }
        binding.tvConfirm.setOnClickListener {
            dismiss()
            confirmListener.invoke()
        }
    }
}