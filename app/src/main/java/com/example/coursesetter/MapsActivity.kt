package com.example.coursesetter

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import kotlin.math.round




class MapsActivity : AppCompatActivity(), OnMapReadyCallback, SensorEventListener {

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private lateinit var currentLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101
    private val droppedPins: MutableList<LatLng> = mutableListOf()
    private val polylines: MutableList<Polyline> = mutableListOf()
    private var totalDistanceMeters: Float = 0f
    private lateinit var previousLocation: Location
    private var lastMarkerDistanceMiles: Float = 0.0f
    private lateinit var chronometer: Chronometer



    //Step Vars
    var sensorManager: SensorManager? = null
    var running = false
    var totalSteps = 0f
    var previousTotalSteps = 0f
    public var userDistance: Float = 0.0f
    protected var steps: Int = 0
    public var currentDistance: Float = 0.0f
    val startingSteps: Int = 0
    var hasInitialSteps = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        previousLocation = Location("")
        lastMarkerDistanceMiles = 0.0f
        chronometer = findViewById(R.id.chronometer)


        findViewById<TextView>(R.id.textViewDistance).text = "Distance Traveled: $currentDistance"
        findViewById<TextView>(R.id.textViewTargetDistance).text = "Distance Goal: $userDistance"

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            this.googleMap = googleMap
            getCurrentLocationUser()
            googleMap.setOnMapClickListener { latLng ->
                googleMap.addMarker(MarkerOptions().position(latLng).title("Marker"))
            }
        }

        //Gets the step counter
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        hasInitialSteps = false

        getCurrentLocationUser()

        mapFragment.getMapAsync { googleMap ->
            googleMap.apply {
                this.isMyLocationEnabled

                setOnMapClickListener { latLng ->
                    addMarker(MarkerOptions().position(latLng).title("Marker"))
                }
            }
        }
        //Finish run button. sends data in a bundle to Main
        val endButton = findViewById<Button>(R.id.buttonFinishEarly)
        endButton.setOnClickListener {
            running = false
            val intent = Intent(this, MainActivity::class.java)
            currentDistance = (round((steps / 22.22f))) / 100
            intent.putExtra("distance", currentDistance)
            startActivity(intent)
            finish()
        }

        requestLocationUpdates()
        chronometer.start()
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
                    // Calculate distance between current and previous location
                    if (previousLocation.latitude != 0.0 && previousLocation.longitude != 0.0) {
                        val distance = location.distanceTo(previousLocation)
                        totalDistanceMeters += distance
                        // Convert distance to miles
                        val totalDistanceMiles = totalDistanceMeters * 0.000621371f

                        // Check if the user has traveled 0.05 miles since the last marker
                        if (totalDistanceMiles - lastMarkerDistanceMiles >= 0.02f) {
                            // Set marker at current location
                            val latLng = LatLng(location.latitude, location.longitude)
                            googleMap.addMarker(MarkerOptions().position(latLng).title("Marker at ${"%.2f".format(totalDistanceMiles)} miles"))
                            // Update last marker distance
                            lastMarkerDistanceMiles = totalDistanceMiles
                        }
                    }
                    // Update previous location
                    previousLocation = location

                    // Update UI to display distance in miles
                    val currentDistanceMiles = totalDistanceMeters * 0.000621371f
                    findViewById<TextView>(R.id.textViewDistance).text = "Distance Traveled: ${"%.2f".format(currentDistanceMiles)} miles"
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
        val context = GeoApiContext.Builder().apiKey("AIzaSyAi-frfUzEuBn22NStQ-DWlj9kAxFZLu-U").build()

        DirectionsApi.getDirections(context, "${origin.latitude},${origin.longitude}", "${destination.latitude},${destination.longitude}")
            .mode(TravelMode.DRIVING)
            .setCallback(object : PendingResult.Callback<DirectionsResult> {
                override fun onResult(result: DirectionsResult) {

                    val route = result.routes[0]
                    val routePoints = route.overviewPolyline.decodePath()

                    // Adjust polyline color and width
                    val polylineOptions = PolylineOptions().addAll(routePoints.map { LatLng(it.lat, it.lng) })
                        .color(Color.BLACK) // Set polyline color to red
                        .width(100f) // Set polyline width to 10 pixels

                    val polyline = googleMap.addPolyline(polylineOptions)
                    polylines.add(polyline)

                    // Adjust camera to fit the bounds of the route
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
        //Toast.makeText(this, "User Entered $userDistance", Toast.LENGTH_SHORT).show()
        findViewById<TextView>(R.id.textViewTargetDistance).text = "Distance Goal: $userDistance"
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (running) {
            totalSteps = event!!.values[0]
            if (!hasInitialSteps) {
                hasInitialSteps = true
                previousTotalSteps = totalSteps
            }

            val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
            steps = currentSteps

            val distanceMiles = (steps / 22.22f) * 0.000621371f
            val formattedDistance = String.format("%.2f", distanceMiles)

            findViewById<TextView>(R.id.textViewDistance).text = "Distance Traveled: $formattedDistance miles"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.w("Main: Sensor", "Accuracy Changed")
    }
    override fun onResume() {
        super.onResume()

            running = true
            val stepSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            if(stepSensor == null)
            {
                Toast.makeText(this, "No sensor detected", Toast.LENGTH_LONG).show()
            }else{
                sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
                Log.w("Main: Sensor", "Resuming")
            }
    }


}