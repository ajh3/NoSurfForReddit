/*
 * Copyright (c) 2018-present, Aaron J. Halbert.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package com.aaronhalbert.nosurfforreddit.data.remote.auth;

import com.aaronhalbert.nosurfforreddit.BuildConfig;
import com.aaronhalbert.nosurfforreddit.data.remote.auth.model.AppOnlyOAuthToken;
import com.aaronhalbert.nosurfforreddit.data.remote.auth.model.UserOAuthToken;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface RetrofitAuthenticationInterface {

    String GRANT_TYPE = "grant_type";
    String DEVICE_ID = "device_id";
    String AUTHORIZATION = "Authorization";
    String CODE = "code";
    String REDIRECT_URI = "redirect_uri";
    String REFRESH_TOKEN = "refresh_token";

    @Headers({BuildConfig.USER_AGENT})
    @FormUrlEncoded
    @POST // can't provide a relative URL here when using @Url
    Single<AppOnlyOAuthToken> fetchAppOnlyOAuthTokenASync(
            @Url String baseUrl,
            @Field(GRANT_TYPE) String grantType,
            @Field(DEVICE_ID) String deviceId,
            @Header(AUTHORIZATION) String authorization);

    @Headers({BuildConfig.USER_AGENT})
    @FormUrlEncoded
    @POST
    Single<UserOAuthToken> fetchUserOAuthTokenASync(
            @Url String baseUrl,
            @Field(GRANT_TYPE) String grantType,
            @Field(CODE) String code,
            @Field(REDIRECT_URI) String redirectUri,
            @Header(AUTHORIZATION) String authorization);

    @Headers({BuildConfig.USER_AGENT})
    @FormUrlEncoded
    @POST
    Single<UserOAuthToken> refreshExpiredUserOAuthTokenASync(
            @Url String baseUrl,
            @Field(GRANT_TYPE) String grantType,
            @Field(REFRESH_TOKEN) String refreshToken,
            @Header(AUTHORIZATION) String authorization);
}
