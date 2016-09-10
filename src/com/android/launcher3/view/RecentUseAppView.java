package com.android.launcher3.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.launcher3.DeviceProfile;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.db.DBContent.RecentUserAppInfo;
import com.cuan.launcher.R;

public class RecentUseAppView extends RelativeLayout {

	private ImageView appIcon;
	private TextView appText;

	public RecentUseAppView(Context context) {
        super(context);
        initView();
    }

	public RecentUseAppView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public RecentUseAppView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView();
	}

	private void initView() {
		appText = (TextView) findViewById(R.id.app_text);
		appIcon = (ImageView) findViewById(R.id.app_icon);
	}

	public void setData(RecentUserAppInfo info) {
		if (appIcon == null || appText == null){
			initView();
		}
		setTag(info);
        DeviceProfile mDeviceProfile = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) appIcon.getLayoutParams();
        lp.width = mDeviceProfile.iconSizePx;
        lp.height = mDeviceProfile.iconSizePx;

        int padding = mDeviceProfile.iconDrawablePaddingPx;
        lp.setMargins(padding, padding, padding, (int)(padding*0.9));
		appIcon.setImageDrawable(info.icon);
		appText.setText(info.title);
	}
}
