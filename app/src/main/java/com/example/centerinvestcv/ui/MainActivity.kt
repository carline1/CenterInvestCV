package com.example.centerinvestcv.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.commit
import androidx.navigation.fragment.NavHostFragment
import com.example.centerinvestcv.R
import com.example.centerinvestcv.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        this.binding = binding
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController =navHostFragment.navController
    }
}