package org.supla.android.features.details.actiontrigger.list
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

import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.core.shared.shareable
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.extensions.subscribeBy
import org.supla.android.features.details.detailbase.base.DetailPage
import org.supla.android.features.details.detailbase.base.ItemBundle
import org.supla.android.tools.SuplaThreading
import org.supla.android.ui.lists.sensordata.RelatedChannelData
import org.supla.android.usecases.channel.ObserveChannelWithChildrenUseCase
import org.supla.android.usecases.details.ProvideChannelDetailTypeUseCase
import org.supla.android.usecases.details.StandardDetailType
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.GetChannelBatteryIconUseCase
import javax.inject.Inject

@HiltViewModel
class ActionTriggerListViewModel @Inject constructor(
  private val observeChannelWithChildrenUseCase: ObserveChannelWithChildrenUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val getChannelBatteryIconUseCase: GetChannelBatteryIconUseCase,
  private val getCaptionUseCase: GetCaptionUseCase,
  private val preferences: ApplicationPreferences,
  private val provideChannelDetailTypeUseCase: ProvideChannelDetailTypeUseCase,
  threading: SuplaThreading
) : BaseViewModel<ActionTriggerListViewState, ActionTriggerListViewEvent>(
  ActionTriggerListViewState(),
  threading
),
  ActionTriggerListScope {

  private var channelDetails: Map<Int, ChannelDetails> = emptyMap()

  fun observeData(remoteId: Int) {
    observeChannelWithChildrenUseCase(remoteId)
      .attachSilent()
      .subscribeBy(
        onNext = this::handleChannel,
        onError = defaultErrorHandler("observeData($remoteId)")
      )
      .disposeBySelf()
  }

  override fun onItemClick(data: RelatedChannelData) {
    val details = channelDetails[data.channelId] ?: return
    sendEvent(ActionTriggerListViewEvent.OpenDetails(details.itemBundle, details.pages))
  }

  override fun onItemCaptionLongPress(data: RelatedChannelData) {
    sendEvent(ActionTriggerListViewEvent.ChangeCaption(data))
  }

  override fun onItemInfoClick(data: RelatedChannelData) {
    sendEvent(ActionTriggerListViewEvent.ShowInfo(data))
  }

  private fun ChannelChildEntity.toRelatedChannelData() = RelatedChannelData.Visible(
    channelId = channel.remoteId,
    profileId = channel.profileId,
    onlineState = withChildren.onlineState,
    icon = getChannelIconUseCase(channelDataEntity),
    caption = getCaptionUseCase(channelDataEntity.shareable),
    userCaption = channel.caption,
    batteryIcon = getChannelBatteryIconUseCase(withChildren.shareable),
    showChannelStateIcon = channelDataEntity.showInfo
  )

  private fun handleChannel(channelWithChildren: ChannelWithChildren) {
    val actionTriggers = channelWithChildren.children
      .filter { it.function == SuplaFunction.ACTION_TRIGGER }

    channelDetails = actionTriggers.associate { child ->
      val pages = (provideChannelDetailTypeUseCase(child.withChildren) as? StandardDetailType)
        ?.pages
        .orEmpty()

      child.channel.remoteId to ChannelDetails(
        itemBundle = ItemBundle.from(child.channelDataEntity),
        pages = pages
      )
    }

    val relatedChannels = actionTriggers.map { it.toRelatedChannelData() }
    updateState { state ->
      state.copy(
        relatedChannels = relatedChannels,
        scale = preferences.scale
      )
    }
  }

  private data class ChannelDetails(
    val itemBundle: ItemBundle,
    val pages: List<DetailPage>
  )
}

sealed interface ActionTriggerListViewEvent : ViewEvent {
  data class ShowInfo(val data: RelatedChannelData) : ActionTriggerListViewEvent
  data class ChangeCaption(val data: RelatedChannelData) : ActionTriggerListViewEvent
  data class OpenDetails(val bundle: ItemBundle, val pages: List<DetailPage>) : ActionTriggerListViewEvent
}
