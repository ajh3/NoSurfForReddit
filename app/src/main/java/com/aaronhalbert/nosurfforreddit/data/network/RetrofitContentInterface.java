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

package com.aaronhalbert.nosurfforreddit.data.network;

import com.aaronhalbert.nosurfforreddit.BuildConfig;
import com.aaronhalbert.nosurfforreddit.data.model.Listing;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface RetrofitContentInterface {

    @Headers({BuildConfig.USER_AGENT})
    @GET("r/all/hot")
    Single<Listing> fetchAllPostsASync(
            @Header("Authorization") String authorization);

    @Headers({BuildConfig.USER_AGENT})
    @GET("hot")
    Single<Listing> fetchSubscribedPostsASync(
            @Header("Authorization") String authorization);

    @Headers({BuildConfig.USER_AGENT})
    @GET("comments/{article}")
    Single<List<Listing>> fetchPostCommentsASync(
            @Header("Authorization") String authorization,
            @Path("article") String article);
}
