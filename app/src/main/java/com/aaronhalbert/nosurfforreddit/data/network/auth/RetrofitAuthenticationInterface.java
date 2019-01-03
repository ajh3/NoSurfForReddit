package com.aaronhalbert.nosurfforreddit.data.network.auth;

import com.aaronhalbert.nosurfforreddit.BuildConfig;
import com.aaronhalbert.nosurfforreddit.data.model.AppOnlyOAuthToken;
import com.aaronhalbert.nosurfforreddit.data.model.UserOAuthToken;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface RetrofitAuthenticationInterface {

    @Headers({BuildConfig.USER_AGENT})
    @FormUrlEncoded
    @POST   // can't provide a relative URL here when using @Url
    Single<AppOnlyOAuthToken> fetchAppOnlyOAuthTokenASync(
            @Url String baseUrl,
            @Field("grant_type") String grantType,
            @Field("device_id") String deviceId,
            @Header("Authorization") String authorization);

    @Headers({BuildConfig.USER_AGENT})
    @FormUrlEncoded
    @POST
    Single<UserOAuthToken> fetchUserOAuthTokenASync(
            @Url String baseUrl,
            @Field("grant_type") String grantType,
            @Field("code") String code,
            @Field("redirect_uri") String redirectUri,
            @Header("Authorization") String authorization);

    @Headers({BuildConfig.USER_AGENT})
    @FormUrlEncoded
    @POST
    Single<UserOAuthToken> refreshExpiredUserOAuthTokenASync(
            @Url String baseUrl,
            @Field("grant_type") String grantType,
            @Field("refresh_token") String refreshToken,
            @Header("Authorization") String authorization);
}
