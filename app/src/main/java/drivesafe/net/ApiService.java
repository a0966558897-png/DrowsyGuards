package com.example.drivesafe.db;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @POST("members/login")
    Call<LoginResp> loginMembers(@Body LoginReq req);

    @POST("login")
    Call<LoginResp> loginJson(@Body LoginReq req);

    @FormUrlEncoded
    @POST("token")
    Call<LoginResp> loginForm(
            @Field("username") String username,
            @Field("password") String password
    );

    @GET("driving_records")
    Call<List<FatigueDto>> getRecords(
            @Header("Authorization") String bearerToken,
            @Query("user_id") String userId,
            @Query("start_ms") Long startMs,
            @Query("end_ms") Long endMs
    );

    @POST("driving_records")
    Call<FatigueDto> postRecord(
            @Header("Authorization") String bearerToken,
            @Body FatigueDto body
    );

    @POST("driving_records")
    Call<List<FatigueDto>> postRecords(
            @Header("Authorization") String bearerToken,
            @Body List<FatigueDto> payload
    );

    @GET("driving_records")
    Call<List<DrivingRecordDto>> getDrivingRecords(@Query("member_id") int memberId);

    @POST("driving_records")
    Call<DrivingRecordDto> postDrivingRecord(@Body DrivingRecordDto dto);

    @GET("members")
    Call<List<MemberDto>> getMembers();

    @GET("/")
    Call<RootResp> root();
}
