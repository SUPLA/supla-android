package org.supla.android.features.details.windowdetail.rollershutter
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

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.Preferences
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.local.entity.custom.GroupOnlineSummary
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.features.details.windowdetail.base.BaseWindowViewEvent
import org.supla.android.features.details.windowdetail.base.data.RollerShutterWindowState
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.ui.ShadingSystemAction
import org.supla.android.features.details.windowdetail.base.ui.ShadingSystemPositionPresentation
import org.supla.android.features.details.windowdetail.base.ui.WindowViewState
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.tools.VibrationHelper
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.CallSuplaClientOperationUseCase
import org.supla.android.usecases.client.ExecuteShadingSystemActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.android.usecases.client.SuplaClientOperation
import org.supla.android.usecases.group.GetGroupOnlineSummaryUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.group.totalvalue.GroupTotalValue
import org.supla.core.shared.data.model.function.rollershutter.RollerShutterValue
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.data.model.shadingsystem.SuplaShadingSystemFlag
import org.supla.core.shared.infrastructure.LocalizedStringId

@RunWith(MockitoJUnitRunner::class)
class RollerShutterViewModelTest :
  BaseViewModelTest<RollerShutterViewModelState, BaseWindowViewEvent, RollerShutterViewModel>() {

  @Mock
  lateinit var loadingTimeoutManager: LoadingTimeoutManager

  @Mock
  lateinit var executeShadingSystemActionUseCase: ExecuteShadingSystemActionUseCase

  @Mock
  lateinit var executeSimpleActionUseCase: ExecuteSimpleActionUseCase

  @Mock
  lateinit var callSuplaClientOperationUseCase: CallSuplaClientOperationUseCase

  @Mock
  lateinit var readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase

  @Mock
  lateinit var readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase

  @Mock
  lateinit var getGroupOnlineSummaryUseCase: GetGroupOnlineSummaryUseCase

  @Mock
  lateinit var vibrationHelper: VibrationHelper

  @Mock
  lateinit var preferences: Preferences

  @Mock
  lateinit var dateProvider: DateProvider

  @Mock
  lateinit var suplaClientProvider: SuplaClientProvider

  @Mock
  lateinit var profileRepository: RoomProfileRepository

  @Mock
  lateinit var loginUseCase: LoginUseCase

  @Mock
  lateinit var authorizeUseCase: AuthorizeUseCase

  @Mock
  override lateinit var schedulers: SuplaSchedulers

  @InjectMocks
  override lateinit var viewModel: RollerShutterViewModel

  @Before
  override fun setUp() {
    super.setUp()
  }

  @Test
  fun `should load data when online`() {
    // given
    val remoteId = 123
    val position = 33
    val bottomPosition = 87

    mockOnlineChannel(
      remoteId,
      position,
      bottomPosition,
      listOf(SuplaShadingSystemFlag.MOTOR_PROBLEM),
      listOf(SuplaChannelFlag.CALCFG_RECALIBRATE)
    )

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)

    // then
    assertThat(states).containsExactly(
      RollerShutterViewModelState(
        remoteId = remoteId,
        windowState = RollerShutterWindowState(WindowGroupedValue.Similar(position.toFloat()), bottomPosition.toFloat()),
        viewState = WindowViewState(
          issues = listOf(ChannelIssueItem.Error(LocalizedStringId.MOTOR_PROBLEM)),
          enabled = true,
          positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED,
          positionUnknown = false,
          calibrationPossible = true,
          calibrating = false,
        ),
      )
    )
  }

  @Test
  fun `should load group data when online with different positions`() {
    // given
    val remoteId = 133

    mockOnlineGroup(remoteId, "10:1|80:1|20:0")

    // when
    viewModel.loadData(remoteId, ItemType.GROUP)

    // then
    assertThat(states).containsExactly(
      RollerShutterViewModelState(
        remoteId = remoteId,
        windowState = RollerShutterWindowState(
          position = WindowGroupedValue.Different(min = 20.0f, max = 100.0f),
          markers = listOf(100f, 100f, 20f)
        ),
        viewState = WindowViewState(
          enabled = true,
          positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED,
          positionUnknown = false,
          calibrationPossible = false,
          calibrating = false,
          isGroup = true,
          onlineStatusString = "2/4",
        )
      )
    )
  }

  @Test
  fun `should load group data when online with similar positions`() {
    // given
    val remoteId = 123

    mockOnlineGroup(remoteId, "25:0|25:0|25:0")

    // when
    viewModel.loadData(remoteId, ItemType.GROUP)

    // then
    assertThat(states).containsExactly(
      RollerShutterViewModelState(
        remoteId = remoteId,
        windowState = RollerShutterWindowState(position = WindowGroupedValue.Similar(25f), markers = emptyList()),
        viewState = WindowViewState(
          enabled = true,
          positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED,
          positionUnknown = false,
          calibrationPossible = false,
          calibrating = false,
          isGroup = true,
          onlineStatusString = "2/4",
        )
      )
    )
  }

  @Test
  fun `should load data when offline`() {
    // given
    val remoteId = 123
    val position = 33
    val bottomPosition = 87
    val rollerShutterValue: RollerShutterValue = mockk {
      every { this@mockk.position } returns position
      every { this@mockk.bottomPosition } returns bottomPosition
      every { flags } returns listOf(SuplaShadingSystemFlag.CALIBRATION_IN_PROGRESS)
      every { hasValidPosition() } returns false
      every { online } returns false
    }
    val value: ChannelValueEntity = mockk {
      every { asRollerShutterValue() } returns rollerShutterValue
    }
    val channelData: ChannelDataEntity = mockk {
      every { this@mockk.remoteId } returns remoteId
      every { function } returns SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER
      every { channelValueEntity } returns value
      every { flags } returns 0
    }
    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channelData))

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)

    // then
    assertThat(states).containsExactly(
      RollerShutterViewModelState(
        remoteId = remoteId,
        windowState = RollerShutterWindowState(WindowGroupedValue.Similar(25f), bottomPosition.toFloat()),
        viewState = WindowViewState(
          issues = listOf(),
          enabled = false,
          positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED,
          positionUnknown = true,
          calibrating = true
        )
      )
    )
  }

  @Test
  fun `should reveal roller shutter`() {
    // given
    val remoteId = 122
    whenever(executeSimpleActionUseCase.invoke(ActionId.REVEAL, SubjectType.CHANNEL, remoteId))
      .thenReturn(Completable.complete())

    // when
    viewModel.handleAction(ShadingSystemAction.Open, remoteId, ItemType.CHANNEL)

    // then
    verify(executeSimpleActionUseCase).invoke(ActionId.REVEAL, SubjectType.CHANNEL, remoteId)
    assertThat(states).isEmpty()
  }

  @Test
  fun `should shut roller shutter`() {
    // given
    val remoteId = 122
    whenever(executeSimpleActionUseCase.invoke(ActionId.SHUT, SubjectType.CHANNEL, remoteId))
      .thenReturn(Completable.complete())

    // when
    viewModel.handleAction(ShadingSystemAction.Close, remoteId, ItemType.CHANNEL)

    // then
    verify(executeSimpleActionUseCase).invoke(ActionId.SHUT, SubjectType.CHANNEL, remoteId)
    assertThat(states).isEmpty()
  }

  @Test
  fun `should move up roller shutter`() {
    // given
    val remoteId = 122
    whenever(callSuplaClientOperationUseCase.invoke(remoteId, ItemType.GROUP, SuplaClientOperation.MoveUp))
      .thenReturn(Completable.complete())

    // when
    viewModel.handleAction(ShadingSystemAction.MoveUp, remoteId, ItemType.GROUP)

    // then
    verify(callSuplaClientOperationUseCase).invoke(remoteId, ItemType.GROUP, SuplaClientOperation.MoveUp)
    assertThat(states).isEmpty()
  }

  @Test
  fun `should move up roller shutter and set start timer`() {
    // given
    val remoteId = 122
    val timestamp = 22232L
    whenever(callSuplaClientOperationUseCase.invoke(remoteId, ItemType.CHANNEL, SuplaClientOperation.MoveUp))
      .thenReturn(Completable.complete())
    whenever(dateProvider.currentTimestamp()).thenReturn(timestamp)
    mockOnlineChannel(remoteId, hasValidPosition = false)

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)
    viewModel.handleAction(ShadingSystemAction.MoveUp, remoteId, ItemType.CHANNEL)

    // then
    verify(callSuplaClientOperationUseCase).invoke(remoteId, ItemType.CHANNEL, SuplaClientOperation.MoveUp)
    val state = RollerShutterViewModelState(
      remoteId = remoteId,
      windowState = RollerShutterWindowState(WindowGroupedValue.Similar(0f)),
      viewState = WindowViewState(
        issues = emptyList(),
        enabled = true,
        positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED,
        positionUnknown = true,
      )
    )
    assertThat(states).containsExactly(
      state,
      state.copy(moveStartTime = timestamp)
    )
  }

  @Test
  fun `should move down roller shutter`() {
    // given
    val remoteId = 122
    whenever(callSuplaClientOperationUseCase.invoke(remoteId, ItemType.CHANNEL, SuplaClientOperation.MoveDown))
      .thenReturn(Completable.complete())

    // when
    viewModel.handleAction(ShadingSystemAction.MoveDown, remoteId, ItemType.CHANNEL)

    // then
    verify(callSuplaClientOperationUseCase).invoke(remoteId, ItemType.CHANNEL, SuplaClientOperation.MoveDown)
    assertThat(states).isEmpty()
  }

  @Test
  fun `should move down roller shutter and set start time`() {
    // given
    val remoteId = 122
    val timestamp = 22232L
    whenever(callSuplaClientOperationUseCase.invoke(remoteId, ItemType.CHANNEL, SuplaClientOperation.MoveDown))
      .thenReturn(Completable.complete())
    whenever(dateProvider.currentTimestamp()).thenReturn(timestamp)
    mockOnlineChannel(remoteId, hasValidPosition = false, channelFlags = listOf(SuplaChannelFlag.CALCFG_RECALIBRATE))

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)
    viewModel.handleAction(ShadingSystemAction.MoveDown, remoteId, ItemType.CHANNEL)

    // then
    verify(callSuplaClientOperationUseCase).invoke(remoteId, ItemType.CHANNEL, SuplaClientOperation.MoveDown)
    val state = RollerShutterViewModelState(
      remoteId = remoteId,
      windowState = RollerShutterWindowState(WindowGroupedValue.Similar(0f)),
      viewState = WindowViewState(
        issues = emptyList(),
        enabled = true,
        positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED,
        calibrationPossible = true,
        positionUnknown = true,
      )
    )
    assertThat(states).containsExactly(
      state,
      state.copy(moveStartTime = timestamp)
    )
  }

  @Test
  fun `should stop roller shutter`() {
    // given
    val remoteId = 122
    whenever(executeSimpleActionUseCase.invoke(ActionId.STOP, SubjectType.CHANNEL, remoteId))
      .thenReturn(Completable.complete())

    // when
    viewModel.handleAction(ShadingSystemAction.Stop, remoteId, ItemType.CHANNEL)

    // then
    verify(executeSimpleActionUseCase).invoke(ActionId.STOP, SubjectType.CHANNEL, remoteId)
    assertThat(states).isEmpty()
  }

  @Test
  fun `should move down roller shutter then stop and calculate time`() {
    // given
    val remoteId = 122
    val timestamp = 22232L
    val afterTimestamp = 25232L
    whenever(callSuplaClientOperationUseCase.invoke(remoteId, ItemType.CHANNEL, SuplaClientOperation.MoveDown))
      .thenReturn(Completable.complete())
    whenever(executeSimpleActionUseCase.invoke(ActionId.STOP, SubjectType.CHANNEL, remoteId))
      .thenReturn(Completable.complete())
    whenever(dateProvider.currentTimestamp()).thenReturn(timestamp, afterTimestamp)
    mockOnlineChannel(remoteId, hasValidPosition = false)

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)
    viewModel.handleAction(ShadingSystemAction.MoveDown, remoteId, ItemType.CHANNEL)
    viewModel.handleAction(ShadingSystemAction.Stop, remoteId, ItemType.CHANNEL)

    // then
    verify(callSuplaClientOperationUseCase).invoke(remoteId, ItemType.CHANNEL, SuplaClientOperation.MoveDown)
    verify(executeSimpleActionUseCase).invoke(ActionId.STOP, SubjectType.CHANNEL, remoteId)
    val state = RollerShutterViewModelState(
      remoteId = remoteId,
      windowState = RollerShutterWindowState(WindowGroupedValue.Similar(0f)),
      viewState = WindowViewState(
        issues = emptyList(),
        enabled = true,
        positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED,
        positionUnknown = true,
      )
    )
    assertThat(states).containsExactly(
      state,
      state.copy(moveStartTime = timestamp),
      state.copy(moveStartTime = null, viewState = state.viewState.copy(touchTime = 3f))
    )
  }

  @Test
  fun `should open calibration dialog`() {
    // given
    val remoteId = 122

    // when
    viewModel.handleAction(ShadingSystemAction.Calibrate, remoteId, ItemType.CHANNEL)

    // then
    assertThat(states).containsExactly(
      RollerShutterViewModelState(showCalibrationDialog = true)
    )
    verifyNoInteractions(executeSimpleActionUseCase, executeShadingSystemActionUseCase)
  }

  @Test
  fun `should open roller shutter at specified position`() {
    // given
    val remoteId = 122
    whenever(executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 45f))
      .thenReturn(Completable.complete())

    // when
    viewModel.handleAction(ShadingSystemAction.OpenAt(45f), remoteId, ItemType.CHANNEL)

    // then
    verify(executeShadingSystemActionUseCase).invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 45f)
    assertThat(states).isEmpty()
  }

  @Test
  fun `should move roller view to position`() {
    // given
    val remoteId = 122

    // when
    viewModel.handleAction(ShadingSystemAction.MoveTo(45f), remoteId, ItemType.CHANNEL)

    // then
    assertThat(states).containsExactly(
      RollerShutterViewModelState(
        windowState = RollerShutterWindowState(WindowGroupedValue.Similar(45f)),
        manualMoving = true
      )
    )
    verifyNoInteractions(executeShadingSystemActionUseCase, executeSimpleActionUseCase)
  }

  @Test
  fun `shouldn't change roller position when calibrating`() {
    // given
    val remoteId = 122
    mockOnlineChannel(remoteId, valueFlags = listOf(SuplaShadingSystemFlag.CALIBRATION_IN_PROGRESS))

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)
    viewModel.handleAction(ShadingSystemAction.OpenAt(45f), remoteId, ItemType.CHANNEL)

    // then
    val state = RollerShutterViewModelState(
      remoteId = remoteId,
      windowState = RollerShutterWindowState(WindowGroupedValue.Similar(0f)),
      viewState = WindowViewState(
        issues = emptyList(),
        enabled = true,
        positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED,
        calibrating = true,
      )
    )
    assertThat(states).containsExactly(state)
    verifyNoInteractions(executeShadingSystemActionUseCase, executeSimpleActionUseCase)
  }

  @Test
  fun `shouldn't change roller position on view when calibrating`() {
    // given
    val remoteId = 122
    mockOnlineChannel(remoteId, valueFlags = listOf(SuplaShadingSystemFlag.CALIBRATION_IN_PROGRESS))

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)
    viewModel.handleAction(ShadingSystemAction.MoveTo(45f), remoteId, ItemType.CHANNEL)

    // then
    val state = RollerShutterViewModelState(
      remoteId = remoteId,
      windowState = RollerShutterWindowState(WindowGroupedValue.Similar(0f)),
      viewState = WindowViewState(
        issues = emptyList(),
        enabled = true,
        positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED,
        calibrating = true,
      )
    )
    assertThat(states).containsExactly(state)
    verifyNoInteractions(executeShadingSystemActionUseCase, executeSimpleActionUseCase)
  }

  @Test
  fun `should close calibration dialog`() {
    // given
    val remoteId = 122

    // when
    viewModel.handleAction(ShadingSystemAction.Calibrate, remoteId, ItemType.CHANNEL)
    viewModel.cancelCalibration()

    // then
    assertThat(states).containsExactly(
      RollerShutterViewModelState(showCalibrationDialog = true),
      RollerShutterViewModelState(showCalibrationDialog = false)
    )
    verifyNoInteractions(executeSimpleActionUseCase, executeShadingSystemActionUseCase)
  }

  @Test
  fun `should show authorization dialog`() {
    // given
    val remoteId = 122

    val profile: ProfileEntity = mockk {
      every { email } returns "some-email@supla.org"
      every { isCloudAccount } returns true
    }
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.just(profile))

    // when
    viewModel.handleAction(ShadingSystemAction.Calibrate, remoteId, ItemType.CHANNEL)
    viewModel.startCalibration()

    // then
    assertThat(states).containsExactly(
      RollerShutterViewModelState(showCalibrationDialog = true),
      RollerShutterViewModelState(showCalibrationDialog = false),
      RollerShutterViewModelState(
        authorizationDialogState = AuthorizationDialogState(
          userName = "some-email@supla.org",
          isCloudAccount = true,
          userNameEnabled = false
        )
      )
    )
    verify(profileRepository).findActiveProfile()
    verifyNoInteractions(executeSimpleActionUseCase, executeShadingSystemActionUseCase)
    verifyNoMoreInteractions(profileRepository)
  }

  @Test
  fun `should start calibration when user authorized`() {
    // given
    val remoteId = 122
    mockOnlineChannel(remoteId)
    whenever(callSuplaClientOperationUseCase.invoke(remoteId, ItemType.CHANNEL, SuplaClientOperation.Command.Recalibrate))
      .thenReturn(Completable.complete())

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)
    viewModel.onAuthorized()

    // then
    val state = RollerShutterViewModelState(
      remoteId = remoteId,
      windowState = RollerShutterWindowState(WindowGroupedValue.Similar(0f)),
      viewState = WindowViewState(
        issues = emptyList(),
        enabled = true,
        positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED,
        positionUnknown = false,
      )
    )
    assertThat(states).containsExactly(state)
    verify(callSuplaClientOperationUseCase).invoke(remoteId, ItemType.CHANNEL, SuplaClientOperation.Command.Recalibrate)
    verifyNoInteractions(executeShadingSystemActionUseCase, executeSimpleActionUseCase)
  }

  private fun mockOnlineChannel(
    remoteId: Int,
    position: Int = 0,
    bottomPosition: Int = 100,
    valueFlags: List<SuplaShadingSystemFlag> = emptyList(),
    channelFlags: List<SuplaChannelFlag> = emptyList(),
    hasValidPosition: Boolean = true,
    function: SuplaFunction = SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER
  ) {
    val rollerShutterValue: RollerShutterValue = mockk {
      every { this@mockk.position } returns position
      every { this@mockk.bottomPosition } returns bottomPosition
      every { this@mockk.flags } returns valueFlags
      every { hasValidPosition() } returns hasValidPosition
      every { online } returns true
    }
    val value: ChannelValueEntity = mockk {
      every { asRollerShutterValue() } returns rollerShutterValue
    }
    val channelData: ChannelDataEntity = mockk {
      every { this@mockk.remoteId } returns remoteId
      every { this@mockk.function } returns function
      every { channelValueEntity } returns value
      every { flags } returns channelFlags.fold(0L) { result, flag -> result or flag.rawValue }
    }

    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channelData))
  }

  private fun mockOnlineGroup(
    remoteId: Int,
    totalValue: String
  ) {
    val groupId = 123L
    val group: ChannelGroupEntity = mockk {
      every { this@mockk.groupTotalValues } returns GroupTotalValue.parse(SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER, totalValue)
    }
    val groupData: ChannelGroupDataEntity = mockk {
      every { id } returns groupId
      every { this@mockk.remoteId } returns remoteId
      every { this@mockk.function } returns SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER
      every { channelGroupEntity } returns group
      every { isOnline() } returns true
    }

    whenever(readChannelGroupByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(groupData))
    whenever(getGroupOnlineSummaryUseCase.invoke(groupId)).thenReturn(Maybe.just(GroupOnlineSummary(2, 4)))
  }
}
