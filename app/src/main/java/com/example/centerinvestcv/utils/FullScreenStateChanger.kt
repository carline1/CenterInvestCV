package com.example.centerinvestcv.utils

import android.app.Activity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import com.example.centerinvestcv.R
import com.google.android.material.bottomnavigation.BottomNavigationView

object FullScreenStateChanger {

    fun fullScreen(activity: Activity, state: Boolean) {
        val navHostFragment =
            (activity as AppCompatActivity).findViewById<FragmentContainerView>(R.id.nav_host_fragment)
        val visibility = when (state) {
            true -> {
                View.GONE
            }
            false -> {
                View.VISIBLE
            }
        }
        navHostFragment.requestLayout()
        val bottomNavView =
            (activity).findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNavView.visibility = visibility
    }

}