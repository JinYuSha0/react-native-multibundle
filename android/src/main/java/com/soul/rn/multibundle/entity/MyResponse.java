package com.soul.rn.multibundle.entity;

import androidx.annotation.Keep;

@Keep
public class MyResponse<T> {
    public T data;
    public int code;
    public boolean success;
    public String message;

    public MyResponse(T data, int code, boolean success, String message) {
        this.data = data;
        this.code = code;
        this.success = success;
        this.message = message;
    }
}
