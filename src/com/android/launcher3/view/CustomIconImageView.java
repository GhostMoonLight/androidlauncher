package com.android.launcher3.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher.TimeChangeListener;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.Utilities;
import com.android.launcher3.utils.Util;
import com.android.launcher3.wallpaper.BitmapUtils;
import com.cuan.launcher.R;

import java.util.Calendar;
import java.util.Locale;

/**
 * 支持动态日历，动态时钟的ImageView
 */
public class CustomIconImageView extends ImageView implements TimeChangeListener{
	
	private int mHourHand, mMinuteHand;
	private boolean isDynamicCalendar, isDynamicClock;
	private Paint mPaintLine;
	private float mOriginalAngle = -90f;
	private float mCenterX, mCenterY;
	private float mEachAngle;
	private int mMinute, mHour;  //分针数值 从0到59；
	private int mWeek, mDate;   //周1-7   1是周日，7是周六
	private int[] dates, weeks, weeksEnglish;

	public CustomIconImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public CustomIconImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomIconImageView(Context context) {
		super(context);
	}
	
	@Override
	public Drawable getDrawable() {
		final ItemInfo info = (ItemInfo) getTag();
		if ((isDynamicCalendar || isDynamicClock) && info != null){
			return info.iconBg;
		}
		return super.getDrawable();
	}
	
	public void setResourceName(String resName){
		if (TextUtils.isEmpty(resName)){
			isDynamicCalendar = isDynamicClock = false;
		} else if("ic_alarmclock".equals(resName)){
			isDynamicClock = true;
		} else if ("ic_calendar".equals(resName)){
			isDynamicCalendar = true;
		} else {
			isDynamicCalendar = isDynamicClock = false;
		}
		if (isDynamicCalendar){
			dates = new int[]{
					R.drawable.widget_calendr_date_0,
					R.drawable.widget_calendr_date_1,
					R.drawable.widget_calendr_date_2,
					R.drawable.widget_calendr_date_3,
					R.drawable.widget_calendr_date_4,
					R.drawable.widget_calendr_date_5,
					R.drawable.widget_calendr_date_6,
					R.drawable.widget_calendr_date_7,
					R.drawable.widget_calendr_date_8,
					R.drawable.widget_calendr_date_9
			};
			weeks = new int[]{
					R.drawable.widget_calendr_day1_ch,
					R.drawable.widget_calendr_day2_ch,
					R.drawable.widget_calendr_day3_ch,
					R.drawable.widget_calendr_day4_ch,
					R.drawable.widget_calendr_day5_ch,
					R.drawable.widget_calendr_day6_ch,
					R.drawable.widget_calendr_day7_ch
			};
			weeksEnglish = new int[]{
					R.drawable.widget_calendr_day1_en,
					R.drawable.widget_calendr_day2_en,
					R.drawable.widget_calendr_day3_en,
					R.drawable.widget_calendr_day4_en,
					R.drawable.widget_calendr_day5_en,
					R.drawable.widget_calendr_day6_en,
					R.drawable.widget_calendr_day7_en,
			};
		}
		if (isDynamicCalendar || isDynamicClock){
			LauncherAppState.getInstance().getLauncher().getTimeChangeReceiver().addTimeChangeListener(this);
	        updateClockOrDate();
	        invalidate();
		}
	}
	
	public void updateClockOrDate(){
		final ItemInfo info = (ItemInfo) getTag();
		Calendar sCalendar = Calendar.getInstance();
		mHour = sCalendar.get(Calendar.HOUR_OF_DAY);
		mMinute = sCalendar.get(Calendar.MINUTE);
		int date = sCalendar.get(Calendar.DATE);
		int week = sCalendar.get(Calendar.DAY_OF_WEEK);
		if (isDynamicCalendar){
			if (date != mDate || week != mWeek){
				mDate = date;
				mWeek = week;
				invalidate();
			}
		}
		
		if (isDynamicClock){
			if (!DateFormat.is24HourFormat(getContext())){
			}
			invalidate();
		}
		postDelayed(new Runnable() {
			
			@Override
			public void run() {
				Bitmap b = BitmapUtils.getBitmap(CustomIconImageView.this);
				if (b != null && info != null){
					info.iconBg = Utilities.createIconDrawable(BitmapUtils.getBitmap(CustomIconImageView.this));
				}
			}
		}, 50);
	}
	
