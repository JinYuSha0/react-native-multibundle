package com.soul.rn.multibundle;

import android.os.Bundle;

import com.soul.rn.multibundle.constant.StatusBar;

public class RNActivity extends RNActivityImpl {
  protected String getDefaultComponentName() {
    return MultiBundle.DEFAULT_MODULE_NAME != null ? MultiBundle.DEFAULT_MODULE_NAME : "Home";
  }

  protected Bundle getDefaultParams() {
    Bundle bundle = new Bundle();
    bundle.putInt("statusBarMode", StatusBar.lightMode);
    return bundle;
  }

  @Override
  public RNBundle getBundle() {
    Bundle bundle;
    if (getIntent() == null) {
      bundle = new Bundle();
    } else {
      bundle = getIntent().getExtras();
    }
    if (bundle == null) bundle = new Bundle();
    Bundle params = getDefaultParams();
    Bundle extraParams = bundle.getBundle("params");
    if (extraParams == null) extraParams = new Bundle();
    params.putAll(bundle);
    params.putAll(extraParams);
    String moduleName = bundle.getString("moduleName", getDefaultComponentName());
    return new RNBundle(moduleName, params);
  }
}
