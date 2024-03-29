package com.soul.rn.multibundle;

import static com.soul.rn.multibundle.utils.MathUtil.getRandomString;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.ViewManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.soul.rn.multibundle.component.CustomInputManager;
import com.soul.rn.multibundle.component.ProgressBarDialog;
import com.soul.rn.multibundle.constant.ComponentType;
import com.soul.rn.multibundle.constant.EventName;
import com.soul.rn.multibundle.constant.StorageKey;
import com.soul.rn.multibundle.constant.UpdateStage;
import com.soul.rn.multibundle.entity.Component;
import com.soul.rn.multibundle.entity.ComponentSetting;
import com.soul.rn.multibundle.entity.MyResponse;
import com.soul.rn.multibundle.iface.Callback;
import com.soul.rn.multibundle.iface.ReactNativeHostHolder;
import com.soul.rn.multibundle.utils.AppVersionInfoUtil;
import com.soul.rn.multibundle.utils.FileUtil;
import com.soul.rn.multibundle.utils.RNConvert;
import com.soul.rn.multibundle.utils.RequestManager;
import com.soul.rn.multibundle.utils.SPUtil;
import com.soul.rn.multibundle.utils.ThreadPool;
import com.soul.rn.multibundle.utils.download.DownloadProgressListener;
import com.soul.rn.multibundle.utils.download.DownloadTask;

