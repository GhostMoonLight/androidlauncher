package com.android.launcher3.download;

import android.text.TextUtils;

import com.android.launcher3.LauncherApplication;
import com.android.launcher3.utils.PackageUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class DownloadManager {
	public static final int STATE_NONE = 0;
	/** 等待中 */
	public static final int STATE_WAITING = 1;
	/** 下载中 */
	public static final int STATE_DOWNLOADING = 2;
	/** 暂停 */
	public static final int STATE_PAUSED = 3;
	/** 下载完毕 */
	public static final int STATE_DOWNLOADED = 4;
	/** 下载失败 */
	public static final int STATE_ERROR = 5;

	private static DownloadManager instance;

	private DownloadManager() {
        //初始化下载任务列表
        ArrayList<DownloadTaskInfo> list = DownloadDB.getInstance().queryAllThemeUnfinished();
        for (DownloadTaskInfo i: list){
            addDownloadInfo(i);
        }

        list = DownloadDB.getInstance().queryAllThemeFinished();
        for (DownloadTaskInfo i: list){
            addDownloadInfo(i);
        }
	}

	private Map<Integer, DownloadTaskInfo> mDownloadMap = new ConcurrentHashMap<Integer, DownloadTaskInfo>();
	/** 用于记录观察者，当信息发送了改变，需要通知他们 */
	private List<DownloadObserver> mObservers = new ArrayList<DownloadObserver>();
	/** 用于记录所有下载的任务，方便在取消下载时，通过id能找到该任务进行删除 */
	private Map<Integer, DownloadTask> mTaskMap = new ConcurrentHashMap<Integer, DownloadTask>();

	public static synchronized DownloadManager getInstance() {
		if (instance == null) {
			instance = new DownloadManager();
		}
		return instance;
	}
	
	//获取下载任务
	public Map<Integer, DownloadTaskInfo> getDownloadMap(){
		return mDownloadMap;
	}
	//添加下载任务
	public void addDownloadInfo(DownloadTaskInfo info){
		DownloadTaskInfo i = mDownloadMap.get(info.getId());
		if (i == null) {
			mDownloadMap.put(info.getId(), info);
		}
	}

	/** 注册观察者 */
	public void registerObserver(DownloadObserver observer) {
		synchronized (mObservers) {
			if (!mObservers.contains(observer)) {
				mObservers.add(observer);
			}
		}
	}

	/** 反注册观察者 */
	public void unRegisterObserver(DownloadObserver observer) {
		synchronized (mObservers) {
			if (mObservers.contains(observer)) {
				mObservers.remove(observer);
			}
		}
	}

	/** 当下载状态发送改变的时候回调 */
	public void notifyDownloadStateChanged(DownloadTaskInfo info) {
		synchronized (mObservers) {
			for (DownloadObserver observer : mObservers) {
				observer.onDownloadStateChanged(info);
			}
		}
	}

	/** 当下载进度发送改变的时候回调 */
	public void notifyDownloadProgressed(DownloadTaskInfo info) {
		synchronized (mObservers) {
			for (DownloadObserver observer : mObservers) {
				observer.onDownloadProgressed(info);
			}
		}
	}

	/** 下载，需要传入一个DownloadInfo对象 */
	public synchronized void download(DownloadInfo appInfo) {
		//先判断是否有这个app的下载信息
		DownloadTaskInfo info = mDownloadMap.get(appInfo.id);
		if (info == null) {//如果没有，则根据appInfo创建一个新的下载信息
			info = DownloadTaskInfo.clone(appInfo);
			mDownloadMap.put(appInfo.id, info);
		}
		//如果下载任务存在，且状态是暂停，继续下载
		DownloadTask ctask = mTaskMap.get(info.getId());
		if (ctask != null && info.getDownloadState() == STATE_PAUSED){
			ctask.continueTask();
			return;
		}
		
		//判断状态是否为STATE_NONE、STATE_PAUSED、STATE_ERROR。只有这3种状态才能进行下载，其他状态不予处理
		if (info.getDownloadState() == STATE_NONE || info.getDownloadState() == STATE_PAUSED || info.getDownloadState() == STATE_ERROR) {
			//下载之前，把状态设置为STATE_WAITING，因为此时并没有产开始下载，只是把任务放入了线程池中，当任务真正开始执行时，才会改为STATE_DOWNLOADING
			info.setDownloadState(STATE_WAITING);
			notifyDownloadStateChanged(info);//每次状态发生改变，都需要回调该方法通知所有观察者
			DownloadTask task = new DownloadTask(info);//创建一个下载任务，放入线程池
			mTaskMap.remove(info.getId());
			mTaskMap.put(info.getId(), task);
			DownloadDB.getInstance().deleteThemeFinished(info.name);
			ThreadManager.getDownloadPool().execute(task);
			DownloadDB.getInstance().deleteThemeUnfinished(info.name);
			DownloadDB.getInstance().insertThemeUnfinished(info);
		}
	}

	/** 暂停下载 */
	public synchronized void pause(DownloadInfo appInfo) {
//		stopDownload(appInfo);
		DownloadTaskInfo info = mDownloadMap.get(appInfo.id);//找出下载信息
		if (info != null) {//修改下载状态
			info.setDownloadState(STATE_PAUSED);
			notifyDownloadStateChanged(info);
		}
	}
	
	/** 继续下载，需要传入一个DownloadInfo对象 */
	public synchronized void gonoDownload(DownloadTaskInfo info) {
		//如果下载任务存在，且状态是暂停，继续下载
		DownloadTask ctask = mTaskMap.get(info.getId());
		if (ctask != null && info.getDownloadState() == STATE_PAUSED){
			ctask.continueTask();
			return;
		}
		
		//判断状态是否为STATE_NONE、STATE_PAUSED、STATE_ERROR。只有这3种状态才能进行下载，其他状态不予处理
		if (info.getDownloadState() == STATE_NONE || info.getDownloadState() == STATE_PAUSED || info.getDownloadState() == STATE_ERROR) {
			//下载之前，把状态设置为STATE_WAITING，因为此时并没有产开始下载，只是把任务放入了线程池中，当任务真正开始执行时，才会改为STATE_DOWNLOADING
			info.setDownloadState(STATE_WAITING);
			notifyDownloadStateChanged(info);//每次状态发生改变，都需要回调该方法通知所有观察者
			DownloadTask task = new DownloadTask(info);//创建一个下载任务，放入线程池
			mTaskMap.remove(info.getId());
			mTaskMap.put(info.getId(), task);
			DownloadDB.getInstance().deleteThemeFinished(info.name);
			ThreadManager.getDownloadPool().execute(task);
			DownloadDB.getInstance().deleteThemeUnfinished(info.name);
			DownloadDB.getInstance().insertThemeUnfinished(info);
		}
	}
	/** 暂停下载 */
	public synchronized void pause(DownloadTaskInfo info) {
//		stopDownload(appInfo);
		if (info != null) {//修改下载状态
			info.setDownloadState(STATE_PAUSED);
			notifyDownloadStateChanged(info);
		}
	}

	/** 取消下载，逻辑和暂停类似，只是需要删除已下载的文件 */
	public synchronized void cancel(DownloadInfo appInfo) {
		stopDownload(appInfo);
		DownloadTaskInfo info = mDownloadMap.get(appInfo.id);//找出下载信息
		if (info != null) {//修改下载状态并删除文件
			info.setDownloadState(STATE_NONE);
			notifyDownloadStateChanged(info);
			info.setCurrentSize(0);
			File file = new File(info.getPath());
			file.delete();
		}
	}

	/** 如果该下载任务还处于线程池中，且没有执行，先从线程池中移除 */
	private synchronized void stopDownload(DownloadInfo appInfo) {
		DownloadTask task = mTaskMap.remove(appInfo.id);//先从集合中找出下载任务
		if (task != null) {
			task.stopTask();
			ThreadManager.getDownloadPool().cancel(task);//然后从线程池中移除
		}
	}

	/** 获取下载信息 */
	public synchronized DownloadTaskInfo getDownloadInfo(int id) {
		
		DownloadTaskInfo tInfo = mDownloadMap.get(id);
		
		if (tInfo != null){
			//判断下载的文件是否还存在
			File f = new File(tInfo.getPath());
			if (!f.exists()){
				mDownloadMap.remove(id);
				tInfo = null;
			}
		}
		
		return tInfo;
	}
	/** 获取全部下载中的信息 */
	public synchronized List<DownloadTaskInfo> getAllDownloadingInfo() {
		List<DownloadTaskInfo> all=new ArrayList<DownloadTaskInfo>();
		List<Integer> allId=new ArrayList<Integer>();
		for (Entry<Integer, DownloadTaskInfo> entry : mDownloadMap.entrySet()) {  
		    System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());  
		    if(entry.getValue().getDownloadState()!=STATE_DOWNLOADED){
		    	int downloadId=entry.getValue().getId();
		    	boolean needAdd=true;
		    	for(int i=0;i<allId.size();i++){
		    		if(downloadId==allId.get(i)){
		    			needAdd=false;
		    			break;
		    		}
		    	}
				if (needAdd) {
					all.add(entry.getValue());
					allId.add(entry.getValue().getId());
				}
		    }
		    
		}  
		return all;
	}
	/** 下载任务 */
	public class DownloadTask implements Runnable {
		private DownloadTaskInfo info;
		private boolean isStop = false;

		public DownloadTask(DownloadTaskInfo info) {
			this.info = info;
		}
		
		public void stopTask(){
			isStop = true;
		}
		
		public void continueTask(){
			info.setDownloadState(STATE_DOWNLOADING);
			notifyDownloadStateChanged(info);
			synchronized (info) {
				info.notifyAll();
			}
		}

		@Override
		public void run() {
			info.setDownloadState(STATE_DOWNLOADING);//先改变下载状态
			notifyDownloadStateChanged(info);
			File file = new File(info.getPath());//获取下载文件
			HttpURLConnection conn = null;
			FileOutputStream fos = null;
			InputStream stream = null;
			try {
				URL url = new URL(info.getUrl());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(10 * 1000);
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Connection", "Keep-Alive");
				long downloaded = 0;
//				double oldCompleted = 0;
//				double completed = 0;
				long lastUpdateTime = 0;
				long oldDownloaded = 0;
				long currentTime;

				if (isStop || info.getDownloadState() != STATE_DOWNLOADING) return;
				
				if (info.getCurrentSize() == 0 || !file.exists()) {
					//如果文件不存在，或者进度为0，就需要重新下载
					info.setCurrentSize(0);
					file.delete();
				} else {
					oldDownloaded = downloaded = file.length();
					conn.setRequestProperty("Range", "bytes=" + file.length() + "-");// 设置获取实体数据流的范围
					info.setCurrentSize(downloaded);
				}
				if ((stream = conn.getInputStream()) == null) {
					info.setDownloadState(STATE_ERROR);//没有下载内容返回，修改为错误状态
					notifyDownloadStateChanged(info);
				} else {
					if (info.size == 0)
						info.size = conn.getContentLength();
					
					try{
						fos = new FileOutputStream(file, true);
					}catch(Exception e){
						e.printStackTrace();
						if (e.getMessage().contains("open failed: EBUSY (Device or resource busy)")) {
							//出异常后需要修改状态并
							info.setDownloadState(STATE_PAUSED);
							notifyDownloadStateChanged(info);
							mTaskMap.remove(info.getId());
							return;
						}
					}
					int count = -1;
					
					byte[] buffer = new byte[1024*100];
					boolean downloading = true;
					while (!isStop && downloading) {
						synchronized (info) {
							if (info.getDownloadState() == STATE_PAUSED){
								try {
									info.wait();
								} catch (Exception e) {
								}
							}
						}
						
						//每次读取到数据后，都需要判断是否为下载状态，如果不是，下载需要终止; 如果是，则刷新进度
						if ((count = stream.read(buffer)) > 0){
							fos.write(buffer, 0, count);
							fos.flush();
							info.setCurrentSize(info.getCurrentSize() + count);
							downloaded += count;
//							completed = (downloaded * 100f) / info.size;
//							if (oldCompleted + 2 <= completed) {
//								notifyDownloadProgressed(info);//刷新进度
//								oldCompleted = completed;
//							}
							currentTime = System.currentTimeMillis();
							if (currentTime - lastUpdateTime > 1000){
								notifyDownloadProgressed(info);//刷新进度
								lastUpdateTime = currentTime;
								info.setSpeed((downloaded - oldDownloaded)/1000.0f);
								oldDownloaded = downloaded;
							}
							DownloadDB.getInstance().updateThemeUnfinished(info);
						}else{
							downloading = false;
							info.setSpeed(0);
							if (checkDownloadFile(info.getPath())){
								info.setDownloadState(STATE_DOWNLOADED);
								notifyDownloadStateChanged(info);
								DownloadDB.getInstance().insertThemeFinished(info);
								DownloadDB.getInstance().deleteThemeUnfinished(info.name);
							}else{
								info.setDownloadState(STATE_ERROR);
								notifyDownloadStateChanged(info);
								file.delete();
								DownloadDB.getInstance().deleteThemeUnfinished(info.name);
							}
						}
					}
				}
			} catch (Exception e) {
				System.out.println("下载异常");
				e.printStackTrace();
				//出异常后需要修改状态并
				info.setDownloadState(STATE_PAUSED);
				notifyDownloadStateChanged(info);
				mTaskMap.remove(info.getId());
				info.setSpeed(0);
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if (isStop || info.getDownloadState() == STATE_ERROR
					|| info.getDownloadState() == STATE_DOWNLOADED)
				mTaskMap.remove(info.getId());
		}
	}
	
	/**
	 * 检查文件是否下载完成
	 */
	private boolean checkDownloadFile(String path) {
		PackageUtil.AppSnippet sAppSnippet = PackageUtil.getAppSnippet(LauncherApplication.getInstance(), path);
		
		if(sAppSnippet == null || TextUtils.isEmpty(sAppSnippet.packageName)) {
			return false;
		}
		return true;
	}

	public interface DownloadObserver {

		public void onDownloadStateChanged(DownloadTaskInfo info);

		public void onDownloadProgressed(DownloadTaskInfo info);
	}
}
