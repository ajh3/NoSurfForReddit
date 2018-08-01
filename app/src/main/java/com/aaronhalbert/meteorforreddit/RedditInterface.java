package com.aaronhalbert.meteorforreddit;

import java.util.UUID;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RedditInterface {

    @Headers({"Authorization: Basic alBGNTlVRjVNYk1rV2c6",
            "User-Agent: android:com.aaronhalbert.meteorforreddit:v0.01a (by /u/Suspicious_Advantage)"})
    @FormUrlEncoded
    @POST("/api/v1/access_token")
    Call<AppOnlyOAuthToken> requestAppOnlyOAuthToken(
            @Field("grant_type") String GRANT_TYPE,
            @Field("device_id") String DEVICE_ID);


}
