// MultiBundleModule.java

package com.soul.rn.multibundle;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.soul.rn.multibundle.iface.Callback;
import com.soul.rn.multibundle.utils.RNConvert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MultiBundleModule extends ReactContextBaseJavaModule {
    public static final String NAME = "MultiBundle";

    public MultiBundleModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public Map<String, Object> getConstants() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("prefix", MultiBundle.PREFIX);
        return map;
    }

    @ReactMethod
    public void openComponent(String moduleName, @Nullable Integer statusBarMode) {
        Activity activity = getCurrentActivity();
        if (activity == null) return;
        MultiBundle.openComponent(activity, moduleName, statusBarMode);
    }

    @ReactMethod
    public void getAllComponent(Promise promise) {
        WritableArray array = Arguments.createArray();
        ArrayList<RNDBHelper.Result> results = RNDBHelper.selectAll();
        for (RNDBHelper.Result result : results) {
            array.pushMap((WritableMap) RNConvert.convert(result));
        }
        promise.resolve(array);
    }

    @ReactMethod
    public void checkUpdate(Promise promise) {
        MultiBundle.checkUpdate(getCurrentActivity(), new Callback() {
            @Override
            public void onSuccess(Object result) {
                promise.resolve(RNConvert.convert(result));
            }

            @Override
            public void onError(String errorMsg) {
                promise.reject(null, errorMsg);
            }
        });
    }

    @ReactMethod
    public void goBack() {
        getCurrentActivity().finish();
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }
}
