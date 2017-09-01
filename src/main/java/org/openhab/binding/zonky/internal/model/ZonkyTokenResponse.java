package org.openhab.binding.zonky.internal.model;

import com.google.gson.annotations.SerializedName;

public class ZonkyTokenResponse {
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("refresh_token")
    private String refreshToken;

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
