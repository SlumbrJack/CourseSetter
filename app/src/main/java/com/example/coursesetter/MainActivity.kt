package com.example.coursesetter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.coursesetter.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

// This is my comment (nick was here)//
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup for BottomNavigationView
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Set of top-level destinations in the bottom nav
        val topLevelDestinations = setOf(
            R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications,

        )

        appBarConfiguration = AppBarConfiguration(topLevelDestinations, binding.drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Setup for (Side Navigation)
        val drawerNavView: NavigationView = binding.sideNavigation
        drawerNavView.setupWithNavController(navController)

        // Handling navigation drawer item clicks
        drawerNavView.setNavigationItemSelectedListener { menuItem ->
            // Handle drawer item clicks here
            when (menuItem.itemId) {
                // Add cases for different menu items, e.g., R.id.nav_home
            }
            menuItem.isChecked = true
            binding.drawerLayout.closeDrawers()
            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }
}