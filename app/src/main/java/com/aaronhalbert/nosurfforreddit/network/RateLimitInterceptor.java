package com.aaronhalbert.nosurfforreddit.network;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

// Reddit allows on average 1 request per second, and counts requests in 10 minute (600 second periods)
// Rate limit the user if the average request rate in the current period is 1/sec +

public class RateLimitInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        if (response.header("x-ratelimit-reset") != null && response.header("x-ratelimit-remaining") != null) {

            int xRateLimitReset = Integer.parseInt(response.header("x-ratelimit-reset"));
            //int xRateLimitRemaining = (int) Double.parseDouble(response.header("x-ratelimit-remaining"));
            int xRateLimitUsed = Integer.parseInt(response.header("x-ratelimit-used"));

            int requestRatePerSecondDuringThisPeriod = xRateLimitUsed / (600 - xRateLimitReset);

            if (requestRatePerSecondDuringThisPeriod >= 1) {
                try {
                    Log.e(getClass().toString(), "sleeping on rate limit...");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(getClass().toString(), "InterruptedException while trying to sleep on rate limit");
                }
            }
        }
        return response;
    }
}