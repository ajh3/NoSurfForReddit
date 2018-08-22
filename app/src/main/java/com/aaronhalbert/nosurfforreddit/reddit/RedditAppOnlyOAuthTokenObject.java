package com.aaronhalbert.nosurfforreddit.reddit;

import com.google.gson.annotations.SerializedName;

public class RedditAppOnlyOAuthTokenObject {
    @SerializedName("access_token") private String accessToken;
    @SerializedName("token_type") private String tokenType;
    @SerializedName("expires_in") private int expiresIn;
    @SerializedName("scope") private String scope;

    public String getAccess_token() {
        return accessToken;
    }

    public String getToken_type() {
        return tokenType;
    }

    public int getExpires_in() {
        return expiresIn;
    }

    public String getScope() {
        return scope;
    }

}
