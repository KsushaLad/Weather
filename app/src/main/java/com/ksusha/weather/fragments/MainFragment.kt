package com.ksusha.weather.fragments

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.tabs.TabLayoutMediator
import com.ksusha.weather.adapters.ViewPagerAdapter
import com.ksusha.weather.databinding.FragmentMainBinding
import com.ksusha.weather.extencions.isPermissionGranted
import com.ksusha.weather.model.WeatherModel
import com.ksusha.weather.utils.API_KEY
import com.ksusha.weather.viewmodel.MainViewModel
import com.squareup.picasso.Picasso
import org.json.JSONObject

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private val fragmentList = listOf(HoursFragment.newInstance(), DaysFragment.newInstance())
    private val titleList = listOf("HOURS", "DAYS")
    private val mainViewModel: MainViewModel by activityViewModels()

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
        updateCurrentCard()
        requestWeatherData("Kiev")
    }

    private fun init() = with(binding){
        val adapter = ViewPagerAdapter(activity as FragmentActivity, fragmentList)
        vp.adapter = adapter
        TabLayoutMediator(tabLayout, vp){
            tab, position -> tab.text = titleList[position]
        }.attach()
    }

    private fun updateCurrentCard() = with(binding){
        mainViewModel.liveDataCurrent.observe(viewLifecycleOwner){
            val maxMinTemp = "${it.maxTemp}°/${it.minTemp}°"
            tvData.text = it.time
            tvCity.text = it.city
            tvCurrentTemp.text = it.currentTemp
            tvCondition.text = it.condition
            tvMaxMin.text = maxMinTemp
            Picasso.get().load("https:" + it.imageUrl).into(imWeather)
        }
    }

    private fun permissionListener(){
        pLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
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
        val list = parseDaysList(mainObject)
        parseCurrentData(mainObject, list[0])
    }

    private fun parseDaysList(mainObject: JSONObject): List<WeatherModel>{
        val list = ArrayList<WeatherModel>()
        val daysArray = mainObject.getJSONObject("forecast").getJSONArray("forecastday")
        val name = mainObject.getJSONObject("location").getString("name")
        for (position in 0 until daysArray.length()){
            val day = daysArray[position] as JSONObject
            val item = WeatherModel(
                name,                                                                                            //city
                day.getString("date"),                                                                     //time
                day.getJSONObject("day").getJSONObject("condition").getString("text"),         //condition
                "",                                                                                   //currentTemp
                day.getJSONObject("day").getString("maxtemp_c"),                                     //maxTemp
                day.getJSONObject("day").getString("mintemp_c"),                                     //minTemp
                day.getJSONObject("day").getJSONObject("condition").getString("icon"),         //imageUrl
                day.getJSONArray("hour").toString()                                                       //hours
            )
            list.add(item)
        }
        return list
    }

    private fun parseCurrentData(mainObject: JSONObject, weatherItem: WeatherModel){
        val item = WeatherModel(
            mainObject.getJSONObject("location").getString("name"),                                   //city
            mainObject.getJSONObject("current").getString("last_updated"),                            //time
            mainObject.getJSONObject("current").getJSONObject("condition").getString("text"),   //condition
            mainObject.getJSONObject("current").getString("temp_c"),                                  //currentTemp
            weatherItem.maxTemp,                                                                                  //maxTemp
            weatherItem.minTemp,                                                                                  //minTemp
            mainObject.getJSONObject("current").getJSONObject("condition").getString("icon"),   //imageUrl
            weatherItem.hours                                                                                     //hours
        )
        mainViewModel.liveDataCurrent.value = item
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}