package com.android.launcher3.net;

import com.android.launcher3.utils.Log;

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

    public void getWallpaperOnline(){
        String url = "http://a.holaworld.cn/wallpapers/byTag?&page=1&tagCode=e43f0bc1ebad494891bb325aa3a3e582&cw=720&net=wifi&h=1280&w=720&lang=zh_CN&lc=22700";
        mClientUtil.doGetAsync(url, new ResultCallBack<String>(){

            @Override
            public void onError(Exception e) {
                Log.e("AAAAA", "Error:"+e.toString());
            }

            @Override
            public void onResponse(String response) {
                Log.e("AAAAA", "response:"+response);
            }
        }, true);
    }
}
