package com.example.coursesetter

import android.os.Bundle
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


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                    navController.navigate(R.id.navigation_home)

                }

                R.id.navigation_dashboard ->  {
                    // Handle navigation to the dashboard destination
                    navController.navigate(R.id.navigation_dashboard)
                }

                R.id.navigation_settings -> {
                    // Handle navigation to the notifications destination
                    navController.navigate(R.id.navigation_settings)

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
}