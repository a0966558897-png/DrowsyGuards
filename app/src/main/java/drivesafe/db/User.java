package com.example.drivesafe.db;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "createdAt")
    private long createdAt;  // millis

    // --- 建構子 ---
    public User() {
        this.createdAt = System.currentTimeMillis();
        this.email = "";
        this.name = "";
    }

    public User(String email, String name) {
        this.email = email;
        this.name = name;
        this.createdAt = System.currentTimeMillis();
    }

    // --- Getter / Setter ---
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getEmail() { return email != null ? email : ""; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name != null ? name : ""; }
    public void setName(String name) { this.name = name; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
