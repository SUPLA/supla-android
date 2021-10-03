package org.supla.android.cfg

import android.content.Context
import android.app.Activity
import android.os.Bundle
import android.os.Build
import android.graphics.Typeface
import android.view.inputmethod.InputMethodManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.databinding.FragmentCfgBinding

class CfgFragment: Fragment() {

    private val viewModel: CfgViewModel by activityViewModels()
    private lateinit var binding: FragmentCfgBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_cfg,
					  container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

	      var type = SuplaApp.getApp().typefaceOpenSansRegular

        arrayOf(binding.appSettingsTitle,
                binding.channelHeightLabel,
                binding.temperatureUnitLabel,
                binding.buttonAutohideLabel).forEach {
            it.setTypeface(type)
        }

        when(viewModel.cfgData.channelHeight.value!!) {
            ChannelHeight.HEIGHT_150 -> binding.channelHeight.position = 0
            ChannelHeight.HEIGHT_100 -> binding.channelHeight.position = 1
            ChannelHeight.HEIGHT_60 -> binding.channelHeight.position = 2
        }

        binding.temperatureUnit.setOnPositionChangedListener() {
            pos -> when(pos) {
                0 -> viewModel.setTemperatureUnit(TemperatureUnit.CELSIUS)
                1 -> viewModel.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
            }
            viewModel.saveConfig()
        }
        binding.channelHeight.setOnPositionChangedListener() {
            pos -> when(pos) {
                0 -> viewModel.setChannelHeight(ChannelHeight.HEIGHT_150)
                1 -> viewModel.setChannelHeight(ChannelHeight.HEIGHT_100)
                2 -> viewModel.setChannelHeight(ChannelHeight.HEIGHT_60)
            }
            viewModel.saveConfig()
        }

        binding.buttonAutohide.setOnClickListener() {
            viewModel.setButtonAutohide(!viewModel.cfgData.buttonAutohide.value!!)
            viewModel.saveConfig()
        }

        return binding.root
    }
}
