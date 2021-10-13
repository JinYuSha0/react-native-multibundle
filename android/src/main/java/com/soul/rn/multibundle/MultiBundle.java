package com.soul.rn.multibundle;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactPackage;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.ViewManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.soul.rn.multibundle.constant.EventName;
import com.soul.rn.multibundle.constant.StorageKey;
import com.soul.rn.multibundle.entity.Component;
import com.soul.rn.multibundle.entity.ComponentSetting;
import com.soul.rn.multibundle.iface.Callback;
import com.soul.rn.multibundle.entity.MyResponse;
import com.soul.rn.multibundle.iface.ReactNativeHostHolder;
import com.soul.rn.multibundle.utils.FileUtil;
import com.soul.rn.multibundle.utils.Preferences;
import com.soul.rn.multibundle.utils.RNConvert;
import com.soul.rn.multibundle.utils.RequestManager;
import com.soul.rn.multibundle.utils.download.DownloadProgressListener;
import com.soul.rn.multibundle.utils.download.DownloadTask;

import net.lingala.zip4j.ZipFile;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.soul.rn.multibundle.utils.MathUtil.getRandomString;

public class MultiBundle implements ReactPackage {
  public static final String PREFIX = getRandomString(6) + "_";
  public static String MULTI_BUNDLE_SERVER_HOST;
  public static String DEFAULT_MODULE_NAME;
  public static ReactNativeHostHolder mReactNativeHostHolder;
  public static Class<ReactRootView> mReactRootViewClazz = ReactRootView.class;
  public static Context mContext;

  public MultiBundle(Context context, String defaultModuleName, String multiBundleSeverHost) {
    DEFAULT_MODULE_NAME = defaultModuleName;
    MULTI_BUNDLE_SERVER_HOST = multiBundleSeverHost;
    mContext = context;
    Preferences.init(mContext);
    RNDBHelper.init(mContext);
    initDB(mContext);
  }

  public static void setReactNativeHostHolder(ReactNativeHostHolder reactNativeHostHolder) {
    if (reactNativeHostHolder == null) return;;
    mReactNativeHostHolder = reactNativeHostHolder;
    if (mReactNativeHostHolder.createReactContextInBackground() && reactNativeHostHolder.getReactNativeHost() != null) {
      ReactInstanceManager reactInstanceManager = reactNativeHostHolder.getReactNativeHost().getReactInstanceManager();
      Boolean isDev = reactNativeHostHolder.getReactNativeHost().getUseDeveloperSupport();
      if (!isDev && !reactInstanceManager.hasStartedCreatingInitialContext()) {
        reactInstanceManager.createReactContextInBackground();
      }
    }
  }

  public static void setReactRootView(Class<ReactRootView> reactRootViewClass) {
    mReactRootViewClazz = reactRootViewClass;
  }

  public static void openComponent(Activity activity, String moduleName, @Nullable Integer statusBarMode) {
    Intent intent = new Intent(activity, RNActivity.class);
    Bundle params = new Bundle();
    params.putBoolean("goBack", true);
    Bundle bundle = createBundle(moduleName, statusBarMode, params);
    intent.putExtras(bundle);
    activity.startActivity(intent);
  }

  public static Bundle createBundle(String moduleName, @Nullable Integer statusBarMode, @Nullable Bundle params) {
    Bundle bundle = new Bundle();
    bundle.putString("moduleName", moduleName);
    bundle.putInt("statusBarMode", statusBarMode == null ? 0 : statusBarMode);
    if (params != null) bundle.putAll(params);
    return bundle;
  }

