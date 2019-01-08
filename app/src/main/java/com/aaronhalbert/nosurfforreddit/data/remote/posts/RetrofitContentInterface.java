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

package com.aaronhalbert.nosurfforreddit.data.remote.posts;

import com.aaronhalbert.nosurfforreddit.BuildConfig;
import com.aaronhalbert.nosurfforreddit.data.remote.posts.model.Listing;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface RetrofitContentInterface {

    String R_ALL_HOT = "r/all/hot";
    String AUTHORIZATION = "Authorization";
    String COMMENTS_ARTICLE = "comments/{article}";
    String ARTICLE = "article";
    String HOT = "hot";

    @Headers({BuildConfig.USER_AGENT})
    @GET(R_ALL_HOT)
    Single<Listing> fetchAllPostsASync(
            @Header(AUTHORIZATION) String authorization);

    @Headers({BuildConfig.USER_AGENT})
    @GET(HOT)
    Single<Listing> fetchSubscribedPostsASync(
            @Header(AUTHORIZATION) String authorization);

    @Headers({BuildConfig.USER_AGENT})
    @GET(COMMENTS_ARTICLE)
    Single<List<Listing>> fetchPostCommentsASync(
            @Header(AUTHORIZATION) String authorization,
            @Path(ARTICLE) String article);
}
