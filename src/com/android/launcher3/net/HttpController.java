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
    public void getWallpaperOnline(ResultCallBack callBack, int page, String tagCode){
        String url = "http://a.holaworld.cn/wallpapers/byTag?&page="+page+"&tagCode="+tagCode+"&cw="
                + Util.getScreenW()+"&net=wifi&h="
                + Util.getRealScreenH()+"&w="
                + Util.getScreenW()+"&lang=zh_CN&lc=22700";
        mClientUtil.doGetAsync(url, callBack, false);
    }

    //获取壁纸分类列表
    public void getWallpaperClassify(ResultCallBack callBack){
        String url = "http://a.holaworld.cn/wallpapers/tags?cw="+Util.getScreenW()+"&net=wifi&h="+Util.getRealScreenH()+"&w="+Util.getScreenW()+"&lang=zh_CN";
        mClientUtil.doGetAsync(url, callBack, true);
    }
}
