package com.example.weatherapi

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherapi.model.WeatherResponse
import com.example.weatherapi.repo.WeatherServiceObject
import com.google.android.gms.location.*
import com.google.android.gms.location.R
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.N)
class MainActivityViewModel(application: Application): AndroidViewModel(application) {


    val context = getApplication<Application>().applicationContext

    // A global variable for Progress Dialog
    private var mProgressDialog: Dialog? = null

    // A fused location client variable which is further user to get the user's current location
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    // A global variable for Current Latitude
    private var mLatitude: Double = 0.0
    // A global variable for Current Longitude
    private var mLongitude: Double = 0.0

    private val _weatherResponse = MutableLiveData<WeatherResponse>()

    val weatherResponse : LiveData<WeatherResponse> get() = _weatherResponse

    private lateinit var mSharedPreferences: SharedPreferences

    private val _tvMain = MutableLiveData<String>()
    val tvMain: LiveData<String> get() = _tvMain

    private val _tvDescription = MutableLiveData<String>()
    val tvDescription: LiveData<String> get() = _tvDescription

    private val _tvTemp = MutableLiveData<String>()
    val tvTemp: LiveData<String> get() = _tvTemp

    private val _tvHumidity = MutableLiveData<String>()
    val tv_humidity: LiveData<String> get() = _tvHumidity

    private val _tvMin = MutableLiveData<String>()
    val tvMin: LiveData<String> get() = _tvMin

    private val _tvMax = MutableLiveData<String>()
    val tvMax: LiveData<String> get() = _tvMax

    private val _tvSpeed = MutableLiveData<String>()
    val tvSpeed: LiveData<String> get() = _tvSpeed

    private val _tvName = MutableLiveData<String>()
    val tvName: LiveData<String> get() = _tvName

    private val _tvCountry = MutableLiveData<String>()
    val tvCountry: LiveData<String> get() = _tvCountry

    private val _tvSunRiseTime = MutableLiveData<String>()
    val tvSunRiseTime: LiveData<String> get() = _tvSunRiseTime

    private val _tvSunSetTime = MutableLiveData<String>()
    val tvSunSetTime: LiveData<String> get() = _tvSunSetTime


    private val _weatherListIcon = MutableLiveData<String>()

    val weatherListIcon: LiveData<String> get() = _weatherListIcon




    private val mLocationCallback = object : LocationCallback() {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun onLocationResult(locationResult: LocationResult) {

            val mLastLocation: Location = locationResult.lastLocation
            mLatitude = mLastLocation.latitude
            Log.e("Current Latitude", "$mLatitude")
            mLongitude = mLastLocation.longitude
            Log.e("Current Longitude", "$mLongitude")

            getLocationWeatherDetails()
        }
    }

    init {


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        mSharedPreferences = context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE)

        setupUI()

        //isLocationEnabled()

       // openLocationServices()
    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun getLocationWeatherDetails() {

        if (Constants.isNetworkAvailable(context)) {
            Toast.makeText(context, "You are connected to the internet.", Toast.LENGTH_SHORT).show()



            GlobalScope.launch(Dispatchers.IO) {
                val response = WeatherServiceObject.RETROFIT_SERVICE.getWeather(
                    mLatitude,
                    mLongitude,
                    Constants.METRIC_UNIT,
                    Constants.APP_ID
                ).awaitResponse()



                if (response.isSuccessful) {
                    val weatherList = response.body()



                    withContext(Dispatchers.Main) {

                        _weatherResponse.value = weatherList

                        val weatherResponseJsonString = Gson().toJson(weatherList)
                        // Save the converted string to shared preferences
                        val editor = mSharedPreferences.edit()
                        editor.putString(Constants.WEATHER_RESPONSE_DATA, weatherResponseJsonString)
                        editor.apply()

                        setupUI()

                    }
                }
            }


        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setupUI() {
        // TODO (STEP 6: Here we get the stored response from
        //  SharedPreferences and again convert back to data object
        //  to populate the data in the UI.)
        // START
        // Here we have got the latest stored response from the SharedPreference and converted back to the data model object.
        val weatherResponseJsonString =
            mSharedPreferences.getString(Constants.WEATHER_RESPONSE_DATA, "")

        if (!weatherResponseJsonString.isNullOrEmpty()) {

            val weatherList =
                Gson().fromJson(weatherResponseJsonString, WeatherResponse::class.java)

            // For loop to get the required data. And all are populated in the UI.
            for (z in weatherList.weather.indices) {
                Log.i("NAMEEEEEEEE", weatherList.weather[z].main)

                _tvMain.value = weatherList.weather[z].main

                _tvDescription.value = weatherList.weather[z].description
                _tvTemp.value =
                    weatherList.main.temp.toString() + getUnit(context.resources.configuration.locales.toString())
                _tvHumidity.value = weatherList.main.humidity.toString() + " per cent"
                _tvMin.value = weatherList.main.tempMin.toString() + " min"
                _tvMax.value= weatherList.main.tempMax.toString() + " max"
                _tvSpeed.value = weatherList.wind.speed.toString()
                _tvName.value = weatherList.name
                _tvCountry.value = weatherList.sys.country
                _tvSunRiseTime.value = unixTime(weatherList.sys.sunrise.toLong())
                _tvSunSetTime.value = unixTime(weatherList.sys.sunset.toLong())

                // Here we update the main icon
                _weatherListIcon.value = weatherList.weather[z].icon
            }
        }
        // END
    }

    private fun getUnit(value: String): String? {
        Log.i("unitttttt", value)
        var value = "°C"
        if ("US" == value || "LR" == value || "MM" == value) {
            value = "°F"
        }
        return value
    }

    private fun unixTime(timex: Long): String? {
        val date = Date(timex * 1000L)
        @SuppressLint("SimpleDateFormat") val sdf =
            SimpleDateFormat("HH:mm:ss")
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }


    fun isLocationEnabled(): Boolean {

        // This provides access to the system location services.
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    fun openLocationServices(activity: MainActivity){
        if (!isLocationEnabled()) {
            Toast.makeText(
                context,
                "Location provider turned off, please turn it on",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }else{
            Dexter.withActivity(activity)
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            requestLocationData()
                        }

                        if (report.isAnyPermissionPermanentlyDenied) {
                            Toast.makeText(
                                context,
                                "You have denied location permission. Please allow it is mandatory.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }
                }).onSameThread()
                .check()

        }
    }


    @SuppressLint("MissingPermission")
    private fun requestLocationData() {

        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(context)
            .setMessage("It Looks like you have turned off permissions required for this feature. It can be enabled under Application Settings")
            .setPositiveButton(
                "GO TO SETTINGS"
            ) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", context.packageName, null)
                    intent.data = uri
                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog,
                                           _ ->
                dialog.dismiss()
            }.show()
    }


}