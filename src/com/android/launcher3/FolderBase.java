package com.android.launcher3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v4.widget.AutoScrollHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.launcher3.FolderInfo.FolderListener;
import com.cuan.launcher.R;

public class FolderBase extends LinearLayout implements DragSource, DropTarget, FolderListener 
				,View.OnClickListener, View.OnLongClickListener, TextView.OnEditorActionListener, View.OnFocusChangeListener{

	protected static final int STATE_NONE = -1;
	protected static final int STATE_SMALL = 0;
	protected static final int STATE_ANIMATING = 1;
	protected static final int STATE_OPEN = 2;
	
	protected int mState = STATE_NONE;
	
	protected static final int REORDER_ANIMATION_DURATION = 230;
	protected static final int REORDER_DELAY = 250;
	protected static final int ON_EXIT_CLOSE_DELAY = 400;
	
	protected LayoutInflater mInflater;
	protected final IconCache mIconCache;
	
	protected DragController mDragController;
	protected Launcher mLauncher;
	protected FolderInfo mInfo;
	protected FolderIcon mFolderIcon;
	protected CellLayout mContent;
	protected FolderEditText mFolderName;
	protected boolean mIsEditingName = false;   //是否正在编辑文件夹名称

	private ArrayList<View> mItemsInReadingOrder = new ArrayList<View>();
	protected int mMaxCountX;
	protected int mMaxCountY;
	public static boolean isFullEnter = false;
	protected boolean mDeferDropAfterUninstall;
	protected boolean mUninstallSuccessful;
	protected Runnable mDeferredAction;
	protected boolean mDestroyed;
	boolean mItemsInvalidated = false;
	protected float mFolderIconPivotX;
	protected float mFolderIconPivotY;
	protected InputMethodManager mInputMethodManager;
	protected int mExpandDuration;
	protected int mMaterialExpandDuration;
	protected int mMaterialExpandStagger;
	protected static String sDefaultFolderName;
	protected static String sHintText;
	protected FocusIndicatorView mFocusIndicatorHandler;
	protected AutoScrollHelper mAutoScrollHelper;
	protected ShortcutInfo mCurrentDragInfo;
	protected View mCurrentDragView;
	protected int[] mTargetCell = new int[2];
	protected int[] mPreviousTargetCell = new int[2];
	protected int[] mEmptyCell = new int[2];
	protected boolean mDragInProgress = false;
	protected boolean mDeleteFolderOnDropCompleted = false;
	protected boolean mSuppressFolderDeletion = false;
	protected boolean mItemAddedBackToSelfViaIcon = false;
	protected boolean mRearrangeOnClose = false;
	protected boolean mIsExternalDrag;
	protected Alarm mReorderAlarm = new Alarm();
	protected Alarm mOnExitAlarm = new Alarm();
	protected boolean mSuppressOnAdd = false;
	protected Rect mTempRect = new Rect();
	protected int mMaxNumItems;

	// We avoid measuring the scroll view with a 0 width or height, as this
    // results in CellLayout being measured as UNSPECIFIED, which it does
    // not support.
	protected static final int MIN_CONTENT_DIMEN = 5;
    
	public FolderBase(Context context, AttributeSet attrs) {
		super(context, attrs);
		mLauncher = (Launcher) context;
		mInflater = LayoutInflater.from(context);
		mIconCache = LauncherAppState.getInstance().getIconCache();
		mMaxCountX = 3;
		mMaxCountY = 3;
		mInputMethodManager = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		Resources res = getResources();
		mExpandDuration = res.getInteger(R.integer.config_folderExpandDuration);
        mMaterialExpandDuration = res.getInteger(R.integer.config_materialFolderExpandDuration);
        mMaterialExpandStagger = res.getInteger(R.integer.config_materialFolderExpandStagger);
        if (sDefaultFolderName == null) {
            sDefaultFolderName = res.getString(R.string.folder_name);
        }
        if (sHintText == null) {
            sHintText = res.getString(R.string.folder_hint_text);
        }
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		 mFocusIndicatorHandler = new FocusIndicatorView(getContext());
	}

	/**
	 * Creates a new UserFolder, inflated from R.layout.user_folder.
	 * 
	 * @param context
	 *            The application's context.
	 * @return A new UserFolder.
	 */
	static FolderBase fromXml(Context context) {
		int folderType = context.getResources().getInteger(R.integer.folder_type);
		FolderBase fb = null;
		if (folderType == 1){
			fb = Folder.fromXml(context);
		} else if (folderType == 2){
			fb = FolderSingleHanded.fromXml(context);
		} else if (folderType == 3){
			fb = FolderSingleHanded.fromXml(context);
		} else{
			fb = Folder.fromXml(context);
		}
		return fb;
	
	}
	
	protected void setFolderIcon(FolderIcon icon) {
		mFolderIcon = icon;
	}

	public void setDragController(DragController dragController) {
		mDragController = dragController;
	}

	void bind(FolderInfo info) {
	}

	/**
	 * @return the FolderInfo object associated with this folder
	 */
	FolderInfo getInfo() {
		return mInfo;
	}

	public View getListContainer() {
		return null;
	}

	public int getItemCount() {
		return mContent.getShortcutsAndWidgets().getChildCount();
	}

	protected void centerAboutIcon() {
	}

	protected void animateOpen() {
	}
	
	protected void animateClosed(){
		
	}

	protected void onCloseComplete(boolean draged) {
	}

	protected void doneEditingFolderName(boolean commit) {
	}

	protected void doneEditingFolderName(String newTitle, boolean commit) {
	}

	//是否正在编辑文件夹名字
	protected boolean isEditingName() {
		return false;
	}

	//结束编辑文件夹名称
	protected void dismissEditingName() {
	}
	
	public void setContentPadding(){
		mContent.setPadding(0, 0, 0, 0);
	}

	protected void setFullScreenState(boolean fullScreen) {
	}

	/**
	 * 获取文件夹状态
	 */
	protected int getFolderState() {
		return STATE_NONE;
	}

	protected boolean isFull() {
		return false;
	}

	public void notifyDrop() {
		if (mDragInProgress) {
            mItemAddedBackToSelfViaIcon = true;
        }
	}

	public View getEditTextRegion() {
		return null;
	}

	public void completeDragExit() {
		mLauncher.closeFolder();
        mSuppressOnAdd = false;
        mRearrangeOnClose = true;
        mIsExternalDrag = false;
	}

	public void setContainerTop(int top) {
	}

	public void resetContainer() {
	}
	
	public void setFolderName() {}

	public void deferCompleteDropAfterUninstallActivity() {
		mDeferDropAfterUninstall = true;
	}

	public void onUninstallActivityReturned(boolean success) {
		mDeferDropAfterUninstall = false;
		mUninstallSuccessful = success;
		if (mDeferredAction != null) {
			mDeferredAction.run();
		}
	}

	boolean isDestroyed() {
		return mDestroyed;
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

	protected View getViewForInfo(ShortcutInfo item) {
		for (int j = 0; j < mContent.getCountY(); j++) {
			for (int i = 0; i < mContent.getCountX(); i++) {
				View v = mContent.getChildAt(i, j);
				if (v != null && v.getTag() == item) {
					return v;
				}
			}
		}
		return null;
	}

	public List<Pair<ComponentName, CharSequence>> getComponents() {
		int size = mItemsInReadingOrder.size();
		List<Pair<ComponentName, CharSequence>> components = new ArrayList<Pair<ComponentName, CharSequence>>();

		for (int i = 0; i < size; i++) {
			View v = mItemsInReadingOrder.get(i);
			Object tag = v.getTag();
			if (tag instanceof ShortcutInfo) {
				ShortcutInfo shortcut = (ShortcutInfo) tag;
				components.add(Pair.create(shortcut.getIntent().getComponent(), shortcut.title));
			}
		}

		return components;
	}
	
	public View getViewFromPosition(int position) {
        return mItemsInReadingOrder.get(position);
    }
	
	public ArrayList<View> getItemsInReadingOrder() {
		return getItemsInReadingOrder(true);
	}
	
	public ArrayList<View> getItemsInReadingOrder(boolean forceReorder) {
		if (mItemsInvalidated || forceReorder) {
			mItemsInReadingOrder.clear();
			for (int j = 0; j < mContent.getCountY(); j++) {
				for (int i = 0; i < mContent.getCountX(); i++) {
					View v = mContent.getChildAt(i, j);
					if (v != null) {
						mItemsInReadingOrder.add(v);
					}
				}
			}
			mItemsInvalidated = false;
		}
		return mItemsInReadingOrder;
	}

	public ShortcutInfo getShortcutForComponent(ComponentName componentName) {
		for (View v : mItemsInReadingOrder) {
			Object tag = v.getTag();
			if (tag instanceof ShortcutInfo) {
				ComponentName cName = ((ShortcutInfo) tag).getIntent().getComponent();
				if (cName.equals(componentName)) {
					return (ShortcutInfo) tag;
				}
			}
		}

		return null;
	}

	public ShortcutInfo getShortcutForPosition(int position) {
		if (position < 0 || position >= mItemsInReadingOrder.size()) {
			return null;
		}
		View v = mItemsInReadingOrder.get(position);
		Object tag = v.getTag();
		if (tag instanceof ShortcutInfo) {
			return (ShortcutInfo) tag;
		}
		return null;
	}
	
	protected void updateItemLocationsInDatabase() {
		ArrayList<View> list = getItemsInReadingOrder();
		for (int i = 0; i < list.size(); i++) {
			View v = list.get(i);
			ItemInfo info = (ItemInfo) v.getTag();
			LauncherModel.moveItemInDatabase(mLauncher, info, mInfo.id, 0, info.cellX, info.cellY);
		}
	}

	protected void updateItemLocationsInDatabaseBatch() {
		ArrayList<View> list = getItemsInReadingOrder();
		ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
		for (int i = 0; i < list.size(); i++) {
			View v = list.get(i);
			ItemInfo info = (ItemInfo) v.getTag();
			items.add(info);
		}

		LauncherModel.moveItemsInDatabase(mLauncher, items, mInfo.id, 0);
	}

	protected void addItemLocationsInDatabase() {
		ArrayList<View> list = getItemsInReadingOrder();
		for (int i = 0; i < list.size(); i++) {
			View v = list.get(i);
			ItemInfo info = (ItemInfo) v.getTag();
			LauncherModel.addItemToDatabase(mLauncher, info, mInfo.id, 0, info.cellX, info.cellY, false);
		}
	}
	
	protected void setupContentDimensions(int count) {
		ArrayList<View> list = getItemsInReadingOrder();
		int countX = mMaxCountX;
		int countY = count / countX + (count % countX == 0 ? 0 : 1);
		if (countY == 0) countY = 1;
		mContent.setGridSize(countX, countY);
		arrangeChildren(list);
	}
	
	protected void arrangeChildren(ArrayList<View> list) {
		int[] vacant = new int[2];
		if (list == null) {
			list = getItemsInReadingOrder();
		}
		mContent.removeAllViews();

		for (int i = 0; i < list.size(); i++) {
			View v = list.get(i);
			mContent.getVacantCell(vacant, 1, 1);
			CellLayout.LayoutParams lp = (CellLayout.LayoutParams) v.getLayoutParams();
			lp.cellX = vacant[0];
			lp.cellY = vacant[1];
			ItemInfo info = (ItemInfo) v.getTag();
			if (info.cellX != vacant[0] || info.cellY != vacant[1]) {
				info.cellX = vacant[0];
				info.cellY = vacant[1];
				LauncherModel.addOrMoveItemInDatabase(mLauncher, info, mInfo.id, 0, info.cellX, info.cellY);
			}
			boolean insert = false;
			mContent.addViewToCellLayout(v, insert ? 0 : -1, (int) info.id, lp, true);
		}
		mItemsInvalidated = true;
	}
	
	public void startHiddenFolderManager() {}
	
	float getPivotXForIconAnimation() {
        return mFolderIconPivotX;
    }
    float getPivotYForIconAnimation() {
        return mFolderIconPivotY;
    }
	
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		return false;
	}


	@Override
	public boolean onLongClick(View v) {
		return false;
	}

	@Override
	public void onClick(View v) {
	}
	
	public void beginExternalDrag(ShortcutInfo item) {
		
	}
	
	/*FolderListener  start*/
	@Override
	public void onAdd(ShortcutInfo item) {
	}

	@Override
	public void onRemove(ShortcutInfo item) {
	}

	@Override
	public void onTitleChanged(CharSequence title) {
	}

	@Override
	public void onItemsChanged() {

	}
	/*FolderListener  end*/

	/*DropTarget  start*/
	@Override
	public boolean isDropEnabled() {
		return true;
	}

	@Override
	public void onDrop(DragObject dragObject) {
	}

	@Override
	public void onDragEnter(DragObject dragObject) {
		mPreviousTargetCell[0] = -1;
        mPreviousTargetCell[1] = -1;
        mOnExitAlarm.cancelAlarm();
	}

	@Override
	public void onDragOver(DragObject dragObject) {
	}
	
	OnAlarmListener mOnExitAlarmListener = new OnAlarmListener() {
        public void onAlarm(Alarm alarm) {
            completeDragExit();
        }
    };

	@Override
	public void onDragExit(DragObject dragObject) {
		mAutoScrollHelper.setEnabled(false);
        // We only close the folder if this is a true drag exit, ie. not because
        // a drop has occurred above the folder.
        if (!dragObject.dragComplete) {
            mOnExitAlarm.setOnAlarmListener(mOnExitAlarmListener);
            mOnExitAlarm.setAlarm(ON_EXIT_CLOSE_DELAY);
        }
        mReorderAlarm.cancelAlarm();
	}

	@Override
	public void onFlingToDelete(DragObject dragObject, int x, int y, PointF vec) {
	}

	@Override
	public boolean acceptDrop(DragObject dragObject) {
		final ItemInfo item = (ItemInfo) dragObject.dragInfo;
        final int itemType = item.itemType;
        return ((itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                    itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) &&
                    !isFull());
	}
	/*DropTarget  end*/

	/*DragSource  start*/
	@Override
	public void getHitRectRelativeToDragLayer(Rect outRect) {
	}

	@Override
	public void getLocationInDragLayer(int[] loc) {
	}

	@Override
	public boolean supportsFlingToDelete() {
		return false;
	}

	@Override
	public boolean supportsAppInfoDropTarget() {
		return false;
	}

	@Override
	public boolean supportsDeleteDropTarget() {
		return false;
	}

	@Override
	public float getIntrinsicIconScaleFactor() {
		return 0;
	}

	@Override
	public void onFlingToDeleteCompleted() {
	}

	@Override
	public void onDropCompleted(final View target, final DragObject d, final boolean isFlingToDelete, final boolean success) {
        if (mDeferDropAfterUninstall) {
            Log.d(TAG, "Deferred handling drop because waiting for uninstall.");
            mDeferredAction = new Runnable() {
                    public void run() {
                        onDropCompleted(target, d, isFlingToDelete, success);
                        mDeferredAction = null;
                    }
                };
            return;
        }

        boolean beingCalledAfterUninstall = mDeferredAction != null;
        boolean successfulDrop =
                success && (!beingCalledAfterUninstall || mUninstallSuccessful);

        if (successfulDrop) {
            if (mDeleteFolderOnDropCompleted && !mItemAddedBackToSelfViaIcon && target != this) {
                replaceFolderWithFinalItem();
            }
        } else {
            setupContentForNumItems(getItemCount());
            // The drag failed, we need to return the item to the folder
            mFolderIcon.onDrop(d);
        }

        if (target != this) {
            if (mOnExitAlarm.alarmPending()) {
                mOnExitAlarm.cancelAlarm();
                if (!successfulDrop) {
                    mSuppressFolderDeletion = true;
                }
                completeDragExit();
            }
        }

        mDeleteFolderOnDropCompleted = false;
        mDragInProgress = false;
        mItemAddedBackToSelfViaIcon = false;
        mCurrentDragInfo = null;
        mCurrentDragView = null;
        mSuppressOnAdd = false;

        // Reordering may have occured, and we need to save the new item locations. We do this once
        // at the end to prevent unnecessary database operations.
        updateItemLocationsInDatabaseBatch();
    }
	/*DragSource  end*/
	
	protected ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public void onDestroyActionMode(ActionMode mode) {
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
    };
    
    protected class GridComparator implements Comparator<ShortcutInfo> {
        int mNumCols;
        public GridComparator(int numCols) {
            mNumCols = numCols;
        }

        @Override
        public int compare(ShortcutInfo lhs, ShortcutInfo rhs) {
            int lhIndex = lhs.cellY * mNumCols + lhs.cellX;
            int rhIndex = rhs.cellY * mNumCols + rhs.cellX;
            return (lhIndex - rhIndex);
        }
    }
    
    protected boolean findAndSetEmptyCells(ShortcutInfo item) {
        int[] emptyCell = new int[2];
        if (mContent.findCellForSpan(emptyCell, item.spanX, item.spanY)) {
            item.cellX = emptyCell[0];
            item.cellY = emptyCell[1];
            return true;
        } else {
            return false;
        }
    }
    
    protected View getItemAt(int index) {
        return mContent.getShortcutsAndWidgets().getChildAt(index);
    }
    
    protected void setFocusOnFirstChild() {
        View firstChild = mContent.getChildAt(0, 0);
        if (firstChild != null) {
            firstChild.requestFocus();
        }
    }

    protected View createAndAddShortcut(ShortcutInfo item) {
        final BubbleTextView textView =
            (BubbleTextView) mInflater.inflate(R.layout.folder_application, this, false);
        textView.applyFromShortcutInfo(item, mIconCache, false);

        textView.setOnClickListener(this);
        textView.setOnLongClickListener(this);
        textView.setOnFocusChangeListener(mFocusIndicatorHandler);

        // We need to check here to verify that the given item's location isn't already occupied
        // by another item.
        if (mContent.getChildAt(item.cellX, item.cellY) != null || item.cellX < 0 || item.cellY < 0
                || item.cellX >= mContent.getCountX() || item.cellY >= mContent.getCountY()) {
            // This shouldn't happen, log it.
            Log.e(TAG, "Folder order not properly persisted during bind");
            if (!findAndSetEmptyCells(item)) {
                return null;
            }
        }

        CellLayout.LayoutParams lp =
            new CellLayout.LayoutParams(item.cellX, item.cellY, item.spanX, item.spanY);
        boolean insert = false;
        textView.setOnKeyListener(new FolderKeyEventListener());
        mContent.addViewToCellLayout(textView, insert ? 0 : -1, (int)item.id, lp, true);
        return textView;
    }
    
    /**
     * 图标交换的定时监听
     */
    OnAlarmListener mReorderAlarmListener = new OnAlarmListener() {
        public void onAlarm(Alarm alarm) {
            realTimeReorder(mEmptyCell, mTargetCell);
        }
    };
    boolean readingOrderGreaterThan(int[] v1, int[] v2) {
        if (v1[1] > v2[1] || (v1[1] == v2[1] && v1[0] > v2[0])) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 图标交换
     * @param empty
     * @param target
     */
    private void realTimeReorder(int[] empty, int[] target) {
        boolean wrap;
        int startX;
        int endX;
        int startY;
        int delay = 0;
        float delayAmount = 30;
        if (readingOrderGreaterThan(target, empty)) {
            wrap = empty[0] >= mContent.getCountX() - 1;
            startY = wrap ? empty[1] + 1 : empty[1];
            for (int y = startY; y <= target[1]; y++) {
                startX = y == empty[1] ? empty[0] + 1 : 0;
                endX = y < target[1] ? mContent.getCountX() - 1 : target[0];
                for (int x = startX; x <= endX; x++) {
                    View v = mContent.getChildAt(x,y);
                    if (mContent.animateChildToPosition(v, empty[0], empty[1],
                            REORDER_ANIMATION_DURATION, delay, true, true)) {
                        empty[0] = x;
                        empty[1] = y;
                        delay += delayAmount;
                        delayAmount *= 0.9;
                    }
                }
            }
        } else {
            wrap = empty[0] == 0;
            startY = wrap ? empty[1] - 1 : empty[1];
            for (int y = startY; y >= target[1]; y--) {
                startX = y == empty[1] ? empty[0] - 1 : mContent.getCountX() - 1;
                endX = y > target[1] ? 0 : target[0];
                for (int x = startX; x >= endX; x--) {
                    View v = mContent.getChildAt(x,y);
                    if (mContent.animateChildToPosition(v, empty[0], empty[1],
                            REORDER_ANIMATION_DURATION, delay, true, true)) {
                        empty[0] = x;
                        empty[1] = y;
                        delay += delayAmount;
                        delayAmount *= 0.9;
                    }
                }
            }
        }
    }

    public boolean isLayoutRtl() {
        return (getLayoutDirection() == LAYOUT_DIRECTION_RTL);
    }
    
    protected void setupContentForNumItems(int count) {
        setupContentDimensions(count);

        DragLayer.LayoutParams lp = (DragLayer.LayoutParams) getLayoutParams();
        if (lp == null) {
            lp = new DragLayer.LayoutParams(0, 0);
            lp.customPosition = true;
            setLayoutParams(lp);
        }
        centerAboutIcon();
    }
    
    protected void onCloseComplete() {
        DragLayer parent = (DragLayer) getParent();
        if (parent != null) {
            parent.removeView(this);
        }
        mDragController.removeDropTarget((DropTarget) this);
        clearFocus();
        mFolderIcon.requestFocus();

        if (mRearrangeOnClose) {
            setupContentForNumItems(getItemCount());
            mRearrangeOnClose = false;
        }
        if (getItemCount() <= 1) {
            if (!mDragInProgress && !mSuppressFolderDeletion) {
                replaceFolderWithFinalItem();
            } else if (mDragInProgress) {
                mDeleteFolderOnDropCompleted = true;
            }
        }
        mSuppressFolderDeletion = false;
    }
    /**
     * 文件夹中的icon替换文件夹
     */
    protected void replaceFolderWithFinalItem() {
        // Add the last remaining child to the workspace in place of the folder
        Runnable onCompleteRunnable = new Runnable() {
            @Override
            public void run() {
                CellLayout cellLayout = mLauncher.getCellLayout(mInfo.container, mInfo.screenId);

                View child = null;
                // Move the item from the folder to the workspace, in the position of the folder
                if (getItemCount() == 1) {
                    ShortcutInfo finalItem = mInfo.contents.get(0);
                    child = mLauncher.createShortcut(R.layout.application, cellLayout,
                            finalItem);
                    LauncherModel.addOrMoveItemInDatabase(mLauncher, finalItem, mInfo.container,
                            mInfo.screenId, mInfo.cellX, mInfo.cellY);
                }
                if (getItemCount() <= 1) {
                    // Remove the folder
                    LauncherModel.deleteItemFromDatabase(mLauncher, mInfo);
                    if (cellLayout != null) {
                        // b/12446428 -- sometimes the cell layout has already gone away?
                        cellLayout.removeView(mFolderIcon);
                    }
                    if (mFolderIcon instanceof DropTarget) {
                        mDragController.removeDropTarget((DropTarget) mFolderIcon);
                    }
                    mLauncher.removeFolder(mInfo);
                }
                // We add the child after removing the folder to prevent both from existing at
                // the same time in the CellLayout.  We need to add the new item with addInScreenFromBind()
                // to ensure that hotseat items are placed correctly.
                if (child != null) {
                    mLauncher.getWorkspace().addInScreenFromBind(child, mInfo.container, mInfo.screenId,
                            mInfo.cellX, mInfo.cellY, mInfo.spanX, mInfo.spanY);
                }
            }
        };
        View finalChild = getItemAt(0);
        if (finalChild != null) {
            mFolderIcon.performDestroyAnimation(finalChild, onCompleteRunnable);
        } else {
            onCompleteRunnable.run();
        }
        mDestroyed = true;
    }
    
    // This method keeps track of the last item in the folder for the purposes
    // of keyboard focus
    protected void updateTextViewFocus() {
        View lastChild = getItemAt(getItemCount() - 1);
        getItemAt(getItemCount() - 1);
        if (lastChild != null) {
            mFolderName.setNextFocusDownId(lastChild.getId());
            mFolderName.setNextFocusRightId(lastChild.getId());
            mFolderName.setNextFocusLeftId(lastChild.getId());
            mFolderName.setNextFocusUpId(lastChild.getId());
        }
    }
    
    //手指抬起时，是否在mContainer中
    public boolean isPointInContainer(int x, int y){
    	return false;
    }
    
    public void closeFolder(int x, int y){
    	
    }
}
