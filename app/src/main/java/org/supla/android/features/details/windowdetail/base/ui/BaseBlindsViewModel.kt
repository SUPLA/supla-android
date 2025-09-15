package org.supla.android.features.details.windowdetail.base.ui
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

import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.Preferences
import org.supla.android.Trace
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.rollershutter.SuplaChannelFacadeBlindConfig
import org.supla.android.data.source.remote.rollershutter.SuplaTiltControlType
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.extensions.TAG
import org.supla.android.features.details.windowdetail.base.BaseWindowViewModel
import org.supla.android.features.details.windowdetail.base.BaseWindowViewModelState
import org.supla.android.features.details.windowdetail.base.data.ShadingBlindMarker
import org.supla.android.features.details.windowdetail.base.data.ShadingBlindWindowState
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.data.facadeblinds.FacadeBlindWindowState
import org.supla.android.lib.actions.ActionId
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.CallSuplaClientOperationUseCase
import org.supla.android.usecases.client.ExecuteShadingSystemActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.android.usecases.client.VALUE_IGNORE
import org.supla.android.usecases.group.GetGroupOnlineSummaryUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.group.ReadGroupTiltingDetailsUseCase
import org.supla.android.usecases.group.TiltingDetails
import org.supla.core.shared.extensions.guardLet
import kotlin.math.max
import kotlin.math.min

