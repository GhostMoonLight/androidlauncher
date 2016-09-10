package com.android.launcher3.view;

import java.util.Arrays;
import java.util.List;

import com.cuan.launcher.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class SideBar extends View {
	// 触摸事件
	private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
	// 26个字母
	public static List<String> b =  Arrays.asList(new String[]{ "#", "A", "B", "C", "D", "E", "F", "G", "H", "I",
			"J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
			"W", "X", "Y", "Z" });
	private int choose = -1;// 选中
	private int totalCount = 0;// 选中数量
	private Paint paint = new Paint();

	private TextView mTextDialog;

	public void setTextView(TextView mTextDialog) {
		this.mTextDialog = mTextDialog;
	}


	public SideBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SideBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SideBar(Context context) {
		super(context);
	}

	/**
	 * 重写这个方法
	 */
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 获取焦点改变背景颜色.
		int height = getHeight();// 获取对应高度
		int width = getWidth(); // 获取对应宽度
		int singleHeight = height / b.size();// 获取每一个字母的高度

		for (int i = 0; i < b.size(); i++) {
			paint.setColor(Color.parseColor("#66ffffff"));
			// paint.setColor(Color.WHITE);
			paint.setTypeface(Typeface.DEFAULT_BOLD);
			paint.setAntiAlias(true);
			paint.setTextSize(getContext().getResources().getDimensionPixelOffset(R.dimen.letter_text_size));
			// 选中的状态
			if (i >= choose && i <= choose+totalCount) {
				paint.setColor(Color.parseColor("#ffffff"));
				paint.setFakeBoldText(true);
			}
			// x坐标等于中间-字符串宽度的一半.
			float xPos = width / 2 - paint.measureText(b.get(i)) / 2 - 4;
			float yPos = singleHeight * i + singleHeight;
			canvas.drawText(b.get(i), xPos, yPos, paint);
			paint.reset();// 重置画笔
		}
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		
		final int action = event.getAction();
		final float y = event.getY();// 点击y坐标
		final int oldChoose = choose;
		final int c = (int) (y / getHeight() * b.size());// 点击y坐标所占总高度的比例*b数组的长度就等于点击b中的个数.

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			getParent().requestDisallowInterceptTouchEvent(true);
			changeLetter(oldChoose, c);
			break;
		case MotionEvent.ACTION_UP:
			if (mTextDialog != null) {
				mTextDialog.setVisibility(View.INVISIBLE);
			}
			break;
		default:
			changeLetter(oldChoose, c);
			break;
		}
		return true;
	}
	
	private void changeLetter(int oldChoose, int c){
		if (oldChoose != c) {
			final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
			if (c >= 0 && c < b.size()) {
				if (listener != null) {
					listener.onTouchingLetterChanged(b.get(c));
				}
				if (mTextDialog != null) {
					mTextDialog.setText(b.get(c));
					mTextDialog.setVisibility(View.VISIBLE);
				}
				choose = c;
				invalidate();
			}
		}
	}
	
	public void setSelection(String name, String endName){
		choose = b.indexOf(name);
		this.totalCount = b.indexOf(endName) - choose;
		invalidate();
	}

	/**
	 * 向外公开的方法
	 * 
	 * @param onTouchingLetterChangedListener
	 */
	public void setOnTouchingLetterChangedListener(
			OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
		this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
	}

	/**
	 * 接口
	 * 
	 * @author coder
	 * 
	 */
	public interface OnTouchingLetterChangedListener {
		public void onTouchingLetterChanged(String s);
	}
}