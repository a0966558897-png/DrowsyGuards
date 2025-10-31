package com.example.drivesafe.db;

import com.google.gson.annotations.SerializedName;

public class MemberDto {
    @SerializedName(value = "id", alternate = {"member_id"})
    public Integer id;

    @SerializedName(value = "email", alternate = {"mail"})
    public String email;

    @SerializedName(value = "name", alternate = {"username", "account"})
    public String name;

    public int getIdOr0() { return id != null ? id : 0; }
}
