package com.example.drivesafe.db;

import com.google.gson.annotations.SerializedName;

public class LoginResp {
    // 支援後端各種常見鍵名：access_token / token / jwt / accessToken / access-token
    @SerializedName(value = "access_token", alternate = {"token", "jwt", "accessToken", "access-token"})
    public String access_token;

    @SerializedName(value = "refresh_token", alternate = {"refreshToken", "refresh-token"})
    public String refresh_token;

    @SerializedName(value = "user_id", alternate = {"member_id", "id", "uid"})
    public Integer user_id;

    @SerializedName(value = "email", alternate = {"username", "account"})
    public String account;

    @SerializedName(value = "success", alternate = {"ok"})
    public Boolean success;

    // 方便調用的小工具
    public String getTokenOrNull() { return access_token; }
    public Integer getUserIdOrNull() { return user_id; }
}
