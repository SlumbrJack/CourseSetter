package com.example.coursesetter.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.viewpager2.widget.ViewPager2
import com.example.coursesetter.R
import com.example.coursesetter.StatsViewPagerAdapter
import com.example.coursesetter.databinding.FragmentDashboardBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
var hasReloaded = false
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    val tabTitles = arrayOf("Week", "Month", "All Time")
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root


        //View code for stats page
        val statsTabLayout : TabLayout = root.findViewById(R.id.tab_layout)
        val statsViewPager2 : ViewPager2 = root.findViewById(R.id.view_pager)
        val statsViewPagerAdapter : StatsViewPagerAdapter = StatsViewPagerAdapter(this)
        // val onTabSelectedListener : TabLayout.OnTabSelectedListener =  TabLayout.OnTabSelectedListener
        statsViewPager2.adapter = statsViewPagerAdapter
        statsTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                statsViewPager2.currentItem = tab.position

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Handle tab unselected event
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Handle tab reselected event
                statsViewPager2.currentItem = tab.position
            }
        })

        TabLayoutMediator(statsTabLayout, statsViewPager2) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
        return root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

/* John: I wrote this to try and fix my graphs but it didnt work, hanging on to it for now :(
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController  = this.view?.let { Navigation.findNavController(it) }
        if(!hasReloaded){
            hasReloaded = true
            Log.e("BUG", "RELOADED")
            // Handle navigation to the dashboard destination
            navController!!.navigate(R.id.navigation_dashboard)

        }
    }
    */

}