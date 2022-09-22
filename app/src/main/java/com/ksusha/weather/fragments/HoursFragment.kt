package com.ksusha.weather.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ksusha.weather.R
import com.ksusha.weather.databinding.FragmentHoursBinding
import com.ksusha.weather.databinding.FragmentMainBinding

class HoursFragment : Fragment() {

    private lateinit var binding: FragmentHoursBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHoursBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = HoursFragment()
    }
}