package com.soul.rn.multibundle.component;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import org.jetbrains.annotations.Nullable;

import java.util.StringTokenizer;

public class AmountFormatWatcher implements TextWatcher {
    EditText editText;
    private String regex;
    private boolean decimal = false;
    private String thousands;

    public AmountFormatWatcher(EditText editText, String regex, boolean decimal, @Nullable String thousands) {
        this.editText = editText;
        this.regex = regex;
        this.decimal = decimal;
        this.thousands = thousands;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        try {
            editText.removeTextChangedListener(this);
            String str = editText.getText().toString();
            int selectionEnd = this.editText.getSelectionEnd();

            if (this.decimal) {
                if(str.startsWith(".")){
                    str = "0.";
                }
                str = str.replaceAll("[^(0-9)|\\.]", "");
            } else {
                str = str.replaceAll(regex, "");
            }

            if(this.thousands != null && !str.equals("")) {
                str = getDecimalFormattedString(str, this.thousands);
            }
            editText.setText(str);
            editText.setSelection(Math.min(selectionEnd, str.length()));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            editText.addTextChangedListener(this);
        }
    }

    public static String getDecimalFormattedString(String value, String thousands)
    {
        StringTokenizer lst = new StringTokenizer(value, ".");
        String str1 = value;
        String str2 = "";
        if (lst.countTokens() > 1)
        {
            str1 = lst.nextToken();
            str2 = lst.nextToken();
        }
        String str3 = "";
        int i = 0;
        int j = -1 + str1.length();
        if (str1.charAt( -1 + str1.length()) == '.')
        {
            j--;
            str3 = ".";
        }
        for (int k = j;; k--)
        {
            if (k < 0)
            {
                if (str2.length() > 0)
                    str3 = str3 + "." + str2;
                return str3;
            }
            if (i == 3)
            {
                str3 = thousands + str3;
                i = 0;
            }
            str3 = str1.charAt(k) + str3;
            i++;
        }
    }
}