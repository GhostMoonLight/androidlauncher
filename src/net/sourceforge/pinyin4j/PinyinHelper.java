/**
 * This file is part of pinyin4j (http://sourceforge.net/projects/pinyin4j/) 
 * and distributed under GNU GENERAL PUBLIC LICENSE (GPL).
 * 
 * pinyin4j is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 2 of the License, or 
 * (at your option) any later version. 
 * 
 * pinyin4j is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License 
 * along with pinyin4j.
 */

package net.sourceforge.pinyin4j;

import android.content.Context;

/**
 * A class provides several utility functions to convert Chinese characters
 * (both Simplified and Tranditional) into various Chinese Romanization
 * representations
 * 
 * @author Li Min (xmlerlimin@gmail.com)
 */
public class PinyinHelper
{
    /**
     * Get all unformmatted Hanyu Pinyin presentations of a single Chinese
     * character (both Simplified and Tranditional)
     * 
     * <p>
     * For example, <br/> If the input is '间', the return will be an array with
     * two Hanyu Pinyin strings: <br/> "jian1" <br/> "jian4" <br/> <br/> If the
     * input is '李', the return will be an array with single Hanyu Pinyin
     * string: <br/> "li3"
     * 
     * <p>
     * <b>Special Note</b>: If the return is "none0", that means the input
     * Chinese character exists in Unicode CJK talbe, however, it has no
     * pronounciation in Chinese
     * 
     * @param ch
     *            the given Chinese character
     * 
     * @return a String array contains all unformmatted Hanyu Pinyin
     *         presentations with tone numbers; null for non-Chinese character
     * 
     */
    static public String[] toHanyuPinyinStringArray(Context context, char ch)
    {
        return getUnformattedHanyuPinyinStringArray(context, ch);
    }

    /**
     * Get all Hanyu Pinyin presentations of a single Chinese character (both
     * Simplified and Tranditional)
     * 
     * <p>
     * For example, <br/> If the input is '间', the return will be an array with
     * two Hanyu Pinyin strings: <br/> "jian1" <br/> "jian4" <br/> <br/> If the
     * input is '李', the return will be an array with single Hanyu Pinyin
     * string: <br/> "li3"
     * 
     * <p>
     * <b>Special Note</b>: If the return is "none0", that means the input
     * Chinese character is in Unicode CJK talbe, however, it has no
     * pronounciation in Chinese
     * 
     * @param ch
     *            the given Chinese character
     * @param outputFormat
     *            describes the desired format of returned Hanyu Pinyin String
     * 
     * @return a String array contains all Hanyu Pinyin presentations with tone
     *         numbers; return null for non-Chinese character
     * 
     * @throws BadHanyuPinyinOutputFormatCombination
     *             if certain combination of output formats happens
     * 
     * @see HanyuPinyinOutputFormat
     * @see BadHanyuPinyinOutputFormatCombination
     * 
     */
    static public String[] toHanyuPinyinStringArray(Context context, char ch,
            HanyuPinyinOutputFormat outputFormat)
            throws BadHanyuPinyinOutputFormatCombination
    {
        return getFormattedHanyuPinyinStringArray(context, ch, outputFormat);
    }

    /**
     * Return the formatted Hanyu Pinyin representations of the given Chinese
     * character (both in Simplified and Tranditional) in array format.
     * 
     * @param ch
     *            the given Chinese character
     * @param outputFormat
     *            Describes the desired format of returned Hanyu Pinyin string
     * @return The formatted Hanyu Pinyin representations of the given codepoint
     *         in array format; null if no record is found in the hashtable.
     */
    static private String[] getFormattedHanyuPinyinStringArray(Context context, char ch,
            HanyuPinyinOutputFormat outputFormat)
            throws BadHanyuPinyinOutputFormatCombination
    {
        String[] pinyinStrArray = getUnformattedHanyuPinyinStringArray(context, ch);

        if (null != pinyinStrArray)
        {

            for (int i = 0; i < pinyinStrArray.length; i++)
            {
                pinyinStrArray[i] = PinyinFormatter.formatHanyuPinyin(pinyinStrArray[i], outputFormat);
            }

            return pinyinStrArray;

        } else
            return null;
    }

    /**
     * Delegate function
     * 
     * @param ch
     *            the given Chinese character
     * @return unformatted Hanyu Pinyin strings; null if the record is not found
     */
    private static String[] getUnformattedHanyuPinyinStringArray(Context context, char ch)
    {
        return ChineseToPinyinResource.getInstance(context).getHanyuPinyinStringArray(ch);
    }

    // ! Hidden constructor
    private PinyinHelper()
    {
    }
}
