package com.example.weatherapi.repo

import com.example.weatherapi.Constants
import com.example.weatherapi.model.WeatherResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query



/* working:
    "https://api.github.com/"
    "https://mars.udacity.com/"
 */



/**
 * Build the Moshi object that Retrofit will be using, making sure to add the Kotlin adapter for
 * full Kotlin compatibility.
 */
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

/**
 * Use the Retrofit builder to build a retrofit object using a Moshi converter with our Moshi
 * object.
 */
private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(Constants.BASE_URL)
    .build()

/**
 * A public interface that exposes the [getProperties] method
 */
interface WeatherService {
    @GET("2.5/weather")
    fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String?,
        @Query("appid") appid: String?
    ): Call<WeatherResponse>
}

// general knowledge string: api.php?amount=10&category=21&difficulty=easy&type=multiple

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object WeatherServiceObject {
    val RETROFIT_SERVICE : WeatherService by lazy  { retrofit.create(WeatherService::class.java) }
}