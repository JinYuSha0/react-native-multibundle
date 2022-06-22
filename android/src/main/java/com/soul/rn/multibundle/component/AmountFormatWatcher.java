package com.soul.rn.multibundle.component;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import org.jetbrains.annotations.Nullable;

import java.util.StringTokenizer;

public class AmountFormatWatcher implements TextWatcher {
    EditText editText;
    private static String thousands;
    private boolean onlyNumber = false;

    public AmountFormatWatcher(EditText editText, boolean onlyNumber, @Nullable String thousands) {
        this.editText = editText;
        this.onlyNumber = onlyNumber;
        AmountFormatWatcher.thousands = thousands;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        try
        {
            editText.removeTextChangedListener(this);
            String str = editText.getText().toString();

            if (onlyNumber) {
                if(str.startsWith(".")){
                    str = "0.";
                }
                str = str.replaceAll("[^(0-9)|\\.]", "");
            }


            if(onlyNumber && AmountFormatWatcher.thousands != null && !str.equals("")) {
                str = getDecimalFormattedString(str);
            }

            editText.setText(str);
            editText.setSelection(str.length());
            editText.addTextChangedListener(this);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            editText.addTextChangedListener(this);
        }
    }

    public static String getDecimalFormattedString(String value)
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
                str3 = AmountFormatWatcher.thousands + str3;
                i = 0;
            }
            str3 = str1.charAt(k) + str3;
            i++;
        }
    }
}