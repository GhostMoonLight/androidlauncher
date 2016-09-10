package com.android.launcher3;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

public class VirtualEntryActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		openLauncher();
	}
	
	private void openLauncher() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		
		try {
			intent.setPackage(getPackageName());
			startActivity(intent);
			return;
		} catch (Exception localException) {
			intent.setComponent(new ComponentName(getApplicationContext(), Launcher.class));
			startActivity(intent);
		} finally {
			finish();
		}
	}
}
