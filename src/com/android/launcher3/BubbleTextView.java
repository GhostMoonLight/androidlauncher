package com.android.launcher3;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.launcher3.utils.PackageDbUtil;
import com.android.launcher3.view.CustomIconImageView;
import com.android.launcher3.wallpaper.BitmapUtils;
import com.cuan.launcher.R;

public class BubbleTextView extends RelativeLayout implements OnClickListener {
	
	private static final float SHADOW_LARGE_RADIUS = 4.0f;
//    private static final float SHADOW_SMALL_RADIUS = 1.75f;
    private static final float SHADOW_Y_OFFSET = 2.0f;
    private static final int SHADOW_LARGE_COLOUR = 0xDD000000;
//    private static final int SHADOW_SMALL_COLOUR = 0xCC000000;
    static final float PADDING_V = 3.0f;
	
	private int mTextColor;
    private final boolean mCustomShadowsEnabled;
    private boolean mIsTextVisible;
	
	// TODO: Remove custom background handling code, as no instance of BubbleTextView use any
    // background.
    private boolean mBackgroundSizeChanged;
    private final Drawable mBackground;
    private CheckLongPressHelper mLongPressHelper;
    private boolean mStayPressed;
    private boolean mIgnorePressedStateChange;
    private HolographicOutlineHelper mOutlineHelper;
    private Bitmap mPressedBackground;
    private TextView mTextView;
    private CustomIconImageView mImageView;
    private ImageView ivYellowAppSign;
    private DeviceProfile mDeviceProfile;
    private float mSlop;
	private Drawable iconBg;
	
	public BubbleTextView(Context context) {
        this(context, null, 0);
    }

