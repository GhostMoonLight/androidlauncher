package com.android.launcher3.utils;

/**
 * Created by wen on 2016/10/17.
 * 一些常见的算法
 */

public class Algorithm {
    /**
     * 最大公约数：
     * 碾转相除法，又名欧几里得算法。两个正整数a和b（a>b），它们的最大公约数等于a除以b的余数c和b之间的最大公约数，直到两个数可以整除或者其中一个数减小到1为止（y也就是取模为0时）。  取模运算的性能比较低
     * 更相减损术，两个正整数a和b（a>b），它们的最大公约数等于a-b的差值和较小数b的最大公约数，直到两个数相等为止。  不稳定算法，运算的次数不去定，一般都会比取模运算次数多
     *
     * 在更相减损术的基础上使用移位运算：
     *      移位运算的性能非常快。当A和B中有偶数时就算进行移位运算a>>1或b>>1，当A和B都为奇数时，利用更相减损术运算一次。递归，直到两个数相等
     *
     */
    public static int maxCommonDivisor(int number1, int number2){
        if (number1 == number2){
            return number1;
        }

        if (number1 < number2){
            //保证number1永远大于number2，为了减少代码量
            return maxCommonDivisor(number2, number1);
        } else {
            if (isOdd(number1) && isOdd(number2)){
                return maxCommonDivisor(number1>>1, number2>>1) >> 1;
            } else if (isOdd(number1) && !isOdd(number2)){
                return maxCommonDivisor(number1>>1, number2);
            } else if (!isOdd(number1) && isOdd(number2)){
                return maxCommonDivisor(number1, number2>>1);
            } else {
                return maxCommonDivisor(number1, number1 - number2);
            }
        }
    }

    // 求最小公倍数
    public static int minCommonMultiple(int m, int n) {
        return m * n / maxCommonDivisor(m, n);
    }

    public static boolean isOdd(int i){
        return (i & 1) != 0;
    }
}