  public static void initDB(Context ctx) {
    Boolean isInit = (Boolean) Preferences.getValueByKey(StorageKey.INIT_DB,Boolean.class);
    if (!isInit) {
      try {
        String json = FileUtil.readFileFromAssets(ctx,"appSetting.json");
        JSONObject jsonObject = new JSONObject(json);
        JSONObject componentsObj = jsonObject.getJSONObject("components");
        Long publishTime = (Long) jsonObject.get("timestamp");
        Iterator iterator = componentsObj.keys();
        ArrayList<ContentValues> contentValuesArr = new ArrayList<>();
        while (iterator.hasNext()) {
          String key = (String) iterator.next();
          JSONObject value = (JSONObject) componentsObj.get(key);
          String hash = (String) value.get("hash");
          String componentName = null;
          try {
            componentName = (String) value.get("componentName");
          } catch (Exception ignore) {}
          String filePath = "assets://" + key;
          contentValuesArr.add(RNDBHelper.createContentValues(key,componentName,0,hash,filePath,publishTime));
        }
        RNDBHelper.insertRows(contentValuesArr);
        Preferences.storageKV(StorageKey.INIT_DB,true);
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  public static void checkUpdate(Context ctx, @Nullable Callback callback) {
    try {
      final File downloadPath = ctx.getExternalFilesDir(null);
      final Context mContext = ctx;
      final HashMap<String, RNDBHelper.Result> componentMap = RNDBHelper.selectAllMap();
      sendEventInner(EventName.CHECK_UPDATE_START, null);
      HashMap<String, String> params = new HashMap<>();
      params.put("platform", "android");
      params.put("commonHash", componentMap.get("common").Hash);
      RequestManager.getInstance(ctx).Get(MULTI_BUNDLE_SERVER_HOST + "/rn/checkUpdate", params, new RequestManager.RequestCallBack<MyResponse<ArrayList<Component>>, MyResponse<Object>>() {
        @Override
        public void onFailure(MyResponse<Object> error, Exception exception) {
          String cause;
          if (exception != null) {
            cause = exception.getMessage();
          } else {
            cause = error.message;
          }
          if (callback != null) callback.onError(cause);
          sendEventInner(EventName.CHECK_UPDATE_FAILURE, cause);
        }

        @Override
        public void onSuccess(MyResponse<ArrayList<Component>> result) {
          if (callback != null) callback.onSuccess(result);
          sendEventInner(EventName.CHECK_UPDATE_SUCCESS, result);
          for (int i = 0; i < result.data.size(); i++) {
            final Component newComponent = result.data.get(i);
            final RNDBHelper.Result oldComponent = componentMap.get(newComponent.componentName);
            // 如果hash不相同 且版本大于当前版本 下载新的bundle包
            if (!oldComponent.Hash.equals(newComponent.hash) && newComponent.version > oldComponent.Version) {
              sendEventInner(EventName.CHECK_UPDATE_DOWNLOAD_NEWS,newComponent);
              new Thread(new DownloadTask(
                mContext,
                newComponent.downloadUrl,
                String.format("%s-%s.zip",newComponent.componentName,newComponent.hash),
                downloadPath,
                new DownloadProgressListener() {
                  @Override
                  public void onDownloadSize(int downloadedSize, int fileSize) {
                    WritableMap progress = Arguments.createMap();
                    progress.putString("componentName",newComponent.componentName);
                    progress.putDouble("progress", (double) downloadedSize / (double) fileSize);
                    sendEventInner(EventName.CHECK_UPDATE_DOWNLOAD_PROGRESS,progress);
                  }

                  @Override
                  public void onDownloadFailure(Exception e) {
                    sendEventInner(EventName.CHECK_UPDATE_DOWNLOAD_NEWS_FAILURE,e.getMessage());
                  }

                  @Override
                  public void onDownLoadComplete(File originFile) {
                    File file = new File(String.format("%s/%s",downloadPath.getAbsolutePath(),newComponent.hash));
                    try {
                      originFile.renameTo(file);
                      String dest = String.format("%s/%s/",downloadPath.getAbsolutePath(),newComponent.componentName);
                      ZipFile zipFile = new ZipFile(file);
                      zipFile.extractAll(dest);
                      setupComponent(ctx,String.format("%s%s",dest,newComponent.hash),newComponent.version);
                      sendEventInner(EventName.CHECK_UPDATE_DOWNLOAD_NEWS_SUCCESS,newComponent);
                    } catch (Exception e) {
                      e.printStackTrace();
                    } finally {
                      file.delete();
                    }
                  }
                })
              ).start();
            }
          }
        }

        @Override
        public Type getType(Boolean isFailure) {
          if (!isFailure) {
            return new TypeToken<MyResponse<ArrayList<Component>>>() {}.getType();
          } else {
            return new TypeToken<MyResponse<Object>>() {}.getType();
          }
        }
      }).request();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void setupComponent(Context ctx,String componentDir,Integer version) {
    try {
      File settingJSONFile = new File(String.format("%s/%s",componentDir,"setting.json"));
      String settingJSON = FileUtil.readFile(settingJSONFile);
      ComponentSetting componentSetting = new Gson().fromJson(settingJSON, new TypeToken<ComponentSetting>() {}.getType());
      String bundleFilePath = String.format("%s/%s", componentDir, componentSetting.bundleName);
      if (FileUtil.fileExists(bundleFilePath)) {
        String saveBundleFilePath = bundleFilePath.replaceAll(ctx.getExternalFilesDir(null).getAbsolutePath() + "/","file://");
        RNDBHelper.insertRow(RNDBHelper.createContentValues(
          componentSetting.bundleName,
          componentSetting.componentName,
          version,
          componentSetting.hash,
          saveBundleFilePath,
          componentSetting.timestamp
        ));
        sendEventInner(EventName.CHECK_UPDATE_DOWNLOAD_NEWS_APPLY,componentSetting.componentName);
        // 立即应用新模块
        if (!RNActivity.isExistsModule(componentSetting.componentName)) {
          RNBundleLoader.loadScriptFromFile(ctx,RNBundleLoader.getCatalystInstance(mReactNativeHostHolder.getReactNativeHost()),bundleFilePath,false);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void sendEvent(String eventName, Object eventData) {
    ReactContext reactContext = mReactNativeHostHolder.getReactNativeHost().getReactInstanceManager().getCurrentReactContext();
    eventData = RNConvert.convert(eventData);
    if (reactContext != null) {
      reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName,eventData);
    }
  }

  public static void sendEventInner(String eventName, Object eventData) {
    sendEvent(PREFIX+eventName,eventData);
  }

  @NonNull
  @Override
  public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
    List<NativeModule> modules = new ArrayList<>();
    modules.add(new MultiBundleModule(reactContext));
    return modules;
  }

  @NonNull
  @Override
  public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
    return Collections.emptyList();
  }
}
