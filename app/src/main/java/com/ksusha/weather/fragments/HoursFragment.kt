package com.ksusha.weather.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ksusha.weather.adapters.WeatherAdapter
import com.ksusha.weather.databinding.FragmentHoursBinding
import com.ksusha.weather.model.WeatherModel
import com.ksusha.weather.utils.*
import com.ksusha.weather.viewmodel.MainViewModel
import org.json.JSONArray
import org.json.JSONObject

class HoursFragment : Fragment() {

    private lateinit var binding: FragmentHoursBinding
    private lateinit var adapter: WeatherAdapter
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHoursBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        observer()
    }

    private fun observer(){
        mainViewModel.liveDataCurrent.observe(viewLifecycleOwner){
            adapter.submitList(getHoursList(it))
        }
    }

    private fun getHoursList(weatherModel: WeatherModel): List<WeatherModel>{
        val hoursArray = JSONArray(weatherModel.hours)
        val list = ArrayList<WeatherModel>()
        for (position in 0 until hoursArray.length()){
            val item = WeatherModel(
                "",
                (hoursArray[position] as JSONObject).getString(TIME),
                (hoursArray[position] as JSONObject).getJSONObject(CONDITION).getString(TEXT),
                (hoursArray[position] as JSONObject).getString(TEMP_C),
                "",
                "",
                (hoursArray[position] as JSONObject).getJSONObject(CONDITION).getString(ICON),
                ""
            )
            list.add(item)
        }
        return list
    }

    private fun initRecyclerView() = with(binding){
        rcView.layoutManager = LinearLayoutManager(activity)
        adapter = WeatherAdapter()
        rcView.adapter = adapter

    }

    companion object {
        @JvmStatic
        fun newInstance() = HoursFragment()
    }
}