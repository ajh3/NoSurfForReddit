package com.aaronhalbert.nosurfforreddit.network;

import com.aaronhalbert.nosurfforreddit.network.redditschema.AppOnlyOAuthToken;
import com.aaronhalbert.nosurfforreddit.network.redditschema.Listing;
import com.aaronhalbert.nosurfforreddit.network.redditschema.UserOAuthToken;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Url;

interface RetrofitInterface {
    String USER_AGENT = "User-Agent: android:com.aaronhalbert.nosurfforreddit:v0.01a (by /u/Suspicious_Advantage)";

    @Headers({USER_AGENT})
    @FormUrlEncoded
    @POST   // can't provide a relative URL here when using @Url
    Call<AppOnlyOAuthToken> fetchAppOnlyOAuthTokenSync(
            @Url String baseUrl,
            @Field("grant_type") String grantType,
            @Field("device_id") String deviceId,
            @Header("Authorization") String authorization);

    @Headers({USER_AGENT})
    @FormUrlEncoded
    @POST
    Call<UserOAuthToken> fetchUserOAuthTokenSync(
            @Url String baseUrl,
            @Field("grant_type") String grantType,
            @Field("code") String code,
            @Field("redirect_uri") String redirectUri,
            @Header("Authorization") String authorization);

    @Headers({USER_AGENT})
    @FormUrlEncoded
    @POST
    Call<UserOAuthToken> refreshExpiredUserOAuthTokenSync(
            @Url String baseUrl,
            @Field("grant_type") String grantType,
            @Field("refresh_token") String refreshToken,
            @Header("Authorization") String authorization);

    @Headers({USER_AGENT})
    @GET("r/all/hot")
    Call<Listing> fetchAllPostsSync(
            @Header("Authorization") String authorization);

    @Headers({USER_AGENT})
    @GET("hot")
    Call<Listing> fetchSubscribedPostsSync(
            @Header("Authorization") String authorization);

    @Headers({USER_AGENT})
    @GET("comments/{article}")
    Call<List<Listing>> fetchPostCommentsSync(
            @Header("Authorization") String authorization,
            @Path("article") String article);
}
