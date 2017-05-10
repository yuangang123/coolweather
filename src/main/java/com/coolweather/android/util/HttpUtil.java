package com.coolweather.android.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.internal.connection.RealConnection;

/**
 * Created by 袁刚 on 2017/4/21.
 */

public class HttpUtil {
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
