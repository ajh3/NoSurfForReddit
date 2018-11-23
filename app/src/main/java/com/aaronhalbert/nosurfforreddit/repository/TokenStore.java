package com.aaronhalbert.nosurfforreddit.repository;

public interface TokenStore {
    String getUserOAuthAccessToken();
    String getUserOAuthRefreshToken();

    void setUserOAuthAccessTokenAsync(String userOAuthAccessToken);
    void setUserOAuthRefreshTokenAsync(String userOAuthRefreshToken);

    void clearUserOAuthAccessTokenAsync();
    void clearUserOAuthRefreshTokenAsync();
}
