/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.cuan.launcher.R;

import java.util.ArrayList;

/**
 * DynamicGrid则是根据设备来指定图标显示的行列
 */
public class DynamicGrid {
    @SuppressWarnings("unused")
    private static final String TAG = "DynamicGrid";

    private DeviceProfile mProfile;
    private float mMinWidth;
    private float mMinHeight;

    // This is a static that we use for the default icon size on a 4/5-inch phone
    static float DEFAULT_ICON_SIZE_DP = 53;
    static float DEFAULT_ICON_SIZE_PX = 0;

    public static float dpiFromPx(int size, DisplayMetrics metrics){
        float densityRatio = (float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT;
        return (size / densityRatio);
    }
    public static int pxFromDp(float size, DisplayMetrics metrics) {
        return (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                size, metrics));
    }
    public static int pxFromSp(float size, DisplayMetrics metrics) {
        return (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                size, metrics));
    }

    public DynamicGrid(Context context, Resources resources,
                       int minWidthPx, int minHeightPx,
                       int widthPx, int heightPx,
                       int awPx, int ahPx) {
        DisplayMetrics dm = resources.getDisplayMetrics();
        //设备信息的集合，会事先创建几个设备信息添加到集合中，根据当前设备选取最合适的设备信息对象
        ArrayList<DeviceProfile> deviceProfiles =
                new ArrayList<DeviceProfile>();
        boolean hasAA = !LauncherAppState.isDisableAllApps();
        DEFAULT_ICON_SIZE_PX = pxFromDp(DEFAULT_ICON_SIZE_DP, dm);
        // Our phone profiles include the bar sizes in each orientation
        deviceProfiles.add(new DeviceProfile("Nexus 4",
                335, 567,  5, 4,  DEFAULT_ICON_SIZE_DP, 14, (hasAA ? 5 : 4), (hasAA ? 50 : DEFAULT_ICON_SIZE_DP), R.xml.default_workspace_4x4,
                R.xml.default_workspace_4x4_no_all_apps));
        deviceProfiles.add(new DeviceProfile("Nexus 5",
                359, 567,  5, 4,  DEFAULT_ICON_SIZE_DP, 14, (hasAA ? 5 : 4), (hasAA ? 50 : DEFAULT_ICON_SIZE_DP), R.xml.default_workspace_4x4,
                R.xml.default_workspace_4x4_no_all_apps));
        deviceProfiles.add(new DeviceProfile("Nexus 6",
        		380, 700,  5, 4,  55, 14, (hasAA ? 5 : 4), (hasAA ? 52 : 55), R.xml.default_workspace_4x4,
                R.xml.default_workspace_4x4_no_all_apps));
        deviceProfiles.add(new DeviceProfile("Nexus 7",
        		415, 738,  5, 4,  55, 14, (hasAA ? 5 : 4), (hasAA ? 52 : 65), R.xml.default_workspace_4x4,
                R.xml.default_workspace_4x4_no_all_apps));
        mMinWidth = dpiFromPx(minWidthPx, dm);
        mMinHeight = dpiFromPx(minHeightPx, dm);
        mProfile = new DeviceProfile(context, deviceProfiles,
                mMinWidth, mMinHeight,
                widthPx, heightPx,
                awPx, ahPx,
                resources);
    }

    public DeviceProfile getDeviceProfile() {
        return mProfile;
    }

    public String toString() {
        return "-------- DYNAMIC GRID ------- \n" +
                "Wd: " + mProfile.minWidthDps + ", Hd: " + mProfile.minHeightDps +
                ", W: " + mProfile.widthPx + ", H: " + mProfile.heightPx +
                " [r: " + mProfile.numRows + ", c: " + mProfile.numColumns +
                ", is: " + mProfile.iconSizePx + ", its: " + mProfile.iconTextSizePx +
                ", cw: " + mProfile.cellWidthPx + ", ch: " + mProfile.cellHeightPx +
                ", hc: " + mProfile.numHotseatIcons + ", his: " + mProfile.hotseatIconSizePx + "]";
    }
}
