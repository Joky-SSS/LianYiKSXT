package com.lianyi.ksxt

import android.app.Application
import android.os.Environment
import com.blankj.utilcode.util.CrashUtils
import com.lianyi.ksxt.bean.Token
import com.lianyi.ksxt.utils.Constants
import com.tencent.mmkv.MMKV
import okhttp3.OkHttpClient
import rxhttp.RxHttpPlugins
import java.io.File
import java.util.concurrent.TimeUnit


class AppHolder : Application() {

    companion object {
        private const val PAD_CODE_KEY = "pade_code"
        private const val EXT_DIR = "lianyi"
        private const val CODE_FILE = "id.ca"
        private var app: AppHolder? = null

        @JvmStatic
        fun getApp() = app

        @JvmStatic
        fun getPadCode(): String {
            var padCode = MMKV.defaultMMKV().decodeString(PAD_CODE_KEY, "")
            if (!padCode.isNullOrBlank()) {
                return padCode
            }
            try {
                val dir =
                    File(Environment.getExternalStorageDirectory().absolutePath + File.separator + EXT_DIR + File.separator)
                dir.mkdirs()
                val file = File(dir.absolutePath + File.separator + CODE_FILE)
                if (file.exists()) {
                    padCode = file.readText()
                    if (!padCode.isNullOrBlank()) {
                        MMKV.defaultMMKV().encode(PAD_CODE_KEY, padCode)
                        return padCode
                    }
                } else {
                    file.createNewFile()
                }
                padCode = createPadCode()
                MMKV.defaultMMKV().encode(PAD_CODE_KEY, padCode)
                file.writeText(padCode)
            } catch (e: Exception) {
                e.printStackTrace()
                padCode = createPadCode()
                MMKV.defaultMMKV().encode(PAD_CODE_KEY, padCode)
            }
            return padCode ?: ""
        }

        private fun createPadCode(): String {
            return MutableList(8) {
                "QWERTYUIOPASDFGHJKLZXCVBNM".random()
            }.joinToString(separator = "")
        }
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        MMKV.initialize(this)
        CrashUtils.init(getExternalFilesDir("crash")!!)
        RxHttpPlugins.init(getDefaultOkHttpClient()).setDebug(true).setOnParamAssembly {
            val token: Token? = MMKV.defaultMMKV().decodeParcelable(
                Constants.TOKEN,
                Token::class.java, null
            )
            if (token != null) {
                it.addHeader(
                    Constants.AUTHORIZATION,
                    token.token_type + " " + token.access_token
                )
            }
        }
    }

    private fun getDefaultOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60 * 3, TimeUnit.SECONDS)
            .readTimeout(60 * 3, TimeUnit.SECONDS)
            .writeTimeout(60 * 3, TimeUnit.SECONDS)
            .hostnameVerifier { _, _ -> true } //忽略host验证
            .build()
    }
}