package com.example.weatherapi.model

import java.io.Serializable

data class Main(
    val temp: Int,
    val pressure: Int,
    val humidity: Int,
    val tempMin: Int,
    val tempMax: Int
) : Serializable