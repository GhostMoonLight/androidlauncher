package com.android.launcher3.rxbus;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Created by cgx on 2016/12/23.
 * 用RxJava实现事件总线
 *
 * Subject类它继承Observable类同时实现Observer接口, 因此Subject可以同时担当订阅者和被订阅者的角色。
 * PublishSubject继承Subject，只有被订阅后才会把接收到的事件立刻发送给订阅者
 * BehaviorSubject继承Subject, 可以缓存一个事件，发送事件后，在被订阅，订阅者也能收到改事件
 * ReplaySubject继承Subject, 它可以缓存多个发送给它的事件，在被订阅后会发送所有事件给订阅者
 *
 * Subject类是非线程安全的，通过它的子类SerializedSubject将Subject子类转成线程安全的
 *
 * 发生异常后订阅关系就会被取消。使用try－catch捕获异常，不让异常被RxJava捕获，在onError里重新订阅。
 */
public class RxBus {
    private static class Singleton{
        public static RxBus INSTANCE = new RxBus();
    }

    public static RxBus getInstance(){
        return Singleton.INSTANCE;
    }

    private Subject<Object> mSubject;


    private RxBus(){
        mSubject = PublishSubject.create().toSerialized();
    }

    /**
     * 发送事件
     * @param o
     */
    public void postEvent(Object o){
        mSubject.onNext(o);
    }

    /**
     * 返回指定类型的Obsearvable实例
     * @param calss
     * @param <T>
     * @return
     */
    public <T> Observable<T> toObservable(final Class<T> calss){
        return mSubject.ofType(calss);
    }

    public boolean hasObservables(){
        return mSubject.hasObservers();
    }

    //订阅
    public <T> Disposable doSubscribe(Class<T> type, Consumer<T> consumer){
        return toObservable(type).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
    }
    //订阅
    public <T> Disposable doSubscribe(Class<T> type, Consumer<T> consumer, Consumer<Throwable> error){
        return toObservable(type).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer, error);
    }
}
