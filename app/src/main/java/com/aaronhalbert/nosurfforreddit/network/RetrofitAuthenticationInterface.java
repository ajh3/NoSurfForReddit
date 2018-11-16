package com.aaronhalbert.nosurfforreddit.network;

import com.aaronhalbert.nosurfforreddit.BuildConfig;
import com.aaronhalbert.nosurfforreddit.network.redditschema.AppOnlyOAuthToken;
import com.aaronhalbert.nosurfforreddit.network.redditschema.UserOAuthToken;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

interface RetrofitAuthenticationInterface {

    @Headers({BuildConfig.USER_AGENT})
    @FormUrlEncoded
    @POST   // can't provide a relative URL here when using @Url
    Call<AppOnlyOAuthToken> fetchAppOnlyOAuthTokenASync(
            @Url String baseUrl,
            @Field("grant_type") String grantType,
            @Field("device_id") String deviceId,
            @Header("Authorization") String authorization);

    @Headers({BuildConfig.USER_AGENT})
    @FormUrlEncoded
    @POST
    Call<UserOAuthToken> fetchUserOAuthTokenASync(
            @Url String baseUrl,
            @Field("grant_type") String grantType,
            @Field("code") String code,
            @Field("redirect_uri") String redirectUri,
            @Header("Authorization") String authorization);

    @Headers({BuildConfig.USER_AGENT})
    @FormUrlEncoded
    @POST
    Call<UserOAuthToken> refreshExpiredUserOAuthTokenASync(
            @Url String baseUrl,
            @Field("grant_type") String grantType,
            @Field("refresh_token") String refreshToken,
            @Header("Authorization") String authorization);
}
