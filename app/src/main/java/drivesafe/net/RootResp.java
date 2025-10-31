package com.example.drivesafe.db;

import com.google.gson.annotations.SerializedName;

public class RootResp {
    @SerializedName(value = "message", alternate = {"msg"})
    public String message;

    @SerializedName(value = "status", alternate = {"ok"})
    public String status;   // 可能是 "ok" / "healthy" / "UP" 之類

    @SerializedName(value = "version", alternate = {"ver"})
    public String version;

    @SerializedName(value = "time", alternate = {"timestamp", "ts"})
    public Long time;

    @SerializedName("success")
    public Boolean success;
}
