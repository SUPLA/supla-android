package org.supla.android.features.details.windowdetail.base
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

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.Preferences
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.local.entity.custom.GroupOnlineSummary
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.ifLet
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValueFormat
import org.supla.android.features.details.windowdetail.base.data.WindowState
import org.supla.android.features.details.windowdetail.base.ui.ShadingSystemAction
import org.supla.android.features.details.windowdetail.base.ui.ShadingSystemPositionPresentation
import org.supla.android.features.details.windowdetail.base.ui.WindowViewState
import org.supla.android.lib.actions.ActionId
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.ui.dialogs.authorize.AuthorizationModelState
import org.supla.android.ui.dialogs.authorize.BaseAuthorizationViewModel
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.CallSuplaClientOperationUseCase
import org.supla.android.usecases.client.ExecuteShadingSystemActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.android.usecases.client.SuplaClientOperation
import org.supla.android.usecases.group.GetGroupOnlineSummaryUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import org.supla.core.shared.data.model.shadingsystem.ShadingSystemValue
import org.supla.core.shared.data.model.shadingsystem.SuplaShadingSystemFlag
import kotlin.math.abs

abstract class BaseWindowViewModel<S : BaseWindowViewModelState>(
  private val executeShadingSystemActionUseCase: ExecuteShadingSystemActionUseCase,
  private val executeSimpleActionUseCase: ExecuteSimpleActionUseCase,
  private val callSuplaClientOperationUseCase: CallSuplaClientOperationUseCase,
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  private val getGroupOnlineSummaryUseCase: GetGroupOnlineSummaryUseCase,
  private val preferences: Preferences,
  private val dateProvider: DateProvider,
  suplaClientProvider: SuplaClientProvider,
  profileRepository: RoomProfileRepository,
  loginUseCase: LoginUseCase,
  authorizeUseCase: AuthorizeUseCase,
  defaultState: S,
  schedulers: SuplaSchedulers
) : BaseAuthorizationViewModel<S, BaseWindowViewEvent>(
  suplaClientProvider,
  profileRepository,
  loginUseCase,
  authorizeUseCase,
  defaultState,
  schedulers
) {

  protected val positionTextFormat: WindowGroupedValueFormat
    get() =
      if (preferences.isShowOpeningPercent) {
        WindowGroupedValueFormat.OPENING_PERCENTAGE
      } else {
        WindowGroupedValueFormat.PERCENTAGE
      }

  protected abstract fun updatePosition(state: S, position: Float): S

  protected abstract fun stateCopy(
    state: S,
    remoteId: Int? = state.remoteId,
    moveStartTime: Long? = state.moveStartTime,
    manualMoving: Boolean = state.manualMoving,
    showCalibrationDialog: Boolean = state.showCalibrationDialog,
    authorizationDialogState: AuthorizationDialogState? = state.authorizationDialogState,
    viewStateUpdater: (WindowViewState) -> WindowViewState = { it }
  ): S

  fun loadData(remoteId: Int, itemType: ItemType) {
    when (itemType) {
      ItemType.CHANNEL -> loadChannelData(remoteId)
      ItemType.GROUP -> loadGroupData(remoteId)
    }
  }

  open fun handleAction(action: ShadingSystemAction, remoteId: Int, itemType: ItemType) {
    when (action) {
      is ShadingSystemAction.Open -> {
        updateState { state -> stateCopy(state, moveStartTime = null, manualMoving = false) { it.copy(touchTime = null) } }
        executeSimpleActionUseCase.invoke(ActionId.REVEAL, itemType.toSubjectType(), remoteId).runIt()
      }

      is ShadingSystemAction.Close -> {
        updateState { state -> stateCopy(state, moveStartTime = null, manualMoving = false) { it.copy(touchTime = null) } }
        executeSimpleActionUseCase.invoke(ActionId.SHUT, itemType.toSubjectType(), remoteId).runIt()
      }

      is ShadingSystemAction.MoveUp -> {
        updateState { updateMoveStartTime(it) }
        callSuplaClientOperationUseCase.invoke(remoteId, itemType, SuplaClientOperation.MoveUp).runIt()
      }

      is ShadingSystemAction.MoveDown -> {
        updateState { updateMoveStartTime(it) }
        callSuplaClientOperationUseCase.invoke(remoteId, itemType, SuplaClientOperation.MoveDown).runIt()
      }

      is ShadingSystemAction.Stop -> {
        updateState { state -> stateCopy(calculateMoveTime(state), manualMoving = false) }
        executeSimpleActionUseCase.invoke(ActionId.STOP, itemType.toSubjectType(), remoteId).runIt()
      }

      is ShadingSystemAction.Calibrate -> updateState { state -> stateCopy(state, manualMoving = false, showCalibrationDialog = true) }

      is ShadingSystemAction.OpenAt ->
        updateState { state ->
          if (state.viewState.calibrating) {
            // During calibration the open/close time is not known so it's not possible to open window at expected position
            state
          } else {
            executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, itemType.toSubjectType(), remoteId, action.position).runIt()
            stateCopy(state, moveStartTime = null, manualMoving = false) { it.copy(touchTime = null) }
          }
        }

      is ShadingSystemAction.MoveTo ->
        updateState { state ->
          if (state.viewState.calibrating) {
            // When calibration open/close time is not known so it's not possible to open window at expected position
            state
          } else {
            stateCopy(updatePosition(state, action.position), manualMoving = true) { it.copy(touchTime = null, positionUnknown = false) }
          }
        }

      else -> {} // just nothing, should be handled by child classes
    }
  }

  fun startCalibration() {
    updateState { stateCopy(it, showCalibrationDialog = false) }
    showAuthorizationDialog()
  }

  fun cancelCalibration() {
    updateState { stateCopy(it, showCalibrationDialog = false) }
  }

  override fun updateDialogState(updater: (AuthorizationDialogState?) -> AuthorizationDialogState?) {
    updateState { stateCopy(it, authorizationDialogState = updater(it.authorizationDialogState)) }
  }

  override fun onAuthorized() {
    hideAuthorizationDialog()

    val (remoteId) = guardLet(currentState().remoteId) { return }
    callSuplaClientOperationUseCase(remoteId, ItemType.CHANNEL, SuplaClientOperation.Command.Recalibrate)
      .attachSilent()
      .subscribeBy(
        onError = defaultErrorHandler("onAuthorized")
      )
      .disposeBySelf()
  }

  private fun loadGroupData(remoteId: Int) {
    readChannelGroupByRemoteIdUseCase(remoteId)
      .flatMap { groupData -> getGroupOnlineSummaryUseCase.invoke(groupData.id!!).map { GroupData(groupData, it) } }
      .attachSilent()
      .subscribeBy(
        onSuccess = { handleGroup(it) },
        onError = defaultErrorHandler("loadGroupData")
      )
      .disposeBySelf()
  }

  protected open fun loadChannelData(remoteId: Int) {
    readChannelByRemoteIdUseCase(remoteId)
      .attachSilent()
      .subscribeBy(
        onSuccess = { handleChannel(it) },
        onError = defaultErrorHandler("loadChannelData")
      )
      .disposeBySelf()
  }

  protected abstract fun handleChannel(channel: ChannelDataEntity)

  protected abstract fun handleGroup(group: GroupData)

  protected fun Completable.runIt() {
    this.attachSilent()
      .subscribe()
      .disposeBySelf()
  }

  protected fun <T> getGroupValues(values: List<T>, hadMarkers: Boolean, extractor: ((T) -> Float)? = null): WindowGroupedValue {
    var lastValue: Float? = null
    var minValue: Float? = null
    var maxValue: Float? = null

    for (value in values) {
      val floatValue: Float = if (value is Float) value else extractor!!(value)

      if (minValue == null || minValue > floatValue) {
        minValue = floatValue
      }
      if (maxValue == null || maxValue < floatValue) {
        maxValue = floatValue
      }

      if (lastValue == null) {
        lastValue = floatValue
      }
    }

    return if (lastValue == null || lastValue == -1f) {
      WindowGroupedValue.Invalid
    } else {
      ifLet(minValue, maxValue) { (min, max) ->
        if ((hadMarkers && abs(min - max) > 3) || abs(min - max) > 5) {
          return WindowGroupedValue.Different(min, max)
        }
      }

      WindowGroupedValue.Similar(lastValue)
    }
  }

  protected open fun canShowMoveTime(state: S): Boolean =
    state.viewState.positionUnknown

  private fun updateMoveStartTime(state: S): S =
    if (canShowMoveTime(state)) {
      stateCopy(state, moveStartTime = dateProvider.currentTimestamp()) { it.copy(touchTime = null) }
    } else {
      state
    }

  private fun calculateMoveTime(state: S): S =
    if (canShowMoveTime(state)) {
      val (startTime) = guardLet(state.moveStartTime) { return state }
      stateCopy(state, moveStartTime = null) { it.copy(touchTime = dateProvider.currentTimestamp().minus(startTime).div(1000f)) }
    } else {
      state
    }

  protected fun updateChannel(
    state: S,
    channel: ChannelDataEntity,
    value: ShadingSystemValue,
    customHandler: (S) -> S
  ): S =
    customHandler(
      stateCopy(state, remoteId = channel.remoteId) {
        it.copy(
          issues = createIssues(value.flags),
          enabled = value.online,
          positionPresentation = getPositionPresentation(),
          positionUnknown = value.hasValidPosition().not(),
          calibrating = value.flags.contains(SuplaShadingSystemFlag.CALIBRATION_IN_PROGRESS),
          calibrationPossible = SuplaChannelFlag.CALCFG_RECALIBRATE inside channel.flags
        )
      }
    )

  protected fun updateGroup(
    state: S,
    group: ChannelGroupDataEntity,
    onlineSummary: GroupOnlineSummary,
    customHandler: (S) -> S
  ): S =
    customHandler(
      stateCopy(state, remoteId = group.remoteId) {
        it.copy(
          enabled = group.isOnline(),
          positionPresentation = getPositionPresentation(),
          calibrating = false,
          calibrationPossible = false,
          isGroup = true,
          onlineStatusString = "${onlineSummary.onlineCount}/${onlineSummary.count}",
        )
      }
    )

  private fun createIssues(flags: List<SuplaShadingSystemFlag>) =
    flags.filter { it.isIssueFlag() }.mapNotNull { it.asChannelIssues() }

  private fun getPositionPresentation() =
    if (preferences.isShowOpeningPercent) ShadingSystemPositionPresentation.AS_OPENED else ShadingSystemPositionPresentation.AS_CLOSED

  protected data class GroupData(
    val groupDataEntity: ChannelGroupDataEntity,
    val onlineSummary: GroupOnlineSummary
  )
}

sealed class BaseWindowViewEvent : ViewEvent {
  data object LoadingError : BaseWindowViewEvent()
}

abstract class BaseWindowViewModelState : AuthorizationModelState() {
  abstract val remoteId: Int?
  abstract val windowState: WindowState
  abstract val viewState: WindowViewState

  abstract val moveStartTime: Long?
  abstract val manualMoving: Boolean

  abstract val showCalibrationDialog: Boolean
}
