package com.android.launcher3.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.launcher3.Launcher;
import com.android.launcher3.Launcher.TimeChangeListener;
import com.android.launcher3.utils.Util;
import com.cuan.launcher.R;

import java.util.Calendar;

public class WidgetTimeWeatherView extends LinearLayout implements OnClickListener, OnLongClickListener ,TimeChangeListener{

	private Launcher mLauncher;
	private View mMainView;
	private SwitchImageView time_hour1, time_hour2, time_minute1, time_minute2;
	private TextView mDate;
	private int lastTimeHour1=-1, lastTimeHour2=-1, lastTimeMinute1=-1, lastTimeMinute2=-1;
	
	public WidgetTimeWeatherView(Context context) {
		this(context, null);
		mLauncher = (Launcher) context;
	}

	public WidgetTimeWeatherView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mLauncher = (Launcher) context;
		
		mMainView = View.inflate(mLauncher, R.layout.widget_time_weather_view, null);
		mMainView.setOnLongClickListener(this);
		time_minute1 = (SwitchImageView) mMainView.findViewById(R.id.time_minute1);
		time_minute2 = (SwitchImageView) mMainView.findViewById(R.id.time_minute2);
		time_hour1 = (SwitchImageView) mMainView.findViewById(R.id.time_hour1);
		time_hour2 = (SwitchImageView) mMainView.findViewById(R.id.time_hour2);
		mDate = (TextView) mMainView.findViewById(R.id.date_txt);
		updateTime();
		addView(mMainView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}
	
	private void updateTime(){
		Calendar sCalendar = Calendar.getInstance();
		int mHour = sCalendar.get(Calendar.HOUR_OF_DAY);
		int mMinute = sCalendar.get(Calendar.MINUTE);
		String date = mLauncher.getString(R.string.time_format1, sCalendar.get(Calendar.MONTH) + 1, sCalendar.get(Calendar.DATE));
		String week = Util.getWeek();
		mDate.setText(date+"  "+week);
		int timeHour1 = mHour / 10;
		int timeHour2 = mHour % 10;
		int timeMinute1 = mMinute / 10;
		int timeMinute2 = mMinute % 10;
		if (lastTimeHour1 != timeHour1){
			lastTimeHour1 = timeHour1;
			time_hour1.setNextDrawable(new BitmapDrawable(BitmapFactory.decodeResource(getResources(), getTimeImage(lastTimeHour1))));
		}
	
		if (lastTimeHour2 != timeHour2){
			lastTimeHour2 = timeHour2;
			time_hour2.setNextDrawable(new BitmapDrawable(BitmapFactory.decodeResource(getResources(), getTimeImage(lastTimeHour2))));
		}
	
		if (lastTimeMinute1 != timeMinute1){
			lastTimeMinute1 = timeMinute1;
			time_minute1.setNextDrawable(new BitmapDrawable(BitmapFactory.decodeResource(getResources(), getTimeImage(lastTimeMinute1))));
		}
	
		if (lastTimeMinute2 != timeMinute2){
			lastTimeMinute2 = timeMinute2;
			time_minute2.setNextDrawable(new BitmapDrawable(BitmapFactory.decodeResource(getResources(), getTimeImage(lastTimeMinute2))));
		}
	}

	@Override
	public boolean onLongClick(View v) {
		mLauncher.onLongClick(this);
		return false;
	}

	@Override
	public void onClick(View v) {
		
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mLauncher.getTimeChangeReceiver().addTimeChangeListener(this);
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if(mLauncher.getTimeChangeReceiver() != null){
			mLauncher.getTimeChangeReceiver().removeTimeChangeListener(this);
		}
	}

	@Override
	public void timeChanged() {
		updateTime();
	}

	private int getTimeImage(int digit) {
		switch (Integer.valueOf(digit)) {
		case 0:
			return R.drawable.singleclock_time_0;
		case 1:
			return R.drawable.singleclock_time_1;
		case 2:
			return R.drawable.singleclock_time_2;
		case 3:
			return R.drawable.singleclock_time_3;
		case 4:
			return R.drawable.singleclock_time_4;
		case 5:
			return R.drawable.singleclock_time_5;
		case 6:
			return R.drawable.singleclock_time_6;
		case 7:
			return R.drawable.singleclock_time_7;
		case 8:
			return R.drawable.singleclock_time_8;
		default:
			return R.drawable.singleclock_time_9;
		}
	}
}
