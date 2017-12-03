package android.coolweather.com.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.coolweather.com.coolweather.BuildConfig;
import android.coolweather.com.coolweather.bean.Weather;
import android.coolweather.com.coolweather.conf.Constant;
import android.coolweather.com.coolweather.util.HttpUtil;
import android.coolweather.com.coolweather.util.LogUtil;
import android.coolweather.com.coolweather.util.Utility;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by lgf on 17-12-2.
 */

public class AutoUpdateService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        if (BuildConfig.DEBUG) {
            LogUtil.info("AutoUpdateService.onStartCommand() ## Thread name " + Thread.currentThread().getName());
        }
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime = SystemClock.elapsedRealtime() + Constant.AUTO_UPDATE_INTERVAL;
        Intent serviceIntent = new Intent(this, AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, serviceIntent, 0);
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, triggerAtTime, pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateBingPic() {
       HttpUtil.sendOkHttpRequest(Constant.BACKGROUND_IMG_URL, new Callback() {
           @Override
           public void onFailure(Call call, IOException e) {

           }

           @Override
           public void onResponse(Call call, Response response) throws IOException {
                if (response != null) {
                    String backgroundImageUrl = response.body().string();
                    if (!TextUtils.isEmpty(backgroundImageUrl)) {
                        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor edit = defaultSharedPreferences.edit();
                        edit.putString(Constant.SHARED_PREFERENCE_BACKGROUND_IMG_URL_KEY, backgroundImageUrl);
                        edit.apply();
                        if (BuildConfig.DEBUG) {
                            LogUtil.debug("AutoUpdateService.updateBingPic().Callback.onResponse() ##");
                        }
                    }
                }
           }
       });
    }

    private void updateWeather() {
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String weatherData = defaultSharedPreferences.getString(Constant.SHARED_PREFERENCE_WEATHER_KEY, null);
        if (weatherData != null) {
            Weather weather = Utility.handleWeatherResponse(weatherData);
            if (weather != null) {
                String weatherId = weather.basic.weatherId;
                String weatherUrl = Constant.WEATHER_URL + "?cityid=" + weatherId + "&key=" + Constant.KEY;
                HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response != null) {
                            String responseData = response.body().string();
                            if (!TextUtils.isEmpty(responseData)) {
                                Weather weather = Utility.handleWeatherResponse(responseData);
                                if (weather != null && Constant.OK_STATUS.equals(weather.status)) {
                                    SharedPreferences.Editor edit = defaultSharedPreferences.edit();
                                    edit.putString(Constant.SHARED_PREFERENCE_WEATHER_KEY, responseData);
                                    edit.apply();
                                    if (BuildConfig.DEBUG) {
                                        LogUtil.debug("AutoUpdateService.updateWeather().Callback.onResponse() ## Thread name " + Thread.currentThread().getName());
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
    }
}
