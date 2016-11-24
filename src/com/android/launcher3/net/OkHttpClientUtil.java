package com.android.launcher3.net;

import android.os.Handler;
import android.os.Looper;

import com.android.launcher3.LauncherApplication;
import com.android.launcher3.common.LogUtils;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
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
    private CacheControl mUseCache, mNoUseCache;

    private OkHttpClientUtil(){
        File sdCache = new File(LauncherApplication.getInstance().getUrlCacheDir());
        mOkHttpClient = (new OkHttpClient.Builder())
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(new LoggingInterceptor())
                .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
                .cache(new Cache(sdCache.getAbsoluteFile(), CACHE_SIZE)).build();
        mGson = new Gson();
        initCacheControl();
    }

    private void initCacheControl(){
        final CacheControl.Builder builder = new CacheControl.Builder();
        //        builder.onlyIfCached();//只使用缓存
        //        builder.noTransform();//禁止转码
        builder.maxAge(60*60*12, TimeUnit.SECONDS);//这个是控制缓存的最大生命时间
//        builder.maxStale(60*60*12, TimeUnit.SECONDS);//这个是控制缓存的过时时间
        //        builder.minFresh(10, TimeUnit.SECONDS);//指示客户机可以接收响应时间小于当前时间加上指定时间的响应。
        mUseCache = builder.build();//cacheControl

        final CacheControl.Builder builder1 = new CacheControl.Builder();
        builder1.noCache();//不使用缓存，全部走网络
        builder1.noStore();//不使用缓存，也不存储缓存
        mNoUseCache = builder1.build();//cacheControl
    }

    /**
     * 云端响应头拦截器，用来配置缓存策略
     * Dangerous interceptor that rewrites the server's cache-control header.
     */
    private static final Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (!NetworkStatus.isNetWorking(LauncherApplication.getInstance())) {
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
            }
            Response originalResponse = chain.proceed(request);
            if (NetworkStatus.isNetWorking(LauncherApplication.getInstance())) {
                //有网的时候读接口上的@Headers里的配置，你可以在这里进行统一的设置(注掉部分)
                String cacheControl = request.cacheControl().toString();
                return originalResponse.newBuilder()
                        .header("Cache-Control", cacheControl)
                        //.header("Cache-Control", "max-age=3600")
                        .removeHeader("Pragma") // 清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                        .build();
            } else {
                int maxAge= 60 * 60 * 12;
                return originalResponse.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-age=" + maxAge)
                        .removeHeader("Pragma")
                        .build();
            }
        }
    };

    public class LoggingInterceptor implements Interceptor {

        @Override public Response intercept(Interceptor.Chain chain) throws IOException {

            Request request = chain.request();
            LogUtils.e("AAAAA",String.format("Sending request %s on %s%n%s", request.url(),  chain.connection(), request.headers()));

            long t1 = System.nanoTime();
            okhttp3.Response response = chain.proceed(chain.request());
            long t2 = System.nanoTime();
            LogUtils.e("AAAAA",String.format(Locale.getDefault(), "Received response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6d, response.headers()));

            okhttp3.MediaType mediaType = response.body().contentType();
            String content = response.body().string();
            return response.newBuilder()
                    .body(okhttp3.ResponseBody.create(mediaType, content))
                    .build();
        }
    }

    /**
     * 同步的Get请求,获取请求结果
     *
     * @param url
     * @return Response
     */
    private Response getHttp(String url, boolean isUseCache) throws IOException{
        final Request request = buildGetRequest(url, isUseCache);
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
    private String getAsString(String url, boolean isUseCache) throws IOException{
        Response execute = getHttp(url, isUseCache);
        return execute.body().string();
    }

    /**
     * 同步的Post请求, 获取结果
     *
     * @param url
     * @param params post的参数
     * @return
     */
    private Response post(String url, boolean isUseCache, Param... params) throws IOException {
        Request request = buildPostRequest(url, params, isUseCache);
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
    private String postAsString(String url, boolean isUseCache, Param... params) throws IOException {
        Response response = post(url, isUseCache, params);
        return response.body().string();
    }

    /**
     * 异步get请求
     */
    private void getAsynHttp(String url, ResultCallBack callback, boolean isUseCache) {
        exectAsyncHttp(buildGetRequest(url, isUseCache), callback);
    }

    /**
     * post异步请求
     */
    private void postAsynHttp(String url, boolean isUseCache, ResultCallBack callback, Param... params) {
        exectAsyncHttp(buildPostRequest(url, params, isUseCache), callback);
    }

    /**
     * 异步请求
     */
    private void exectAsyncHttp(final Request request, final ResultCallBack callBack){
        Call mcall= mOkHttpClient.newCall(request);
        mcall.enqueue(new Callback() {  //请求网络后的回调，在异步线程
            @Override
            public void onFailure(Call call, IOException e) {
                sendFailedStringCallback(e, callBack);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try
                {
                    final String string = response.body().string();
                    if (callBack.mType == String.class){
                        sendSuccessResultCallback(string, callBack);
                    } else{
                        Object o = mGson.fromJson(string, callBack.mType);
                        sendSuccessResultCallback(o, callBack);
                    }

                    if (response.networkResponse() == null){
                        LogUtils.e("AAAAA", "使用缓存");
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

    private Request buildGetRequest(String url, boolean isUseCache) {
        Request.Builder requestBuilder = new Request.Builder().url(url);
        //可以省略，默认是GET请求
        requestBuilder.method("GET", null);
        if (isUseCache) {
            requestBuilder.cacheControl(mUseCache);
        } else {
            requestBuilder.cacheControl(mNoUseCache);
        }
        Request request = requestBuilder.build();
        return request;
    }

    private Request buildPostRequest(String url, Param[] params, boolean isUseCache)
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
        if (isUseCache){
            return new Request.Builder()
                    .url(url)
                    .cacheControl(mUseCache)
                    .post(requestBody)
                    .build();
        }else{
            return new Request.Builder()
                    .url(url)
                    .cacheControl(mNoUseCache)
                    .post(requestBody)
                    .build();
        }
    }

    /**
     *
     * @param url
     * @param callback
     * @param isUseCache   是否使用缓存，
     */
    public void doGetAsync(String url, ResultCallBack callback, boolean isUseCache){
        getAsynHttp(url, callback, isUseCache);
    }
    public String doGet(String url, boolean isUseCache) throws IOException{
        return getAsString(url, isUseCache);
    }

    public void doPostAsync(String url, boolean isUseCache, ResultCallBack callback, Param... params){
        postAsynHttp(url, isUseCache, callback, params);
    }
    public String dopost(String url, boolean isUseCache, Param... params)throws IOException{
        return postAsString(url, isUseCache, params);
    }
}