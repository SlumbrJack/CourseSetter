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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Database Code
        userID = FirebaseAuth.getInstance().currentUser!!.uid
        RunDatabaseQuery()
        
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

                R.id.navigation_appaccsettings -> {
                    // Handle navigation to the notifications destination
                    navController.navigate(R.id.navigation_appaccsettings)

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
            navigation_home, navigation_dashboard, R.id.navigation_notifications

        )

        appBarConfiguration = AppBarConfiguration(topLevelDestinations, binding.drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }

    //This Function retrieves the data from the database and stores it in lists to be accessed from fragments
    public fun RunDatabaseQuery(){
        val formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
        var totalRuns = 0
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
                        }
                }
            }
    }
}
//hi