abstract class BaseBlindsViewModel<S : BaseBlindsViewModelState>(
  private val channelConfigEventsManager: ChannelConfigEventsManager,
  private val executeShadingSystemActionUseCase: ExecuteShadingSystemActionUseCase,
  private val suplaClientProvider: SuplaClientProvider,
  private val readGroupTiltingDetailsUseCase: ReadGroupTiltingDetailsUseCase,
  executeSimpleActionUseCase: ExecuteSimpleActionUseCase,
  callSuplaClientOperationUseCase: CallSuplaClientOperationUseCase,
  readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  getGroupOnlineSummaryUseCase: GetGroupOnlineSummaryUseCase,
  preferences: Preferences,
  dateProvider: DateProvider,
  profileRepository: RoomProfileRepository,
  loginUseCase: LoginUseCase,
  authorizeUseCase: AuthorizeUseCase,
  defaultState: S,
  schedulers: SuplaSchedulers
) : BaseWindowViewModel<S>(
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
  defaultState,
  schedulers
) {

  protected abstract fun updateWindowState(state: S, position: WindowGroupedValue, tilt: Float?, markers: List<ShadingBlindMarker>): S

  protected abstract fun updateTiltingDetails(
    tilt0Angle: Float,
    tilt100Angle: Float,
    tiltControlType: SuplaTiltControlType,
    tiltingTime: Int? = null,
    openingTime: Int? = null,
    closingTime: Int? = null
  )

  override fun handleAction(action: ShadingSystemAction, remoteId: Int, itemType: ItemType) {
    when (action) {
      is ShadingSystemAction.TiltTo ->
        updateState { state ->
          if (state.viewState.calibrating) {
            // When calibration open/close time is not known so it's not possible to open window at expected position
            state
          } else {
            val markers = when {
              state.tiltControlType == SuplaTiltControlType.TILTS_ONLY_WHEN_FULLY_CLOSED ->
                emptyList()

              state.windowState.position is WindowGroupedValue.Different ->
                state.windowState.markers.map { marker -> ShadingBlindMarker(marker.position, action.tilt) }

              else -> emptyList()
            }
            val position = when {
              state.tiltControlType == SuplaTiltControlType.TILTS_ONLY_WHEN_FULLY_CLOSED -> WindowGroupedValue.Similar(100f)
              else -> state.windowState.position
            }
            stateCopy(
              updateWindowState(state, position = position, tilt = action.tilt, markers = markers),
              manualMoving = true
            ) { it.copy(touchTime = null, positionUnknown = false) }
          }
        }

      is ShadingSystemAction.TiltSetTo ->
        updateState {
          if (it.viewState.calibrating) {
            // During calibration the open/close time is not known so it's not possible to open window at expected position
            it
          } else {
            executeShadingSystemActionUseCase.invoke(
              actionId = ActionId.SHUT_PARTIALLY,
              type = itemType.toSubjectType(),
              remoteId = remoteId,
              tilt = action.tilt
            ).runIt()
            stateCopy(it, manualMoving = false)
          }
        }

      is ShadingSystemAction.MoveAndTiltTo ->
        updateState { state ->
          if (state.viewState.calibrating) {
            // When calibration open/close time is not known so it's not possible to open window at expected position
            state
          } else {
            val tilt = when {
              state.windowState.slatTilt == null -> null
              state.tiltControlType == SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING -> limitTilt(action.tilt, action.position, state)
              state.tiltControlType != SuplaTiltControlType.TILTS_ONLY_WHEN_FULLY_CLOSED || action.position == 100f -> action.tilt
              else -> 0f
            }
            stateCopy(
              updateWindowState(state, position = WindowGroupedValue.Similar(action.position), tilt = tilt, markers = emptyList()),
              manualMoving = true
            ) { it.copy(touchTime = null, positionUnknown = false) }
          }
        }

      is ShadingSystemAction.MoveAndTiltSetTo ->
        updateState {
          if (it.viewState.calibrating) {
            // During calibration the open/close time is not known so it's not possible to open window at expected position
            it
          } else {
            val tilt = when {
              it.windowState.slatTilt == null -> VALUE_IGNORE
              it.tiltControlType == SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING -> limitTilt(action.tilt, action.position, it)
              it.tiltControlType != SuplaTiltControlType.TILTS_ONLY_WHEN_FULLY_CLOSED || action.position == 100f -> action.tilt
              else -> 0f
            }
            executeShadingSystemActionUseCase.invoke(
              ActionId.SHUT_PARTIALLY,
              itemType.toSubjectType(),
              remoteId,
              percentage = action.position,
              tilt = tilt
            ).runIt()
            stateCopy(it, manualMoving = false)
          }
        }

      else -> super.handleAction(action, remoteId, itemType)
    }
  }

  fun loadConfig(remoteId: Int, itemType: ItemType) {
    when (itemType) {
      ItemType.CHANNEL ->
        suplaClientProvider.provide()?.getChannelConfig(remoteId, ChannelConfigType.DEFAULT)

      ItemType.GROUP ->
        readGroupTiltingDetailsUseCase(remoteId)
          .attachSilent()
          .subscribeBy(
            onSuccess = {
              if (it is TiltingDetails.Similar) {
                updateTiltingDetails(it.tilt0Angle.toFloat(), it.tilt100Angle.toFloat(), it.tiltControlType)
              } else {
                Trace.i(TAG, "Tilting details differs from Similar: $it")
              }
            },
            onError = defaultErrorHandler("loadConfig")
          )
          .disposeBySelf()
    }
  }

  fun observeConfig(remoteId: Int, itemType: ItemType) {
    if (itemType == ItemType.CHANNEL) {
      channelConfigEventsManager.observerConfig(remoteId).filter { it.config is SuplaChannelFacadeBlindConfig }
        .attachSilent()
        .subscribeBy(
          onNext = { handleConfig(it) },
          onError = defaultErrorHandler("loadChannelData")
        )
        .disposeBySelf()
    }
  }

  private fun limitTilt(tilt: Float, position: Float, state: S): Float {
    val (tiltingTime, openingTime, closingTime, lastPosition) =
      guardLet(state.tiltingTime, state.openingTime, state.closingTime, state.lastPosition) { return tilt }

    val time = if (position > lastPosition) closingTime else openingTime
    val positionTime = time.times(position).div(100f)

    if (positionTime < tiltingTime) {
      return min(tilt, 100f.times(positionTime.div(tiltingTime)))
    }
    if (positionTime > time - tiltingTime) {
      return max(tilt, 100f.minus(100f.times(time.minus(positionTime).div(tiltingTime))))
    }

    return tilt
  }

  private fun handleConfig(config: ChannelConfigEventsManager.ConfigEvent) {
    val (facadeConfig) = guardLet(config.config as? SuplaChannelFacadeBlindConfig) { return }
    val (tilt0, tilt100) =
      if (facadeConfig.tilt0Angle == facadeConfig.tilt100Angle) {
        listOf(FacadeBlindWindowState.DEFAULT_TILT_0_ANGLE, FacadeBlindWindowState.DEFAULT_TILT_100_ANGLE)
      } else {
        listOf(facadeConfig.tilt0Angle.toFloat(), facadeConfig.tilt100Angle.toFloat())
      }

    with(facadeConfig) {
      updateTiltingDetails(tilt0, tilt100, type, tiltingTimeMs, openingTimeMs, closingTimeMs)
    }
  }
}

abstract class BaseBlindsViewModelState : BaseWindowViewModelState() {
  abstract val tiltControlType: SuplaTiltControlType?
  abstract val tiltingTime: Int?
  abstract val openingTime: Int?
  abstract val closingTime: Int?
  abstract val lastPosition: Int?

  abstract override val windowState: ShadingBlindWindowState
}
