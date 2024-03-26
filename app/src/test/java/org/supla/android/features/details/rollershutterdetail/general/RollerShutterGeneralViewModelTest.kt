package org.supla.android.features.details.rollershutterdetail.general
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
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.model.general.ChannelIssueItem
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.local.entity.custom.GroupOnlineSummary
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.data.source.remote.rollershutter.RollerShutterValue
import org.supla.android.data.source.remote.rollershutter.SuplaRollerShutterFlag
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.features.details.rollershutterdetail.general.ui.WindowState
import org.supla.android.features.details.rollershutterdetail.general.ui.WindowType
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.tools.VibrationHelper
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.ui.lists.data.IssueIconType
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.CallSuplaClientOperationUseCase
import org.supla.android.usecases.client.ExecuteRollerShutterActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.android.usecases.client.SuplaClientOperation
import org.supla.android.usecases.group.GetGroupOnlineSummaryUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase

@RunWith(MockitoJUnitRunner::class)
class RollerShutterGeneralViewModelTest :
  BaseViewModelTest<RollerShutterGeneralModelState, RollerShutterGeneralViewEvent, RollerShutterGeneralViewModel>() {

  @Mock
  lateinit var executeRollerShutterActionUseCase: ExecuteRollerShutterActionUseCase

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
  override lateinit var viewModel: RollerShutterGeneralViewModel

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
      listOf(SuplaRollerShutterFlag.MOTOR_PROBLEM),
      listOf(SuplaChannelFlag.CALCFG_RECALIBRATE)
    )

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)

    // then
    assertThat(states).containsExactly(
      RollerShutterGeneralModelState(
        remoteId = remoteId,
        rollerState = WindowState(position.toFloat(), bottomPosition.toFloat()),
        viewState = RollerShutterGeneralViewState(
          issues = listOf(ChannelIssueItem(IssueIconType.ERROR, R.string.motor_problem)),
          enabled = true,
          showClosingPercentage = true,
          positionUnknown = false,
          calibrationPossible = true,
          calibrating = false,
          positionText = "33%"
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
      RollerShutterGeneralModelState(
        remoteId = remoteId,
        rollerState = WindowState(position = 0f, markers = listOf(100f, 100f, 20f)),
        viewState = RollerShutterGeneralViewState(
          enabled = true,
          showClosingPercentage = true,
          positionUnknown = false,
          calibrationPossible = false,
          calibrating = false,
          isGroup = true,
          onlineStatusString = "2/4",
          positionText = "20% - 100%"
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
      RollerShutterGeneralModelState(
        remoteId = remoteId,
        rollerState = WindowState(position = 25f, markers = emptyList()),
        viewState = RollerShutterGeneralViewState(
          enabled = true,
          showClosingPercentage = true,
          positionUnknown = false,
          calibrationPossible = false,
          calibrating = false,
          isGroup = true,
          onlineStatusString = "2/4",
          positionText = "25%"
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
      every { flags } returns listOf(SuplaRollerShutterFlag.CALIBRATION_IN_PROGRESS)
      every { hasValidPosition() } returns false
      every { online } returns false
    }
    val value: ChannelValueEntity = mockk {
      every { asRollerShutterValue() } returns rollerShutterValue
    }
    val channelData: ChannelDataEntity = mockk {
      every { this@mockk.remoteId } returns remoteId
      every { function } returns SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW
      every { channelValueEntity } returns value
      every { flags } returns 0
    }
    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channelData))

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)

    // then
    assertThat(states).containsExactly(
      RollerShutterGeneralModelState(
        remoteId = remoteId,
        rollerState = WindowState(25f, bottomPosition.toFloat()),
        viewState = RollerShutterGeneralViewState(
          issues = listOf(),
          enabled = false,
          showClosingPercentage = true,
          positionUnknown = true,
          calibrating = true,
          positionText = "0%",
          windowType = WindowType.ROOF_WINDOW
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
    viewModel.handleAction(RollerShutterAction.Open, remoteId, ItemType.CHANNEL)

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
    viewModel.handleAction(RollerShutterAction.Close, remoteId, ItemType.CHANNEL)

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
    viewModel.handleAction(RollerShutterAction.MoveUp, remoteId, ItemType.GROUP)

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
    viewModel.handleAction(RollerShutterAction.MoveUp, remoteId, ItemType.CHANNEL)

    // then
    verify(callSuplaClientOperationUseCase).invoke(remoteId, ItemType.CHANNEL, SuplaClientOperation.MoveUp)
    val state = RollerShutterGeneralModelState(
      remoteId = remoteId,
      rollerState = WindowState(0f),
      viewState = RollerShutterGeneralViewState(
        issues = emptyList(),
        enabled = true,
        showClosingPercentage = true,
        positionUnknown = true,
        positionText = "0%"
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
    viewModel.handleAction(RollerShutterAction.MoveDown, remoteId, ItemType.CHANNEL)

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
    viewModel.handleAction(RollerShutterAction.MoveDown, remoteId, ItemType.CHANNEL)

    // then
    verify(callSuplaClientOperationUseCase).invoke(remoteId, ItemType.CHANNEL, SuplaClientOperation.MoveDown)
    val state = RollerShutterGeneralModelState(
      remoteId = remoteId,
      rollerState = WindowState(0f),
      viewState = RollerShutterGeneralViewState(
        issues = emptyList(),
        enabled = true,
        showClosingPercentage = true,
        calibrationPossible = true,
        positionUnknown = true,
        positionText = "0%"
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
    viewModel.handleAction(RollerShutterAction.Stop, remoteId, ItemType.CHANNEL)

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
    viewModel.handleAction(RollerShutterAction.MoveDown, remoteId, ItemType.CHANNEL)
    viewModel.handleAction(RollerShutterAction.Stop, remoteId, ItemType.CHANNEL)

    // then
    verify(callSuplaClientOperationUseCase).invoke(remoteId, ItemType.CHANNEL, SuplaClientOperation.MoveDown)
    verify(executeSimpleActionUseCase).invoke(ActionId.STOP, SubjectType.CHANNEL, remoteId)
    val state = RollerShutterGeneralModelState(
      remoteId = remoteId,
      rollerState = WindowState(0f),
      viewState = RollerShutterGeneralViewState(
        issues = emptyList(),
        enabled = true,
        showClosingPercentage = true,
        positionUnknown = true,
        positionText = "0%"
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
    viewModel.handleAction(RollerShutterAction.Calibrate, remoteId, ItemType.CHANNEL)

    // then
    assertThat(states).containsExactly(
      RollerShutterGeneralModelState(showCalibrationDialog = true)
    )
    verifyZeroInteractions(executeSimpleActionUseCase, executeRollerShutterActionUseCase)
  }

  @Test
  fun `should open roller shutter at specified position`() {
    // given
    val remoteId = 122
    whenever(executeRollerShutterActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 45f))
      .thenReturn(Completable.complete())

    // when
    viewModel.handleAction(RollerShutterAction.OpenAt(45f), remoteId, ItemType.CHANNEL)

    // then
    verify(executeRollerShutterActionUseCase).invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 45f)
    assertThat(states).isEmpty()
  }

  @Test
  fun `should move roller view to position`() {
    // given
    val remoteId = 122

    // when
    viewModel.handleAction(RollerShutterAction.MoveTo(45f), remoteId, ItemType.CHANNEL)

    // then
    assertThat(states).containsExactly(
      RollerShutterGeneralModelState(
        rollerState = WindowState(45f),
        viewState = RollerShutterGeneralViewState(positionText = "55%"),
        manualMoving = true
      )
    )
    verifyZeroInteractions(executeRollerShutterActionUseCase, executeSimpleActionUseCase)
  }

  @Test
  fun `shouldn't change roller position when calibrating`() {
    // given
    val remoteId = 122
    mockOnlineChannel(remoteId, valueFlags = listOf(SuplaRollerShutterFlag.CALIBRATION_IN_PROGRESS))

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)
    viewModel.handleAction(RollerShutterAction.OpenAt(45f), remoteId, ItemType.CHANNEL)

    // then
    val state = RollerShutterGeneralModelState(
      remoteId = remoteId,
      rollerState = WindowState(0f),
      viewState = RollerShutterGeneralViewState(
        issues = emptyList(),
        enabled = true,
        showClosingPercentage = true,
        calibrating = true,
        positionText = "0%"
      )
    )
    assertThat(states).containsExactly(state)
    verifyZeroInteractions(executeRollerShutterActionUseCase, executeSimpleActionUseCase)
  }

  @Test
  fun `shouldn't change roller position on view when calibrating`() {
    // given
    val remoteId = 122
    mockOnlineChannel(remoteId, valueFlags = listOf(SuplaRollerShutterFlag.CALIBRATION_IN_PROGRESS))

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)
    viewModel.handleAction(RollerShutterAction.MoveTo(45f), remoteId, ItemType.CHANNEL)

    // then
    val state = RollerShutterGeneralModelState(
      remoteId = remoteId,
      rollerState = WindowState(0f),
      viewState = RollerShutterGeneralViewState(
        issues = emptyList(),
        enabled = true,
        showClosingPercentage = true,
        calibrating = true,
        positionText = "0%"
      )
    )
    assertThat(states).containsExactly(state)
    verifyZeroInteractions(executeRollerShutterActionUseCase, executeSimpleActionUseCase)
  }

  @Test
  fun `should close calibration dialog`() {
    // given
    val remoteId = 122

    // when
    viewModel.handleAction(RollerShutterAction.Calibrate, remoteId, ItemType.CHANNEL)
    viewModel.cancelCalibration()

    // then
    assertThat(states).containsExactly(
      RollerShutterGeneralModelState(showCalibrationDialog = true),
      RollerShutterGeneralModelState(showCalibrationDialog = false)
    )
    verifyZeroInteractions(executeSimpleActionUseCase, executeRollerShutterActionUseCase)
  }

  @Test
  fun `should show authorization dialog`() {
    // given
    val remoteId = 122

    val profile: ProfileEntity = mockk {
      every { email } returns "some-email@supla.org"
      every { serverForEmail } returns "cloud.supla.org"
    }
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.just(profile))

    // when
    viewModel.handleAction(RollerShutterAction.Calibrate, remoteId, ItemType.CHANNEL)
    viewModel.startCalibration()

    // then
    assertThat(states).containsExactly(
      RollerShutterGeneralModelState(showCalibrationDialog = true),
      RollerShutterGeneralModelState(showCalibrationDialog = false),
      RollerShutterGeneralModelState(
        authorizationDialogState = AuthorizationDialogState(
          userName = "some-email@supla.org",
          isCloudAccount = true,
          userNameEnabled = false
        )
      )
    )
    verify(profileRepository).findActiveProfile()
    verifyZeroInteractions(executeSimpleActionUseCase, executeRollerShutterActionUseCase)
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
    val state = RollerShutterGeneralModelState(
      remoteId = remoteId,
      rollerState = WindowState(0f),
      viewState = RollerShutterGeneralViewState(
        issues = emptyList(),
        enabled = true,
        showClosingPercentage = true,
        positionUnknown = false,
        positionText = "0%"
      )
    )
    assertThat(states).containsExactly(state)
    verify(callSuplaClientOperationUseCase).invoke(remoteId, ItemType.CHANNEL, SuplaClientOperation.Command.Recalibrate)
    verifyZeroInteractions(executeRollerShutterActionUseCase, executeSimpleActionUseCase)
  }

  private fun mockOnlineChannel(
    remoteId: Int,
    position: Int = 0,
    bottomPosition: Int = 100,
    valueFlags: List<SuplaRollerShutterFlag> = emptyList(),
    channelFlags: List<SuplaChannelFlag> = emptyList(),
    hasValidPosition: Boolean = true,
    function: Int = SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
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
      every { this@mockk.totalValue } returns totalValue
    }
    val groupData: ChannelGroupDataEntity = mockk {
      every { id } returns groupId
      every { this@mockk.remoteId } returns remoteId
      every { this@mockk.function } returns SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
      every { channelGroupEntity } returns group
      every { isOnline() } returns true
    }

    whenever(readChannelGroupByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(groupData))
    whenever(getGroupOnlineSummaryUseCase.invoke(groupId)).thenReturn(Maybe.just(GroupOnlineSummary(2, 4)))
  }
}