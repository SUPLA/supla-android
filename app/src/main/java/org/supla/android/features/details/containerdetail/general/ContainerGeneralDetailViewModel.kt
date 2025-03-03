package org.supla.android.features.details.containerdetail.general
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
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.Preferences
import org.supla.android.core.shared.shareable
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.shareable
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.local.entity.extensions.onlineState
import org.supla.android.data.source.remote.SuplaChannelConfig
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.data.source.remote.container.SuplaChannelContainerConfig
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.lists.sensordata.SensorItemData
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import org.supla.android.usecases.channelconfig.LoadChannelConfigUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.GetAllChannelIssuesUseCase
import org.supla.core.shared.usecase.channel.GetChannelBatteryIconUseCase
import javax.inject.Inject

@HiltViewModel
class ContainerGeneralDetailViewModel @Inject constructor(
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  private val getChannelBatteryIconUseCase: GetChannelBatteryIconUseCase,
  private val getAllChannelIssuesUseCase: GetAllChannelIssuesUseCase,
  private val loadChannelConfigUseCase: LoadChannelConfigUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val getCaptionUseCase: GetCaptionUseCase,
  private val valuesFormatter: ValuesFormatter,
  private val preferences: Preferences,
  schedulers: SuplaSchedulers
) : BaseViewModel<ContainerGeneralDetailViewModeState, ContainerGeneralDetailViewEvent>(
  ContainerGeneralDetailViewModeState(),
  schedulers
) {

  fun loadData(remoteId: Int) {
    Maybe.zip(
      readChannelWithChildrenUseCase(remoteId),
      loadChannelConfigUseCase(remoteId).toMaybe(),
    ) { channel, config -> Pair(channel, config) }
      .attach()
      .subscribeBy(
        onSuccess = this::handle,
        onError = defaultErrorHandler("loadData()")
      )
      .disposeBySelf()
  }

  private fun handle(data: Pair<ChannelWithChildren, SuplaChannelConfig>) {
    val channelWithChildren = data.first
    val config = data.second as? SuplaChannelContainerConfig
    val value = channelWithChildren.channel.channelValueEntity.asContainerValue()
    val level = value.levelKnown.ifTrue { value.level.div(100f) }
    val levelString = when {
      value.online.not() -> "offline"
      level == null -> "---"
      else -> valuesFormatter.getPercentageString(level)
    }
    updateState { state ->
      state.copy(
        viewState = state.viewState.copy(
          fluidLevel = level,
          fluidLevelString = levelString,
          scale = preferences.scale,
          controlLevels = config?.let { createControlLevels(it) } ?: emptyList(),
          sensors = channelWithChildren.children
            .filter { it.relationType == ChannelRelationType.DEFAULT }
            .map { it.toSensorData() },
          issues = getAllChannelIssuesUseCase(channelWithChildren.shareable)
        )
      )
    }
  }

  private fun ChannelChildEntity.toSensorData(): SensorItemData =
    SensorItemData(
      channelId = channel.remoteId,
      profileId = channel.profileId,
      onlineState = channelDataEntity.channelValueEntity.onlineState,
      icon = getChannelIconUseCase(channelDataEntity),
      caption = getCaptionUseCase(channelDataEntity.shareable),
      userCaption = channel.caption,
      batteryIcon = getChannelBatteryIconUseCase(channelDataEntity.shareable),
      showChannelStateIcon = channelDataEntity.channelValueEntity.online && SuplaChannelFlag.CHANNEL_STATE inside channel.flags
    )

  private fun createControlLevels(config: SuplaChannelContainerConfig): List<ControlLevel> {
    val result = mutableListOf<ControlLevel>()

    if (config.alarmAboveLevel > 0) {
      result.add(errorLevel(config.alarmAboveLevel))
    }
    if (config.warningAboveLevel > 0) {
      result.add(warningLevel(config.warningAboveLevel))
    }
    if (config.warningBelowLevel > 0) {
      result.add(warningLevel(config.warningBelowLevel))
    }
    if (config.alarmBelowLevel > 0) {
      result.add(errorLevel(config.alarmBelowLevel))
    }

    return result.sortedBy { it.level }
  }

  private fun errorLevel(level: Int) = level.minus(1).div(100f).let {
    ErrorLevel(it, valuesFormatter.getPercentageString(it))
  }

  private fun warningLevel(level: Int) = level.minus(1).div(100f).let {
    WarningLevel(it, valuesFormatter.getPercentageString(it))
  }
}

sealed class ContainerGeneralDetailViewEvent : ViewEvent

data class ContainerGeneralDetailViewModeState(
  val viewState: ContainerGeneralDetailViewState = ContainerGeneralDetailViewState()
) : ViewState()