    public BubbleTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BubbleTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mDeviceProfile = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.BubbleTextView, defStyle, 0);
        mCustomShadowsEnabled = a.getBoolean(R.styleable.BubbleTextView_customShadows, true);
        a.recycle();

        mBackground = getBackground();
        LayoutInflater.from(getContext()).inflate(R.layout.bubble_textview, this);
        mTextView = (TextView) findViewById(R.id.textview);
        mTextColor = getResources().getColor(R.color.workspace_icon_text_color);
        mImageView = (CustomIconImageView) findViewById(R.id.imageview);
        ivYellowAppSign = (ImageView) findViewById(R.id.iv_sign_new_app);
        
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mImageView.getLayoutParams();
		lp.width = mDeviceProfile.iconSizePx;
		lp.height = mDeviceProfile.iconSizePx;
		
		int padding = mDeviceProfile.iconDrawablePaddingPx;
		lp.setMargins(padding, padding, padding, (int)(padding*0.9));
        init();
    }
    
    private void init() {
        mLongPressHelper = new CheckLongPressHelper(this);

        mOutlineHelper = HolographicOutlineHelper.obtain(getContext());
        mTextView.setShadowLayer(SHADOW_LARGE_RADIUS, 0.0f, SHADOW_Y_OFFSET, SHADOW_LARGE_COLOUR);
    }
    
    public void onFinishInflate() {
        super.onFinishInflate();

        // Ensure we are using the right text size
        LauncherAppState app = LauncherAppState.getInstance();
        DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, grid.iconTextSizePx);
    }
    
    public ImageView getImageView(){
		return mImageView;
	}
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mBackground != null) mBackground.setCallback(this);

        mSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mBackground != null) mBackground.setCallback(null);
    }

    public void setTextColor(int color) {
        mTextColor = color;
        mTextView.setTextColor(color);
    }
    
    public void applyFromShortcutInfo(ShortcutInfo info, IconCache iconCache) {
        applyFromShortcutInfo(info, iconCache, false);
    }
    

    public void applyFromShortcutInfo(ShortcutInfo info, IconCache iconCache,
            boolean setDefaultPadding) {
        applyFromShortcutInfo(info, iconCache, setDefaultPadding, false);
    }
    
    public void applyFromShortcutInfo(ShortcutInfo info, IconCache iconCache,
            boolean setDefaultPadding, boolean promiseStateChanged) {
        Bitmap b = info.getIcon(iconCache);
        FastBitmapDrawable iconDrawable;
        info.resName = IconCache.getDefaultIconResourceName(getContext(), info.title.toString(), info.getPckName(), PackageDbUtil.getInstance(getContext()).getTargetPackages(info.getPckName()));
        
        if (!TextUtils.isEmpty(info.resName)){
        	Bitmap bitmap = BitmapUtils.getBitmap(info.resName);
        	if(bitmap == null){
        		bitmap = b;
        	}
        	iconDrawable = Utilities.createIconDrawable(bitmap);
        }else{
        	iconDrawable = Utilities.createIconDrawable(b);
        }
        iconDrawable.setGhostModeEnabled(info.isDisabled);
        info.themeDrawable = iconDrawable;
        setCompoundDrawablesWithIntrinsicBounds(info.themeDrawable);
        
        if (info.contentDescription != null) {
            setContentDescription(info.contentDescription);
        }
        mImageView.setResourceName(info.resName);
        mImageView.setTag(info);
        mTextView.setText(info.title);
        setTag(info);
    }
    
    public void applyFromApplicationInfo(AppInfo info) {
        LauncherAppState app = LauncherAppState.getInstance();
        DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();

        Drawable topDrawable = Utilities.createIconDrawable(info.iconBitmap);
        topDrawable.setBounds(0, 0, grid.allAppsIconSizePx, grid.allAppsIconSizePx);
        setCompoundDrawablesWithIntrinsicBounds(topDrawable);
        mTextView.setText(info.title);
        if (info.contentDescription != null) {
            setContentDescription(info.contentDescription);
        }
        setTag(info);
    }
    
    public void fromShortcutInfo(ShortcutInfo info, IconCache iconCache) {
    	setTag(info);
		Bitmap b;
		if (info.themeDrawable != null){
			setCompoundDrawablesWithIntrinsicBounds(info.themeDrawable);
		}else{
			b = info.getIcon(iconCache);
			setCompoundDrawablesWithIntrinsicBounds(new FastBitmapDrawable(b));
		}
		mImageView.setResourceName(info.resName);
		setText(info.title);
    }
    
    public void setCompoundDrawablesWithIntrinsicBounds(Drawable drawable) {
    	mImageView.setImageDrawable(drawable);
    }
    
    public void setImageViewBitmap(Bitmap b){
		ShortcutInfo info = (ShortcutInfo) getTag();
		info.setIcon(b);
		setCompoundDrawablesWithIntrinsicBounds(new FastBitmapDrawable(b));
	}
    
    public Drawable getCompoundDrawable() {
    	ShortcutInfo info = (ShortcutInfo) getTag();
    	Drawable drawable = mImageView.getDrawable();
    	if (drawable == null){
	    	if("ic_alarmclock".equals(info.resName)){
	    		if (iconBg == null){
	    			Bitmap bitmap = BitmapUtils.getBitmap("ic_alarmclock_bg");
	    			iconBg = Utilities.createIconDrawable(bitmap);
	    		}
				return iconBg;
			} else if ("ic_calendar".equals(info.resName)){
				if (iconBg == null){
					Bitmap bitmap = BitmapUtils.getBitmap("ic_calendar_bg");
	    			iconBg = Utilities.createIconDrawable(bitmap);
				}
				return iconBg;
			}
    	}
		return drawable;
	}
    
    public void setTextVisibility(boolean visible) {
        Resources res = getResources();
        if (visible) {
            setTextColor(mTextColor);
        } else {
            setTextColor(res.getColor(android.R.color.transparent));
        }
        mIsTextVisible = visible;
    }

    public boolean isTextVisible() {
        return mIsTextVisible;
    }
    
    @Override
    public void cancelLongPress() {
        super.cancelLongPress();

        mLongPressHelper.cancelLongPress();
    }
    
    public void setText(int resid) {
        mTextView.setText(resid);
    }
	
	public void setText(CharSequence text) {
		mTextView.setText(text);
    }
	
	public Layout getLayout() {
        return mTextView.getLayout();
    }
	
	public int getExtendedPaddingTop() {
		return mTextView.getExtendedPaddingTop();
	}
	
	public TextView getTextView(){
		return mTextView;
	}
	
    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mBackground || super.verifyDrawable(who);
    }

    @Override
    public void setTag(Object tag) {
        if (tag != null) {
            LauncherModel.checkItemInfo((ItemInfo) tag);
        }
        super.setTag(tag);
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);

        if (!mIgnorePressedStateChange) {
            updateIconState();
        }
    }

    private void updateIconState() {
        Drawable top = getCompoundDrawable();
        if (top instanceof FastBitmapDrawable) {
            ((FastBitmapDrawable) top).setPressed(isPressed() || mStayPressed);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Call the superclass onTouchEvent first, because sometimes it changes the state to
        // isPressed() on an ACTION_UP
        boolean result = super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // So that the pressed outline is visible immediately on setStayPressed(),
                // we pre-create it on ACTION_DOWN (it takes a small but perceptible amount of time
                // to create it)
                if (mPressedBackground == null) {
                    mPressedBackground = mOutlineHelper.createMediumDropShadow(this);
                }

                mLongPressHelper.postCheckForLongPress();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // If we've touched down and up on an item, and it's still not "pressed", then
                // destroy the pressed outline
                if (!isPressed()) {
                    mPressedBackground = null;
                }

                mLongPressHelper.cancelLongPress();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!Utilities.pointInView(this, event.getX(), event.getY(), mSlop)) {
                    mLongPressHelper.cancelLongPress();
                }
                break;
        }
        return result;
    }

    void setStayPressed(boolean stayPressed) {
        mStayPressed = stayPressed;
        if (!stayPressed) {
            mPressedBackground = null;
        }

        // Only show the shadow effect when persistent pressed state is set.
        if (getParent() instanceof ShortcutAndWidgetContainer) {
            CellLayout layout = (CellLayout) getParent().getParent();
            layout.setPressedIcon(this, mPressedBackground, mOutlineHelper.shadowBitmapPadding);
        }

        updateIconState();
    }

    void clearPressedBackground() {
        setPressed(false);
        setStayPressed(false);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Unlike touch events, keypress event propagate pressed state change immediately,
        // without waiting for onClickHandler to execute. Disable pressed state changes here
        // to avoid flickering.
        mIgnorePressedStateChange = true;
        boolean result = super.onKeyUp(keyCode, event);

        mPressedBackground = null;
        mIgnorePressedStateChange = false;
        updateIconState();
        return result;
    }
	

	@Override
	public void onClick(View v) {

	}

	public void setCompoundDrawablePadding(int i) {
	}

}
