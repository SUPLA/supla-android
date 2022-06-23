package org.supla.android.widget.single.configuration
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
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.databinding.ActivitySingleWidgetConfigurationBinding
import org.supla.android.widget.WidgetPreferences
import org.supla.android.widget.shared.configuration.WidgetConfigurationActivityBase
import org.supla.android.widget.shared.configuration.WidgetConfigurationChannelsSpinnerAdapter
import org.supla.android.widget.shared.configuration.WidgetConfigurationProfilesSpinnerAdapter
import org.supla.android.widget.single.intent
import javax.inject.Inject

/**
 * Activity which displays a list of available switches during the on-off widget configuration.
 */
@AndroidEntryPoint
class SingleWidgetConfigurationActivity : WidgetConfigurationActivityBase<ActivitySingleWidgetConfigurationBinding>() {

    @Inject
    lateinit var widgetPreferences: WidgetPreferences

    private val viewModel: SingleWidgetConfigurationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupSwitchesSpinner()
        setupProfilesSpinner()
        setContentView(binding.root)

        observeSelectionConfirmation()
        observeCancellation()
    }

    private fun setupSwitchesSpinner() {
        val adapter = WidgetConfigurationChannelsSpinnerAdapter(this, mutableListOf())
        binding.widgetSingleConfigureSwitches.adapter = adapter
        binding.widgetSingleConfigureSwitches.onItemSelectedListener =
                channelItemSelectedListener(adapter)
    }

    private fun setupProfilesSpinner() {
        val adapter = WidgetConfigurationProfilesSpinnerAdapter(this, mutableListOf())
        binding.widgetSingleConfigureProfiles.adapter = adapter
        binding.widgetSingleConfigureProfiles.onItemSelectedListener =
                profileItemSelectedListener(adapter)
    }

    override fun updateSwitchDisplayName(caption: String) {
        binding.widgetSingleConfigureName.setText(caption)
    }

    override fun onWidgetNameError() {
        binding.widgetSingleConfigureName.background =
                ContextCompat.getDrawable(this,
                        R.drawable.rounded_edittext_err)
    }

    private fun observeCancellation() {
        binding.widgetSingleConfigureClose.setOnClickListener {
            // user's just closing the window, nothing to do..
            finish()
        }
    }

    override fun viewModel() = viewModel

    override fun bind() = ActivitySingleWidgetConfigurationBinding
            .inflate(layoutInflater).apply {
                viewmodel = viewModel
                lifecycleOwner = this@SingleWidgetConfigurationActivity
            }

    override fun getIntent(context: Context, intentAction: String, widgetId: Int) =
            intent(context, intentAction, widgetId)

    override fun getNameMaxLength() = resources.getInteger(R.integer.single_widget_name_max_length)
}
