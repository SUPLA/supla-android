package org.supla.android.features.about
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
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.databinding.FragmentComposeBinding
import org.supla.android.navigator.MainNavigator
import javax.inject.Inject

@AndroidEntryPoint
class AboutFragment : BaseFragment<AboutViewModelState, AboutViewEvent>(R.layout.fragment_compose) {
  override val viewModel: AboutViewModel by viewModels()
  private val binding by viewBinding(FragmentComposeBinding::bind)

  @Inject
  internal lateinit var navigator: MainNavigator

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.composeContent.setContent {
      val modelState by viewModel.getViewState().collectAsState()
      SuplaTheme {
        AboutView(
          viewState = modelState.viewState,
          onSuplaUrlClick = navigator::navigateToSuplaOrgExternal,
          onVersionClick = viewModel::onVersionClick
        )
      }
    }
  }

  override fun handleEvents(event: AboutViewEvent) {
    when (event) {
      AboutViewEvent.NavigateToDeveloperInfoScreen -> navigator.navigateTo(R.id.developer_info_fragment)
      AboutViewEvent.ShowDeveloperModeActivated -> Toast.makeText(context, R.string.developer_info_activated, Toast.LENGTH_SHORT).show()
    }
  }

  override fun handleViewState(state: AboutViewModelState) {}
}
