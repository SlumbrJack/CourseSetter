package com.example.coursesetter

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.coursesetter.fragments.UserEnterDistance
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private lateinit var currentLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101
    private val droppedPins: MutableList<LatLng> = mutableListOf()
    private val polylines: MutableList<Polyline> = mutableListOf()
    public var userDistance: Float = 0.0f
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
                } else {
                    requestLocationUpdates()
                }
            }
        }
    }

    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
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

                    fusedLocationProviderClient.removeLocationUpdates(this)
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

        googleMap.setOnMapClickListener { latLng ->
            droppedPins.add(latLng)
            googleMap.addMarker(MarkerOptions().position(latLng))

            if (droppedPins.size >= 2) {
                drawRoute(googleMap)
            }

            val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
            val markerOptions = MarkerOptions().position(latLng).title("Current Location")

            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 7f))
            googleMap.addMarker(markerOptions)
        }
    }

    private fun drawRoute(googleMap: GoogleMap) {

        for (polyline in polylines) {
            polyline.remove()
        }
        polylines.clear()

        val boundsBuilder = LatLngBounds.Builder()
        for (pin in droppedPins) {
            boundsBuilder.include(pin)
        }
        val bounds = boundsBuilder.build()
        val origin = droppedPins.first()
        val destination = droppedPins.last()
        val context = GeoApiContext.Builder().apiKey("YOUR_API_KEY").build()

        DirectionsApi.getDirections(context, "${origin.latitude},${origin.longitude}", "${destination.latitude},${destination.longitude}")
            .mode(TravelMode.DRIVING)
            .setCallback(object : PendingResult.Callback<DirectionsResult> {
                override fun onResult(result: DirectionsResult) {

                    val route = result.routes[0]
                    val routePoints = route.overviewPolyline.decodePath()
                    val polylineOptions = PolylineOptions().addAll(routePoints.map { LatLng(it.lat, it.lng) })
                    val polyline = googleMap.addPolyline(polylineOptions)
                    polylines.add(polyline)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                }
                override fun onFailure(e: Throwable) {
                    Log.e("DirectionsAPI", "Failed to fetch directions: ${e.message}")
                }
            })
    }
    //This function can be repurposed to actually find the destination, but for now just shows a toast so I know it was called from frag -J
    public fun findDestination()
    {
        Toast.makeText(this, "User Entered $userDistance", Toast.LENGTH_LONG).show()
    }
}