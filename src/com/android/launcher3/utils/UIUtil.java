package com.android.launcher3.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

public class UIUtil {
	public static void showInputMethod(View view) {
        final InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
//        自动弹出软键盘
//        mEditTextUserName.requestFocus();
//		InputMethodManager imm = (InputMethodManager) mEditTextUserName.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//		imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED); 
    }

    public static void hideInputMethod(View view) {
        final InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    
    /**
     * Same as {@link View#findViewById}, but crashes if there's no view.
     */
    @SuppressWarnings("unchecked")
    public static <T extends View> T getView(View parent, int viewId) {
        return (T) checkView(parent.findViewById(viewId));
    }

    private static View checkView(View v) {
        if (v == null) {
            throw new IllegalArgumentException("View doesn't exist");
        }
        return v;
    }

    /**
     * Same as {@link View#setVisibility(int)}, but doesn't crash even if {@code view} is null.
     */
    public static void setVisibilitySafe(View v, int visibility) {
        if (v != null) {
            v.setVisibility(visibility);
        }
    }

    /**
     * Same as {@link View#setVisibility(int)}, but doesn't crash even if {@code view} is null.
     */
    public static void setVisibilitySafe(View parent, int viewId, int visibility) {
        setVisibilitySafe(parent.findViewById(viewId), visibility);
    }
    
    public static void setStatusBarColor(Activity activity, int colorId) {
        if (isRunningLOrLater() && activity != null) {
            final Window window = activity.getWindow();
            if (window != null) {
//                window.setStatusBarColor(activity.getResources().getColor(colorId));
            }
        }
    }
    
    private static boolean isRunningLOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    //当前是否是主线程
    public boolean isMainThread(){
        return Looper.getMainLooper().getThread() == Thread.currentThread();
//        return Looper.getMainLooper() == Looper.myLooper();
//        return Looper.getMainLooper().getThread().getId() == Thread.currentThread().getId();
    }
}
