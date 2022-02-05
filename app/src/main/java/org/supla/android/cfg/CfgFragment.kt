package org.supla.android.cfg
/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


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
                binding.buttonAutohideLabel,
                binding.showChannelInfoLabel,
                binding.showOpeningPercentLabel,
                binding.locationOrderingButton).forEach {
            it.setTypeface(type)
        }

        val pos: Int
        when(viewModel.cfgData.channelHeight.value!!) {
            ChannelHeight.HEIGHT_150 -> pos = 2
            ChannelHeight.HEIGHT_100 -> pos = 1
            ChannelHeight.HEIGHT_60 -> pos = 0
        }
        binding.channelHeight.position = pos

        if(viewModel.cfgData.temperatureUnit.value == TemperatureUnit.FAHRENHEIT) {
            binding.temperatureUnit.position = 1
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
                2 -> viewModel.setChannelHeight(ChannelHeight.HEIGHT_150)
                1 -> viewModel.setChannelHeight(ChannelHeight.HEIGHT_100)
                0 -> viewModel.setChannelHeight(ChannelHeight.HEIGHT_60)
            }
            viewModel.saveConfig()
            
        }

        binding.showOpeningMode.position = if(viewModel.cfgData.showOpeningPercent.value == true) 0 else 1
        binding.showOpeningMode.setOnPositionChangedListener() { 
            pos -> when(pos) {
                0 -> viewModel.setShowOpeningPercent(true)
                1 -> viewModel.setShowOpeningPercent(false)
            }
            viewModel.saveConfig()
        }

        binding.buttonAutohide.setOnClickListener() {
            viewModel.setButtonAutohide(!viewModel.cfgData.buttonAutohide.value!!)
            viewModel.saveConfig()
        }

        binding.showChannelInfo.setOnClickListener() {
            viewModel.setShowChannelInfo(!viewModel.cfgData.showChannelInfo.value!!)
            viewModel.saveConfig()
        }
        return binding.root
    }

}
