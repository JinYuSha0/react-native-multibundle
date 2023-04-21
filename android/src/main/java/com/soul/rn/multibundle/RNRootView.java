package com.soul.rn.multibundle;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.facebook.react.ReactRootView;
import com.soul.rn.multibundle.iface.Callback;

public class RNRootView extends ReactRootView {
    private boolean mShowLoading = false;

    private Callback renderListener = null;

    public RNRootView(Context context) {
        super(context);
    }

    public RNRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RNRootView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setRenderListener(Callback callback) {
        renderListener = callback;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (!mShowLoading && getChildCount() > 0) {
            if (renderListener != null) {
                renderListener.onSuccess(true);
            }
            mShowLoading = true;
        }
    }
}
