package com.soul.rn.multibundle;

import android.app.Activity;
import android.os.Bundle;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactDelegate;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactRootView;

import java.lang.reflect.Field;

public class RNActivityDelegate extends ReactActivityDelegate {
  private ReactDelegate mReactDelegate;
  private ReactNativeHost mReactNativeHost;

  public RNActivityDelegate(Activity activity, String mainComponentName) {
    super(activity, mainComponentName);
    mReactNativeHost = getReactNativeHost();
  }

  public RNActivityDelegate(ReactActivity activity, String mainComponentName) {
    super(activity, mainComponentName);
    mReactNativeHost = getReactNativeHost();
  }

  @Override
  protected void onCreate(Bundle bundle) {
    String moduleName = bundle.getString("moduleName");
    Bundle params = bundle.getBundle("params");
   if (mReactDelegate == null) {
      mReactDelegate = new ReactDelegate(this.getPlainActivity(), mReactNativeHost, moduleName, params) {
        protected ReactRootView createRootView() {
          ReactRootView reactRootView = RNActivityDelegate.this.createRootView();
          reactRootView.setIsFabric(BuildConfig.IS_NEW_ARCHITECTURE_ENABLED);
          return reactRootView;
        }
      };
    }

    try {
      // 反射替换父类属性
      Field privateReactDelegateField = ReactActivityDelegate.class.getDeclaredField("mReactDelegate");
      privateReactDelegateField.setAccessible(true);
      privateReactDelegateField.set(this, mReactDelegate);
    } catch (Exception err) {
      err.printStackTrace();
    }
  }

  @Override
  protected boolean isConcurrentRootEnabled() {
    return BuildConfig.IS_NEW_ARCHITECTURE_ENABLED;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void loadApp(String appKey) {
    super.loadApp(appKey);
  }

  @Override
  protected ReactNativeHost getReactNativeHost() {
    if (MultiBundle.mReactNativeHostHolder != null) {
      return MultiBundle.mReactNativeHostHolder.getReactNativeHost();
    }
    return null;
  }
}
