/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import android.os.Handler;

/**
 * 可以定时执行的Runnable，在UI线程中执行
 * 利用Handler实现
 */
public class Alarm implements Runnable{
    // if we reach this time and the alarm hasn't been cancelled, call the listener
	// 定时任务的触发时间，如果到了这个时间任务没有被取消(mAlarmTriggerTime为0时，该Runnable还执行但是逻辑不执行), 则执行监听回调
    private long mAlarmTriggerTime;

    // if we've scheduled a call to run() (ie called mHandler.postDelayed), this variable is true.
    // We use this to avoid having multiple pending callbacks
    //如果我们已经设置了一个run(),这个值会被置为true，用来避免多个挂起的回调
    //也就是说只会设置一个定时run
    private boolean mWaitingForCallback;

    private Handler mHandler;
    private OnAlarmListener mAlarmListener;
    private boolean mAlarmPending = false;    //是否是定时等待时间,已经设置了定时任务，还没开始执行

    public Alarm() {
        mHandler = new Handler();
    }

    public void setOnAlarmListener(OnAlarmListener alarmListener) {
        mAlarmListener = alarmListener;
    }

    // Sets the alarm to go off in a certain number of milliseconds. If the alarm is already set,
    // it's overwritten and only the new alarm setting is used
    public void setAlarm(long millisecondsInFuture) {
        long currentTime = System.currentTimeMillis();
        mAlarmPending = true;
        mAlarmTriggerTime = currentTime + millisecondsInFuture;
        if (!mWaitingForCallback) {
            mHandler.postDelayed(this, mAlarmTriggerTime - currentTime);
            mWaitingForCallback = true;
        }
    }

    public void cancelAlarm() {
        mAlarmTriggerTime = 0;
        mAlarmPending = false;
    }

    // this is called when our timer runs out
    public void run() {
        mWaitingForCallback = false;
        if (mAlarmTriggerTime != 0) {
            long currentTime = System.currentTimeMillis();
            if (mAlarmTriggerTime > currentTime) {
                // We still need to wait some time to trigger spring loaded mode--
                // post a new callback
                mHandler.postDelayed(this, Math.max(0, mAlarmTriggerTime - currentTime));
                mWaitingForCallback = true;
            } else {
                mAlarmPending = false;
                if (mAlarmListener != null) {
                    mAlarmListener.onAlarm(this);
                }
            }
        }
    }

    public boolean alarmPending() {
        return mAlarmPending;
    }
}
