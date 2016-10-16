package com.android.launcher3;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.launcher3.bean.AppSortEntity;
import com.android.launcher3.utils.PackageUtil;
import com.android.launcher3.utils.Util;
import com.android.launcher3.view.HeadLetterListView;
import com.android.launcher3.view.HeadLetterListView.OnHeaderUpdateListener;
import com.android.launcher3.view.SideBar;
import com.android.launcher3.view.SideBar.OnTouchingLetterChangedListener;
import com.android.launcher3.view.ThreeView;
import com.android.launcher3.wallpaper.BitmapUtils;
import com.cuan.launcher.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 所有app的显示列表，按照字母分类
 * 通过使用DDMS heap发现：调用汉子转拼音之后，内存占用立马变大，整个程序创建的对象总数急剧增加
 * 已经修改，先调用系统原本的方法，如果获取到的拼音为空，则在用原先的方法
 */
public class AllAppListView extends LinearLayout implements OnScrollListener, OnHeaderUpdateListener, Insettable{
	
	private ExecutorService mThreadPool;
	
	private SideBar sb;
	private HeadLetterListView mListView;
	private Context mContext;
	private View mHeaderView;
	private TextView mHeaderName;
	private MyHandler mHandler;
	private boolean mLoading;
	private AllAppListAdapter mAdapter;
	private IconCache mIconCache;
	private ProgressBar loadingProgress;
	protected final Rect mInsets = new Rect();

