package android.coolweather.com.coolweather.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by lgf on 17-12-2.
 */

public class Weather {
    public String status;

    public Basic basic;

    public AQI aqi;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
