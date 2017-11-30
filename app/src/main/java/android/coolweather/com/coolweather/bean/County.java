package android.coolweather.com.coolweather.bean;

import org.litepal.crud.DataSupport;

/**
 * Created by lgf on 17-11-30.
 */

public class County extends DataSupport {
    private int id;
    private int countyCode;
    private String countyName;
    private int cityId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCountyCode() {
        return countyCode;
    }

    public void setCountyCode(int countyCode) {
        this.countyCode = countyCode;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
