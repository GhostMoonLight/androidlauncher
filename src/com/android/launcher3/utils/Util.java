package com.android.launcher3.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import com.android.launcher3.LauncherAppState;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    /**
     * 获取屏幕高度
     *
     * @return 屏幕高度
     */
    public static int getScreenH() {
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        return dm.heightPixels;
    }

    /**
     * 获取屏幕宽度
     *
     * @return 屏幕宽度
     */
    public static int getScreenW() {
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        return dm.widthPixels;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(float dpValue) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpValue, Resources.getSystem().getDisplayMetrics());
        return (int) px;
    }

    /**
     * 根据手机的分辨率从 px 的单位 转成为 dp
     */
    public static int px2dip(float px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }


    /**
     * 根据手机的分辨率从 px 的单位 转成为 sp
     */
    public static int px2sp(float px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().scaledDensity);
    }

    /**
     * 根据手机的分辨率从 sp 的单位 转成为 px(像素)
     */
    public static int sp2px(float spValue) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spValue, Resources.getSystem().getDisplayMetrics());
        return (int) px;
    }
	
	/**
	 * 打开设置界面
	 * @param context
     */
	public static void startSettings(Context context) {
		Intent sIntent = new Intent();
		sIntent.setAction(Settings.ACTION_SETTINGS);
		context.startActivity(sIntent);
	}
	
	//获取设备名称
	public static String getDeviceName() {
		return android.os.Build.MODEL;
	}
	//获取sdk版本号
	public static int getSdkVersionCode() {
		return android.os.Build.VERSION.SDK_INT;
	}
	//获取sdk版本
	public static String getSdkVersion() {
		return "" + android.os.Build.VERSION.SDK_INT;
	}
	//获取sdk版本名称
	public static String getSdkVersionName() {
		return android.os.Build.VERSION.RELEASE + "";
	}
	
	/**
	 * 制造商
	 * @return
	 */
	public static String getManufactruer() {
		return android.os.Build.MANUFACTURER + "";
	}
	
	/**
	 * 厂商
	 * @return
	 */
	public static String getCompany() {
		if (isYunOs()) {
			return "yunos";
		} else if (isBaiduOs()) {
			return "baidu";
		} else {
			return android.os.Build.BRAND;
		}
	}
	
	/**
	 * 是否存在其它桌面
	 */
	public static boolean existOtherLauncherApps(Context context) {
		ArrayList<String> apps = getLauncherApps(context);
		if (apps != null && apps.size() > 1) {
			return true;
		}
		return false;
	}
	
	/**
	 * 获取已安装桌面应用包名列表
	 */
	public static ArrayList<String> getLauncherApps(Context context) {
		ArrayList<String> apps = new ArrayList<String>();

		try {
			Intent homeIntent = new Intent(Intent.ACTION_MAIN);
			homeIntent.addCategory(Intent.CATEGORY_HOME);
			homeIntent.addCategory(Intent.CATEGORY_DEFAULT);
			List<ResolveInfo> sResolveInfos = context.getPackageManager().queryIntentActivities(homeIntent, 0);
			
			if (sResolveInfos != null && sResolveInfos.size() > 0) {
				for (ResolveInfo resolveInfo : sResolveInfos) {
					if (resolveInfo.activityInfo.applicationInfo.enabled) {
						apps.add(resolveInfo.activityInfo.packageName);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return apps;
	}
	
	/**
	 * 获取状态栏高度
	 */
	public static int getStatusBarHeight(Activity activity) {
		int statusBarHeight = 0;
		
		try {
			Class<?> c = Class.forName("com.android.internal.R$dimen");
			Object obj = c.newInstance();
			Field field = c.getField("status_bar_height");
			int x = Integer.parseInt(field.get(obj).toString());
			statusBarHeight = activity.getResources().getDimensionPixelSize(x);
		} catch (Exception e1) {
			e1.printStackTrace();
			try {
				Rect frame = new Rect();
				activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
				statusBarHeight = frame.top;
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				statusBarHeight = 60;
			}
		}
		return statusBarHeight;
	}
	
	/**
	 * 获取底部导航栏高度（虚拟按键）
	 * @param context
	 * @return
	 */
	public static int getBottomNavBarHeight(Context context) {
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display d = windowManager.getDefaultDisplay();
		
		DisplayMetrics realDisplayMetrics = new DisplayMetrics();
		d.getRealMetrics(realDisplayMetrics);
		int realHeight = realDisplayMetrics.heightPixels;

		DisplayMetrics displayMetrics = new DisplayMetrics();
		d.getMetrics(displayMetrics);
		int displayHeight = displayMetrics.heightPixels;

		return realHeight - displayHeight;
	}
	
	/**
	 * 是否是数字
	 */
	public static boolean isNumeric(String str) {
		try {
			if (!TextUtils.isEmpty(str)) {
				Pattern pattern = Pattern.compile("[0-9]*");
				Matcher isNum = pattern.matcher(str);
				if (!isNum.matches()) {
					return false;
				}
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}
	
	/**
	 * 是否是Miui
	 */
	public static boolean isMiui() {
		String strCompany = getCompany();
		if (strCompany != null) {
			return "xiaomi".equals(strCompany.toLowerCase(Locale.getDefault()));
		}
		return false;
	}
	
	/**
	 * 是否是阿里os
	 */
	public static boolean isYunOs() {
		BufferedReader sReader = null;
		try {
			File sFile = new File("/system/build.prop");
			sReader = new BufferedReader(new InputStreamReader(new FileInputStream(sFile)));
			String strBuffer;
			
			while ((strBuffer = sReader.readLine()) != null) {
				if (strBuffer.startsWith("ro.sys.vendor") && strBuffer.toLowerCase(Locale.getDefault()).contains("yunos")) {
					try {
						if (sReader != null) {
							sReader.close();
							sReader = null;
						}
					} catch (Exception e2) {
						e2.printStackTrace();
					}
					return true;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if (sReader != null) {
				try {
					sReader.close();
				} catch (IOException e) {}
			}
		}
		return false;
	}
	
	/**
	 * 是否是百度os
	 * @return
	 */
	public static boolean isBaiduOs() {
		try {
			String manufactruer = getManufactruer();
			
			if (!TextUtils.isEmpty(manufactruer) && manufactruer.toLowerCase(Locale.getDefault()).trim().equals("baidu")) {
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean isArrayContainsString(String key, String[] strArray, boolean ignoreCase) {
		if (strArray == null || strArray.length == 0) return false;
		if (TextUtils.isEmpty(key)) return false;
		if (ignoreCase) key = key.toLowerCase(Locale.getDefault());
		
		for (String string : strArray) {
			if (ignoreCase) {
				if (key.equals(string.toLowerCase(Locale.getDefault()))) return true;
			} else {
				if (key.equals(string)) return true;				
			}
		}
		return false;
	}
	
	/**
	 * 有使用限制分隔符必须在字符串中间单个出现，不能连续出现，不能出现在字符串开头和结尾
	 * 
	 * @param line    要分隔的字符串 
	 * @param split   分隔符
	 * @return
	 */
	public static String[] splitByIndex(String line, char split) {
		if (TextUtils.isEmpty(line)){
			return new String[0];
		}
//		if (line.charAt(0) == split){
//			line = line.substring(1);
//		}
//		if (TextUtils.isEmpty(line)){
//			return new String[0];
//		}
//		if (line.charAt(line.length()-1) == split){
//			line = line.substring(0, line.length()-1);
//		}
//		if (TextUtils.isEmpty(line)){
//			return new String[0];
//		}
        int[] lp = getPsLinePos(line, split);
        int length = lp[0];
        String[] l = new String[length];
        for (int i1 = 2; i1 < length+1; i1++) {
        	if (i1 == 2){
        		l[i1-2] = line.substring(lp[i1 - 1], lp[i1]);
        	}else{
        		l[i1-2] = line.substring(lp[i1 - 1]+1, lp[i1]);
        	}
        }
        l[length-1] = line.substring(lp[length]+1);
        return l;
    }

	/**
	 * @param line    要分隔的字符串
	 * @param split   分隔符
	 * @return int[]
	 */
    private static int[] getPsLinePos(String line, char split) {
        // 以下是为了得到每一列的pos;不在循环里面判空,节省调用
    	int total = 200;
        int[] lp = new int[total];
        lp[1] = 0; // 第一个起点是开始
        int count = 1;
        int index = 1;
        char lastChar;
        char curChar;

        int length = line.length();
        for (int j = 1; j < length; j++) {
            lastChar = line.charAt(j - 1);
            curChar = line.charAt(j);

            if (index + 1 >= total) {
                break;
            }

            if (lastChar == split && curChar != split) {
            	count++;
                // 如果是从空格突变为非空格,那么就是起始点
                lp[++index] = j-1;
            }
        }
        lp[0] = count;
        return lp;
    }
    
    public static String hanZiToPinYin(String input){
    	String pinyin = HanziToPinyin.getInstance().transliterate(input).trim().toUpperCase();
    	if (TextUtils.isEmpty(pinyin)){
			pinyin = HanziUtils.getNamePinyin(LauncherAppState.getInstance().getContext(), input);
		}
    	return pinyin;
    }
    
    /**
	 * 重启程序
	 */
	public static void restartApplication() {
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	/**
	 * 判断sdcard是否可用
	 * 
	 * @return
	 */
	public static boolean isSDCardAvailable() {
		boolean sdcardAvailable = false;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			sdcardAvailable = true;
		}
		return sdcardAvailable;
	}
	
	public static void deleteFile(File file) {
		try {
			if (file != null && file.exists()) {
				File toFile = new File(file.getAbsolutePath() + System.currentTimeMillis());
				file.renameTo(toFile);
				toFile.delete();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public static String getWeek() {
//		String Week = "星期";
//		Calendar c = Calendar.getInstance();
//
//		if (c.get(Calendar.DAY_OF_WEEK) == 1) {
//			Week += "天";
//		}
//		if (c.get(Calendar.DAY_OF_WEEK) == 2) {
//			Week += "一";
//		}
//		if (c.get(Calendar.DAY_OF_WEEK) == 3) {
//			Week += "二";
//		}
//		if (c.get(Calendar.DAY_OF_WEEK) == 4) {
//			Week += "三";
//		}
//		if (c.get(Calendar.DAY_OF_WEEK) == 5) {
//			Week += "四";
//		}
//		if (c.get(Calendar.DAY_OF_WEEK) == 6) {
//			Week += "五";
//		}
//		if (c.get(Calendar.DAY_OF_WEEK) == 7) {
//			Week += "六";
//		}
//
//		return Week;
        return getDataTime("EEEE");
	}

	/**
	 * 注：格式化字符串存在区分大小写
	 * 对于创建SimpleDateFormat传入的参数：EEEE代表星期，如“星期四”；
	 * MMMM代表中文月份，如“十一月”；MM代表月份，如“11”；
	 * yyyy代表年份，如“2010”；dd代表天，如“25”
	 * @return
     */
	public static String getWeeK(){
		Date date=new Date();
		SimpleDateFormat dateFm = new SimpleDateFormat("EEEE");
		dateFm.format(date);
		return dateFm.format(date);
	}

    /**
     * 指定格式返回当前系统时间
     */
    public static String getDataTime(String format) {
        SimpleDateFormat df = new SimpleDateFormat(format, Locale.getDefault());
        return df.format(new Date());
    }

    /**
     * 返回当前系统时间(格式以HH:mm形式)
     */
    public static String getDataTime() {
        return getDataTime("HH:mm");
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public static boolean isToday(long when) {
        android.text.format.Time time = new android.text.format.Time();
        time.set(when);

        int thenYear = time.year;
        int thenMonth = time.month;
        int thenMonthDay = time.monthDay;

        time.set(System.currentTimeMillis());
        return (thenYear == time.year)
                && (thenMonth == time.month)
                && (time.monthDay == thenMonthDay);
    }

    public static String getFriendlyTime2(Date date) {
        String showStr = "";
        if (isToday(date.getTime())) {
            showStr = "今天";
        } else {
            showStr = getFriendlyTime(date);
        }
        return showStr;
    }

    /**
     * 转换日期到指定格式方便查看的描述说明
     *
     * @return 几秒前，几分钟前，几小时前，几天前，几个月前，几年前，很久以前（10年前）,如果出现之后的时间，则提示：未知
     */
    public static String getFriendlyTime(Date date) {
        String showStr = "";
        long yearSeconds = 31536000L;//365 * 24 * 60 * 60;
        long monthSeconds = 2592000L;//30 * 24 * 60 * 60;
        long daySeconds = 86400L;//24 * 60 * 60;
        long hourSeconds = 3600L;//60 * 60;
        long minuteSeconds = 60L;

        long time = (System.currentTimeMillis() - date.getTime()) / 1000;
        if (time <= 50) {
            showStr = "刚刚";
            return showStr;
        }
        if (time / yearSeconds > 0) {
            int year = (int) (time / yearSeconds);
            if (year > 10)
                showStr = "很久以前";
            else {
                showStr = year + "年前";
            }
        } else if (time / monthSeconds > 0) {
            showStr = time / monthSeconds + "个月前";
        } else if (time / daySeconds > 7) {
            SimpleDateFormat formatter = new SimpleDateFormat("MM-dd", Locale.getDefault());
            showStr = formatter.format(date);
        } else if (time / daySeconds > 0) {
            showStr = time / daySeconds + "天前";
        } else if (time / hourSeconds > 0) {
            showStr = time / hourSeconds + "小时前";
        } else if (time / minuteSeconds > 0) {
            showStr = time / minuteSeconds + "分钟前";
        } else if (time > 0) {
            showStr = time + "秒前";
        }
        return showStr;
    }

}
