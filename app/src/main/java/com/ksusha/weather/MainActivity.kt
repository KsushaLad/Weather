package com.ksusha.weather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ksusha.weather.databinding.ActivityMainBinding
import com.ksusha.weather.fragments.MainFragment

//val url = "https://api.weatherapi.com/v1/current.json" +
//        "?key=$API_KEY&q=London&aqi=no"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction().replace(R.id.placeHolder, MainFragment.newInstance()).commit()
    }

}