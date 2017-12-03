package android.coolweather.com.coolweather.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.coolweather.com.coolweather.BuildConfig;
import android.coolweather.com.coolweather.R;
import android.coolweather.com.coolweather.bean.Basic;
import android.coolweather.com.coolweather.bean.Forecast;
import android.coolweather.com.coolweather.bean.Now;
import android.coolweather.com.coolweather.bean.Weather;
import android.coolweather.com.coolweather.conf.Constant;
import android.coolweather.com.coolweather.service.AutoUpdateService;
import android.coolweather.com.coolweather.util.HttpUtil;
import android.coolweather.com.coolweather.util.LogUtil;
import android.coolweather.com.coolweather.util.Utility;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
    private SwipeRefreshLayout swipeRefreshLayout;
    private DrawerLayout drawerLayout;
    private Button btnChooseCity;
    private ProgressDialog progressDialog;
    private String currentWeatherId;

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
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        btnChooseCity = (Button) findViewById(R.id.btn_choose_city);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
        showProgressDialog();
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String weatherData = defaultSharedPreferences.getString(Constant.SHARED_PREFERENCE_WEATHER_KEY, null);
        if (weatherData != null) {
            Weather weather = Utility.handleWeatherResponse(weatherData);
            if (weather != null) {
                currentWeatherId = weather.basic.weatherId;
            }
            showWeatherInfo(weather);
        } else {
            currentWeatherId = getIntent().getStringExtra(Constant.BUNDLE_WEATHER_ID_KEY);
            requestWeather(currentWeatherId);
        }
        String backgroundImgUrl = defaultSharedPreferences.getString(Constant.SHARED_PREFERENCE_BACKGROUND_IMG_URL_KEY, null);
        if (backgroundImgUrl != null) {
            updateBackgroundImage(backgroundImgUrl);
        } else {
            getBackgroundImage();
        }

        btnChooseCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(currentWeatherId);
            }
        });
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    private void getBackgroundImage() {
        HttpUtil.sendOkHttpRequest(Constant.BACKGROUND_IMG_URL, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), getString(R.string.get_image_failed), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null) {
                    final String responseData = response.body().string();
                    if (!TextUtils.isEmpty(responseData)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateBackgroundImage(responseData);
                            }
                        });
                        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                        edit.putString(Constant.SHARED_PREFERENCE_BACKGROUND_IMG_URL_KEY, responseData);
                        edit.apply();
                        return;
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), getString(R.string.get_image_failed), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void updateWeather(String weatherId) {
        drawerLayout.closeDrawers();
        swipeRefreshLayout.setRefreshing(true);
        requestWeather(weatherId);
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
                        Toast.makeText(getApplicationContext(), getString(R.string.get_weather_info_failed), Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
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
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                        editor.putString(Constant.SHARED_PREFERENCE_WEATHER_KEY, responseData);
                        editor.apply();
                        return;
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), getString(R.string.get_weather_info_failed), Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
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
                currentWeatherId = basic.weatherId;
            }
            Now now = weather.now;
            if (now != null) {
                tvDegree.setText(getString(R.string.degree_unit, now.temperature));
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

            String comfort = getString(R.string.comfort_level, weather.suggestion.comfort.info);
            String carWash = getString(R.string.wash_car_level, weather.suggestion.carWash.info);
            String sport = getString(R.string.sport_level, weather.suggestion.sport.info);
            tvComfort.setText(comfort);
            tvCarWash.setText(carWash);
            tvSport.setText(sport);
            svWeather.setVisibility(View.VISIBLE);
        }
        hideProgressDialog();
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.loading_data));
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
