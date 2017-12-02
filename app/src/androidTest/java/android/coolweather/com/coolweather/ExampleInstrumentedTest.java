package android.coolweather.com.coolweather;

import android.content.Context;
import android.coolweather.com.coolweather.util.HttpUtil;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("android.coolweather.com.coolweather", appContext.getPackageName());
    }

    @Test
    public void testHttpUtil() {
        HttpUtil.sendOkHttpRequest("http://guolin.tech/api/china", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null) {
                    Log.d("AndroidTest", response.body().toString());
                }
            }
        });
    }
}
