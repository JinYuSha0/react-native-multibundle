package com.soul.rn.multibundle.component;

import android.text.InputType;
import android.text.TextWatcher;

import androidx.annotation.Nullable;

import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.views.textinput.ReactEditText;
import com.facebook.react.views.textinput.ReactTextInputManager;

@ReactModule(name = CustomInputManager.REACT_CLASS)
public class CustomInputManager extends ReactTextInputManager {
    final static String REACT_CLASS = "CustomInput";
    private String Thousands;
    private TextWatcher currWatcher;
    private Integer type = 0;  // 1 纯数字 2 数字加千分位
    private boolean decimal = false; // 允许小数点

    @Override
    public String getName() {
        return CustomInputManager.REACT_CLASS;
    }

    @ReactProp(name = "type", defaultInt = 0)
    public void setType(ReactEditText view, @Nullable int type) {
        view.setInputType(type);
        this.type = type;
        if (type == 1) {
            view.setInputType(InputType.TYPE_CLASS_NUMBER);
            addWatcher(view, new AmountFormatWatcher(view, true, this.decimal, null));
        } else if (type == 2) {
            view.setInputType(InputType.TYPE_CLASS_NUMBER);
            addWatcher(view, new AmountFormatWatcher(view, true, this.decimal, this.Thousands));
        } else {
            removeCurrWatcher(view);
        }
    }

    @ReactProp(name = "thousands")
    public void setThousands(ReactEditText view, @Nullable String thousands) {
        if (thousands != null) {
            this.Thousands = thousands;
            setType(view,2);
        }
    }

    @ReactProp(name = "decimal")
    public void setDecimal(ReactEditText view, @Nullable Boolean decimal) {
        if (decimal != null) {
            this.decimal = decimal;
            setType(view,this.type);
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