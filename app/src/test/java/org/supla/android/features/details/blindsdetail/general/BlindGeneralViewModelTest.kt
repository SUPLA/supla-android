package org.supla.android.features.details.blindsdetail.general

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
import org.supla.android.core.networking.suplaclient.SuplaClientMessageHandlerWrapper
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.model.general.ChannelIssueItem
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.local.entity.custom.GroupOnlineSummary
import org.supla.android.data.source.remote.rollershutter.RollerShutterValue
import org.supla.android.data.source.remote.rollershutter.SuplaRollerShutterFlag
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.features.details.blindsdetail.ui.BlindRollerState
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.tools.VibrationHelper
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.ui.lists.data.IssueIconType
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.client.CallConfigCommandUseCase
import org.supla.android.usecases.client.ExecuteBlindsActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.client.SuplaConfigCommand
import org.supla.android.usecases.group.GetGroupOnlineSummaryUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase

@RunWith(MockitoJUnitRunner::class)
class BlindGeneralViewModelTest : BaseViewModelTest<BlindsGeneralModelState, BlindsGeneralViewEvent, BlindGeneralViewModel>() {

  @Mock
  lateinit var executeBlindsActionUseCase: ExecuteBlindsActionUseCase

  @Mock
  lateinit var executeSimpleActionUseCase: ExecuteSimpleActionUseCase

  @Mock
  lateinit var callConfigCommandUseCase: CallConfigCommandUseCase

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
  lateinit var suplaClientMessageHandlerWrapper: SuplaClientMessageHandlerWrapper

  @Mock
  override lateinit var schedulers: SuplaSchedulers

  @InjectMocks
  override lateinit var viewModel: BlindGeneralViewModel

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