	public void setMinuteAndHour(int minute, int hour){
		this.mMinute = minute;
		this.mHour = hour;
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (isDynamicCalendar) {
			Paint paint = new Paint();
			int startX, startY=0;
			//画周
			Bitmap weekBitmap = getWeekBitmap(mWeek);
			startX = (int) ((getWidth() - weekBitmap.getWidth()) * 0.5f);
			startY = -Util.dip2px(2);
			canvas.drawBitmap(weekBitmap, startX, startY, paint);
			weekBitmap.recycle();
			
			//画date
			int first = mDate / 10;
			int second = 0;
			boolean isFirst = false;
			if (first == 0){
				//说明是一位数
				isFirst = true;
				first = mDate;
			}else{
				second = mDate % 10;
			}
			Bitmap firstDateBitmap, secondDateBitmap = null;
			firstDateBitmap = getDataBitmap(first);
			startY = (getWidth() - firstDateBitmap.getHeight() - weekBitmap.getHeight())/2 + weekBitmap.getHeight();
			if (isFirst){
				startX = (int) ((getWidth() - firstDateBitmap.getWidth()) * 0.5f);
				canvas.drawBitmap(firstDateBitmap, startX, startY, paint);
			} else {
				secondDateBitmap = getDataBitmap(second);
				startX = (int) ((getWidth() - firstDateBitmap.getWidth()*2) * 0.5f);
				canvas.drawBitmap(firstDateBitmap, startX, startY, paint);
				canvas.drawBitmap(secondDateBitmap, startX+firstDateBitmap.getWidth(), startY, paint);
			}
			firstDateBitmap.recycle();
			if (secondDateBitmap != null){
				secondDateBitmap.recycle();
			}
		} else if (isDynamicClock) {
//			Paint paint = new Paint();
			if(mHourHand == 0 || mMinuteHand == 0){
				mEachAngle = 360f / 60;
				mCenterX = getWidth() * 0.5f;
				mCenterY = getHeight() * 0.5f;
				mMinuteHand = (int) (getWidth()*0.5f*0.7f);
				mHourHand = (int) (mMinuteHand*0.7f);
				mPaintLine = new Paint();
				mPaintLine.setAntiAlias(true);
				mPaintLine.setStyle(Style.FILL);
				mPaintLine.setStrokeCap(Paint.Cap.ROUND);//线条带弧度，
				mPaintLine.setStrokeWidth(2.4f);
				mPaintLine.setColor(Color.BLACK);
			}
			canvas.drawCircle(mCenterX, mCenterY, 3.4f, mPaintLine);
			//画分针
			canvas.save();
			canvas.translate(mCenterX, mCenterY);
			canvas.rotate(mOriginalAngle+mMinute*mEachAngle);  //不平移的话，默认以0，0为原点，进行旋转
			canvas.drawLine(0, 0, mMinuteHand, 0, mPaintLine);
			canvas.restore();
			//画时针
			canvas.save();
			canvas.translate(mCenterX, mCenterY);
			canvas.rotate(mOriginalAngle+mHour*5*mEachAngle+30f/60*mMinute);
			canvas.drawLine(0, 0, mHourHand, 0, mPaintLine);
			canvas.restore();
		}
	}
	
	private Bitmap getWeekBitmap(int week){
		if (Locale.getDefault().getLanguage().equals("zh")){
			return BitmapFactory.decodeResource(getResources(), weeks[week-1]);
		}else{
			return BitmapFactory.decodeResource(getResources(), weeksEnglish[week-1]);
		}
	}
	
	private Bitmap getDataBitmap(int date){
		return BitmapFactory.decodeResource(getResources(), dates[date]);
	}

	@Override
	public void timeChanged() {
		updateClockOrDate();
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (isDynamicCalendar || isDynamicClock){
			LauncherAppState.getInstance().getLauncher().getTimeChangeReceiver().addTimeChangeListener(this);
			updateClockOrDate();
		}
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (isDynamicCalendar || isDynamicClock){
			if(LauncherAppState.getInstance().getLauncher() != null
					&& LauncherAppState.getInstance().getLauncher().getTimeChangeReceiver() != null){
				LauncherAppState.getInstance().getLauncher().getTimeChangeReceiver().removeTimeChangeListener(this);
			}
		}
	}
}
