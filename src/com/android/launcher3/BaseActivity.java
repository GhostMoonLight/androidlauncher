package com.android.launcher3;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import com.cuan.launcher.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

public class BaseActivity extends Activity {
	
	private Dialog dialog;
	
	public void showProgressDialog(){
		if (dialog == null){
			dialog = new Dialog(this, R.style.dialogloading);
			dialog.setContentView(R.layout.dialog_loading);//此处布局为一个progressbar
			dialog.setCancelable(false); // 不可以取消
			dialog.show();
		}
	}
	
	public void dismisProgressDialog(){
		if (dialog != null && dialog.isShowing()){
			dialog.dismiss();
			dialog = null;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PushAgent.getInstance(this).onAppStart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

}
