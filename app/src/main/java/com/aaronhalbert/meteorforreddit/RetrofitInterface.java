package com.aaronhalbert.meteorforreddit;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface RetrofitInterface {
    String AUTHORIZATION_HEADER = "Authorization: Basic alBGNTlVRjVNYk1rV2c6";
    String USER_AGENT = "User-Agent: android:com.aaronhalbert.meteorforreddit:v0.01a (by /u/Suspicious_Advantage)";

    @Headers({AUTHORIZATION_HEADER, USER_AGENT})
    @FormUrlEncoded
    @POST
    Call<AppOnlyOAuthToken> requestAppOnlyOAuthToken(
            @Url String url,
            @Field("grant_type") String GRANT_TYPE,
            @Field("device_id") String DEVICE_ID);

    @Headers({USER_AGENT})
    @GET("r/all")
    Call<RedditListingObject> requestSubRedditListing(@Header("Authorization") String authorization);

}
