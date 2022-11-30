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
import android.view.View
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.databinding.ActivitySingleWidgetConfigurationBinding
import org.supla.android.widget.WidgetPreferences
import org.supla.android.widget.shared.configuration.*
import org.supla.android.widget.single.intent
import javax.inject.Inject

/**
 * Activity which displays a list of available switches during the on-off widget configuration.
 */
@AndroidEntryPoint
class SingleWidgetConfigurationActivity :
  WidgetConfigurationActivityBase<ActivitySingleWidgetConfigurationBinding>() {

  @Inject
  lateinit var widgetPreferences: WidgetPreferences

  private val viewModel: SingleWidgetConfigurationViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setupProfilesSpinner()
    setupTypeSelection()
    setupItemsSpinner()
    setupActionsSpinner()
    setContentView(binding.root)

    observeSelectionConfirmation()
    observeCancellation()
  }

  private fun setupTypeSelection() {
    binding.widgetSingleCommon.widgetSingleConfigureType.setOnPositionChangedListener {
      viewModel.changeType(ItemType.values()[it])
    }
  }

  private fun setupItemsSpinner() {
    val adapter = WidgetConfigurationChannelsSpinnerAdapter(this, mutableListOf())
    binding.widgetSingleCommon.widgetSingleConfigureSwitches.adapter = adapter
    binding.widgetSingleCommon.widgetSingleConfigureSwitches.onItemSelectedListener =
      itemSelectedListener(adapter)
  }

  private fun setupProfilesSpinner() {
    val adapter = WidgetConfigurationProfilesSpinnerAdapter(this, mutableListOf())
    binding.widgetSingleCommon.widgetSingleConfigureProfiles.adapter = adapter
    binding.widgetSingleCommon.widgetSingleConfigureProfiles.onItemSelectedListener =
      profileItemSelectedListener(adapter)
  }

  private fun setupActionsSpinner() {
    val adapter = WidgetConfigurationActionsSpinnerAdapter(this, mutableListOf())
    binding.widgetSingleCommon.widgetSingleConfigureActions.adapter = adapter
    binding.widgetSingleCommon.widgetSingleConfigureActions.onItemSelectedListener =
      actionItemSelectedListener(adapter)
  }

  private fun actionItemSelectedListener(adapter: WidgetConfigurationSpinnerBase<WidgetAction>) =
    object : AdapterView.OnItemSelectedListener {
      override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        viewModel().selectedAction = adapter.getItem(position)
      }

      override fun onNothingSelected(p0: AdapterView<*>?) {
        viewModel().selectedAction = null
      }
    }

  override fun updateSwitchDisplayName(caption: String) {
    binding.widgetSingleCommon.widgetSingleConfigureName.setText(caption)
  }

  override fun onWidgetNameError() {
    binding.widgetSingleCommon.widgetSingleConfigureName.background =
      ContextCompat.getDrawable(
        this,
        R.drawable.background_widget_configuration_field_err
      )
  }

  private fun observeCancellation() {
    binding.widgetSingleCommon.widgetSingleConfigureClose.setOnClickListener {
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
}
