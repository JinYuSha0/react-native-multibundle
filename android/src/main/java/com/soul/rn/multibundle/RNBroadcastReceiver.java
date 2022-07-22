package com.soul.rn.multibundle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.soul.rn.multibundle.iface.Callback;

public class RNBroadcastReceiver extends BroadcastReceiver {
    private Callback mCallback;

    public RNBroadcastReceiver(Callback callback) {
        mCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mCallback.onSuccess(this);
    }
}
