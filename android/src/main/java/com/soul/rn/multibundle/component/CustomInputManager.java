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
    private String Thousands;
    private TextWatcher currWatcher;
    private Integer type = -1;
    private boolean decimal = false;

    public boolean getIsOnlyNumber() {
        return this.type == 2;
    }

    public CustomInputManager() {
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
            addWatcher(view, new AmountFormatWatcher(view, getIsOnlyNumber(), this.decimal, this.Thousands));
        } else {
            removeCurrWatcher(view);
        }
    }

    @ReactProp(name = "format", defaultInt = 0)
    public void setFormat(ReactEditText view, @Nullable int format) {
        removeCurrWatcher(view);
        if (format == 1) {
            if (this.Thousands == null) {
                this.Thousands = ",";
            }
            addWatcher(view, new AmountFormatWatcher(view, getIsOnlyNumber(), this.decimal, this.Thousands));
        } else {
            this.Thousands = null;
            removeCurrWatcher(view);
        }
    }

    @ReactProp(name = "thousands")
    public void setThousands(ReactEditText view, @Nullable String thousands) {
        if (thousands != null) {
            this.Thousands = thousands;
            addWatcher(view, new AmountFormatWatcher(view, getIsOnlyNumber(), this.decimal, this.Thousands));
        } else {
            removeCurrWatcher(view);
        }
    }

    @ReactProp(name = "decimal")
    public void setDecimal(ReactEditText view, @Nullable Boolean decimal) {
        if (decimal != null) {
            this.decimal = decimal;
            addWatcher(view, new AmountFormatWatcher(view, getIsOnlyNumber(), this.decimal, this.Thousands));
        } else {
            removeCurrWatcher(view);
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