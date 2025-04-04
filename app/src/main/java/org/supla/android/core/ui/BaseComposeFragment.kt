package org.supla.android.core.ui
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.supla.android.R
import org.supla.android.databinding.FragmentComposeBinding

abstract class BaseComposeFragment<S : ViewState, E : ViewEvent> : BaseFragment<S, E>(R.layout.fragment_compose) {

  private val binding by viewBinding(FragmentComposeBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.composeContent.setContent {
      val state by viewModel.getViewState().collectAsState()
      ComposableContent(state)
    }
  }

  @Composable
  abstract fun ComposableContent(modelState: S)

  override fun handleViewState(state: S) {
  }
}
