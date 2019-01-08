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

package com.aaronhalbert.nosurfforreddit.data.remote

import com.aaronhalbert.nosurfforreddit.noSurfLog
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/* Reddit allows on average 1 request per second, and counts requests in 10 minute
 * (600 second) periods.
 *
 * This limit is per-user, not per-app.
 *
 * Rate limit the user if the average request rate in the current period is > 1/sec
 *
 * X-Ratelimit-Reset: Approximate number of seconds to end of period
 * X-Ratelimit-Used: Approximate number of requests used in this period
 */

private const val X_RATELIMIT_RESET = "x-ratelimit-reset"
private const val X_RATELIMIT_USED = "x-ratelimit-used"
private const val SLEEPING_ON_RATE_LIMIT = "sleeping on rate limit..."
private const val INTERRUPTED_EXCEPTION = "InterruptedException while trying to sleep on rate limit"

class RateLimitInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val xRateLimitReset = response.header(X_RATELIMIT_RESET)
        val xRateLimitUsed = response.header(X_RATELIMIT_USED)

        if (xRateLimitReset != null
            && xRateLimitUsed != null
            && (xRateLimitUsed.toInt() >= (600 - xRateLimitReset.toInt()))
        ) {
            // equivalent to "if >1 request per second has been made so far during this period"
            try {
                noSurfLog { SLEEPING_ON_RATE_LIMIT }
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                noSurfLog { INTERRUPTED_EXCEPTION }
            }
        }
        return response
    }
}
