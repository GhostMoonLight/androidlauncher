/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.launcher3.bean;

import com.android.launcher3.ItemInfo;

import android.appwidget.AppWidgetHostView;
import android.content.ContentValues;
import android.content.Context;

/**
 * Represents a widget (either instantiated or about to be) in the Launcher.
 */
public class CustomWidgetInfo extends ItemInfo {


	public String providerName;

	// TODO: Are these necessary here?
	public int minWidth = -1;
	public int minHeight = -1;

	public int previewImageResId;
	
	/**
	 * View that holds this widget after it's been created. This view isn't created until Launcher knows it's needed.
	 */
	public AppWidgetHostView hostView = null;

	/**
	 * Constructor for use with AppWidgets that haven't been instantiated yet.
	 */
	public CustomWidgetInfo() {
		// Since the widget isn't instantiated yet, we don't know these values. Set them to -1
		// to indicate that they should be calculated based on the layout and minWidth/minHeight
		spanX = -1;
		spanY = -1;
	}


	@Override
	public void onAddToDatabase(Context context, ContentValues values) {
		super.onAddToDatabase(context, values);
	}

	@Override
	public String toString() {
		return "";
	}

	@Override
	public void unbind() {
		super.unbind();
		hostView = null;
	}
	
	public void copyFrom(CustomWidgetInfo result) {
		// TODO Auto-generated method stub
		super.copyFrom(result);
		result.providerName = providerName;
		result.minWidth = minWidth;
		result.minHeight = minHeight;
		result.previewImageResId = previewImageResId;
		result.hostView = hostView;
	}
}
