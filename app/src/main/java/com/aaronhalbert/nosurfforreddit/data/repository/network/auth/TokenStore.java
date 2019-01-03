package com.aaronhalbert.nosurfforreddit.data.repository.network.auth;

public interface TokenStore {
    String getUserOAuthAccessToken();
    String getUserOAuthRefreshToken();

    void setUserOAuthAccessTokenAsync(String userOAuthAccessToken);
    void setUserOAuthRefreshTokenAsync(String userOAuthRefreshToken);

    void clearUserOAuthAccessTokenAsync();
    void clearUserOAuthRefreshTokenAsync();
}