	public AllAppListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public AllAppListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public AllAppListView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context){
		mHandler = new MyHandler();
		mContext = context;
		View.inflate(mContext, R.layout.layout_allapplist, this);
		mThreadPool = Executors.newSingleThreadExecutor();
		mIconCache = LauncherAppState.getInstance().getIconCache();
		
		loadingProgress = (ProgressBar) findViewById(R.id.loadingbar);
		mListView = (HeadLetterListView) findViewById(R.id.listview);
		sb = (SideBar) findViewById(R.id.sb);
		mListView.setOnScrollListener(this);
		mListView.setOnHeaderUpdateListener(this);
		mListView.setVerticalScrollBarEnabled(false);
		mAdapter = new AllAppListAdapter(mContext);
		mListView.setAdapter(mAdapter);
		
		sb.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
			
			@Override
			public void onTouchingLetterChanged(String s) {
				if (mAdapter != null){
					int count = mAdapter.getPositionForSection(s.charAt(0));
					mListView.setSelection(count);
				}
			}
		});
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}
	
	public synchronized void refreshList() {
		if(mLoading) return;
		mLoading = true;
		LoadListTask task = new LoadListTask();
		mThreadPool.execute(task);
	}
	
	private class LoadListTask implements Runnable{
		
		private ArrayList<ShortcutInfo> list;

		@Override
		public void run() {
			list = loadList();
			mHandler.addList(list);
			mLoading = false;
		}
		
		/**
		 * 获取应用列表
		 */
		private synchronized ArrayList<ShortcutInfo> loadList() {
			ArrayList<ShortcutInfo> mSortList = new ArrayList<>();
			ArrayList<AppSortEntity> mTmpSortList = new ArrayList<>();
			HashMap<String, ArrayList<ShortcutInfo>> map = new HashMap<>();
			ArrayList<ShortcutInfo> appAppList = new ArrayList<>();
			appAppList.addAll(getAllApps());
			String pinyin;
			String letter;
			for (ShortcutInfo appInfo: appAppList) {
				if (PackageUtil.isInstalledApk(mContext, ItemInfo.getPackageName(appInfo.intent))){
					if (TextUtils.isEmpty(appInfo.title)){
						continue;
					}
					/**
					 * 通过使用DDMS heap发现：调用汉子转拼音之后，内存占用立马变大，整个程序创建的对象总数急剧增加;
					 * 已经修改，先调用系统原本的方法，如果获取到的拼音为空，则在用原先的方法
					 */
					//获取拼音，去掉汉子中的空格  \\u00A0另类的空格形式和平常用的空格不一样   
					pinyin = Util.hanZiToPinYin(appInfo.title.toString().replaceAll("[\\u00A0| ]", ""));
					if (TextUtils.isEmpty(pinyin)){
						continue;
					}
					letter = pinyin.substring(0, 1);
					if (!letter.matches("[A-Z]")) {
						letter = "#";
					}
					if (map.containsKey(letter)) {
//						ArrayList<ShortcutInfo> arrayList = map.get(letter);
//						boolean isContainApp = false;
//						for(ShortcutInfo item : arrayList){
//							if(ItemInfo.getPackageName(item.intent).equals(ItemInfo.getPackageName(appInfo.intent))){
//								isContainApp = true;
//								break;
//							}
//						}
//						if(!isContainApp)
//							arrayList.add(appInfo);
						map.get(letter).add(appInfo);
					} else {
						ArrayList<ShortcutInfo> arrayList = new ArrayList<>();
						arrayList.add(appInfo);
						map.put(letter, arrayList);
					}
				}
			}
			Set<String> keySet = map.keySet();
			Iterator<String> iterator = keySet.iterator();
			AppSortEntity entity;
			while (iterator.hasNext()) {
				String key = iterator.next();
				ArrayList<ShortcutInfo> arrayList2 = map.get(key);
				entity = new AppSortEntity();
				entity.name = key;
				entity.mDatas = arrayList2;
				mTmpSortList.add(entity);
			}
			Collections.sort(mTmpSortList, new PinyinComparator());
			
			for (AppSortEntity as: mTmpSortList){
				if (as.mDatas.size() > 3){
					for (int i=0; i<as.mDatas.size()/3+1; i++){
						entity = new AppSortEntity();
						entity.name = as.name;
						if (i == 0){
							entity.isFirst = true;	
						}
						int length = (i+1)*3<as.mDatas.size()? (i+1)*3:as.mDatas.size();
						for (int j=i*3; j<length; j++){
							entity.mDatas.add(as.mDatas.get(j));
						}
						if (entity.mDatas.size() > 0){
							mSortList.add(entity);
						}
					}
				}else{
					as.isFirst = true;
					mSortList.add(as);
				}
			}
			
			return mSortList;
		}
	}
	
	private Collection<ShortcutInfo> getAllApps() {
		ArrayList<ShortcutInfo> list = new ArrayList<>();
		FolderInfo fi;
		for (ItemInfo item: LauncherModel.sBgWorkspaceItems){
			if (item instanceof AppInfo){
				list.add(new ShortcutInfo((AppInfo)item));
			} else if (item instanceof ShortcutInfo){
				ShortcutInfo sInfo = (ShortcutInfo)item;
				if (item.itemType != LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT){
					list.add(sInfo);
				} else if (mContext.getPackageName().equals(sInfo.getPckName()) && !mContext.getString(R.string.all_apps_name).equals(sInfo.title)){
					list.add(sInfo);
				}
			} else if (item instanceof FolderInfo){
				fi = (FolderInfo)item;
				for (ShortcutInfo info: fi.contents){
					list.add(info);
				}
			}
		}
		return list;
	}

	@SuppressLint("HandlerLeak")
	private class MyHandler extends Handler{
		
		public static final int MSG_REFRESH_LIST = 2;
		public static final int MSG_ADD_LIST = 5;
		
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REFRESH_LIST:
				break;
			case MSG_ADD_LIST:
				ArrayList<ShortcutInfo> list = (ArrayList<ShortcutInfo>) msg.obj;
				if (list != null && list.size() > 0) {
					mAdapter.removeAllItems();
					mAdapter.addAll(list);
				}
				mLoading = false;
				if (loadingProgress != null){
					loadingProgress.setVisibility(View.GONE);
					removeView(loadingProgress);
					loadingProgress = null;
				}
				break;
			}
		}
		
		public void addList(ArrayList<ShortcutInfo> list) {
			removeMessages(MSG_ADD_LIST);
			Message sMessage = obtainMessage(MSG_ADD_LIST);
			sMessage.obj = list;
			sendMessage(sMessage);
		}
	}

	@Override
	public View getPinnedHeader() {
		if (mHeaderView == null){
			mHeaderView = View.inflate(mContext, R.layout.allappslist_head, null);
			mHeaderName = (TextView) mHeaderView.findViewById(R.id.name);
			mHeaderView.setLayoutParams(new ViewGroup.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		}
		return mHeaderView;
	}

	@Override
	public void updatePinnedHeader(View headerView, int firstVisiblePosition) {
		if (mAdapter != null){
			ShortcutInfo info = mAdapter.getItem(firstVisiblePosition);
			if(info != null){
				mHeaderName.setText(((AppSortEntity)info).name);
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if(mAdapter != null && mAdapter.getItem(firstVisibleItem) != null)
		sb.setSelection(((AppSortEntity)mAdapter.getItem(firstVisibleItem)).name, 
				((AppSortEntity)mAdapter.getItem(firstVisibleItem+visibleItemCount-1)).name);
	
	}
	
	private class AllAppListAdapter extends BaseAdapter{
		private Context mContext;
		private LayoutInflater mLayoutInflater;
		private ArrayList<ShortcutInfo> mList;
		
		public AllAppListAdapter(Context ctx){
			mContext = ctx;
			mLayoutInflater = LayoutInflater.from(mContext);
			mList = new ArrayList<>();
		}
		
		public int getPositionForSection(char section) {
			int i;
			for (i = 0; i < getCount(); i++) {
				String sortStr = ((AppSortEntity)mList.get(i)).name;
				char firstChar = sortStr.charAt(0);
				if (firstChar == section) {
					return i;
				}
			}
			
			if (section == '#'){
				section = 'A';
			}
			
			for (i = 0; i < getCount(); i++) {
				String sortStr = ((AppSortEntity)mList.get(i)).name;
				char firstChar = sortStr.charAt(0);
				if (section <= firstChar){
					return i;
				}
			}
			if (i-1 > 0){
				return ((AppSortEntity)mList.get(i-1)).name.charAt(0);
			}
			return 0;
		}
		
		public void addAll(ArrayList<ShortcutInfo> list) {
			if (mList != null) {
				mList.addAll(list);
			}
			notifyDataSetChanged();
		}

		public void removeAllItems() {
			if (mList != null) {
				mList.clear();
				notifyDataSetChanged();
			}
		}
		
		@Override
		public void unregisterDataSetObserver(DataSetObserver observer) {
			if (observer != null) {
				super.unregisterDataSetObserver(observer);
			}
		}
		
		@Override
		public int getCount() {
			return mList == null ? 0 : mList.size();
		}

		@Override
		public ShortcutInfo getItem(int position) {
			if (mList == null) {
				return null;
			} else {
				if (getCount() > 0 && position < mList.size()) {
					return mList.get(position);
				} else {
					return null;
				}
			}
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ShortcutInfo item = getItem(position);
			ListViewHolder subjectView;
			AppSortEntity subjectInfo = (AppSortEntity) item;
			if (convertView == null) {
				subjectView = new ListViewHolder();
				convertView = mLayoutInflater.inflate(R.layout.list_item_widget_app, null);
				subjectView.name = (TextView)convertView.findViewById(R.id.name);
				subjectView.mLayout = (ThreeView) convertView.findViewById(R.id.body);
				convertView.setTag(R.id.tag_viewholder, subjectView);
			}else{
				subjectView = (ListViewHolder) convertView.getTag(R.id.tag_viewholder);
			}
			
			if (subjectInfo.isFirst){
				subjectView.name.setVisibility(View.VISIBLE);
				convertView.setTag(R.id.tag_is_first, true);
                subjectView.name.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
			}else{
				subjectView.name.setVisibility(View.INVISIBLE);
				convertView.setTag(R.id.tag_is_first, false);
                subjectView.name.setOnClickListener(null);
			}
			
			int size = subjectInfo.mDatas.size();
			subjectView.mLayout.removeAllViews();
			int childCount = subjectView.mLayout.getChildCount();
			BubbleTextView view;
			for (int i=0; i<size; i++){
				if (i<childCount){
					view = (BubbleTextView) subjectView.mLayout.getChildAt(i);
					view.setVisibility(View.VISIBLE);
					view.fromShortcutInfo(subjectInfo.mDatas.get(i), mIconCache);
				}else{
					view = (BubbleTextView) mLayoutInflater.inflate(R.layout.application, null);
					view.fromShortcutInfo(subjectInfo.mDatas.get(i), mIconCache);
					view.setOnClickListener((Launcher)mContext);
					subjectView.mLayout.addView(view);
				}
			}
		
			for (int i=size; i<childCount; i++){
				view = (BubbleTextView) subjectView.mLayout.getChildAt(i);
				view.setCompoundDrawablesWithIntrinsicBounds(null);
				view.setTag(null);
				view.setVisibility(View.GONE);
			}

            if (subjectView.mLayout.getChildCount() < 3) {
                ((ViewGroup)subjectView.mLayout.getParent()).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        animateClose();
                    }
                });
            } else {
                ((ViewGroup)subjectView.mLayout.getParent()).setOnClickListener(null);
            }
			
			subjectView.name.setText(subjectInfo.name);
			return convertView;
		}
	}
	
	class ListViewHolder {
		public TextView name;
		public ThreeView mLayout;
	}

	public class PinyinComparator implements Comparator<AppSortEntity> {

		public int compare(AppSortEntity o1, AppSortEntity o2) {
			if (o1.name.equals("@")
					|| o2.name.equals("#")) {
				return 1;
			} else if (o1.name.equals("#")
					|| o2.name.equals("@")) {
				return -1;
			} else {
				return o1.name.compareTo(o2.name);
			}
		}

	}
	
	public void animateOpen(){
		setVisibility(View.VISIBLE);
		Bitmap bluredBitmap = BitmapUtils.getBluredBackgroundImage((Launcher)mContext);
		setBackgroundDrawable(new BitmapDrawable(getResources(), bluredBitmap));
		
		refreshList();
		animate()
		.setDuration(200)
		.alpha(1.f)
		.scaleX(1.f)
		.scaleY(1.f)
		.setListener(new Animator.AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
			}
		}).start();
	}
	
	public void animateClose(){
		if (!(getParent() instanceof DragLayer)) return;
		
		animate()
		.setDuration(200)
		.alpha(0.f)
		.scaleX(60 / getResources().getDisplayMetrics().widthPixels)
		.scaleY(60 / getResources().getDisplayMetrics().heightPixels)
		.setListener(new Animator.AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				animateClosedComplete();
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				animateClosedComplete();
			}
		}).start();
	}
	
	private synchronized void animateClosedComplete() {
		try {
//			DragLayer parent = (DragLayer) getParent();
//			if (parent == null) return;
//			parent.removeView(AllAppListView.this);
			setBackgroundDrawable(null);
			setVisibility(View.GONE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setInsets(Rect insets) {
		mInsets.set(insets);
	}
	
}
