package org.supla.android.features.deviceslist
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

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.features.webcontent.WebContentFragment
import org.supla.android.navigator.MainNavigator
import javax.inject.Inject

@AndroidEntryPoint
class DevicesListFragment : WebContentFragment<DevicesListViewState, DeviceListViewEvent>() {

  override val url: String by lazy { getString(R.string.devices_list_url) }
  override val viewModel: DevicesListViewModel by viewModels()

  @Inject
  internal lateinit var navigator: MainNavigator

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    (binding.root.layoutParams as FrameLayout.LayoutParams).topMargin = 0
  }

  override fun handleEvents(event: DeviceListViewEvent) = when (event) {
    is DeviceListViewEvent.OpenUrl -> navigator.navigateToWeb(event.url)
  }
}
