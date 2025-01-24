package org.supla.android.widget.shared.configuration
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
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import org.supla.android.R
import org.supla.android.Trace
import org.supla.android.data.source.local.entity.Scene
import org.supla.android.db.AuthProfileItem
import org.supla.android.db.ChannelBase
import org.supla.android.db.DbItem
import org.supla.android.extensions.setStatusBarColor

abstract class WidgetConfigurationActivityBase<T : Any> : FragmentActivity() {

  protected lateinit var binding: T

  protected abstract val widgetWarningView: RelativeLayout

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStatusBarColor(R.color.primary_container, false)

    binding = bind()

    // set default response
    setResult(RESULT_CANCELED)

    val appWidgetId = intent?.extras?.getInt(
      AppWidgetManager.EXTRA_APPWIDGET_ID,
      AppWidgetManager.INVALID_APPWIDGET_ID
    )
      ?: AppWidgetManager.INVALID_APPWIDGET_ID
    viewModel().widgetId = appWidgetId

    widgetWarningView.setOnClickListener {
      Intent().also {
        it.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        it.setData(Uri.parse("package:$packageName"))
        startActivity(it)
      }
    }
  }

  override fun onResume() {
    super.onResume()

    val powerManager = getSystemService(POWER_SERVICE) as PowerManager
    widgetWarningView.isVisible = !powerManager.isIgnoringBatteryOptimizations(packageName)
  }

  protected fun profileItemSelectedListener(
    adapter: WidgetConfigurationSpinnerBase<AuthProfileItem>
  ) =
    object : AdapterView.OnItemSelectedListener {
      override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        viewModel().changeProfile(adapter.getItem(position))
      }

      override fun onNothingSelected(p0: AdapterView<*>?) {
        viewModel().selectedItem = null
      }
    }

  protected fun channelItemSelectedListener(adapter: WidgetConfigurationSpinnerBase<DbItem>) =
    object : AdapterView.OnItemSelectedListener {
      override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        viewModel().changeItem(adapter.getItem(position))
        updateSwitchName()
      }

      override fun onNothingSelected(p0: AdapterView<*>?) {
        viewModel().changeItem(null)
      }
    }

  protected fun itemSelectedListener(adapter: WidgetConfigurationChannelsSpinnerAdapter) =
    object : AdapterView.OnItemSelectedListener {
      override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        viewModel().changeItem(adapter.getItem(position)?.value)
        updateSwitchName()
      }

      override fun onNothingSelected(p0: AdapterView<*>?) {
        viewModel().changeItem(null)
      }
    }

  protected fun observeSelectionConfirmation() {
    viewModel().confirmationResult.observe(this) {
      when {
        it.isSuccess -> {
          val widgetId = viewModel().widgetId ?: return@observe
          sendBroadcast(getIntent(this, AppWidgetManager.ACTION_APPWIDGET_UPDATE, widgetId))
          setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
          finish()
        }

        it.exceptionOrNull() is NoItemSelectedException ->
          Toast.makeText(this, R.string.widget_configure_no_selection, Toast.LENGTH_SHORT).show()

        it.exceptionOrNull() is EmptyDisplayNameException ->
          onWidgetNameError()

        else -> {
          Toast.makeText(this, R.string.widget_configure_error, Toast.LENGTH_SHORT).show()
          Trace.e(
            WidgetConfigurationActivityBase<*>::javaClass.name,
            "Could not configure switch",
            Exception(it.exceptionOrNull())
          )
        }
      }
    }
  }

  private fun observeConfigSet() {
    viewModel().userLoggedIn.observe(this) {
      if (!it) {
        Toast.makeText(this, R.string.widget_app_not_initialized, Toast.LENGTH_SHORT).show()
        finish()
      } else {
        updateSwitchName()
      }
    }
  }

  private fun getSelectedChannelCaption(): String {
    val maxLength = resources.getInteger(R.integer.widget_name_max_length)
    val caption = when (val item = viewModel().selectedItem) {
      is ChannelBase -> item.getCaption(this)
      is Scene -> item.caption
      else -> ""
    }

    return if (caption.length > maxLength) {
      caption.substring(0, maxLength)
    } else {
      caption
    }
  }

  private fun updateSwitchName() {
    val caption = getSelectedChannelCaption()
    updateSwitchDisplayName(caption)
    viewModel().displayName = caption
  }

  protected abstract fun viewModel(): WidgetConfigurationViewModelBase

  protected abstract fun updateSwitchDisplayName(caption: String)

  protected abstract fun onWidgetNameError()

  protected abstract fun bind(): T

  protected abstract fun getIntent(
    context: Context,
    intentAction: String,
    widgetId: Int
  ): Intent
}
