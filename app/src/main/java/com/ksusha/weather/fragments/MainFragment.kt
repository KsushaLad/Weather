package com.ksusha.weather.fragments

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.tabs.TabLayoutMediator
import com.ksusha.weather.adapters.ViewPagerAdapter
import com.ksusha.weather.databinding.FragmentMainBinding
import com.ksusha.weather.extencions.isPermissionGranted
import com.ksusha.weather.model.WeatherModel
import com.ksusha.weather.utils.API_KEY
import org.json.JSONObject

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private val fragmentList = listOf(HoursFragment.newInstance(), DaysFragment.newInstance())
    private val titleList = listOf("HOURS", "DAYS")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        init()
        requestWeatherData("Barcelona")
    }

    private fun init() = with(binding){
        val adapter = ViewPagerAdapter(activity as FragmentActivity, fragmentList)
        vp.adapter = adapter
        TabLayoutMediator(tabLayout, vp){
            tab, position -> tab.text = titleList[position]
        }.attach()
    }

    private fun permissionListener(){
        pLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            Toast.makeText(activity, "$it", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermission(){
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)){
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun requestWeatherData(city: String){
        val url = "https://api.weatherapi.com/v1/forecast.json?key=" +
                API_KEY +
                "&q=" +
                city +
                "&days=" +
                "3" +
                "&aqi=no&alerts=no"
        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(Request.Method.GET, url, { result -> parseWeatherData(result) }, { error -> })
        queue.add(request)
    }

    private fun parseWeatherData(result: String){
        val mainObject = JSONObject(result)
        val item = WeatherModel(
            mainObject.getJSONObject("location").getString("name"),                                   //city
            mainObject.getJSONObject("current").getString("last_updated"),                            //time
            mainObject.getJSONObject("current").getJSONObject("condition").getString("text"),   //condition
            mainObject.getJSONObject("current").getString("temp_c"),                                  //currentTemp
            "",                                                                                          //maxTemp
            "",                                                                                          //minTemp
            mainObject.getJSONObject("current").getJSONObject("condition").getString("icon"),   //imageUrl
            ""                                                                                              //hours
        )
        Log.d("Tag", "${item.city}, ${item.condition}, ${item.currentTemp}")
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}