package com.example.coursesetter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.coursesetter.fragments.AllTimeStatsFragment
import com.example.coursesetter.fragments.MonthStatsFragment
import com.example.coursesetter.fragments.WeekStatsFragment

class StatsViewPagerAdapter(fragment: Fragment) :
    FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        //T//ODO("Not yet implemented")
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        //val weekStatsFragment = WeekStatsFragment()
         when (position) {
             0 -> return WeekStatsFragment()
             1 -> return MonthStatsFragment()
             2 -> return AllTimeStatsFragment()
             else -> {return WeekStatsFragment()}
         }

    }
}