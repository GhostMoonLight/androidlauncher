package com.android.launcher3;

import android.app.Activity;
import android.os.Bundle;

public class JumpActivity extends Activity {
	
	private Launcher mLauncher;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLauncher = LauncherAppState.getInstance().getLauncher();
		int type = getIntent().getIntExtra("type", -1);
		if (mLauncher == null)
			type = -1;
		switch (type) {
		case 1:
			mLauncher.showAllAppListView();
			break;
		case 2:
			break;
		case 3:
			break;
		case 4:
			break;
		default:
			break;
		}
		finish();
	}

}
