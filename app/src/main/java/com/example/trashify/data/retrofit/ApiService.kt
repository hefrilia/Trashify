package com.example.trashify.data.retrofit

import com.example.trashify.data.response.CraftingResponse
import com.example.trashify.data.response.LoginResponse
import com.example.trashify.data.response.RegisterResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @FormUrlEncoded
    @POST("auth/login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String,
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("auth/register")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
    ): Call<RegisterResponse>

    @GET("tutorial/glass")
    suspend fun getTutorial(
        @Path("material") material: String
    ): CraftingResponse

//    @GET("tutorial/glass")
//    suspend fun getTutorial(
//        @Path("material") material: String
//    ): Tutorial
}