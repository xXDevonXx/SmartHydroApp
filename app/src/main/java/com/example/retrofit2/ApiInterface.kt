package com.example.retrofit2

import io.reactivex.Observable
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiInterface {

    @GET("/M") //get command, with end value of URL
    fun getSensorData(): Observable<Response<List<SensorData>>>

    @POST("/T") //get command, with end value of URL
    fun switchLED(): Observable<Void>
}