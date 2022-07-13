package com.soul.rn.multibundle.iface;

import com.soul.rn.multibundle.constant.RNException;

public interface Callback {
    void onSuccess(Object result);
    void onError(String errorMsg);
    default void onError(RNException exception) {};
}
