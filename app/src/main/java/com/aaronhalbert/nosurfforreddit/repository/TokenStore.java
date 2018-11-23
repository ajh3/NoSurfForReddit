package com.aaronhalbert.nosurfforreddit.repository;

public interface TokenStore {
    String getUserOAuthAccessToken();
    String getUserOAuthRefreshToken();

    void setUserOAuthAccessToken(String userOAuthAccessToken);
    void setUserOAuthRefreshToken(String userOAuthRefreshToken);

    void clearUserOAuthAccessToken();
    void clearUserOAuthRefreshToken();
}
