package com.android.launcher3.net;

import com.android.launcher3.utils.Util;

/**
 * Created by wen on 2016/11/5.
 * 联网操作的辅助工具类
 */

public class HttpController {

    private static class Singleton{
        public static HttpController INSTANCE = new HttpController();
    }

    public static HttpController getInstance(){
        return Singleton.INSTANCE;
    }

    private OkHttpClientUtil mClientUtil;
    private HttpController(){
        mClientUtil = OkHttpClientUtil.getInstance();
    }

    //获取在线壁纸
    public void getWallpaperOnline(ResultCallBack callBack, int page){
        String url = "http://a.holaworld.cn/wallpapers/byTag?&page="+page+"&tagCode=e43f0bc1ebad494891bb325aa3a3e582&cw=720&net=wifi&h="
                + Util.getScreenH()+"&w="
                + Util.getScreenW()+"&lang=zh_CN&lc=22700";
        mClientUtil.doGetAsync(url, callBack, false);
    }

    //获取壁纸分类列表
    public void getWallpaperClassify(ResultCallBack callBack){
        String url = "http://a.holaworld.cn/wallpapers/tags?cw=1440&net=wifi&h=2560&w="+Util.getScreenW()+"&lang=zh_CN";
        mClientUtil.doGetAsync(url, callBack, true);
    }
}
