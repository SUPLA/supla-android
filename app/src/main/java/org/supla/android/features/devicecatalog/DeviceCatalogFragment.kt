package org.supla.android.features.devicecatalog
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

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.visibleIf
import org.supla.android.features.webcontent.WebContentFragment
import org.supla.android.navigator.MainNavigator
import javax.inject.Inject

private const val JS_MOBILE_CLASS = "document.body.classList.add('mobile'); "
private const val JS_NIGHT_CLASS = "document.body.classList.add('darkTheme'); "

@AndroidEntryPoint
class DeviceCatalogFragment : WebContentFragment<DeviceCatalogViewState, DeviceCatalogViewEvent>() {

  override val url: String by lazy { getString(R.string.devices_list_url) }
  override val viewModel: DeviceCatalogViewModel by viewModels()

  @Inject
  internal lateinit var navigator: MainNavigator

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.setUrl(url)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    (binding.root.layoutParams as FrameLayout.LayoutParams).topMargin = 0
    binding.webCompose.apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent {
        val modelState by viewModel.getViewState().collectAsState()
        SuplaTheme {
          if (modelState.showError) {
            DeviceCatalogErrorView { viewModel.onTryAgainClick() }
          }
        }
      }
    }
  }

  override fun handleViewState(state: DeviceCatalogViewState) {
    binding.webCompose.visibleIf(state.showError)

    if (state.showError) {
      binding.caProgressBar.visibility = View.GONE
      binding.webBrowser.visibility = View.GONE
    } else {
      super.handleViewState(state)
    }
  }

  override fun handleEvents(event: DeviceCatalogViewEvent) {
    when (event) {
      DeviceCatalogViewEvent.Reload -> binding.webBrowser.loadUrl(url)
      is DeviceCatalogViewEvent.OpenUrl -> navigator.navigateToWeb(event.url)
      is DeviceCatalogViewEvent.ApplyStyling -> {
        var jsClasses = JS_MOBILE_CLASS
        if (nightModeActive()) {
          jsClasses += JS_NIGHT_CLASS
        }
        binding.webBrowser.loadUrl("javascript:(function() { $jsClasses })()")
      }
    }
  }

  private fun nightModeActive(): Boolean {
    val configuration = requireContext().resources.configuration
    return (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
  }
}
