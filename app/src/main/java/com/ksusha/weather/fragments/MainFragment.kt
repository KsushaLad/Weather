package com.ksusha.weather.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.ksusha.weather.adapters.ViewPagerAdapter
import com.ksusha.weather.databinding.FragmentMainBinding
import com.ksusha.weather.extencions.isPermissionGranted
import com.ksusha.weather.model.WeatherModel
import com.ksusha.weather.objects.DialogManager
import com.ksusha.weather.utils.*
import com.ksusha.weather.viewmodel.MainViewModel
import com.squareup.picasso.Picasso
import org.json.JSONObject

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private val fragmentList = listOf(HoursFragment.newInstance(), DaysFragment.newInstance())
    private val titleList = listOf(HOURS, DAYS)
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

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
    }

    override fun onResume() {
        super.onResume()
        checkLocation()
    }

    private fun init() = with(binding){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val adapter = ViewPagerAdapter(activity as FragmentActivity, fragmentList)
        vp.adapter = adapter
        TabLayoutMediator(tabLayout, vp){
            tab, position -> tab.text = titleList[position]
        }.attach()
        ibSync.setOnClickListener {
            tabLayout.selectTab(tabLayout.getTabAt(0))
            checkLocation()
        }
    }

    private fun isLocationEnabled(): Boolean{
        val locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun getLocation(){
        val token = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token.token).addOnCompleteListener {
            requestWeatherData("${it.result.latitude},${it.result.longitude},")
        }
    }

    private fun checkLocation(){
        if (isLocationEnabled()){
            getLocation()
        } else {
            DialogManager.locationSettingsDialog(requireContext(), object : DialogManager.Listener{
                override fun onClick() {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }

            })
        }
    }

    private fun updateCurrentCard() = with(binding){
        mainViewModel.liveDataCurrent.observe(viewLifecycleOwner){
            val maxMinTemp = "${it.minTemp}°C / ${it.maxTemp}°C"
            tvData.text = it.time
            tvCity.text = it.city
            tvCurrentTemp.text = it.currentTemp.ifEmpty { maxMinTemp }
            tvCondition.text = it.condition
            tvMaxMin.text = if (it.currentTemp.isEmpty()) " " else maxMinTemp
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
                "8" +
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
        val daysArray = mainObject.getJSONObject(FORECAST).getJSONArray(FORECASTDAY)
        val name = mainObject.getJSONObject(LOCATION).getString(NAME)
        for (position in 0 until daysArray.length()){
            val day = daysArray[position] as JSONObject
            val item = WeatherModel(
                name,                                                                     //city
                day.getString(DATE),                                                      //time
                day.getJSONObject(DAY).getJSONObject(CONDITION).getString(TEXT),          //condition
                "",                                                             //currentTemp
                day.getJSONObject(DAY).getString(MAXTEMP_C).toFloat().toInt().toString(), //maxTemp
                day.getJSONObject(DAY).getString(MINTEMP_C).toFloat().toInt().toString(), //minTemp
                day.getJSONObject(DAY).getJSONObject(CONDITION).getString(ICON),          //imageUrl
                day.getJSONArray(HOUR).toString()                                         //hours
            )
            list.add(item)
        }
        mainViewModel.liveDataList.value = list
        return list
    }

    private fun parseCurrentData(mainObject: JSONObject, weatherItem: WeatherModel){
        val item = WeatherModel(
            mainObject.getJSONObject(LOCATION).getString(NAME),                                   //city
            mainObject.getJSONObject(CURRENT).getString(LAST_UPDATED),                            //time
            mainObject.getJSONObject(CURRENT).getJSONObject(CONDITION).getString(TEXT),           //condition
            mainObject.getJSONObject(CURRENT).getString(TEMP_C),                                  //currentTemp
            weatherItem.maxTemp,                                                                  //maxTemp
            weatherItem.minTemp,                                                                  //minTemp
            mainObject.getJSONObject(CURRENT).getJSONObject(CONDITION).getString(ICON),           //imageUrl
            weatherItem.hours                                                                     //hours
        )
        mainViewModel.liveDataCurrent.value = item
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}