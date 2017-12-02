package android.coolweather.com.coolweather.activity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.coolweather.com.coolweather.BuildConfig;
import android.coolweather.com.coolweather.R;
import android.coolweather.com.coolweather.bean.Basic;
import android.coolweather.com.coolweather.bean.Forecast;
import android.coolweather.com.coolweather.bean.Now;
import android.coolweather.com.coolweather.bean.Weather;
import android.coolweather.com.coolweather.conf.Constant;
import android.coolweather.com.coolweather.util.HttpUtil;
import android.coolweather.com.coolweather.util.LogUtil;
import android.coolweather.com.coolweather.util.Utility;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by lgf on 17-12-2.
 */

public class WeatherActivity extends AppCompatActivity {
    private ScrollView svWeather;
    private TextView tvTitle;
    private TextView tvUpdateTime;
    private TextView tvDegree;
    private TextView tvWeatherInfo;
    private TextView tvAQI;
    private TextView tvPM25;
    private TextView tvComfort;
    private TextView tvCarWash;
    private TextView tvSport;
    private LinearLayout llForecast;
    private ImageView ivBackgroundImg;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            if (decorView != null) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        svWeather = (ScrollView) findViewById(R.id.sv_weather);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvUpdateTime = (TextView) findViewById(R.id.tv_update_time);
        tvDegree = (TextView) findViewById(R.id.tv_degree);
        tvWeatherInfo = (TextView) findViewById(R.id.tv_weather_info);
        tvAQI = (TextView) findViewById(R.id.tv_aqi);
        tvPM25 = (TextView) findViewById(R.id.tv_pm25);
        tvComfort = (TextView) findViewById(R.id.tv_comfort);
        tvCarWash = (TextView) findViewById(R.id.tv_car_wash);
        tvSport = (TextView) findViewById(R.id.tv_sport);
        llForecast = (LinearLayout) findViewById(R.id.ll_forecast);
        ivBackgroundImg = (ImageView) findViewById(R.id.iv_bing_pic);
        showProgressDialog();
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String weatherData = defaultSharedPreferences.getString("weather", null);
        if(weatherData != null) {
            Weather weather = Utility.handleWeatherResponse(weatherData);
            showWeatherInfo(weather);
        } else {
            String weatherId = getIntent().getStringExtra("weatherId");
            requestWeather(weatherId);
        }
        String backgroundImgUrl = defaultSharedPreferences.getString("backgroundImg", null);
        if (backgroundImgUrl != null) {
            updateBackgroundImage(backgroundImgUrl);
        } else {
            getBackgroundImage();
        }
    }

    private void getBackgroundImage() {
        HttpUtil.sendOkHttpRequest(Constant.BACKGROUND_IMG_URL, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "获取图片失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null) {
                    final String responseData = response.body().string();
                    if(!TextUtils.isEmpty(responseData)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateBackgroundImage(responseData);
                            }
                        });
                        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                        edit.putString("backgroundImg", responseData);
                        edit.apply();
                        return;
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "获取图片失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void updateBackgroundImage(String url) {
        Glide.with(this).load(url).into(ivBackgroundImg);
    }

    private void requestWeather(String weatherId) {
        String weatherUrl = Constant.WEATHER_URL + "?cityid=" + weatherId + "&key=" + Constant.KEY;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null) {
                    String responseData = response.body().string();
                    if (BuildConfig.DEBUG) {
                        LogUtil.info("WeatherActivity.onResponse() ## request weather response-->" + responseData);
                    }
                    final Weather weather = Utility.handleWeatherResponse(responseData);
                    if (weather != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showWeatherInfo(weather);
                            }
                        });
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                        editor.putString("weather", responseData);
                        editor.apply();
                        return;
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showWeatherInfo(Weather weather) {
        if (weather != null) {
            Basic basic = weather.basic;
            if (basic != null) {
                tvTitle.setText(basic.cityName);
                Basic.Update update = basic.update;
                if (update != null) {
                    tvUpdateTime.setText(update.updateTime.split(" ")[1]);
                } else {
                    tvUpdateTime.setVisibility(View.GONE);
                }
            }
            Now now = weather.now;
            if (now != null) {
                tvDegree.setText(now.temperature + "°Ｃ");
                Now.More more = now.more;
                if (more != null) {
                    tvWeatherInfo.setText(more.info);
                }
            }
            llForecast.removeAllViews();
            for (Forecast forecast : weather.forecastList) {
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_weather_forecast_item, llForecast, false);
                TextView tvDate = view.findViewById(R.id.tv_date);
                TextView tvInfo = view.findViewById(R.id.tv_info);
                TextView tvMax = view.findViewById(R.id.tv_max);
                TextView tvMin = view.findViewById(R.id.tv_min);
                tvDate.setText(forecast.date);
                tvInfo.setText(forecast.more.info);
                tvMax.setText(forecast.temperature.max);
                tvMin.setText(forecast.temperature.min);
                llForecast.addView(view);
            }

            if (weather.aqi != null) {
                tvAQI.setText(weather.aqi.city.aqi);
                tvPM25.setText(weather.aqi.city.pm25);
            }

            String comfort = "舒适度：" + weather.suggestion.comfort.info;
            String carwash = "洗车指数：" + weather.suggestion.carWash.info;
            String sport = "运动建议：" + weather.suggestion.sport.info;
            tvComfort.setText(comfort);
            tvCarWash.setText(carwash);
            tvSport.setText(sport);
            svWeather.setVisibility(View.VISIBLE);
        }
        hideProgressDialog();
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载数据");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
