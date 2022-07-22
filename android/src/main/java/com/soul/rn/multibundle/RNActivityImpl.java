package com.soul.rn.multibundle;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.facebook.react.ReactInstanceEventListener;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.CatalystInstance;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.modules.core.PermissionAwareActivity;
import com.facebook.react.modules.core.PermissionListener;
import com.jaeger.library.StatusBarUtil;
import com.soul.rn.multibundle.constant.ComponentType;
import com.soul.rn.multibundle.constant.StatusBar;
import com.soul.rn.multibundle.iface.Callback;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public abstract class RNActivityImpl extends androidx.fragment.app.FragmentActivity implements DefaultHardwareBackBtnHandler, PermissionAwareActivity {
  protected boolean bundleLoaded = false;
  private ReactNativeHost mReactNativeHost;
  private boolean isDev;
  private RNActivityDelegate mDelegate;
  protected String reallyFilePath = null;
  private static ArrayList<RNActivityImpl> mActivityList = new ArrayList();

  synchronized public static RNActivityImpl getActivity() {
    if (mActivityList.size() > 0) {
      for (int i = mActivityList.size() - 1; i >= 0; i--) {
        RNActivityImpl cActivity = mActivityList.get(i);
        if (!cActivity.isFinishing()) {
          return cActivity;
        }
      }
    }
    return null;
  }

  synchronized public static Boolean isExistsModule(String moduleName) {
    for (int i = mActivityList.size() - 1; i >= 0; i--) {
      RNActivityImpl cActivity = mActivityList.get(i);
      if (moduleName.equals(cActivity.getBundle().moduleName)) {
        return true;
      }
    }
    return false;
  }

  protected RNActivityImpl() {
    // 创建delegate
    mDelegate = createRNActivityDelegate();
    // 初始化ReactNativeHost
    if (mReactNativeHost == null) {
      mReactNativeHost = getReactNativeHost();
    }
    if (mReactNativeHost != null) {
      isDev = mReactNativeHost.getUseDeveloperSupport();
    }
  }

  protected RNActivityDelegate createRNActivityDelegate() {
    if (mDelegate == null) {
      mDelegate = new RNActivityDelegate(this, null) {
        @Override
        protected ReactRootView createRootView() {
          if (MultiBundle.mReactRootViewClazz != null) {
            try {
              Constructor constructor = MultiBundle.mReactRootViewClazz.getDeclaredConstructor(Context.class);
              constructor.setAccessible(true);
              return (ReactRootView) constructor.newInstance(RNActivityImpl.this);
            } catch (Exception ignore) {}
          }
          return new ReactRootView(RNActivityImpl.this);
        }
      };
    }
    return mDelegate;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // ReactNativeHost为空直接关闭
    if (mReactNativeHost == null) {
      this.finish();
      Log.e(BuildConfig.LOG_TAG, "Please set `MultiBundle.setReactNativeHostHolder` before open a activity!");
      return;
    }
    mActivityList.add(this);

    RNBundle innerBundle = getBundle();
    Intent intent = getIntent();
    if (intent != null) {
      Uri uri = intent.getData();
      if (uri != null) {
        String scheme = uri.getScheme();
        String host = uri.getHost();
        String query = uri.getQuery();
        innerBundle.setModuleName(host);
        // todo
//        getBundle().setParams(null);
        Log.d("deepLink", "scheme: " + scheme + " host: " + host + " query: " + query);
      }
    }

    // 设置StatusBar样式
    setStatusBar(innerBundle.params);
    mDelegate.onCreate(innerBundle.toBundle());
    final Activity currActivity = this;
    ReactInstanceManager manager = mReactNativeHost.getReactInstanceManager();
    if (isDev) {
      initView();
    } else {
      // 非开发模式走拆包流程
      if (!MultiBundle.mReactNativeHostHolder.createReactContextInBackground() && (!manager.hasStartedCreatingInitialContext() || RNBundleLoader.getCatalystInstance(mReactNativeHost) == null)) {
        if (manager.hasStartedCreatingInitialContext()) {
          mReactNativeHost.getReactInstanceManager().destroy();
        }
        manager.addReactInstanceEventListener(new ReactInstanceEventListener() {
          @Override
          public void onReactContextInitialized(ReactContext context) {
            RNDBHelper.Result result = RNDBHelper.selectByComponentName("Bootstrap");
            RNBundleLoader.loadScript(context,RNBundleLoader.getCatalystInstance(mReactNativeHost),result.FilePath,false);
            load();
            manager.removeReactInstanceEventListener(this);
          }
        });
        mReactNativeHost.getReactInstanceManager().createReactContextInBackground();
      } else {
        if (!MultiBundle.BootstrapLoaded) {
          RNActivityImpl self = this;
          Callback callback = new Callback() {
            @Override
            public void onSuccess(Object result) {
              load();
              self.unregisterReceiver((BroadcastReceiver) result);
            }

            @Override
            public void onError(String errorMsg) {
            }
          };
          IntentFilter intentFilter = new IntentFilter();
          intentFilter.addAction("RN_BOOTSTRAP");
          this.registerReceiver(new RNBroadcastReceiver(callback), intentFilter);
        } else {
          load();
        }
      }
    }
  }

  protected void load() {
    loadScript(new LoadScriptListener() {
      @Override
      public void onLoadComplete(boolean success, String bundlePath) {
        bundleLoaded = success;
        if (success) {
          runApp(bundlePath);
        } else {
          RNActivityImpl.this.finish();
        }
      }
    });
  }

  protected void loadScript(LoadScriptListener loadScriptListener) {
    final RNBundle innerBundle = getBundle();
    String moduleName = innerBundle.moduleName;
    RNDBHelper.Result result = RNDBHelper.selectByComponentName(moduleName);
    CatalystInstance instance = RNBundleLoader.getCatalystInstance(mReactNativeHost);
    if (result == null || result.ComponentType != ComponentType.Default.getIndex()) {
      // 未曾安装的模块或者无法打开的模块
      loadScriptListener.onLoadComplete(false,null);
    } else {
      RNBundleLoader.loadScript(getApplicationContext(),instance,result.FilePath,false);
      loadScriptListener.onLoadComplete(true,result.FilePath);
    }
  }

  protected void runApp(String bundlePath) {
    initView();
  }

  protected void initView() {
    RNBundle innerBundle = getBundle();
    if (innerBundle.moduleName != null && !"".equals(innerBundle.moduleName)) {
      mDelegate.loadApp(innerBundle.moduleName);
    }
  }

  protected void setStatusBar(@Nullable Bundle bundle) {
    if (bundle == null) return;
    Integer statusBarMode = bundle.getInt("statusBarMode",0);
    // 沉浸式状态栏
    if ((statusBarMode & StatusBar.transparent) > 0) {
      StatusBarUtil.setTransparent(this);
    }
    // 设置黑色字体
    if ((statusBarMode & StatusBar.darkMode) > 0) {
      StatusBarUtil.setLightMode(this);
      StatusBarUtil.setTranslucent(this);
    }
    // 设置白色字体
    if ((statusBarMode & StatusBar.lightMode) > 0) {
      StatusBarUtil.setDarkMode(this);
      StatusBarUtil.setTranslucent(this);
    }
  }

  protected final void loadApp(String appKey) {
    mDelegate.loadApp(appKey);
  }

  @Override
  protected void onPause() {
    super.onPause();
    mDelegate.onPause();
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  @Override
  protected void onResume() {
    super.onResume();
    mDelegate.onResume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mDelegate.onDestroy();
    mActivityList.remove(this);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    mDelegate.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    return mDelegate.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    return mDelegate.onKeyUp(keyCode, event) || super.onKeyUp(keyCode, event);
  }

  @Override
  public boolean onKeyLongPress(int keyCode, KeyEvent event) {
    return mDelegate.onKeyLongPress(keyCode, event) || super.onKeyLongPress(keyCode, event);
  }

  @Override
  public void onBackPressed() {
    if (!mDelegate.onBackPressed()) {
      super.onBackPressed();
    }
  }

  @Override
  public void invokeDefaultOnBackPressed() {
    super.onBackPressed();
  }

  @Override
  public void onNewIntent(Intent intent) {
    if (!mDelegate.onNewIntent(intent)) {
      super.onNewIntent(intent);
    }
  }

  @Override
  public void requestPermissions(
          String[] permissions,
          int requestCode,
          PermissionListener listener) {
    mDelegate.requestPermissions(permissions, requestCode, listener);
  }

  @Override
  public void onRequestPermissionsResult(
          int requestCode,
          String[] permissions,
          int[] grantResults) {
    mDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  public abstract RNBundle getBundle();

  public static interface LoadScriptListener {
    public void onLoadComplete(boolean success, String bundlePath);
  }

  protected final ReactNativeHost getReactNativeHost() {
    return mDelegate.getReactNativeHost();
  }

  protected final ReactInstanceManager getReactInstanceManager() {
    return mDelegate.getReactInstanceManager();
  }
}