    mockOnlineChannel(remoteId, position, bottomPosition, listOf(SuplaRollerShutterFlag.MOTOR_PROBLEM))

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)

    // then
    assertThat(states).containsExactly(
      BlindsGeneralModelState(
        remoteId = remoteId,
        rollerState = BlindRollerState(position.toFloat(), bottomPosition.toFloat()),
        viewState = BlindsGeneralViewState(
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
      BlindsGeneralModelState(
        remoteId = remoteId,
        rollerState = BlindRollerState(position = 0f, markers = listOf(100f, 100f, 20f)),
        viewState = BlindsGeneralViewState(
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
      BlindsGeneralModelState(
        remoteId = remoteId,
        rollerState = BlindRollerState(position = 25f, markers = emptyList()),
        viewState = BlindsGeneralViewState(
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
      every { channelValueEntity } returns value
    }
    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channelData))

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)

    // then
    assertThat(states).containsExactly(
      BlindsGeneralModelState(
        remoteId = remoteId,
        rollerState = BlindRollerState(25f, bottomPosition.toFloat()),
        viewState = BlindsGeneralViewState(
          issues = listOf(),
          enabled = false,
          showClosingPercentage = true,
          positionUnknown = true,
          calibrationPossible = true,
          calibrating = true,
          positionText = "0%"
        )
      )
    )
  }

  @Test
  fun `should reveal blinds`() {
    // given
    val remoteId = 122
    whenever(executeSimpleActionUseCase.invoke(ActionId.REVEAL, SubjectType.CHANNEL, remoteId))
      .thenReturn(Completable.complete())

    // when
    viewModel.handleAction(BlindsAction.Open, remoteId, ItemType.CHANNEL)

    // then
    verify(executeSimpleActionUseCase).invoke(ActionId.REVEAL, SubjectType.CHANNEL, remoteId)
    assertThat(states).isEmpty()
  }

  @Test
  fun `should shut blinds`() {
    // given
    val remoteId = 122
    whenever(executeSimpleActionUseCase.invoke(ActionId.SHUT, SubjectType.CHANNEL, remoteId))
      .thenReturn(Completable.complete())

    // when
    viewModel.handleAction(BlindsAction.Close, remoteId, ItemType.CHANNEL)

    // then
    verify(executeSimpleActionUseCase).invoke(ActionId.SHUT, SubjectType.CHANNEL, remoteId)
    assertThat(states).isEmpty()
  }

  @Test
  fun `should move up blinds`() {
    // given
    val remoteId = 122
    whenever(executeSimpleActionUseCase.invoke(ActionId.REVEAL, SubjectType.CHANNEL, remoteId))
      .thenReturn(Completable.complete())

    // when
    viewModel.handleAction(BlindsAction.MoveUp, remoteId, ItemType.CHANNEL)

    // then
    verify(executeSimpleActionUseCase).invoke(ActionId.REVEAL, SubjectType.CHANNEL, remoteId)
    assertThat(states).isEmpty()
  }

  @Test
  fun `should move up blinds and set start timer`() {
    // given
    val remoteId = 122
    val timestamp = 22232L
    whenever(executeSimpleActionUseCase.invoke(ActionId.REVEAL, SubjectType.CHANNEL, remoteId))
      .thenReturn(Completable.complete())
    whenever(dateProvider.currentTimestamp()).thenReturn(timestamp)
    mockOnlineChannel(remoteId, hasValidPosition = false)

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)
    viewModel.handleAction(BlindsAction.MoveUp, remoteId, ItemType.CHANNEL)

    // then
    verify(executeSimpleActionUseCase).invoke(ActionId.REVEAL, SubjectType.CHANNEL, remoteId)
    val state = BlindsGeneralModelState(
      remoteId = remoteId,
      rollerState = BlindRollerState(0f),
      viewState = BlindsGeneralViewState(
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
  fun `should move down blinds`() {
    // given
    val remoteId = 122
    whenever(executeSimpleActionUseCase.invoke(ActionId.SHUT, SubjectType.CHANNEL, remoteId))
      .thenReturn(Completable.complete())

    // when
    viewModel.handleAction(BlindsAction.MoveDown, remoteId, ItemType.CHANNEL)

    // then
    verify(executeSimpleActionUseCase).invoke(ActionId.SHUT, SubjectType.CHANNEL, remoteId)
    assertThat(states).isEmpty()
  }

  @Test
  fun `should move down blinds and set start time`() {
    // given
    val remoteId = 122
    val timestamp = 22232L
    whenever(executeSimpleActionUseCase.invoke(ActionId.SHUT, SubjectType.CHANNEL, remoteId))
      .thenReturn(Completable.complete())
    whenever(dateProvider.currentTimestamp()).thenReturn(timestamp)
    mockOnlineChannel(remoteId, hasValidPosition = false)

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)
    viewModel.handleAction(BlindsAction.MoveDown, remoteId, ItemType.CHANNEL)

    // then
    verify(executeSimpleActionUseCase).invoke(ActionId.SHUT, SubjectType.CHANNEL, remoteId)
    val state = BlindsGeneralModelState(
      remoteId = remoteId,
      rollerState = BlindRollerState(0f),
      viewState = BlindsGeneralViewState(
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
  fun `should stop blinds`() {
    // given
    val remoteId = 122
    whenever(executeSimpleActionUseCase.invoke(ActionId.STOP, SubjectType.CHANNEL, remoteId))
      .thenReturn(Completable.complete())

    // when
    viewModel.handleAction(BlindsAction.Stop, remoteId, ItemType.CHANNEL)

    // then
    verify(executeSimpleActionUseCase).invoke(ActionId.STOP, SubjectType.CHANNEL, remoteId)
    assertThat(states).isEmpty()
  }

  @Test
  fun `should move down blinds then stop and calculate time`() {
    // given
    val remoteId = 122
    val timestamp = 22232L
    val afterTimestamp = 25232L
    whenever(executeSimpleActionUseCase.invoke(ActionId.SHUT, SubjectType.CHANNEL, remoteId))
      .thenReturn(Completable.complete())
    whenever(executeSimpleActionUseCase.invoke(ActionId.STOP, SubjectType.CHANNEL, remoteId))
      .thenReturn(Completable.complete())
    whenever(dateProvider.currentTimestamp()).thenReturn(timestamp, afterTimestamp)
    mockOnlineChannel(remoteId, hasValidPosition = false)

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)
    viewModel.handleAction(BlindsAction.MoveDown, remoteId, ItemType.CHANNEL)
    viewModel.handleAction(BlindsAction.Stop, remoteId, ItemType.CHANNEL)

    // then
    verify(executeSimpleActionUseCase).invoke(ActionId.SHUT, SubjectType.CHANNEL, remoteId)
    verify(executeSimpleActionUseCase).invoke(ActionId.STOP, SubjectType.CHANNEL, remoteId)
    val state = BlindsGeneralModelState(
      remoteId = remoteId,
      rollerState = BlindRollerState(0f),
      viewState = BlindsGeneralViewState(
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
      state.copy(moveStartTime = timestamp),
      state.copy(moveStartTime = null, viewState = state.viewState.copy(touchTime = 3f))
    )
  }

  @Test
  fun `should open calibration dialog`() {
    // given
    val remoteId = 122

    // when
    viewModel.handleAction(BlindsAction.Calibrate, remoteId, ItemType.CHANNEL)

    // then
    assertThat(states).containsExactly(
      BlindsGeneralModelState(showCalibrationDialog = true)
    )
    verifyZeroInteractions(executeSimpleActionUseCase, executeBlindsActionUseCase)
  }

  @Test
  fun `should open blinds at specified position`() {
    // given
    val remoteId = 122
    whenever(executeBlindsActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 45f))
      .thenReturn(Completable.complete())

    // when
    viewModel.handleAction(BlindsAction.OpenAt(45f), remoteId, ItemType.CHANNEL)

    // then
    verify(executeBlindsActionUseCase).invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 45f)
    assertThat(states).isEmpty()
  }

  @Test
  fun `should move roller view to position`() {
    // given
    val remoteId = 122

    // when
    viewModel.handleAction(BlindsAction.MoveTo(45f), remoteId, ItemType.CHANNEL)

    // then
    assertThat(states).containsExactly(
      BlindsGeneralModelState(rollerState = BlindRollerState(45f))
    )
    verifyZeroInteractions(executeBlindsActionUseCase, executeSimpleActionUseCase)
  }

  @Test
  fun `shouldn't change roller position when calibrating`() {
    // given
    val remoteId = 122
    mockOnlineChannel(remoteId, flags = listOf(SuplaRollerShutterFlag.CALIBRATION_IN_PROGRESS))

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)
    viewModel.handleAction(BlindsAction.OpenAt(45f), remoteId, ItemType.CHANNEL)

    // then
    val state = BlindsGeneralModelState(
      remoteId = remoteId,
      rollerState = BlindRollerState(0f),
      viewState = BlindsGeneralViewState(
        issues = emptyList(),
        enabled = true,
        showClosingPercentage = true,
        calibrationPossible = true,
        calibrating = true,
        positionText = "0%"
      )
    )
    assertThat(states).containsExactly(state)
    verifyZeroInteractions(executeBlindsActionUseCase, executeSimpleActionUseCase)
  }

  @Test
  fun `shouldn't change roller position on view when calibrating`() {
    // given
    val remoteId = 122
    mockOnlineChannel(remoteId, flags = listOf(SuplaRollerShutterFlag.CALIBRATION_IN_PROGRESS))

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)
    viewModel.handleAction(BlindsAction.MoveTo(45f), remoteId, ItemType.CHANNEL)

    // then
    val state = BlindsGeneralModelState(
      remoteId = remoteId,
      rollerState = BlindRollerState(0f),
      viewState = BlindsGeneralViewState(
        issues = emptyList(),
        enabled = true,
        showClosingPercentage = true,
        calibrationPossible = true,
        calibrating = true,
        positionText = "0%"
      )
    )
    assertThat(states).containsExactly(state)
    verifyZeroInteractions(executeBlindsActionUseCase, executeSimpleActionUseCase)
  }

  @Test
  fun `should close calibration dialog`() {
    // given
    val remoteId = 122

    // when
    viewModel.handleAction(BlindsAction.Calibrate, remoteId, ItemType.CHANNEL)
    viewModel.cancelCalibration()

    // then
    assertThat(states).containsExactly(
      BlindsGeneralModelState(showCalibrationDialog = true),
      BlindsGeneralModelState(showCalibrationDialog = false)
    )
    verifyZeroInteractions(executeSimpleActionUseCase, executeBlindsActionUseCase)
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
    viewModel.handleAction(BlindsAction.Calibrate, remoteId, ItemType.CHANNEL)
    viewModel.startCalibration()

    // then
    assertThat(states).containsExactly(
      BlindsGeneralModelState(showCalibrationDialog = true),
      BlindsGeneralModelState(showCalibrationDialog = false),
      BlindsGeneralModelState(
        authorizationDialogState = AuthorizationDialogState(
          userName = "some-email@supla.org",
          isCloudAccount = true,
          userNameEnabled = false
        )
      )
    )
    verify(profileRepository).findActiveProfile()
    verifyZeroInteractions(executeSimpleActionUseCase, executeBlindsActionUseCase)
    verifyNoMoreInteractions(profileRepository)
  }

  @Test
  fun `should start calibration when user authorized`() {
    // given
    val remoteId = 122
    mockOnlineChannel(remoteId)
    whenever(callConfigCommandUseCase.invoke(remoteId, ItemType.CHANNEL, SuplaConfigCommand.RECALIBRATE))
      .thenReturn(Completable.complete())

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)
    viewModel.onAuthorized()

    // then
    val state = BlindsGeneralModelState(
      remoteId = remoteId,
      rollerState = BlindRollerState(0f),
      viewState = BlindsGeneralViewState(
        issues = emptyList(),
        enabled = true,
        showClosingPercentage = true,
        calibrationPossible = true,
        positionUnknown = false,
        positionText = "0%"
      )
    )
    assertThat(states).containsExactly(state)
    verify(callConfigCommandUseCase).invoke(remoteId, ItemType.CHANNEL, SuplaConfigCommand.RECALIBRATE)
    verifyZeroInteractions(executeBlindsActionUseCase, executeSimpleActionUseCase)
  }

  private fun mockOnlineChannel(
    remoteId: Int,
    position: Int = 0,
    bottomPosition: Int = 100,
    flags: List<SuplaRollerShutterFlag> = listOf(),
    hasValidPosition: Boolean = true
  ) {
    val rollerShutterValue: RollerShutterValue = mockk {
      every { this@mockk.position } returns position
      every { this@mockk.bottomPosition } returns bottomPosition
      every { this@mockk.flags } returns flags
      every { hasValidPosition() } returns hasValidPosition
      every { online } returns true
    }
    val value: ChannelValueEntity = mockk {
      every { asRollerShutterValue() } returns rollerShutterValue
    }
    val channelData: ChannelDataEntity = mockk {
      every { this@mockk.remoteId } returns remoteId
      every { channelValueEntity } returns value
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
      every { channelGroupEntity } returns group
      every { isOnline() } returns true
    }

    whenever(readChannelGroupByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(groupData))
    whenever(getGroupOnlineSummaryUseCase.invoke(groupId)).thenReturn(Maybe.just(GroupOnlineSummary(2, 4)))
  }
}
