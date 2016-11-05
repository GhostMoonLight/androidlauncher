package com.android.launcher3.net;

import android.os.Handler;
import android.os.Looper;

import com.android.launcher3.LauncherApplication;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by wen on 2016/11/5.
 * 网络请求工具类   简单封装OkHttp
 * new CacheControl.Builder() .maxAge(0, TimeUnit.SECONDS)//这个是控制缓存的最大生命时间
 */

public class OkHttpClientUtil {

    /**
     * 静态内部类的单例模式
     */
    private static class Singleton{
        private static OkHttpClientUtil INSTANCE = new OkHttpClientUtil();
    }

    public static OkHttpClientUtil getInstance(){
        return OkHttpClientUtil.Singleton.INSTANCE;
    }

    private final int CACHE_SIZE = 10 * 1024 * 1024;
    private OkHttpClient mOkHttpClient;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Gson mGson;

    private OkHttpClientUtil(){
        File sdCache = new File(LauncherApplication.getInstance().getUrlCacheDir());
        mOkHttpClient = (new OkHttpClient.Builder())
                .connectTimeout(30, TimeUnit.DAYS)
                .writeTimeout(20, TimeUnit.DAYS)
                .readTimeout(20, TimeUnit.DAYS)
                .cache(new Cache(sdCache.getAbsoluteFile(), CACHE_SIZE)).build();
        mGson = new Gson();
    }

    /**
     * 同步的Get请求
     *
     * @param url
     * @return Response
     */
    private Response getHttp(String url) throws IOException{
        final Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = mOkHttpClient.newCall(request);
        Response execute = call.execute();
        return execute;
    }

    /**
     * 同步的Get请求
     *
     * @param url
     * @return 字符串
     */
    private String getAsString(String url) throws IOException{
        Response execute = getHttp(url);
        return execute.body().string();
    }

    /**
     * 同步的Post请求
     *
     * @param url
     * @param params post的参数
     * @return
     */
    private Response post(String url, Param... params) throws IOException {
        Request request = buildPostRequest(url, params);
        Response response = mOkHttpClient.newCall(request).execute();
        return response;
    }


    /**
     * 同步的Post请求
     *
     * @param url
     * @param params post的参数
     * @return 字符串
     */
    private String postAsString(String url, Param... params) throws IOException {
        Response response = post(url, params);
        return response.body().string();
    }

    /**
     * 异步get请求
     */
    protected void getAsynHttp(String url, ResultCallBack callback) {
        Request.Builder requestBuilder = new Request.Builder().url(url);
        //可以省略，默认是GET请求
        requestBuilder.method("GET",null);
        Request request = requestBuilder.build();
        exectAsyncHttp(request, callback);
    }

    /**
     * post异步请求
     */
    private void postAsynHttp(String url, ResultCallBack callback, Param... params) {
        exectAsyncHttp(buildPostRequest(url, params), callback);
    }

    /**
     * 异步请求
     */
    private void exectAsyncHttp(Request request, final ResultCallBack callBack){
        Call mcall= mOkHttpClient.newCall(request);
        mcall.enqueue(new Callback() {  //请求网络后的回调，在异步线程
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try
                {
                    final String string = response.body().string();
                    if (callBack.mType == String.class){
                        sendSuccessResultCallback(string, callBack);
                    } else{
                        Object o = mGson.fromJson(string, callBack.mType);
                        sendSuccessResultCallback(o, callBack);
                    }


                } catch (IOException e) {
                    sendFailedStringCallback(e, callBack);
                } catch (com.google.gson.JsonParseException e) {//Json解析的错误
                    sendFailedStringCallback(e, callBack);
                }
            }
        });
    }
    //发送结果到主线程
    private void sendSuccessResultCallback(final Object object, final ResultCallBack callBack){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack != null)
                    callBack.onResponse(object);
            }
        });
    }
    //发送结果到主线程
    private void sendFailedStringCallback(final Exception e, final ResultCallBack callback){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null)
                    callback.onError(e);
            }
        });
    }

    private Request buildPostRequest(String url, Param[] params)
    {
        if (params == null)
        {
            params = new Param[0];
        }

        FormBody.Builder builder = new FormBody.Builder()
                .add("size", "10");
        for (Param param : params)
        {
            builder.add(param.key, param.value);
        }
        RequestBody requestBody = builder.build();
        return new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
    }
}
