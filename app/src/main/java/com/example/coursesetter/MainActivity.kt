package com.example.coursesetter


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.coursesetter.R.id.mapsActivity
import com.example.coursesetter.R.id.navigation_dashboard
import com.example.coursesetter.R.id.navigation_home
import com.example.coursesetter.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    var DBRunDistances = kotlin.collections.mutableListOf<Float>()
    var DBRunDates = kotlin.collections.mutableListOf<LocalDate>()
    private lateinit var userID: String
    var totalRuns: Int = 4
    val date: LocalDate = LocalDate.now()
    var distanceIntent = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Gets the package intent if coming from MapsActivity, sends the data to DB
        userID = FirebaseAuth.getInstance().currentUser!!.uid
        val bundle2 : Bundle? = intent.extras
        Log.w("Main: Intent", "Intent: $bundle2")
        if(bundle2 != null){
            distanceIntent = bundle2.getFloat("distance")
            Log.w("Main: Intent", "Intent: $distanceIntent")
            //AddMapDistance(distanceIntent)
        }
        RunDatabaseQuery()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)


        // Setup for (Side Nav)
        val drawerNavView: NavigationView = binding.sideNavigation
        drawerNavView.setupWithNavController(navController)

        // Handling navigation drawer item clicks
        drawerNavView.setNavigationItemSelectedListener { menuItem ->

            // Handle drawer item clicks here
            when (menuItem.itemId) {
                R.id.navigation_Home -> {
                    // Handle navigation to the home destination
                    navController.navigate(navigation_home)

                }

                navigation_dashboard ->  {
                    // Handle navigation to the dashboard destination
                    navController.navigate(navigation_dashboard)
                }

                R.id.mapsActivity-> {
                    // Handle navigation to the maps destination
                    navController.navigate(R.id.mapsActivity)

                }




                else -> {
                    // Handle everything else
                }
            }
            menuItem.isChecked = true
            binding.drawerLayout.closeDrawers()
            true
        }



        // Setup for BottomNavigationView


        // Set of top-level destinations in the bottom nav
        val topLevelDestinations = setOf(
            navigation_home, navigation_dashboard, mapsActivity

        )

        appBarConfiguration = AppBarConfiguration(topLevelDestinations, binding.drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }

    //This Function retrieves the data from the database and stores it in lists to be accessed from fragments, calls FoundMatch to add to DB
    public fun RunDatabaseQuery(){
        val formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
        totalRuns = 0
        DBRunDistances.clear()
        DBRunDates.clear()

        Firebase.database.getReference("Users Runs").child("Users").child(userID)
            .child("Total Runs").get().addOnSuccessListener {

                Log.e("AllStats", "total runs is ${it.value}")
                totalRuns = it.value.toString().toInt()

            }.addOnFailureListener {
                Log.e("firebase", "Error getting data", it)
            }.addOnCompleteListener {
                //COULD FIX REDUNDANCIES HERE
                val dblocation : DatabaseReference = Firebase.database.getReference("Users Runs").child("Users").child(userID)
                for (i in 1..totalRuns){
                    dblocation.child("$i").child("Distance").get()
                        .addOnSuccessListener {
                            DBRunDistances.add(it.value.toString().toFloat())
                        }
                }
                for (i in 1..totalRuns){
                    dblocation.child("$i").child("Date").get()
                        .addOnSuccessListener {
                            DBRunDates.add(LocalDate.parse(it.value.toString(), formatter))
                        }.addOnCompleteListener {
                            if(i==totalRuns && distanceIntent != 0f){
                                FoundMatch(distanceIntent)
                            }
                        }
                }
            }
    }

    fun FoundMatch(distance: Float){
        var dbDate: LocalDate
        val dbLocation = Firebase.database.getReference("Users Runs").child("Users").child(userID)
        var foundMatch = false


        for (i in 0..totalRuns - 1) {
            dbDate = DBRunDates[i]
            if (dbDate.isEqual(date)){
                Log.e("Home", "$dbDate = $date, ${DBRunDistances[i]}")
                foundMatch = true
                DBRunDistances[i] = DBRunDistances[i] + distance
                dbLocation.child("${i+1}").child("Distance").setValue(DBRunDistances[i])
            }
            if(i == totalRuns-1)
            {
                if(!foundMatch){
                    totalRuns++

                    dbLocation.child("Total Runs").setValue(totalRuns)
                    val userRunLocation = dbLocation.child("$totalRuns")

                    userRunLocation.child("Distance").setValue("$distanceIntent")
                    userRunLocation.child("Steps").setValue("3000")
                    userRunLocation.child("Calories Burned").setValue("200")
                    userRunLocation.child("Time").setValue("20:10")
                    Log.e("Home: Error", "Adding date here FALSE")

                    //new date stuff
                    val formatted: String =
                        date.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"))
                    userRunLocation.child("Date").setValue(formatted)
                    userRunLocation.child("Day").setValue(date.dayOfWeek)

                    //Add locally
                    DBRunDistances.add(distanceIntent)
                    DBRunDates.add(date)
                }
            }
        }

        if (totalRuns == 0) {
            totalRuns++
            dbLocation.child("Total Runs").setValue(totalRuns)
            val userRunLocation = dbLocation.child("$totalRuns")

            userRunLocation.child("Distance").setValue("$distanceIntent")
            userRunLocation.child("Steps").setValue("3000")
            userRunLocation.child("Calories Burned").setValue("200")
            userRunLocation.child("Time").setValue("20:10")

            //new date stuff

            val formatted: String =
                date.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"))
            userRunLocation.child("Date").setValue(formatted)
            userRunLocation.child("Day").setValue(date.dayOfWeek)

            DBRunDistances.add(distance)
            DBRunDates.add(date)
            Log.e("ERROR", "Adding DATE HERE")
        }
        Log.e("ERROR", "VALUE $foundMatch")


    }
}
//hi