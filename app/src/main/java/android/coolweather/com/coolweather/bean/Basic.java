package android.coolweather.com.coolweather.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lgf on 17-12-2.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {
        @SerializedName("loc")
        public String updateTime;
    }
}
