package org.supla.android.features.details.legacydetail
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
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.ChannelDetailDigiglass
import org.supla.android.ChannelDetailEM
import org.supla.android.ChannelDetailIC
import org.supla.android.ChannelDetailRGBW
import org.supla.android.ChannelDetailThermostatHP
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.databinding.FragmentLegacyDetailBinding
import org.supla.android.db.ChannelBase
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.listview.DetailLayout
import org.supla.android.ui.animations.DEFAULT_ANIMATION_DURATION
import org.supla.android.usecases.channel.GetChannelDefaultCaptionUseCase
import org.supla.android.usecases.details.LegacyDetailType
import javax.inject.Inject

private const val ARG_REMOTE_ID = "ARG_REMOTE_ID"
private const val ARG_DETAIL_TYPE = "ARG_DETAIL_TYPE"
private const val ARG_ITEM_TYPE = "ARG_ITEM_TYPE"

@AndroidEntryPoint
class LegacyDetailFragment : BaseFragment<LegacyDetailViewState, LegacyDetailViewEvent>(R.layout.fragment_legacy_detail) {

  override val viewModel: LegacyDetailViewModel by viewModels()
  private val binding by viewBinding(FragmentLegacyDetailBinding::bind)

  @Suppress("DEPRECATION") // Not deprecated method can't be accessed from API 21
  private val legacyDetailType: LegacyDetailType by lazy { requireArguments().getSerializable(ARG_DETAIL_TYPE) as LegacyDetailType }

  @Suppress("DEPRECATION") // Not deprecated method can't be accessed from API 21
  private val itemType: ItemType by lazy { requireArguments().getSerializable(ARG_ITEM_TYPE) as ItemType }
  private val remoteId: Int by lazy { requireArguments().getInt(ARG_REMOTE_ID) }

  @Inject lateinit var getChannelDefaultCaptionUseCase: GetChannelDefaultCaptionUseCase

  private lateinit var detailView: DetailLayout

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.loadData(remoteId, itemType)
  }

  override fun onResume() {
    super.onResume()

    if (this::detailView.isInitialized) {
      detailView.onDetailShow()
    }
  }

  override fun onPause() {
    super.onPause()

    if (this::detailView.isInitialized) {
      detailView.onDetailHide()
    }
  }

  override fun handleEvents(event: LegacyDetailViewEvent) {
    when (event) {
      is LegacyDetailViewEvent.LoadDetailView -> setupDetailView(event.channelBase)
    }
  }

  override fun handleViewState(state: LegacyDetailViewState) {
  }

  override fun onSuplaMessage(message: SuplaClientMsg) {
    when (message.type) {
      SuplaClientMsg.onDataChanged -> {
        if (this::detailView.isInitialized.not()) {
          return // view will not handle updates because it's not initialized
        }
        if (itemType == ItemType.CHANNEL && message.channelId != remoteId) {
          return // message for another channel
        }
        if (itemType == ItemType.GROUP && message.channelGroupId != remoteId) {
          return // message for another group
        }
        detailView.OnChannelDataChanged()
      }
    }
  }

  private fun setupDetailView(channelBase: ChannelBase) {
    setToolbarTitle(channelBase.getCaption(requireContext()))

    detailView = getDetailView().apply { setData(channelBase) }
    binding.legacyDetailContent.addView(
      detailView,
      ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    )
    detailView.visibility = View.VISIBLE
    Handler(Looper.getMainLooper()).postDelayed({ detailView.onDetailShow() }, 2 * DEFAULT_ANIMATION_DURATION)
  }

  private fun getDetailView(): DetailLayout = when (legacyDetailType) {
    LegacyDetailType.RGBW -> ChannelDetailRGBW(context)
    LegacyDetailType.IC -> ChannelDetailIC(context)
    LegacyDetailType.EM -> ChannelDetailEM(context)
    LegacyDetailType.THERMOSTAT_HP -> ChannelDetailThermostatHP(context)
    LegacyDetailType.DIGIGLASS -> ChannelDetailDigiglass(context)
  }

  companion object {
    fun bundle(remoteId: Int, legacyDetailType: LegacyDetailType, itemType: ItemType) = bundleOf(
      ARG_REMOTE_ID to remoteId,
      ARG_DETAIL_TYPE to legacyDetailType,
      ARG_ITEM_TYPE to itemType
    )
  }
}