import net.lingala.zip4j.ZipFile;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiBundle implements ReactPackage {
  public static final String PREFIX = getRandomString(6) + "_";
  public static String MULTI_BUNDLE_SERVER_HOST;
  public static String DEFAULT_MODULE_NAME;
  public static ReactNativeHostHolder mReactNativeHostHolder;
  public static Class<RNRootView> mReactRootViewClazz = RNRootView.class;
  public static Context mContext;
  public static Boolean BootstrapLoaded = false;
  public static UpdateStage updateStage = UpdateStage.ThisTime;
  public static long CHECK_UPDATE_TIMEOUT_SECOND = 60;
  public static long OKHTTP_CONNECT_TIMEOUT_SECOND = 5;
  public static long OKHTTP_READ_TIMEOUT_SECOND = 5;
  public static long OKHTTP_WRITE_TIMEOUT_SECOND = 5;
  public static boolean PROGRESS_BAR_SHOW = true;
  public static Integer PROGRESS_BAR_MARGIN_BOTTOM = null;
  private static ProgressBarDialog progressBarDialog = null;
  private static HashMap<String, Pair<Integer, Integer>> PROGRESS_MAP = new HashMap<>();
  private static ThreadPool.Promise CHECK_UPDATE_PROMISE;
  private static AtomicBoolean CHECK_UPDATE_FLAG = new AtomicBoolean(false);

  public MultiBundle(Context context, String defaultModuleName, String multiBundleSeverHost) {
    DEFAULT_MODULE_NAME = defaultModuleName;
    MULTI_BUNDLE_SERVER_HOST = multiBundleSeverHost;
    mContext = context;
    RNDBHelper.init(mContext);
    initDB(mContext);
  }

  private static Boolean isDev() {
    if (mReactNativeHostHolder == null) return true;
    return mReactNativeHostHolder.getReactNativeHost().getUseDeveloperSupport();
  }

  public static void setReactNativeHostHolder(ReactNativeHostHolder reactNativeHostHolder) {
    if (reactNativeHostHolder == null) return;;
    mReactNativeHostHolder = reactNativeHostHolder;
    if (mReactNativeHostHolder.createReactContextInBackground() && reactNativeHostHolder.getReactNativeHost() != null) {
      ReactInstanceManager reactInstanceManager = reactNativeHostHolder.getReactNativeHost().getReactInstanceManager();
      if (!isDev() && !reactInstanceManager.hasStartedCreatingInitialContext()) {
        reactInstanceManager.createReactContextInBackground();
      }
    }
  }

  public static void setReactRootView(Class<RNRootView> reactRootViewClass) {
    mReactRootViewClazz = reactRootViewClass;
  }

  public static void openComponent(Activity activity, String moduleName, Boolean finish) {
    openComponent(activity, moduleName, finish, null, null);
  }

  public static void openComponent(Activity activity, String moduleName, Boolean finish, @Nullable Integer statusBarMode) {
    openComponent(activity, moduleName, finish, statusBarMode, null);
  }

  public static void openComponent(Activity activity, String moduleName, Boolean finish, @Nullable Integer statusBarMode, @Nullable ReadableMap params) {
    Intent intent = new Intent(activity, RNActivity.class);
    Bundle paramsBundle = new Bundle();
    paramsBundle.putBoolean("goBack", true);
    if (params != null) {
      paramsBundle.putAll(Arguments.toBundle(params));
    }
    Bundle bundle = createBundle(moduleName, statusBarMode, paramsBundle);
    intent.putExtras(bundle);
    activity.startActivity(intent);
    if (finish) {
      activity.finish();
    }
  }

  public static Bundle createBundle(String moduleName, @Nullable Integer statusBarMode, @Nullable Bundle params) {
    Bundle bundle = new Bundle();
    bundle.putString("moduleName", moduleName);
    bundle.putInt("statusBarMode", statusBarMode == null ? 0 : statusBarMode);
    if (params != null) bundle.putAll(params);
    return bundle;
  }

  public static void initDB(Context ctx) {
    HashMap<String, RNDBHelper.Result> result = RNDBHelper.selectAllMap();
    String versionCode = String.valueOf(AppVersionInfoUtil.getVersionCode(ctx));
    String currBundleVersion = SPUtil.getString(ctx,StorageKey.BUNDLE_VERSION);
    if (result.size() == 0 || !versionCode.equals(currBundleVersion)) {
      try {
        RNDBHelper.deleteAll();
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
          Integer componentType = (Integer) value.get("componentType");
          switch (ComponentType.getByValue(componentType)) {
            case Common:
              componentName = "Common";
              break;
            case Bootstrap:
              componentName = "Bootstrap";
              break;
            case Default:
              componentName = (String) value.get("componentName");
              break;
          }
          String filePath = "assets://" + key;
          contentValuesArr.add(RNDBHelper.createContentValues(key,componentName,componentType,0,hash,filePath,publishTime));
        }
        RNDBHelper.insertRows(contentValuesArr);
        SPUtil.putString(ctx, StorageKey.BUNDLE_VERSION,versionCode);
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  public static void checkUpdate() {
    if (mContext != null) {
      checkUpdate(mContext, true, null);
    }
  }

  public static ThreadPool.Promise checkUpdate(Context ctx, boolean manual, @Nullable Callback callback) {
    try {
      if (CHECK_UPDATE_FLAG.get() && CHECK_UPDATE_PROMISE != null) {
        return CHECK_UPDATE_PROMISE;
      }
      CHECK_UPDATE_FLAG.set(true);
      CHECK_UPDATE_PROMISE = ThreadPool.generateDeferred();
      CHECK_UPDATE_PROMISE.then(new ThreadPool.Promise() {
        @Override
        public void run() {
          CHECK_UPDATE_FLAG.set(false);
          CHECK_UPDATE_PROMISE = null;
        }
      });
      final File downloadPath = ctx.getExternalFilesDir(null);
      final Context mContext = ctx;
      final HashMap<String, RNDBHelper.Result> componentMap = RNDBHelper.selectAllMap();
      sendEventInner(EventName.CHECK_UPDATE_START, null);
      HashMap<String, String> params = new HashMap<>();
      params.put("platform", "android");
      params.put("commonHash", componentMap.get("Common").Hash);
      RequestManager.getInstance(ctx).Get(MULTI_BUNDLE_SERVER_HOST + "/rn/checkUpdate", params, new RequestManager.RequestCallBack<MyResponse<ArrayList<Component>>, MyResponse<Object>>() {
        @Override
        public void onFailure(MyResponse<Object> error, Exception exception) {
          try {
            String cause = "unknown";
            if (exception != null) {
              cause = exception.getMessage();
            } else if (error != null) {
              cause = error.message;
            }
            if (callback != null) callback.onError(cause);
            sendEventInner(EventName.CHECK_UPDATE_FAILURE, cause);
          } catch (Exception e) {
            e.printStackTrace();
          } finally {
            if (CHECK_UPDATE_PROMISE != null) {
              CHECK_UPDATE_PROMISE.reject(exception);
            }
          }
        }

        @Override
        public void onSuccess(MyResponse<ArrayList<Component>> result) {
          try {
            if (callback != null) callback.onSuccess(result);
            sendEventInner(EventName.CHECK_UPDATE_SUCCESS, result);
            ArrayList<ThreadPool.Promise> promises = new ArrayList<>();
            Activity currActivity = RNActivity.getActivity();
            if (!manual && PROGRESS_BAR_SHOW && result.data.size() > 0 && currActivity != null && !currActivity.isFinishing()) {
              progressBarDialog = new ProgressBarDialog(mContext,PROGRESS_BAR_MARGIN_BOTTOM);
              progressBarDialog.show(RNActivity.getActivity().getSupportFragmentManager(),"ProgressDialog");
            }
            for (int i = 0; i < result.data.size(); i++) {
              final Component newComponent = result.data.get(i);
              final RNDBHelper.Result oldComponent = componentMap.get(newComponent.componentName);
              // 如果hash不相同 且版本大于当前版本 下载新的bundle包
              if (!oldComponent.Hash.equals(newComponent.hash) && newComponent.version > oldComponent.Version) {
                sendEventInner(EventName.CHECK_UPDATE_DOWNLOAD_NEWS,newComponent);
                ThreadPool.Promise deferred = ThreadPool.generateDeferred();
                promises.add(deferred);
                PROGRESS_MAP.put(newComponent.componentName, null);
                new Thread(new DownloadTask(
                        mContext,
                        newComponent.downloadUrl,
                        String.format("%s-%s.zip",newComponent.componentName,newComponent.hash),
                        downloadPath,
                        new DownloadProgressListener() {
                          @Override
                          public void onDownloadStart(int fileSize) {
                            synchronized (PROGRESS_MAP) {
                              if (PROGRESS_MAP.get(newComponent.componentName) == null) {
                                PROGRESS_MAP.put(newComponent.componentName, new Pair<>(fileSize,0));
                              }
                            }
                          }

                          @Override
                          public void onDownloadSize(int downloadedSize, int fileSize) {
                            synchronized (PROGRESS_MAP) {
                              if (PROGRESS_MAP.get(newComponent.componentName) != null) {
                                Pair<Integer, Integer> old = PROGRESS_MAP.get(newComponent.componentName);
                                PROGRESS_MAP.put(newComponent.componentName, new Pair<>(old.first, downloadedSize));
                              }
                              calcTotalProgress();
                            }
                            WritableMap progress = Arguments.createMap();
                            progress.putString("componentName",newComponent.componentName);
                            progress.putDouble("progress", (double) downloadedSize / (double) fileSize);
                            sendEventInner(EventName.CHECK_UPDATE_DOWNLOAD_PROGRESS,progress);
                          }

                          @Override
                          public void onDownloadFailure(Exception e) {
                            sendEventInner(EventName.CHECK_UPDATE_DOWNLOAD_NEWS_FAILURE,e.getMessage());
                            deferred.resolve(null);
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
                              deferred.resolve(file.getName());
                              file.delete();
                            }
                          }
                        })).start();
              }
            }
            ThreadPool.Promise[] promisesArgs = promises.toArray(new ThreadPool.Promise[promises.size()]);
            ThreadPool.getInstance(mContext).all(promisesArgs).then(new ThreadPool.Promise() {
              @Override
              public void run() {
                Object results = null;
                try {
                  results = this.getResult();
                } catch (Exception e) {
                  e.printStackTrace();
                }
                currActivity.runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    hideProgressBarDialog();
                  }
                });
                if (CHECK_UPDATE_PROMISE != null) {
                  CHECK_UPDATE_PROMISE.resolve(results);
                }
              }
            });
          } catch (Exception e) {
            e.printStackTrace();
            if (CHECK_UPDATE_PROMISE != null) {
              CHECK_UPDATE_PROMISE.reject(e);
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
      if (CHECK_UPDATE_PROMISE != null) {
        CHECK_UPDATE_PROMISE.reject(e);
      }
    }
    return CHECK_UPDATE_PROMISE;
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
                componentSetting.componentType,
                version,
                componentSetting.hash,
                saveBundleFilePath,
                componentSetting.timestamp
        ));
        sendEventInner(EventName.CHECK_UPDATE_DOWNLOAD_NEWS_APPLY,componentSetting.componentName);
        // 立即应用新模块
        if (!isDev() && !RNActivity.isExistsModule(componentSetting.componentName)) {
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

  public static void hideProgressBarDialog() {
    try {
      if (progressBarDialog != null) {
        progressBarDialog.dismiss();
        progressBarDialog = null;
      }
    } catch (Exception ignore) {
      ignore.printStackTrace();
    }
  }

  private static void calcTotalProgress() {
    synchronized (PROGRESS_MAP) {
      try {
        boolean getAllFileSize = PROGRESS_MAP.values().contains(null);
        if (getAllFileSize) return;
        Pair<Integer, Integer>[] arr = PROGRESS_MAP.values().toArray(new Pair[PROGRESS_MAP.values().size()]);
        int fileSize = 0, downloadSize = 0;
        for (int i = 0; i < arr.length; i++) {
          fileSize += arr[i].first;
          downloadSize += arr[i].second;
        }
        DecimalFormat df = new DecimalFormat("0.00");
        String resultStr = df.format((float)downloadSize / fileSize);
        float resultFloat = Float.parseFloat(resultStr);
        int resultInt = (int)(resultFloat * 100);
        if (progressBarDialog != null) {
          progressBarDialog.setProgress(resultInt);
        }
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
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
    return Arrays.<ViewManager>asList(
            new CustomInputManager()
    );
  }
}
