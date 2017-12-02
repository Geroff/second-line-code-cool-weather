package android.coolweather.com.coolweather.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.coolweather.com.coolweather.R;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String weatherData = defaultSharedPreferences.getString("weather", null);
        if (weatherData != null) {
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_main);
    }
}
