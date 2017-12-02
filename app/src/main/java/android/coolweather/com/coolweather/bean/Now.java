package android.coolweather.com.coolweather.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lgf on 17-12-2.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {
        @SerializedName("txt")
        public String info;
    }
}
