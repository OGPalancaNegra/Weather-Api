package com.example.weatherapi

import android.content.Context
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapi.databinding.ActivityMainBinding
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private val viewModel: MainActivityViewModel by lazy { ViewModelProvider(this).get(MainActivityViewModel::class.java) }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        val activityMainBinding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        activityMainBinding.lifecycleOwner = this

        activityMainBinding.mainActVM = viewModel

        viewModel.openLocationServices(this)

        viewModel.weatherListIcon.observe(this, Observer { weatherListIcon ->

            when (weatherListIcon) {
                "01d" -> activityMainBinding.ivMain.setImageResource(R.drawable.sunny)
                "02d" -> activityMainBinding.ivMain.setImageResource(R.drawable.cloud)
                "03d" -> activityMainBinding.ivMain.setImageResource(R.drawable.cloud)
                "04d" -> activityMainBinding.ivMain.setImageResource(R.drawable.cloud)
                "04n" -> activityMainBinding.ivMain.setImageResource(R.drawable.cloud)
                "10d" -> activityMainBinding.ivMain.setImageResource(R.drawable.rain)
                "11d" -> activityMainBinding.ivMain.setImageResource(R.drawable.storm)
                "13d" -> activityMainBinding.ivMain.setImageResource(R.drawable.snowflake)
                "01n" -> activityMainBinding.ivMain.setImageResource(R.drawable.cloud)
                "02n" -> activityMainBinding.ivMain.setImageResource(R.drawable.cloud)
                "03n" -> activityMainBinding.ivMain.setImageResource(R.drawable.cloud)
                "10n" -> activityMainBinding.ivMain.setImageResource(R.drawable.cloud)
                "11n" -> activityMainBinding.ivMain.setImageResource(R.drawable.rain)
                "13n" -> activityMainBinding.ivMain.setImageResource(R.drawable.snowflake)
            }
        })

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                viewModel.getLocationWeatherDetails()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}