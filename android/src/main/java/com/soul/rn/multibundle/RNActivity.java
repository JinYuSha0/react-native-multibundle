package com.soul.rn.multibundle;

import android.content.Intent;
import android.os.Bundle;

import com.soul.rn.multibundle.constant.StatusBar;

public class RNActivity extends RNActivityImpl {
  private RNBundle rnBundle;

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
   if (rnBundle == null) {
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
     rnBundle = new RNBundle(moduleName, params);
   }
   return rnBundle;
  }

  @Override
  public void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
  }

  @Override
  public void processDeepLink(Intent intent) {
  }
}
