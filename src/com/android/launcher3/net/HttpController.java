package com.android.launcher3.net;

/**
 * Created by wen on 2016/11/5.
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
}
