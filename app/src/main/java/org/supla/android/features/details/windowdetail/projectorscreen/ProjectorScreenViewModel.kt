package org.supla.android.features.details.windowdetail.projectorscreen
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
import org.supla.android.Preferences
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.extensions.guardLet
import org.supla.android.features.details.windowdetail.base.BaseWindowViewModel
import org.supla.android.features.details.windowdetail.base.BaseWindowViewModelState
import org.supla.android.features.details.windowdetail.base.data.ProjectorScreenState
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.ui.ShadingSystemPositionPresentation
import org.supla.android.features.details.windowdetail.base.ui.WindowViewState
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.CallSuplaClientOperationUseCase
import org.supla.android.usecases.client.ExecuteShadingSystemActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.android.usecases.group.GetGroupOnlineSummaryUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.group.totalvalue.ProjectorScreenGroupValue
import javax.inject.Inject

@HiltViewModel
class ProjectorScreenViewModel @Inject constructor(
  executeShadingSystemActionUseCase: ExecuteShadingSystemActionUseCase,
  executeSimpleActionUseCase: ExecuteSimpleActionUseCase,
  callSuplaClientOperationUseCase: CallSuplaClientOperationUseCase,
  readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  getGroupOnlineSummaryUseCase: GetGroupOnlineSummaryUseCase,
  preferences: Preferences,
  dateProvider: DateProvider,
  suplaClientProvider: SuplaClientProvider,
  profileRepository: RoomProfileRepository,
  loginUseCase: LoginUseCase,
  authorizeUseCase: AuthorizeUseCase,
  schedulers: SuplaSchedulers
) : BaseWindowViewModel<ProjectorScreenViewModelState>(
  executeShadingSystemActionUseCase,
  executeSimpleActionUseCase,
  callSuplaClientOperationUseCase,
  readChannelByRemoteIdUseCase,
  readChannelGroupByRemoteIdUseCase,
  getGroupOnlineSummaryUseCase,
  preferences,
  dateProvider,
  suplaClientProvider,
  profileRepository,
  loginUseCase,
  authorizeUseCase,
  ProjectorScreenViewModelState(),
  schedulers
) {

  override fun updatePosition(state: ProjectorScreenViewModelState, position: Float) =
    state.copy(windowState = state.windowState.copy(position = WindowGroupedValue.Similar(position)))

  override fun stateCopy(
    state: ProjectorScreenViewModelState,
    remoteId: Int?,
    moveStartTime: Long?,
    manualMoving: Boolean,
    showCalibrationDialog: Boolean,
    authorizationDialogState: AuthorizationDialogState?,
    viewStateUpdater: (WindowViewState) -> WindowViewState
  ): ProjectorScreenViewModelState =
    state.copy(
      remoteId = remoteId,
      moveStartTime = moveStartTime,
      manualMoving = manualMoving,
      showCalibrationDialog = showCalibrationDialog,
      authorizationDialogState = authorizationDialogState,
      viewState = viewStateUpdater(state.viewState)
    )

  override fun handleChannel(channel: ChannelDataEntity) {
    updateState { state ->
      if (state.manualMoving) {
        return@updateState state // Skip position updating when moving by finger
      }
      val value = channel.channelValueEntity.asRollerShutterValue()
      val position = if (value.hasValidPosition()) value.position else 0

      updateChannel(state, channel, value) {
        it.copy(
          windowState = it.windowState.copy(
            position = WindowGroupedValue.Similar(if (value.online) position.toFloat() else 25f)
          ),
          viewState = it.viewState.copy(positionPresentation = ShadingSystemPositionPresentation.AS_EXTENSION)
        )
      }
    }
  }

  override fun handleGroup(group: GroupData) {
    updateState { state ->
      if (state.manualMoving) {
        return@updateState state // Skip position updating when moving by finger
      }

      val positions = group.groupDataEntity.channelGroupEntity.getProjectorScreenPositions()
      val overallPosition = getGroupValues(positions, state.windowState.markers.isNotEmpty())

      updateGroup(state, group.groupDataEntity, group.onlineSummary) {
        it.copy(
          remoteId = group.groupDataEntity.remoteId,
          windowState = it.windowState.copy(
            position = if (group.groupDataEntity.isOnline()) overallPosition else WindowGroupedValue.Similar(25f),
            markers = if (overallPosition is WindowGroupedValue.Different) positions else emptyList()
          ),
          viewState = it.viewState.copy(
            positionUnknown = overallPosition is WindowGroupedValue.Invalid,
            positionPresentation = ShadingSystemPositionPresentation.AS_EXTENSION
          ),
        )
      }
    }
  }
}

private fun ChannelGroupEntity.getProjectorScreenPositions(): List<Float> =
  groupTotalValues.mapNotNull {
    val (value) = guardLet(it as? ProjectorScreenGroupValue) { return@mapNotNull null }
    value.position.toFloat()
  }

data class ProjectorScreenViewModelState(
  override val remoteId: Int? = null,
  override val windowState: ProjectorScreenState = ProjectorScreenState(WindowGroupedValue.Similar(0f)),
  override val viewState: WindowViewState = WindowViewState(),
  override val moveStartTime: Long? = null,
  override val manualMoving: Boolean = false,
  override val showCalibrationDialog: Boolean = false,
  override val authorizationDialogState: AuthorizationDialogState? = null
) : BaseWindowViewModelState()
