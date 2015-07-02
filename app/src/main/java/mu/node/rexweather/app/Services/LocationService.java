package mu.node.rexweather.app.Services;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import rx.Observable;
import rx.Subscriber;

/**
 *
 * 假设你编写的Android app需要从网络请求数据（感觉这是必备的了，还有单机么？）。网络请求需要话费较长的时间，因此你打算在另外一个线程中加载数据。为问题来了！
 编写多线程的Android应用程序是很难的，因为你必须确保代码在正确的线程中运行，否则的话可能会导致app崩溃。最常见的就是在非主线程更新UI。
 使用RxJava，你可以使用subscribeOn()指定被观察者代码运行的线程，使用observerOn()指定观察者运行的线程：

 myObservableServices.retrieveImage(url)
 .subscribeOn(Schedulers.io())
 .observeOn(AndroidSchedulers.mainThread())
 .subscribe(bitmap -> myImageView.setImageBitmap(bitmap));
 是不是很简单？任何在我的Subscriber前面执行的代码都是在I/O线程中运行。最后，操作view的代码在主线程中运行.
 最棒的是我可以把subscribeOn()和observerOn()添加到任何Observable对象上。这两个也是操作符！。我不需要关心Observable对象以及它上面有哪些操作符。仅仅运用这两个操作符就可以实现在不同的线程中调度。
 如果使用AsyncTask或者其他类似的，我将不得不仔细设计我的代码，找出需要并发执行的部分。使用RxJava，我可以保持代码不变，仅仅在需要并发的时候调用这两个操作符就可以。

 RxJava 可以理解为 任务队列，并且可以指定运行所在的线程 ，有点类似于线程池

 http://wiki.xby1993.net/doku.php?id=opensourcelearn:rxjava_rxandroid:introduce

 Map returns an object of type T
 FlatMap returns an Observable<T>.

 */
public class LocationService {
    private final LocationManager mLocationManager;

    public LocationService(LocationManager locationManager) {
        mLocationManager = locationManager;
    }

    public Observable<Location> getLocation() {
        return Observable.create(new Observable.OnSubscribe<Location>() {
            @Override
            public void call(final Subscriber<? super Location> subscriber) {
                Log.e("aaa", "Observable.OnSubscribe.call" + Thread.currentThread().getId());
                final LocationListener locationListener = new LocationListener() {
                    public void onLocationChanged(final Location location) {
                        Log.e("aaa", "onLocationChanged" + Thread.currentThread().getId());
                        //执行subscriber.onNext，就会执行Observable一系列的map，至到subscriber.onNext
                        subscriber.onNext(location);
                        subscriber.onCompleted();

                        Looper.myLooper().quit();
                    }

                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    public void onProviderEnabled(String provider) {
                    }

                    public void onProviderDisabled(String provider) {
                    }
                };

                final Criteria locationCriteria = new Criteria();
                locationCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
                locationCriteria.setPowerRequirement(Criteria.POWER_LOW);
                final String locationProvider = mLocationManager
                        .getBestProvider(locationCriteria, true);

                Looper.prepare();

                mLocationManager.requestSingleUpdate(locationProvider,
                        locationListener, Looper.myLooper());

                Looper.loop();
            }
        });
    }
}