package com.soul.rn.multibundle.iface;

import com.facebook.react.ReactNativeHost;

public interface ReactNativeHostHolder {
  ReactNativeHost getReactNativeHost();

  Boolean createReactContextInBackground();
}
