package android.coolweather.com.coolweather.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.coolweather.com.coolweather.BuildConfig;
import android.coolweather.com.coolweather.R;
import android.coolweather.com.coolweather.activity.MainActivity;
import android.coolweather.com.coolweather.activity.WeatherActivity;
import android.coolweather.com.coolweather.bean.City;
import android.coolweather.com.coolweather.bean.County;
import android.coolweather.com.coolweather.bean.Province;
import android.coolweather.com.coolweather.conf.Constant;
import android.coolweather.com.coolweather.util.HttpUtil;
import android.coolweather.com.coolweather.util.LogUtil;
import android.coolweather.com.coolweather.util.Utility;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by lgf on 17-12-2.
 */

public class AreaFragment extends Fragment {
    public static final int PROVINCE_LEVEL = 0;
    public static final int CITY_LEVEL = 1;
    public static final int COUNTY_LEVEL = 2;
    private ListView lvAreaList;
    private Button btnBack;
    private TextView tvTitle;
    private ProgressDialog progressDialog;
    private ArrayAdapter adapter;
    private List<String> dataList = new ArrayList<String>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private int currentLevel = PROVINCE_LEVEL;
    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_area, container, false);
        lvAreaList = view.findViewById(R.id.lv_area_list);
        btnBack = view.findViewById(R.id.btn_back);
        tvTitle = view.findViewById(R.id.tv_title);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, dataList);
        lvAreaList.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        lvAreaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == PROVINCE_LEVEL) {
                    selectedProvince = provinceList.get(position);
                    if (BuildConfig.DEBUG) {
                        LogUtil.info("AreaFragment.onItemClick() ## province name-->" + selectedProvince.getProvinceName() + ", province code-->" + selectedProvince.getProvinceCode());
                    }
                    queryCities();
                } else if (currentLevel == CITY_LEVEL) {
                    selectedCity = cityList.get(position);
                    if (BuildConfig.DEBUG) {
                        LogUtil.info("AreaFragment.onItemClick() ## city name-->" + selectedCity.getCityName() + ", city code-->" + selectedCity.getCityCode());
                    }
                    queryCounties();
                } else if (currentLevel == COUNTY_LEVEL) {
                    selectedCounty = countyList.get(position);
                    String weatherId = selectedCounty.getWeatherId();
                    if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra(Constant.BUNDLE_WEATHER_ID_KEY, weatherId);
                        startActivity(intent);
                        getActivity().finish();
                        if (BuildConfig.DEBUG) {
                            LogUtil.info("AreaFragment.onItemClick() ## county name-->" + selectedCounty.getCountyName() + ", county weather id-->" + weatherId);
                        }
                    } else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity weatherActivity = (WeatherActivity) getActivity();
                        weatherActivity.updateWeather(weatherId);
                    }
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == CITY_LEVEL) {
                    queryProvinces();
                } else if (currentLevel == COUNTY_LEVEL) {
                    queryCities();
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces() {
        tvTitle.setText(getString(R.string.fragment_choose_area_default_title));
        btnBack.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList != null && provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                if (province == null) {
                    continue;
                }

                dataList.add(province.getProvinceName());
            }

            adapter.notifyDataSetChanged();
            lvAreaList.setSelection(0);
            currentLevel = PROVINCE_LEVEL;
        } else {
            queryFromServer(Constant.PROVINCE_URL, PROVINCE_LEVEL);
        }
    }

    private void queryCities() {
        tvTitle.setText(selectedProvince.getProvinceName());
        btnBack.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceCode = ?", String.valueOf(selectedProvince.getProvinceCode())).find(City.class);
        if (cityList != null && cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                if (city == null) {
                    continue;
                }

                dataList.add(city.getCityName());
            }

            adapter.notifyDataSetChanged();
            lvAreaList.setSelection(0);
            currentLevel = CITY_LEVEL;
        } else {
            queryFromServer(Constant.PROVINCE_URL + "/" + selectedProvince.getProvinceCode(), CITY_LEVEL);
        }
    }

    private void queryCounties() {
        tvTitle.setText(selectedCity.getCityName());
        btnBack.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityCode = ?", String.valueOf(selectedCity.getCityCode())).find(County.class);
        if (countyList != null && countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                if (county == null) {
                    continue;
                }
                dataList.add(county.getCountyName());
            }

            adapter.notifyDataSetChanged();
            lvAreaList.setSelection(0);
            currentLevel = COUNTY_LEVEL;
        } else {
            queryFromServer(Constant.PROVINCE_URL + "/" + selectedProvince.getProvinceCode() + "/" + selectedCity.getCityCode(), COUNTY_LEVEL);
        }
    }

    private void queryFromServer(String url, final int type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        Toast.makeText(getContext(), getString(R.string.get_data_failed), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null) {
                    String responseData = response.body().string();
                    if (BuildConfig.DEBUG) {
                        LogUtil.debug("AreaFragment.onResponse() ## type--> " + type + ", response-->" + responseData);
                    }
                    boolean result = false;
                    if (type == PROVINCE_LEVEL) {
                        result = Utility.handleProvinceResponse(responseData);
                    } else if (type == CITY_LEVEL) {
                        result = Utility.handleCityResponse(selectedProvince.getProvinceCode(), responseData);
                    } else if (type == COUNTY_LEVEL) {
                        result = Utility.handleCountyResponse(selectedCity.getCityCode(), responseData);
                    }
                    if (result) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressDialog();
                                if (type == PROVINCE_LEVEL) {
                                    queryProvinces();
                                } else if (type == CITY_LEVEL) {
                                    queryCities();
                                } else if (type == COUNTY_LEVEL) {
                                    queryCounties();
                                }
                            }
                        });
                        return;
                    }
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        Toast.makeText(getContext(), getString(R.string.get_data_failed), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
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
