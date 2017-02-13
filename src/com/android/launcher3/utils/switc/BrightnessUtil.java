package com.android.launcher3.utils.switc;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;

import com.android.launcher3.plugin.ReflexUtil;

/**
 * Created by cgx on 2017/1/23.
 */

public class BrightnessUtil {

    public static final int BRIGHTNESS_DIM;
    public static final int BRIGHTNESS_ON;
    public static final int BRIGHTNESS_MID;

    static {
        BRIGHTNESS_DIM = BrightnessUtil.getValue("BRIGHTNESS_DIM") + 10;
        BRIGHTNESS_ON = BrightnessUtil.getValue("BRIGHTNESS_ON");
        BRIGHTNESS_MID = BrightnessUtil.getValue("BRIGHTNESS_DIM") + (BRIGHTNESS_ON - BrightnessUtil.getValue("BRIGHTNESS_DIM")) / 2 + 1;
    }


    private static int getValue(String str){
        int value=0;
        try {
            Object object = ReflexUtil.getField("android.os.Power", str, null);
            value = ((Integer) object).intValue();
        } catch(Throwable e) {

            try {
                Object object = ReflexUtil.getField("android.os.PowerManager", str, null);
                value = ((Integer) object).intValue();
            } catch(Throwable ee) {
            }
        }

        return value;
    }

    public static int getBrightnessMode(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "screen_brightness_mode", 0);
    }

    public static int getBrightnessValue(Context arg3) {
        return Settings.System.getInt(arg3.getContentResolver(), "screen_brightness", BRIGHTNESS_MID);
    }


    public static void setBrightness(Activity context, int value){
        int mode = getBrightnessMode(context);
        if (mode == 1){  //当前是自动的
            mode = 0;
        }
        setBirghtnessValueAndMode(context, mode, value);
    }

    private static void setBirghtnessValueAndMode(Activity context, int mode, int value){
        Settings.System.putInt(context.getContentResolver(), "screen_brightness_mode", mode);
        Settings.System.putInt(context.getContentResolver(), "screen_brightness", value);

//        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
//        lp.screenBrightness = value * 1.0f / 255f;
//        lp.flags |= 16;
//        context.getWindow().setAttributes(lp);
    }
}
