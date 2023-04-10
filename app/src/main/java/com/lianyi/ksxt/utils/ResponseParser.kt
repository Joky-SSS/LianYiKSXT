package com.lianyi.ksxt.utils

import okhttp3.Response
import rxhttp.wrapper.annotation.Parser
import rxhttp.wrapper.entity.ParameterizedTypeImpl
import rxhttp.wrapper.exception.ParseException
import rxhttp.wrapper.parse.TypeParser
import rxhttp.wrapper.utils.Converter.convert
import java.io.IOException
import java.lang.reflect.Type
import java.net.HttpURLConnection

/**
 * @ClassName: ResponseParser
 * @Description:
 * @Author: Lee
 * @CreateDate: 2020/6/28 11:16
 */
@Parser(name = "Response")
open class ResponseParser<T> : TypeParser<T> {
    //注意，以下两个构造方法是必须的
    protected constructor() : super()
    constructor(type: Type?) : super(type!!) {}

    @Throws(IOException::class)
    override fun onParse(response: Response): T? {
        //获取泛型类型
        val type: Type = ParameterizedTypeImpl.get(com.lianyi.ksxt.bean.Response::class.java, *types)
        val data = convert<com.lianyi.ksxt.bean.Response<T>>(response, type)
        //获取data字段
        var t = data.data
        //网络成功
        return if (response.code == HttpURLConnection.HTTP_OK) {
            if (data.code == 0) {
                if (t == null) {
                    t = "" as T
                    t
                } else {
                    t
                }
            } else {
                throw ParseException(
                    data.code.toString(),
                    data.message,
                    response
                )
            }
        } else { //网络失败
            throw ParseException(data.code.toString(), data.message, response)
        }
    }
}