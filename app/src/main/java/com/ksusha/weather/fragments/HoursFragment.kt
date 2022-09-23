package com.ksusha.weather.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.ksusha.weather.adapters.WeatherAdapter
import com.ksusha.weather.databinding.FragmentHoursBinding
import com.ksusha.weather.model.WeatherModel

class HoursFragment : Fragment() {

    private lateinit var binding: FragmentHoursBinding
    private lateinit var adapter: WeatherAdapter

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
    }

    private fun initRecyclerView() = with(binding){
        rcView.layoutManager = LinearLayoutManager(activity)
        adapter = WeatherAdapter()
        rcView.adapter = adapter
        val list = listOf(
            WeatherModel("", "12:00", "Sunny", "23C", "", "", "", ""),
            WeatherModel("", "13:00", "Sunny", "24C", "", "", "", ""),
            WeatherModel("", "14:00", "Sunny", "26C", "", "", "", ""),
            WeatherModel("", "15:00", "Sunny", "27C", "", "", "", ""),
            WeatherModel("", "16:00", "Sunny", "25C", "", "", "", ""),
            WeatherModel("", "17:00", "Sunny", "23C", "", "", "", ""),
            WeatherModel("", "18:00", "Sunny", "19C", "", "", "", "")
        )
        adapter.submitList(list)
    }

    companion object {
        @JvmStatic
        fun newInstance() = HoursFragment()
    }
}