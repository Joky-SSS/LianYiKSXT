package com.lianyi.ksxt.bean;

import com.google.gson.annotations.SerializedName;

/**
  * @ClassName:      Response
  * @Description:
  * @Author:         Lee
  * @CreateDate:     2020/6/28 11:37
 */
public class Response<T> {
    private int code;
    @SerializedName(value = "message",alternate = {"msg"})
    private String message;
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
