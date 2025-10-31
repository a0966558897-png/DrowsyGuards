package com.example.drivesafe.db;

public class LoginReq {
    // 後端接受哪一個就填哪一個；不用的可以留空
    public String email;     // e.g. "user@example.com"
    public String username;  // 若後端用 username 登入
    public String password;

    public LoginReq() {}  // Gson 需要無參數建構子
}
