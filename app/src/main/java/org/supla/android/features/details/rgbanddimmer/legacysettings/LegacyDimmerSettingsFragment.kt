package org.supla.android.features.details.rgbanddimmer.legacysettings
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
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.ChannelDetailRGBW
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.UpHandler
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.databinding.FragmentLegacyDetailBinding
import org.supla.android.navigator.MainNavigator
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessage
import javax.inject.Inject

@AndroidEntryPoint
class LegacyDimmerSettingsFragment :
  BaseFragment<LegacyDimmerSettingsViewState, LegacyDimmerSettingsViewEvent>(R.layout.fragment_legacy_detail), UpHandler {
  override val viewModel: LegacyDimmerSettingsViewModel by viewModels()
  private val binding by viewBinding(FragmentLegacyDetailBinding::bind)

  private lateinit var detailView: ChannelDetailRGBW

  @Inject
  lateinit var navigator: MainNavigator

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.loadData(item.remoteId)

    val onBackPressedCallback = object : OnBackPressedCallback(true) {
      override fun handleOnBackPressed() {
        if (!detailView.onBackPressed()) {
          navigator.back()
        }
      }
    }
    requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    detailView = ChannelDetailRGBW(context)
    binding.legacyDetailContent.addView(
      detailView,
      ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    )
    detailView.visibility = View.VISIBLE
  }

  override fun onResume() {
    super.onResume()
    detailView.onDetailShow()
  }

  override fun onPause() {
    super.onPause()
    detailView.onDetailHide()
  }

  override fun onDestroyView() {
    binding.legacyDetailContent.removeView(detailView)
    super.onDestroyView()
  }

  override fun handleViewState(state: LegacyDimmerSettingsViewState) {
  }

  override fun handleEvents(event: LegacyDimmerSettingsViewEvent) {
    when (event) {
      is LegacyDimmerSettingsViewEvent.LoadView -> setupDetailView(event.channel)
    }
  }

  override fun onSuplaMessage(message: SuplaClientMessage) {
    (message as? SuplaClientMessage.ChannelDataChanged)?.let {
      if (item.itemType == ItemType.CHANNEL && message.channelId == item.remoteId) {
        detailView.OnChannelDataChanged()
      }
    }
  }

  private fun setupDetailView(channelDataEntity: ChannelDataEntity) {
    detailView.setData(channelDataEntity.getLegacyChannel())
    detailView.onDetailShow()
  }

  override fun onUpPressed(): Boolean {
    if (!detailView.onBackPressed()) {
      navigator.back()
    }

    return true
  }
}
