package com.aaronhalbert.nosurfforreddit.data.model;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("ALL")
public class AppOnlyOAuthToken {
    @SerializedName("access_token") private String accessToken;
    @SerializedName("token_type") private String tokenType;
    @SerializedName("expires_in") private int expiresIn;
    @SerializedName("scope") private String scope;

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getScope() {
        return scope;
    }
}
