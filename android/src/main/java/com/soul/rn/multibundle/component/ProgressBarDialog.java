package com.soul.rn.multibundle.component;

import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.soul.rn.multibundle.R;

public class ProgressBarDialog extends DialogFragment {
    private final Context mContext;
    private final Integer mMarginBottom;
    private ProgressBar mProgressBar;

    public ProgressBarDialog(Context context, Integer marginBottom) {
        mContext = context;
        mMarginBottom = marginBottom;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.my_dialog);
        if (getDialog() != null) {
            Window window = getDialog().getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            }
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getDialog().setCanceledOnTouchOutside(false);
        }
        View view = inflater.inflate(R.layout.progressbar_dialog,null);
        mProgressBar = view.findViewById(R.id.progressBar);
        if (mMarginBottom != null && mProgressBar != null) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mProgressBar.getLayoutParams();
            layoutParams.bottomMargin = mMarginBottom;
            mProgressBar.setLayoutParams(layoutParams);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Dialog dialog =  getDialog();
        if (dialog != null) {
            Window window = getDialog().getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setAttributes(params);
            window.getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    public void setProgress(int value) {
        if (value < 0 || value > 100) return;
        if (mProgressBar == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mProgressBar.setProgress(value,true);
        } else {
            mProgressBar.setProgress(value);
        }
    }
}
