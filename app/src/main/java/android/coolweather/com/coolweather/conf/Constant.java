package android.coolweather.com.coolweather.conf;

/**
 * Created by lgf on 17-12-2.
 */
public class Constant {
    public static final String HOST_URL = "http://guolin.tech/api";
    public static final String PROVINCE_URL = HOST_URL + "/china";
    public static final String WEATHER_URL = HOST_URL + "/weather";
    public static final String BACKGROUND_IMG_URL = HOST_URL + "/bing_pic";
    public static final String KEY = "790ebfe18273449ebcd0e145fd3c64aa";

    public static final String OK_STATUS = "ok";

    public static final String JSON_KEY_HE_WEATHER = "HeWeather";
    public static final String JSON_KEY_WEATHER_ID = "weather_id";
    public static final String JSON_KEY_NAME = "name";
    public static final String JSON_KEY_ID = "id";

    public static final String SHARED_PREFERENCE_WEATHER_KEY = "weather";
    public static final String SHARED_PREFERENCE_BACKGROUND_IMG_URL_KEY = "backgroundImageUrl";

    public static final String BUNDLE_WEATHER_ID_KEY = "weatherId";

    public static final int AUTO_UPDATE_INTERVAL = 8 * 60 * 60 * 1000;
}
