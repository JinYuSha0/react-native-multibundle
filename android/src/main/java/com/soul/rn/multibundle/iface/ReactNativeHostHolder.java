package com.soul.rn.multibundle.iface;

import android.content.Intent;

import com.facebook.react.ReactNativeHost;

public interface ReactNativeHostHolder {
  ReactNativeHost getReactNativeHost();

  Boolean createReactContextInBackground();

  void onNewIntent(Intent intent);
}
