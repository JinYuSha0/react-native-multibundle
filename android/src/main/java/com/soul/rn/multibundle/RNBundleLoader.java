package com.soul.rn.multibundle;

import android.content.Context;

import androidx.annotation.Nullable;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.CatalystInstance;
import com.facebook.react.bridge.ReactContext;
import com.soul.rn.multibundle.utils.FileUtil;

import java.util.HashSet;
import java.util.Set;

public class RNBundleLoader {
  public static Set<String> sLoadedBundle = new HashSet<>();

  @Nullable
  public static CatalystInstance getCatalystInstance(ReactNativeHost host) {
    ReactInstanceManager manager = host.getReactInstanceManager();
    if (manager == null) {
      return null;
    }

    ReactContext context = manager.getCurrentReactContext();
    if (context == null) {
      return null;
    }

    if (!context.hasActiveReactInstance()) {
      return null;
    }

    return context.getCatalystInstance();
  }

  public static void loadScript(Context context, CatalystInstance instance, String filePath, boolean isSync) {
    if (filePath.startsWith("assets://")) {
      loadScriptFromAsset(context, instance, filePath, isSync);
    } else if (filePath.startsWith("file://")) {
      loadScriptFromFile(context, instance, filePath, isSync);
    }
  }

  public static void loadScriptFromAsset(Context context, CatalystInstance instance, String assetName, boolean isSync) {
    if (sLoadedBundle.contains(assetName)) {
      return;
    }
    String source = assetName;
    if(!assetName.startsWith("assets://")) {
      source = "assets://" + assetName;
    }
    instance.loadScriptFromAssets(context.getAssets(), source, isSync);
    sLoadedBundle.add(assetName);
  }

  public static void loadScriptFromFile(Context context, CatalystInstance instance, String filepath, boolean isSync) {
    String realFilePath = filepath;
    if (filepath.startsWith("file://")) {
      realFilePath = filepath.replaceAll("file://", FileUtil.getExternalFilesDir(context));
    }
    if (sLoadedBundle.contains(realFilePath)) {
      return;
    }
    instance.loadScriptFromFile(realFilePath,realFilePath,isSync);
    sLoadedBundle.add(realFilePath);
  }
}
