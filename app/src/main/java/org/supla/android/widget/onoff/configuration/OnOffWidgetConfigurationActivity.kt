package org.supla.android.widget.onoff.configuration
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

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.databinding.ActivityOnOffWidgetConfigurationBinding
import org.supla.android.db.AuthProfileItem
import org.supla.android.db.Channel
import org.supla.android.widget.WidgetPreferences
import org.supla.android.widget.onoff.intent
import javax.inject.Inject

/**
 * Activity which displays a list of available switches during the on-off widget configuration.
 */
@AndroidEntryPoint
class OnOffWidgetConfigurationActivity : FragmentActivity() {

    private val viewModel: OnOffWidgetConfigurationViewModel by viewModels()
    private lateinit var binding: ActivityOnOffWidgetConfigurationBinding
    private lateinit var widgetPreferences: WidgetPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOnOffWidgetConfigurationBinding
                .inflate(layoutInflater).apply {
                    viewmodel = viewModel
                    lifecycleOwner = this@OnOffWidgetConfigurationActivity
                }
        setupSwitchesSpinner()
        setupProfilesSpinner()
        setContentView(binding.root)

        // set default response
        setResult(RESULT_CANCELED)

        val appWidgetId = intent?.extras?.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)
                ?: AppWidgetManager.INVALID_APPWIDGET_ID
        viewModel.widgetId = appWidgetId
        widgetPreferences = WidgetPreferences(this)

        observeConfigSet()
        observeSelectionConfirmation()
        observeCancellation()
    }

    private fun setupSwitchesSpinner() {
        val adapter = OnOffWidgetConfigurationChannelsSpinnerAdapter(this, mutableListOf())
        binding.widgetOnOffConfigureSwitches.adapter = adapter
        binding.widgetOnOffConfigureSwitches.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.selectedChannel = adapter.getItem(position)
                updateSwitchDisplayName()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                viewModel.selectedChannel = null
            }
        }
    }

    private fun setupProfilesSpinner() {
        val adapter = OnOffWidgetConfigurationProfilesSpinnerAdapter(this, mutableListOf())
        binding.widgetOnOffConfigureProfiles.adapter = adapter
        binding.widgetOnOffConfigureProfiles.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.changeProfile(adapter.getItem(position))
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                viewModel.selectedChannel = null
            }
        }
    }

    private fun observeConfigSet() {
        viewModel.userLoggedIn.observe(this) {
            if (!it) {
                Toast.makeText(this, R.string.on_off_widget_app_not_initialized, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                updateSwitchDisplayName()
            }
        }
    }

    private fun updateSwitchDisplayName() {
        val caption = getSelectedChannelCaption()
        binding.widgetOnOffConfigureName.setText(caption)
        viewModel.displayName = caption
    }

    private fun getSelectedChannelCaption(): String {
        val maxLength = resources.getInteger(R.integer.widget_name_max_length)
        val caption = viewModel.selectedChannel?.getNotEmptyCaption(this) ?: ""
        return if (caption.length > maxLength) {
            caption.substring(0, maxLength)
        } else {
            caption
        }
    }

    private fun observeSelectionConfirmation() {
        viewModel.confirmationResult.observe(this) {
            when {
                it.isSuccess -> {
                    val widgetId = viewModel.widgetId ?: return@observe
                    sendBroadcast(intent(this, AppWidgetManager.ACTION_APPWIDGET_UPDATE, widgetId))
                    setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
                    finish()
                }
                it.exceptionOrNull() is NoItemSelectedException -> {
                    Toast.makeText(this, R.string.on_off_widget_configure_no_selection, Toast.LENGTH_SHORT).show()
                }
                it.exceptionOrNull() is EmptyDisplayNameException -> {
                    binding.widgetOnOffConfigureName.background = ContextCompat.getDrawable(this, R.drawable.rounded_edittext_err)
                }
                else -> {
                    Toast.makeText(this, R.string.on_off_widget_configure_error, Toast.LENGTH_SHORT).show()
                    Log.e(OnOffWidgetConfigurationActivity::javaClass.name, "Could not configure switch", it.exceptionOrNull())
                }
            }
        }
    }

    private fun observeCancellation() {
        binding.widgetOnOffConfigureClose.setOnClickListener {
            // user's just closing the window, nothing to do..
            finish()
        }
    }
}

@BindingAdapter("visibility")
fun setViewVisibility(view: View, isVisible: Boolean) {
    if (isVisible) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("channels")
fun setSpinnerChannels(spinner: Spinner, items: List<Channel>?) {
    items?.let {
        (spinner.adapter as OnOffWidgetConfigurationChannelsSpinnerAdapter).postItems(it)
    }
}

@BindingAdapter("profiles")
fun setSpinnerProfiles(spinner: Spinner, items: List<AuthProfileItem>?) {
    items?.let {
        (spinner.adapter as OnOffWidgetConfigurationProfilesSpinnerAdapter).postItems(it)
    }
}