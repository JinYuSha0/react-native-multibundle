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
    private String regex;

    @Override
    public String getName() {
        return CustomInputManager.REACT_CLASS;
    }

    @ReactProp(name = "myType", defaultInt = 0)
    public void setMyType(ReactEditText view, @Nullable int type) {
        this.type = type;
        if (type == 1) {
            view.setInputType(InputType.TYPE_CLASS_NUMBER);
            addWatcher(view, new AmountFormatWatcher(view, "[^(0-9)]", this.decimal, null));
        } else if (type == 2) {
            view.setInputType(InputType.TYPE_CLASS_NUMBER);
            addWatcher(view, new AmountFormatWatcher(view, "[^(0-9)]", this.decimal, this.Thousands));
        } else if (type == 3) {
            addWatcher(view, new AmountFormatWatcher(view, regex,false,null));
        } else {
            removeCurrWatcher(view);
        }
    }

    @ReactProp(name = "thousands")
    public void setThousands(ReactEditText view, @Nullable String thousands) {
        if (thousands != null) {
            this.Thousands = thousands;
            setMyType(view,2);
        }
    }

    @ReactProp(name = "decimal")
    public void setDecimal(ReactEditText view, @Nullable Boolean decimal) {
        if (decimal != null) {
            this.decimal = decimal;
            setMyType(view,this.type);
        }
    }

    @ReactProp(name = "regex")
    public void setExtraChar(ReactEditText view, @Nullable String regex) {
        if (regex != null) {
            this.regex = regex;
            setMyType(view,3);
        }
    }

    @ReactProp(name = "inputType")
    public void setInputType(ReactEditText view, @Nullable int inputType) {
        if (view != null) {
            view.setInputType(inputType);
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