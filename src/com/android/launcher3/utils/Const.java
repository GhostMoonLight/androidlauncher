package com.android.launcher3.utils;

public class Const {

	public static final boolean SEARCHA_VOICE_VISIBLE = false;  //不显示系统搜索框
	
	//文件夹id，为负数。   id不能为－1，因为id对应数据库中的_id字段，往数据库插入数据成功的话会返回数据库中该插入记录对应的id值，
	//失败的话会返回－1，如果id为－1，就没法判断数据是否插入成功
	public static final long TYPE_FOLDER_SYSTEM = -10;		// 系统工具
	
	
	public static final String PACKAGE_PHONE = "com.android.phone";			// 拨号
	public static final String PACKAGE_CONTACT = "com.android.contacts";	// 联系人
	public static final String PACKAGE_SMS = "com.android.mms";				// 短信
	public static final String PACKAGE_BROWSER = "com.android.browser";		// 浏览器
	public static final String PACKAGE_CLOCK = "com.android.deskclock";		// 时钟
	public static final String PACKAGE_CALENDAR = "com.android.calendar";	// 日历
	public static final String PACKAGE_CAMERA = "com.android.camera";		// 相机
	public static final String PACKAGE_SETTINGS = "com.android.settings";	// 设置

	//文本中正则表达式（@，##，url，表情）的匹配
	private static final String AT="@[\u4e00-\u9fa5\\w]+";// @人

	private static final String TOPIC="#[\u4e00-\u9fa5\\w]+#";// ##话题

	private static final String EMOJI="\\[(\\S+?)\\]";// 表情

	private static final String URL="http://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";// url
}
