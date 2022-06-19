package com.soul.rn.multibundle;

import android.os.Bundle;

import androidx.annotation.Nullable;

public class RNBundle {
  public String moduleName;
  public Bundle params;

  RNBundle() {}

  RNBundle( String _mainComponentName, @Nullable Bundle _params) {
    moduleName = _mainComponentName;
    params = _params;
  }

  public static RNBundle genBundle(String _bundleName, String _mainComponentName, @Nullable Bundle _params) {
    return new RNBundle(_mainComponentName, _params);
  }

  public Bundle toBundle() {
    Bundle bundle = new Bundle();
    bundle.putString("moduleName", moduleName);
    bundle.putBundle("params", params);
    return bundle;
  }

  public void setModuleName(String newModuleName) {
    moduleName = newModuleName;
  }

  public void setParams(Bundle newParams) {
    if (params != null) {
      params.putAll(newParams);
    } else {
      params = newParams;
    }
  }
}
