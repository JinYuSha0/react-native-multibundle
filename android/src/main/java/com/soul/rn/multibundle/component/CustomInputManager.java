package com.soul.rn.multibundle.component;

import android.text.TextWatcher;

import androidx.annotation.Nullable;

import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.views.textinput.ReactEditText;
import com.facebook.react.views.textinput.ReactTextInputManager;

@ReactModule(name = CustomInputManager.REACT_CLASS)
public class CustomInputManager extends ReactTextInputManager {
    final static String REACT_CLASS = "CustomInput";
    private String Thousands = ",";
    private TextWatcher currWatcher;
    private Integer type = -1;

    public Boolean getIsOnlyNumber() {
        return this.type == 2;
    }

    @Override
    public String getName() {
        return CustomInputManager.REACT_CLASS;
    }

    @ReactProp(name = "type", defaultInt = 0)
    public void setType(ReactEditText view, @Nullable int type) {
        view.setInputType(type);
        this.type = type;
        if (type == 2) {
            addWatcher(view, new AmountFormatWatcher(view, getIsOnlyNumber(), this.Thousands));
        }
    }

    @ReactProp(name = "format", defaultInt = 0)
    public void setFormat(ReactEditText view, @Nullable int format) {
        removeCurrWatcher(view);
        if (format == 1) {
            addWatcher(view, new AmountFormatWatcher(view, getIsOnlyNumber(), this.Thousands));
        } else {
            this.Thousands = null;
        }
    }

    @ReactProp(name = "thousands")
    public void setThousands(ReactEditText view, @Nullable String thousands) {
        if (thousands != null) {
            this.Thousands = thousands;
            addWatcher(view, new AmountFormatWatcher(view, getIsOnlyNumber(), this.Thousands));
        }
    }

    public void removeCurrWatcher(ReactEditText view) {
        if (this.currWatcher != null) {
            view.removeTextChangedListener(this.currWatcher);
            this.currWatcher = null;
        }
    }

    public void addWatcher(ReactEditText view, TextWatcher watcher) {
        removeCurrWatcher(view);
        this.currWatcher = watcher;
        view.addTextChangedListener(watcher);
    }
}