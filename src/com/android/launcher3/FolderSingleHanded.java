package com.android.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.InputType;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.android.launcher3.utils.Log;
import com.android.launcher3.utils.Util;
import com.android.launcher3.view.SingleHandedView;
import com.android.launcher3.wallpaper.BitmapUtils;
import com.cuan.launcher.R;

import java.util.ArrayList;
import java.util.Collections;
/**
 * 支持下滑半屏的文件夹
 */
public class FolderSingleHanded extends FolderBase {
    private static final String TAG = "Launcher.Folder";

    private int mFolderNameHeight;
    private SingleHandedView mScrollView;
    private View mContainer;

    /**
     * Used to inflate the Workspace from XML.
     *
     * @param context The application's context.
     * @param attrs The attribtues set containing the Workspace's customization values.
     */
    public FolderSingleHanded(Context context, AttributeSet attrs) {
        super(context, attrs);

        LauncherAppState app = LauncherAppState.getInstance();
        DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
        setAlwaysDrawnWithCacheEnabled(false);

        mMaxCountX = (int) grid.numColumns;
        // Allow scrolling folders when DISABLE_ALL_APPS is true.
        if (LauncherAppState.isDisableAllApps()) {
            mMaxCountY = mMaxNumItems = Integer.MAX_VALUE;
        } else {
            mMaxCountY = (int) grid.numRows;
            mMaxNumItems = mMaxCountX * mMaxCountY;
        }
        
        mLauncher = (Launcher) context;
        // We need this view to be focusable in touch mode so that when text editing of the folder
        // name is complete, we have something to focus on, thus hiding the cursor and giving
        // reliable behvior when clicking the text field (since it will always gain focus on click).
        setFocusableInTouchMode(true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContainer = findViewById(R.id.container);
        mScrollView = (SingleHandedView) findViewById(R.id.scroll_view);
        mScrollView.setFolder(this);
        mContent = (CellLayout) findViewById(R.id.folder_content);
        mContent.setPadding(0, 0, 0, Util.dip2px(getContext(), 7));

        mContent.addView(mFocusIndicatorHandler, 0);
        mFocusIndicatorHandler.getLayoutParams().height = FocusIndicatorView.DEFAULT_LAYOUT_SIZE;
        mFocusIndicatorHandler.getLayoutParams().width = FocusIndicatorView.DEFAULT_LAYOUT_SIZE;

        LauncherAppState app = LauncherAppState.getInstance();
        DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
        int mContentWidth = getResources().getDisplayMetrics().widthPixels - mScrollView.getPaddingLeft() - mScrollView.getPaddingRight();
        grid.calculateCellWidthWithCount(mContentWidth, 4);
        mContent.setCellDimensions(grid.folderCellWidthPx, grid.folderCellHeightPx);
        mContent.setGridSize(0, 0);
        mContent.getShortcutsAndWidgets().setMotionEventSplittingEnabled(false);
        mContent.setInvertIfRtl(true);
        mContent.setOnClickListener(this);
        mFolderName = (FolderEditText) findViewById(R.id.folder_name);
        mFolderName.setFolder(this);
        mFolderName.setOnFocusChangeListener(this);

        // We find out how tall the text view wants to be (it is set to wrap_content), so that
        // we can allocate the appropriate amount of space for it.
        int measureSpec = MeasureSpec.UNSPECIFIED;
        mFolderName.measure(measureSpec, measureSpec);
        mFolderNameHeight = mFolderName.getMeasuredHeight();

        // We disable action mode for now since it messes up the view on phones
        mFolderName.setCustomSelectionActionModeCallback(mActionModeCallback);
        mFolderName.setOnEditorActionListener(this);
        mFolderName.setSelectAllOnFocus(true);
        mFolderName.setInputType(mFolderName.getInputType() |
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        mAutoScrollHelper = new FolderSingleHandedAutoScrollHelper(mScrollView);
        
        if (mLauncher.isFullScreen()){
        	setPadding(0, mLauncher.getStatusBarHeight(), 0, Util.dip2px(getContext(), 6));
        }
    }
    
    public Rect getContainerRect(){
    	Rect rect = new Rect();
    	int top = mScrollView.getTop()-mScrollView.getScrollY()-10;
//    	rect.set(mScrollView.getPaddingLeft(), top, 0+mContainer.getWidth(), top+mContainer.getHeight());
    	rect.set(0, top, 0+mScrollView.getWidth(), top+mContainer.getHeight()+35);
    	return rect;
    }

    public void onClick(View v) {
        Object tag = v.getTag();
        if (tag instanceof ShortcutInfo) {
            mLauncher.onClick(v);
        } else {
        	mLauncher.closeFolder();
        }
    }

    public boolean onLongClick(View v) {
        // Return if global dragging is not enabled
        if (!mLauncher.isDraggingEnabled()) return true;

        Object tag = v.getTag();
        if (tag instanceof ShortcutInfo) {
            ShortcutInfo item = (ShortcutInfo) tag;
            if (!v.isInTouchMode()) {
                return false;
            }

            mLauncher.getWorkspace().beginDragShared(v, this);

            mCurrentDragInfo = item;
            mEmptyCell[0] = item.cellX;
            mEmptyCell[1] = item.cellY;
            mCurrentDragView = v;

            mContent.removeView(mCurrentDragView);
            mInfo.remove(mCurrentDragInfo);
            mDragInProgress = true;
            mItemAddedBackToSelfViaIcon = false;
        }
        return true;
    }
    
    @Override
    public boolean isEditingName() {
        return mIsEditingName;
    }

    //当获取焦点的时候，FolderEditText开始编辑文件夹名称
    public void startEditingFolderName() {
        mFolderName.setHint("");
        mIsEditingName = true;
    }

    @Override
    public void dismissEditingName() {
        mInputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        doneEditingFolderName(true);
    }

    @Override
    public void doneEditingFolderName(boolean commit) {
        mFolderName.setHint(sHintText);
        // Convert to a string here to ensure that no other state associated with the text field
        // gets saved.
        String newTitle = mFolderName.getText().toString();
        if (TextUtils.isEmpty(newTitle)){
        	newTitle = sDefaultFolderName;
        }
        mInfo.setTitle(newTitle);
        LauncherModel.updateItemInDatabase(mLauncher, mInfo);

        if (commit) {
            sendCustomAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                    String.format(getContext().getString(R.string.folder_renamed), newTitle));
        }
        // In order to clear the focus from the text field, we set the focus on ourself. This
        // ensures that every time the field is clicked, focus is gained, giving reliable behavior.
        requestFocus();

        Selection.setSelection((Spannable) mFolderName.getText(), 0, 0);
        mIsEditingName = false;
    }

    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            dismissEditingName();
            return true;
        }
        return false;
    }

    public View getEditTextRegion() {
        return mFolderName;
    }

    public CellLayout getContent() {
        return mContent;
    }

    /**
     * We need to handle touch events to prevent them from falling through to the workspace below.
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
    	switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
        	break;
        case MotionEvent.ACTION_MOVE:
        	break;
        case MotionEvent.ACTION_UP:
        	Rect rect = getContainerRect();
        	if (!rect.contains((int)ev.getRawX(), (int)ev.getRawY())){
        		mLauncher.closeFolder();
        	}
        	break;
    	}
    	
        return true;
    }

    public void setDragController(DragController dragController) {
        mDragController = dragController;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        // When the folder gets focus, we don't want to announce the list of items.
        return true;
    }

    /**
     * @return the FolderInfo object associated with this folder
     */
    FolderInfo getInfo() {
        return mInfo;
    }

    private void placeInReadingOrder(ArrayList<ShortcutInfo> items) {
        int maxX = 0;
        int count = items.size();
        for (int i = 0; i < count; i++) {
            ShortcutInfo item = items.get(i);
            if (item.cellX > maxX) {
                maxX = item.cellX;
            }
        }

        GridComparator gridComparator = new GridComparator(maxX + 1);
        Collections.sort(items, gridComparator);
        final int countX = mContent.getCountX();
        for (int i = 0; i < count; i++) {
            int x = i % countX;
            int y = i / countX;
            ShortcutInfo item = items.get(i);
            item.cellX = x;
            item.cellY = y;
        }
    }

    void bind(FolderInfo info) {
        mInfo = info;
        ArrayList<ShortcutInfo> children = info.contents;
        ArrayList<ShortcutInfo> overflow = new ArrayList<ShortcutInfo>();
        setupContentForNumItems(children.size());
        placeInReadingOrder(children);
        int count = 0;
        for (int i = 0; i < children.size(); i++) {
            ShortcutInfo child = (ShortcutInfo) children.get(i);
            if (createAndAddShortcut(child) == null) {
                overflow.add(child);
            } else {
                count++;
            }
        }

        // We rearrange the items in case there are any empty gaps
        setupContentForNumItems(count);

        // If our folder has too many items we prune them from the list. This is an issue
        // when upgrading from the old Folders implementation which could contain an unlimited
        // number of items.
        for (ShortcutInfo item: overflow) {
            mInfo.remove(item);
            LauncherModel.deleteItemFromDatabase(mLauncher, item);
        }

        mItemsInvalidated = true;
        updateTextViewFocus();
        mInfo.addListener(this);

        if (!sDefaultFolderName.contentEquals(mInfo.title)) {
            mFolderName.setText(mInfo.title);
        } else {
            mFolderName.setText("");
        }
        mFolderName.setText(mInfo.title);
        updateItemLocationsInDatabase();

        // In case any children didn't come across during loading, clean up the folder accordingly
//        mFolderIcon.post(new Runnable() {
//            public void run() {
//                if (getItemCount() <= 1) {
//                    replaceFolderWithFinalItem();
//                }
//            }
//        });
    }

    /**
     * Creates a new UserFolder, inflated from R.layout.user_folder.
     *
     * @param context The application's context.
     *
     * @return A new UserFolder.
     */
    static FolderSingleHanded fromXml(Context context) {
        return (FolderSingleHanded) LayoutInflater.from(context).inflate(R.layout.user_folder_singlehanded, null);
    }

    /**
     * This method is intended to make the UserFolder to be visually identical in size and position
     * to its associated FolderIcon. This allows for a seamless transition into the expanded state.
     */
    private void positionAndSizeAsIcon() {
        if (!(getParent() instanceof DragLayer)) return;
        setScaleX(0.8f);
        setScaleY(0.8f);
        setAlpha(0f);
        mState = STATE_SMALL;
    }

    private void prepareReveal() {
        setScaleX(1f);
        setScaleY(1f);
        setAlpha(1f);
        mState = STATE_SMALL;
    }

    public void animateOpen() {
        long time = System.currentTimeMillis();
        if (!(getParent() instanceof DragLayer)) return;
        final ViewGroup parent = ((ViewGroup)getParent());
        Bitmap bluredBitmap = BitmapUtils.getBluredBackgroundImage((Launcher)getContext());
//        Bitmap bluredBitmap = mLauncher.buleScreen;
        parent.setBackgroundDrawable(new BitmapDrawable(getResources(), bluredBitmap));
        parent.getBackground().setAlpha(0);
		startBackgroundAnimator(true);
        Log.w("AAAAA", "time:"+(System.currentTimeMillis()-time));
        Animator openFolderAnim = null;
        final Runnable onCompleteRunnable;
        if (!Utilities.isLmpOrAbove()) {
            positionAndSizeAsIcon();
            centerAboutIcon();

            PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1);
            PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f);
            PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f);
            final ObjectAnimator oa =
                LauncherAnimUtils.ofPropertyValuesHolder(this, alpha, scaleX, scaleY);
            oa.setDuration(mExpandDuration);
            openFolderAnim = oa;

            setLayerType(LAYER_TYPE_HARDWARE, null);
            onCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    setLayerType(LAYER_TYPE_NONE, null);
                }
            };
        } else {
            prepareReveal();
            centerAboutIcon();

            int width = getPaddingLeft() + getPaddingRight() + mContent.getDesiredWidth();
            int height = getFolderHeight();

            float transX = - 0.075f * (width / 2 - getPivotX());
            float transY = - 0.075f * (height / 2 - getPivotY());
            setTranslationX(transX);
            setTranslationY(transY);
            PropertyValuesHolder tx = PropertyValuesHolder.ofFloat("translationX", transX, 0);
            PropertyValuesHolder ty = PropertyValuesHolder.ofFloat("translationY", transY, 0);

            int rx = (int) Math.max(Math.max(width - getPivotX(), 0), getPivotX());
            int ry = (int) Math.max(Math.max(height - getPivotY(), 0), getPivotY());
            float radius = (float) Math.sqrt(rx * rx + ry * ry);
            AnimatorSet anim = LauncherAnimUtils.createAnimatorSet();
            Animator reveal = LauncherAnimUtils.createCircularReveal(this, (int) getPivotX(),
                    (int) getPivotY(), 0, radius);
            reveal.setDuration(mMaterialExpandDuration);
            reveal.setInterpolator(new LogDecelerateInterpolator(100, 0));

            mContent.setAlpha(0f);
            Animator iconsAlpha = LauncherAnimUtils.ofFloat(mContent, "alpha", 0f, 1f);
            iconsAlpha.setDuration(mMaterialExpandDuration);
            iconsAlpha.setStartDelay(mMaterialExpandStagger);
            iconsAlpha.setInterpolator(new AccelerateInterpolator(1.5f));

            mFolderName.setAlpha(0f);
            Animator textAlpha = LauncherAnimUtils.ofFloat(mFolderName, "alpha", 0f, 1f);
            textAlpha.setDuration(mMaterialExpandDuration);
            textAlpha.setStartDelay(mMaterialExpandStagger);
            textAlpha.setInterpolator(new AccelerateInterpolator(1.5f));

            Animator drift = LauncherAnimUtils.ofPropertyValuesHolder(this, tx, ty);
            drift.setDuration(mMaterialExpandDuration);
            drift.setStartDelay(mMaterialExpandStagger);
            drift.setInterpolator(new LogDecelerateInterpolator(60, 0));

            anim.play(drift);
            anim.play(iconsAlpha);
            anim.play(textAlpha);
            anim.play(reveal);

            openFolderAnim = anim;

            mContent.setLayerType(LAYER_TYPE_HARDWARE, null);
            onCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    mContent.setLayerType(LAYER_TYPE_NONE, null);
                }
            };
        }
        openFolderAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                sendCustomAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                        String.format(getContext().getString(R.string.folder_opened),
                        mContent.getCountX(), mContent.getCountY()));
                mState = STATE_ANIMATING;
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                mState = STATE_OPEN;

                if (onCompleteRunnable != null) {
                    onCompleteRunnable.run();
                }

                setFocusOnFirstChild();
            }
        });
        openFolderAnim.start();

        // Make sure the folder picks up the last drag move even if the finger doesn't move.
        if (mDragController.isDragging()) {
            mDragController.forceTouchMove();
        }
    }

    @Override
    public void beginExternalDrag(ShortcutInfo item) {
        setupContentForNumItems(getItemCount() + 1);
        findAndSetEmptyCells(item);

        mCurrentDragInfo = item;
        mEmptyCell[0] = item.cellX;
        mEmptyCell[1] = item.cellY;
        mIsExternalDrag = true;

        mDragInProgress = true;
    }

    private void sendCustomAccessibilityEvent(int type, String text) {
        AccessibilityManager accessibilityManager = (AccessibilityManager)
                getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain(type);
            onInitializeAccessibilityEvent(event);
            event.getText().add(text);
            accessibilityManager.sendAccessibilityEvent(event);
        }
    }

    public void animateClosed() {
        if (!(getParent() instanceof DragLayer)) return;
        
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 0.9f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 0.9f);
        final ObjectAnimator oa =
                LauncherAnimUtils.ofPropertyValuesHolder(this, alpha, scaleX, scaleY);

        oa.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
            	if (getParent() != null)
            		((ViewGroup)getParent()).setBackgroundDrawable(null);
                onCloseComplete();
                setLayerType(LAYER_TYPE_NONE, null);
                mState = STATE_SMALL;
                resetContainer();
            }
            @Override
            public void onAnimationStart(Animator animation) {
                sendCustomAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                        getContext().getString(R.string.folder_closed));
                mState = STATE_ANIMATING;
            }
        });
        oa.setDuration(mExpandDuration);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        oa.start();
        startBackgroundAnimator(false);
    }
    
    private void startBackgroundAnimator(final boolean isFadein) {
		ValueAnimator alphaAnimation = ValueAnimator.ofFloat(0.f, 1.f);
		alphaAnimation.setInterpolator(new LinearInterpolator());
		alphaAnimation.setDuration(mExpandDuration);
		alphaAnimation.addUpdateListener(new AnimatorUpdateListener() {
			public void onAnimationUpdate(ValueAnimator animation) {
				try {
					float f = (Float)animation.getAnimatedValue();
					float reverseF = 1.f - f;
					Drawable bgDrawable = ((ViewGroup) getParent()).getBackground();
					bgDrawable.setAlpha((int) (f * 255));					
					mLauncher.getWorkspace().setAlpha(reverseF);
					mLauncher.getHotseat().setAlpha(reverseF);
					mLauncher.getWorkspace().getPageIndicator().setAlpha(reverseF);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		});

		if (!isFadein) {
			mLauncher.getWorkspace().setVisibility(View.VISIBLE);
			mLauncher.getHotseat().setVisibility(View.VISIBLE);
			mLauncher.showDockDivider(false);
		}

		alphaAnimation.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				if (isFadein && mInfo.opened) {
					try {
						mLauncher.getWorkspace().setVisibility(View.INVISIBLE);
						mLauncher.getHotseat().setVisibility(View.INVISIBLE);
						mLauncher.hideDockDivider();
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}else{
				}
			}
		});
		if (!isFadein) {
			alphaAnimation.reverse();
		}
		else {
			alphaAnimation.start();
		}
	}
    
    @Override
    public void onDragEnter(DragObject dragObject) {
    	super.onDragEnter(dragObject);
    	mContent.setBackgroundColor(getResources().getColor(R.color.folder_bg_drag_scrollview));
    }
    
    @Override
    public void onDragExit(DragObject dragObject) {
    	super.onDragExit(dragObject);
    	mContent.setBackgroundColor(getResources().getColor(R.color.transparent));
    }

    public void onDragOver(DragObject d) {
        final DragView dragView = d.dragView;
        final int scrollOffset = mScrollView.getScrollY();
        final float[] r = getDragViewVisualCenter(d.x, d.y, d.xOffset, d.yOffset, dragView, null);
        r[0] -= getPaddingLeft();
        r[1] -= getPaddingTop();

        final long downTime = SystemClock.uptimeMillis();
        final MotionEvent translatedEv = MotionEvent.obtain(
                downTime, downTime, MotionEvent.ACTION_MOVE, d.x, d.y, 0);

        if (!mAutoScrollHelper.isEnabled()) {
            mAutoScrollHelper.setEnabled(true);
        }

        final boolean handled = mAutoScrollHelper.onTouch(this, translatedEv);
        translatedEv.recycle();

        if (handled) {
            mReorderAlarm.cancelAlarm();
        } else {
            mTargetCell = mContent.findNearestArea(
                    (int) r[0], (int) r[1] + scrollOffset, 1, 1, mTargetCell);
            if (isLayoutRtl()) {
                mTargetCell[0] = mContent.getCountX() - mTargetCell[0] - 1;
            }
            if (mTargetCell[0] != mPreviousTargetCell[0]
                    || mTargetCell[1] != mPreviousTargetCell[1]) {
                mReorderAlarm.cancelAlarm();
                mReorderAlarm.setOnAlarmListener(mReorderAlarmListener);
                mReorderAlarm.setAlarm(REORDER_DELAY);
                mPreviousTargetCell[0] = mTargetCell[0];
                mPreviousTargetCell[1] = mTargetCell[1];
            }
        }
    }

    // This is used to compute the visual center of the dragView. The idea is that
    // the visual center represents the user's interpretation of where the item is, and hence
    // is the appropriate point to use when determining drop location.
    private float[] getDragViewVisualCenter(int x, int y, int xOffset, int yOffset,
            DragView dragView, float[] recycle) {
        float res[];
        if (recycle == null) {
            res = new float[2];
        } else {
            res = recycle;
        }

        // These represent the visual top and left of drag view if a dragRect was provided.
        // If a dragRect was not provided, then they correspond to the actual view left and
        // top, as the dragRect is in that case taken to be the entire dragView.
        // R.dimen.dragViewOffsetY.
        int left = x - xOffset;
        int top = y - yOffset;

        // In order to find the visual center, we shift by half the dragRect
        res[0] = left + dragView.getDragRegion().width() / 2;
        res[1] = top + dragView.getDragRegion().height() / 2;

        return res;
    }

    @Override
    public float getIntrinsicIconScaleFactor() {
        return 1f;
    }

    @Override
    public boolean supportsFlingToDelete() {
        return true;
    }

    @Override
    public boolean supportsAppInfoDropTarget() {
        return false;
    }

    @Override
    public boolean supportsDeleteDropTarget() {
        return true;
    }

    public void onFlingToDelete(DragObject d, int x, int y, PointF vec) {
        // Do nothing
    }

    @Override
    public void onFlingToDeleteCompleted() {
        // Do nothing
    }

    @Override
    public boolean isDropEnabled() {
        return true;
    }

    public boolean isFull() {
        return getItemCount() >= mMaxNumItems;
    }

    protected void centerAboutIcon() {
        DragLayer.LayoutParams lp = (DragLayer.LayoutParams) getLayoutParams();

        DragLayer parent = (DragLayer) mLauncher.findViewById(R.id.drag_layer);
        int width = getPaddingLeft() + getPaddingRight() + mContent.getDesiredWidth();
        width = getResources().getDisplayMetrics().widthPixels;
        int height = getFolderHeight();

        float scale = parent.getDescendantRectRelativeToSelf(mFolderIcon, mTempRect);

        LauncherAppState app = LauncherAppState.getInstance();
        DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();

        int centerX = (int) (mTempRect.left + mTempRect.width() * scale / 2);
        int centerY = (int) (mTempRect.top + mTempRect.height() * scale / 2);
        int centeredLeft = centerX - width / 2;
        int centeredTop = centerY - height / 2;
        int currentPage = mLauncher.getWorkspace().getNextPage();
        // In case the workspace is scrolling, we need to use the final scroll to compute
        // the folders bounds.
        mLauncher.getWorkspace().setFinalScrollForPageChange(currentPage);
        // We first fetch the currently visible CellLayoutChildren
        CellLayout currentLayout = (CellLayout) mLauncher.getWorkspace().getChildAt(currentPage);
        ShortcutAndWidgetContainer boundingLayout = currentLayout.getShortcutsAndWidgets();
        Rect bounds = new Rect();
        parent.getDescendantRectRelativeToSelf(boundingLayout, bounds);
        // We reset the workspaces scroll
        mLauncher.getWorkspace().resetFinalScrollForPageChange(currentPage);

        // We need to bound the folder to the currently visible CellLayoutChildren
        int left = Math.min(Math.max(bounds.left, centeredLeft),
                bounds.left + bounds.width() - width);
        int top = Math.min(Math.max(bounds.top, centeredTop),
                bounds.top + bounds.height() - height);
        if (grid.isPhone() && (grid.availableWidthPx - width) < grid.iconSizePx) {
            // Center the folder if it is full (on phones only)
            left = (grid.availableWidthPx - width) / 2;
        } else if (width >= bounds.width()) {
            // If the folder doesn't fit within the bounds, center it about the desired bounds
            left = bounds.left + (bounds.width() - width) / 2;
        }
        if (height >= bounds.height()) {
            top = bounds.top + (bounds.height() - height) / 2;
        }

        int folderPivotX = width / 2 + (centeredLeft - left);
        int folderPivotY = height / 2 + (centeredTop - top);
        setPivotX(folderPivotX);
        setPivotY(folderPivotY);
        mFolderIconPivotX = (int) (mFolderIcon.getMeasuredWidth() *
                (1.0f * folderPivotX / width));
        mFolderIconPivotY = (int) (mFolderIcon.getMeasuredHeight() *
                (1.0f * folderPivotY / height));

        lp.width = width;
        lp.height = height;
        lp.x = 0;
        lp.y = 0;
        if (!mLauncher.isFullScreen()){
        	lp.y = mLauncher.getStatusBarHeight();
        }
    }

    float getPivotXForIconAnimation() {
        return mFolderIconPivotX;
    }
    float getPivotYForIconAnimation() {
        return mFolderIconPivotY;
    }

    private int getContentAreaHeight() {
        LauncherAppState app = LauncherAppState.getInstance();
        DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
        Rect workspacePadding = grid.getWorkspacePadding(grid.isLandscape ?
                CellLayout.LANDSCAPE : CellLayout.PORTRAIT);
        int maxContentAreaHeight = grid.availableHeightPx -
                workspacePadding.top - workspacePadding.bottom -
                mFolderNameHeight;
        int height = Math.min(maxContentAreaHeight,
                mContent.getDesiredHeight())+50;
        return Math.max(height, MIN_CONTENT_DIMEN);
    }

    private int getContentAreaWidth() {
        return Math.max(mContent.getDesiredWidth(), MIN_CONTENT_DIMEN);
    }

    private int getFolderHeight() {
        int height = getPaddingTop() + getPaddingBottom()
                + getContentAreaHeight() + mFolderNameHeight;
        height = getResources().getDisplayMetrics().heightPixels;
        return height;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getResources().getDisplayMetrics().widthPixels;;
        int height = getFolderHeight();
        int contentAreaWidthSpec = MeasureSpec.makeMeasureSpec(width,
                MeasureSpec.EXACTLY);
        if (mLauncher.isFullScreen()){
        	height -= mLauncher.getStatusBarHeight();
        }
        height -= mFolderNameHeight;
        int contentAreaHeightSpec = MeasureSpec.makeMeasureSpec(height,
                MeasureSpec.EXACTLY);

        int mContentWidth = width - mScrollView.getPaddingLeft() - mScrollView.getPaddingRight();
            // Don't cap the height of the content to allow scrolling.
        mContent.setFixedSize(mContentWidth, mContent.getDesiredHeight());
        
        mScrollView.measure(contentAreaWidthSpec, contentAreaHeightSpec);
//        mFolderName.measure(contentAreaWidthSpec,
//                MeasureSpec.makeMeasureSpec(mFolderNameHeight, MeasureSpec.EXACTLY));
        setMeasuredDimension(width, height);
    }

    public void onDrop(DragObject d) {
        Runnable cleanUpRunnable = null;

        // If we are coming from All Apps space, we defer removing the extra empty screen
        // until the folder closes
        if (d.dragSource != mLauncher.getWorkspace() && !(d.dragSource instanceof FolderBase)) {
            cleanUpRunnable = new Runnable() {
                @Override
                public void run() {
                    mLauncher.exitSpringLoadedDragModeDelayed(true,
                            Launcher.EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT,
                            null);
                }
            };
        }

        View currentDragView;
        ShortcutInfo si = mCurrentDragInfo;
        if (mIsExternalDrag) {
            si.cellX = mEmptyCell[0];
            si.cellY = mEmptyCell[1];

            // Actually move the item in the database if it was an external drag. Call this
            // before creating the view, so that ShortcutInfo is updated appropriately.
            LauncherModel.addOrMoveItemInDatabase(
                    mLauncher, si, mInfo.id, 0, si.cellX, si.cellY);

            // We only need to update the locations if it doesn't get handled in #onDropCompleted.
            if (d.dragSource != this) {
                updateItemLocationsInDatabaseBatch();
            }
            mIsExternalDrag = false;

            currentDragView = createAndAddShortcut(si);
        } else {
            currentDragView = mCurrentDragView;
            if (currentDragView != null){
	            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) currentDragView.getLayoutParams();
	            si.cellX = lp.cellX = mEmptyCell[0];
	            si.cellX = lp.cellY = mEmptyCell[1];
	            mContent.addViewToCellLayout(currentDragView, -1, (int) si.id, lp, true);
            }
        }

        if (d.dragView.hasDrawn()) {

            // Temporarily reset the scale such that the animation target gets calculated correctly.
            float scaleX = getScaleX();
            float scaleY = getScaleY();
            setScaleX(1.0f);
            setScaleY(1.0f);
            mLauncher.getDragLayer().animateViewIntoPosition(d.dragView, currentDragView,
                    cleanUpRunnable, null);
            setScaleX(scaleX);
            setScaleY(scaleY);
        } else {
            d.deferDragViewCleanupPostAnimation = false;
            currentDragView.setVisibility(VISIBLE);
        }
        mItemsInvalidated = true;
        setupContentDimensions(getItemCount());

        // Temporarily suppress the listener, as we did all the work already here.
        mSuppressOnAdd = true;
        mInfo.add(si);
        mSuppressOnAdd = false;
        // Clear the drag info, as it is no longer being dragged.
        mCurrentDragInfo = null;
    }

    // This is used so the item doesn't immediately appear in the folder when added. In one case
    // we need to create the illusion that the item isn't added back to the folder yet, to
    // to correspond to the animation of the icon back into the folder. This is
    public void hideItem(ShortcutInfo info) {
        View v = getViewForInfo(info);
        v.setVisibility(INVISIBLE);
    }
    public void showItem(ShortcutInfo info) {
        View v = getViewForInfo(info);
        v.setVisibility(VISIBLE);
    }

    public void onAdd(ShortcutInfo item) {
        mItemsInvalidated = true;
        // If the item was dropped onto this open folder, we have done the work associated
        // with adding the item to the folder, as indicated by mSuppressOnAdd being set
        if (mSuppressOnAdd) return;
        if (!findAndSetEmptyCells(item)) {
            // The current layout is full, can we expand it?
            setupContentForNumItems(getItemCount() + 1);
            findAndSetEmptyCells(item);
        }
        createAndAddShortcut(item);
        LauncherModel.addOrMoveItemInDatabase(
                mLauncher, item, mInfo.id, 0, item.cellX, item.cellY);
    }

    public void onRemove(ShortcutInfo item) {
        mItemsInvalidated = true;
        // If this item is being dragged from this open folder, we have already handled
        // the work associated with removing the item, so we don't have to do anything here.
        if (item == mCurrentDragInfo) return;
        View v = getViewForInfo(item);
        mContent.removeView(v);
        if (mState == STATE_ANIMATING) {
            mRearrangeOnClose = true;
        } else {
            setupContentForNumItems(getItemCount());
        }
        if (getItemCount() <= 1) {
            replaceFolderWithFinalItem();
        }
    }

    public void onItemsChanged() {
        updateTextViewFocus();
    }

    public void onTitleChanged(CharSequence title) {
        mFolderName.setText(title);
    }

    public void getLocationInDragLayer(int[] loc) {
        mLauncher.getDragLayer().getLocationInDragLayer(this, loc);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
    	//当FolderEditText获取焦点的时候，开始编辑文件夹名字
        if (v == mFolderName && hasFocus) {
            startEditingFolderName();
        }
    }

    @Override
    public void getHitRectRelativeToDragLayer(Rect outRect) {
        getHitRect(outRect);
    }
    
    @Override
    public boolean isPointInContainer(int x, int y) {
    	return getContainerRect().contains(x, y);
    }
    
    @Override
    public void closeFolder(int x, int y) {
    	if (!isPointInContainer(x, y)){
    		mLauncher.closeFolder();
    	}
    }
    
    public void resetContainer(){
    	mScrollView.scrollTo(0, 0);
    }
}
