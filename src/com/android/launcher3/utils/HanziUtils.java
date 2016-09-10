package com.android.launcher3.utils;

import net.sourceforge.pinyin4j.BadHanyuPinyinOutputFormatCombination;
import net.sourceforge.pinyin4j.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.PinyinHelper;
import android.content.Context;

/**
 * Created by ChunLei on 2015/6/14.
 */
public class HanziUtils {
    /**
     * 把汉字转成拼音
     *
     * @param src
     * @return
     */
    public static String getNamePinyin(Context context, String src) {

        char[] t1 = null;
        t1 = src.toCharArray();
        // System.out.println(t1.length);
        String[] t2 = new String[t1.length];
        // System.out.println(t2.length);
        // 设置汉字拼音输出的格式
        HanyuPinyinOutputFormat t3 = new HanyuPinyinOutputFormat();
        t3.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        t3.setVCharType(HanyuPinyinVCharType.WITH_V);
        String t4 = "";
        int t0 = t1.length;
        try {
            for (int i = 0; i < t0; i++) {
                // 判断能否为汉字字符
                // System.out.println(t1[i]);
                if (Character.toString(t1[i]).matches("[\\u4E00-\\u9FA5]+")) {
                    t2 = PinyinHelper.toHanyuPinyinStringArray(context, t1[i], t3);// 将汉字的几种全拼都存到t2数组中
                    t4 += t2[0] + " ";// 取出该汉字全拼的第一种读音并连接到字符串t4后
                } else {
                    // 如果不是汉字字符，间接取出字符并连接到字符串t4后
                    t4 += Character.toString(t1[i]);
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        return t4;
    }
}
