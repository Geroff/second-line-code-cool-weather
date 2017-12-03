package android.coolweather.com.coolweather.util;

import android.coolweather.com.coolweather.bean.City;
import android.coolweather.com.coolweather.bean.County;
import android.coolweather.com.coolweather.bean.Province;
import android.coolweather.com.coolweather.bean.Weather;
import android.coolweather.com.coolweather.conf.Constant;
import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lgf on 17-11-30.
 */

public class Utility {
    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray provinceJSONArray = new JSONArray(response);
                int length = provinceJSONArray.length();
                if (length > 0) {
                    for (int i = 0; i < length; i++) {
                        JSONObject provinceJSON = provinceJSONArray.getJSONObject(i);
                        if (provinceJSON != null) {
                            Province province = new Province();
                            province.setProvinceName(provinceJSON.optString(Constant.JSON_KEY_NAME));
                            province.setProvinceCode(provinceJSON.optInt(Constant.JSON_KEY_ID));
                            province.save();
                        }
                    }
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static boolean handleCityResponse(int provinceCode, String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray cityJSONArray = new JSONArray(response);
                int length = cityJSONArray.length();
                if (length > 0) {
                    for (int i = 0; i < length; i++) {
                        JSONObject cityJSON = cityJSONArray.getJSONObject(i);
                        if (cityJSON != null) {
                            City city = new City();
                            city.setProvinceCode(provinceCode);
                            city.setCityName(cityJSON.optString(Constant.JSON_KEY_NAME));
                            city.setCityCode(cityJSON.optInt(Constant.JSON_KEY_ID));
                            city.save();
                        }
                    }
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    public static boolean handleCountyResponse(int cityCode, String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray countyJSONArray = new JSONArray(response);
                int length = countyJSONArray.length();
                if (length > 0) {
                    for (int i = 0; i < length; i++) {
                        JSONObject countyJSON = countyJSONArray.optJSONObject(i);
                        if (countyJSON != null) {
                            County county = new County();
                            county.setCityCode(cityCode);
                            county.setCountyName(countyJSON.optString(Constant.JSON_KEY_NAME));
                            county.setWeatherId(countyJSON.optString(Constant.JSON_KEY_WEATHER_ID));
                            county.save();
                        }
                    }
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject hwWeatherJSON = new JSONObject(response);
            JSONArray heWeatherJSONArray = hwWeatherJSON.optJSONArray(Constant.JSON_KEY_HE_WEATHER);
            if (heWeatherJSONArray != null) {
                JSONObject weatherJSON = heWeatherJSONArray.optJSONObject(0);
                if (weatherJSON != null) {
                    return new Gson().fromJson(weatherJSON.toString(), Weather.class);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
