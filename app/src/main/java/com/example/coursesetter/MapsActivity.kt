package com.example.coursesetter

import android.Manifest
import android.content.BroadcastReceiver
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import org.chromium.base.Callback
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import okhttp3.Route


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private lateinit var currentLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101
    private val droppedPins: MutableList<LatLng> = mutableListOf()
    private val polylines: MutableList<Polyline> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            this.googleMap = googleMap
            getCurrentLocationUser()
            googleMap.setOnMapClickListener { latLng ->
                googleMap.addMarker(MarkerOptions().position(latLng).title("Marker"))
            }
        }



        getCurrentLocationUser()

        mapFragment.getMapAsync { googleMap ->
            googleMap.apply {
                this.isMyLocationEnabled

                setOnMapClickListener { latLng ->
                    addMarker(MarkerOptions().position(latLng).title("Marker"))
                }
            }
        }
    }

    private fun getCurrentLocationUser() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                permissionCode
            )
        } else {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location

                    val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
                    val markerOptions = MarkerOptions().position(latLng).title("Current Location")

                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    googleMap.addMarker(markerOptions)

                    // Request location updates
                    requestLocationUpdates()
                } else {
                    requestLocationUpdates()
                }
            }
        }
    }

    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000 // 10 seconds (adjust as needed)
            fastestInterval = 5000 // 5 seconds (adjust as needed)
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    currentLocation = location
                    val latLng = LatLng(location.latitude, location.longitude)
                    val markerOptions = MarkerOptions().position(latLng).title("Current Location")
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    googleMap.addMarker(markerOptions)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocationUser()
                } else {

                    Toast.makeText(
                        applicationContext,
                        "Location permission denied. Some functionality may be limited.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    override fun onMapReady(gMap: GoogleMap) {

        googleMap = gMap

        googleMap.setOnMapClickListener { latLng ->
            droppedPins.add(latLng)
            googleMap.addMarker(MarkerOptions().position(latLng))

            if (droppedPins.size >= 2) {
                drawRoute()
            }
        }
    }

    private fun drawRoute() {
        for (polyline in polylines) {
            polyline.remove()
        }
        polylines.clear()

        val origin = droppedPins.first()
        val destination = droppedPins.last()
        val context =
            GeoApiContext.Builder().apiKey("AIzaSyAi-frfUzEuBn22NStQ-DWlj9kAxFZLu-U").build()

        DirectionsApi.getDirections(
            context,
            "${origin.latitude},${origin.longitude}",
            "${destination.latitude},${destination.longitude}"
        )
            .mode(TravelMode.DRIVING)
            .setCallback(object : PendingResult.Callback<DirectionsResult> {
                override fun onResult(result: DirectionsResult) {
                    if (result.routes.isNotEmpty()) {
                        for ((index, route) in result.routes.withIndex()) {
                            Log.d("DirectionsAPI", "Route ${index + 1}: ${route.summary}")
                        }
                        val route = result.routes[0]
                        val routePoints = route.overviewPolyline.decodePath()
                        val polylineOptions =
                            PolylineOptions().addAll(routePoints.map { LatLng(it.lat, it.lng) })
                        val polyline = googleMap.addPolyline(polylineOptions)
                        polylines.add(polyline)
                    } else {
                        Log.e("DirectionsAPI", "No routes found")
                    }
                }

                override fun onFailure(e: Throwable) {
                    Log.e("DirectionsAPI", "Failed to fetch directions: ${e.message}")
                }
            })
    }
}