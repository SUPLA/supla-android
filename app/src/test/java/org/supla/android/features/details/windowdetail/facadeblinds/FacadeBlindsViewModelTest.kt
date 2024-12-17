package org.supla.android.features.details.windowdetail.facadeblinds
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
import io.reactivex.rxjava3.core.Observable
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
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.local.entity.custom.GroupOnlineSummary
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.ConfigResult
import org.supla.android.data.source.remote.rollershutter.SuplaChannelFacadeBlindConfig
import org.supla.android.data.source.remote.rollershutter.SuplaTiltControlType
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.features.details.windowdetail.base.BaseWindowViewEvent
import org.supla.android.features.details.windowdetail.base.data.ShadingBlindMarker
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValueFormat
import org.supla.android.features.details.windowdetail.base.data.facadeblinds.FacadeBlindWindowState
import org.supla.android.features.details.windowdetail.base.ui.ShadingSystemAction
import org.supla.android.features.details.windowdetail.base.ui.ShadingSystemPositionPresentation
import org.supla.android.features.details.windowdetail.base.ui.WindowViewState
import org.supla.android.lib.SuplaConst
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.CallSuplaClientOperationUseCase
import org.supla.android.usecases.client.ExecuteShadingSystemActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.android.usecases.group.GetGroupOnlineSummaryUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.group.ReadGroupTiltingDetailsUseCase
import org.supla.android.usecases.group.TiltingDetails
import org.supla.android.usecases.group.totalvalue.ShadowingBlindGroupValue
import org.supla.core.shared.data.model.function.facadeblind.FacadeBlindValue
import org.supla.core.shared.data.model.shadingsystem.SuplaShadingSystemFlag

@RunWith(MockitoJUnitRunner::class)
class FacadeBlindsViewModelTest : BaseViewModelTest<FacadeBlindsViewModelState, BaseWindowViewEvent, FacadeBlindsViewModel>() {

  @Mock
  private lateinit var channelConfigEventsManager: ChannelConfigEventsManager

  @Mock
  private lateinit var executeShadingSystemActionUseCase: ExecuteShadingSystemActionUseCase

  @Mock
  private lateinit var readGroupTiltingDetailsUseCase: ReadGroupTiltingDetailsUseCase

  @Mock
  private lateinit var suplaClientProvider: SuplaClientProvider

  @Mock
  private lateinit var executeSimpleActionUseCase: ExecuteSimpleActionUseCase

  @Mock
  private lateinit var callSuplaClientOperationUseCase: CallSuplaClientOperationUseCase

  @Mock
  private lateinit var readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase

  @Mock
  private lateinit var readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase

  @Mock
  private lateinit var getGroupOnlineSummaryUseCase: GetGroupOnlineSummaryUseCase

  @Mock
  private lateinit var preferences: Preferences

  @Mock
  private lateinit var dateProvider: DateProvider

  @Mock
  private lateinit var profileRepository: RoomProfileRepository

  @Mock
  private lateinit var loginUseCase: LoginUseCase

  @Mock
  private lateinit var authorizeUseCase: AuthorizeUseCase

  @Mock
  override lateinit var schedulers: SuplaSchedulers

  @InjectMocks
  override lateinit var viewModel: FacadeBlindsViewModel

  @Before
  override fun setUp() {
    super.setUp()
  }

  @Test
  fun `should load channel`() {
    // given
    val remoteId = 123
    val position = 10
    val tilt = 33
    val channelData = mockChannel(remoteId, position, tilt)

    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channelData))
    whenever(preferences.isShowOpeningPercent).thenReturn(false)

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)

    // then
    assertThat(states).containsExactly(
      FacadeBlindsViewModelState(
        remoteId = remoteId,
        windowState = FacadeBlindWindowState(
          position = WindowGroupedValue.Similar(position.toFloat()),
          slatTilt = WindowGroupedValue.Similar(tilt.toFloat()),
          positionTextFormat = WindowGroupedValueFormat.PERCENTAGE
        ),
        viewState = WindowViewState(
          enabled = true,
          positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED
        ),
        lastPosition = position
      )
    )
  }

  @Test
  fun `should load channel default position and tilt when channel is offline`() {
    // given
    val remoteId = 123
    val position = 10
    val tilt = 33
    val channelData = mockChannel(remoteId, position, tilt, online = false)

    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channelData))
    whenever(preferences.isShowOpeningPercent).thenReturn(false)

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)

    // then
    assertThat(states).containsExactly(
      FacadeBlindsViewModelState(
        remoteId = remoteId,
        windowState = FacadeBlindWindowState(
          position = WindowGroupedValue.Similar(25f),
          slatTilt = WindowGroupedValue.Similar(50f),
          positionTextFormat = WindowGroupedValueFormat.PERCENTAGE
        ),
        viewState = WindowViewState(
          enabled = false,
          positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED
        ),
        lastPosition = position
      )
    )
  }

  @Test
  fun `should load channel without tilt`() {
    // given
    val remoteId = 123
    val position = 10
    val tilt = 33
    val channelData = mockChannel(remoteId, position, tilt, flags = emptyList())

    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channelData))
    whenever(preferences.isShowOpeningPercent).thenReturn(false)

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL)

    // then
    assertThat(states).containsExactly(
      FacadeBlindsViewModelState(
        remoteId = remoteId,
        windowState = FacadeBlindWindowState(
          position = WindowGroupedValue.Similar(10f),
          slatTilt = null,
          positionTextFormat = WindowGroupedValueFormat.PERCENTAGE
        ),
        viewState = WindowViewState(
          enabled = true,
          positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED
        ),
        lastPosition = position
      )
    )
  }

  @Test
  fun `should load config`() {
    // given
    val remoteId = 123
    val config = mockConfig()

    whenever(channelConfigEventsManager.observerConfig(remoteId))
      .thenReturn(Observable.just(ChannelConfigEventsManager.ConfigEvent(ConfigResult.RESULT_TRUE, config)))

    // when
    viewModel.observeConfig(remoteId, ItemType.CHANNEL)

    // then
    assertThat(states).containsExactly(
      FacadeBlindsViewModelState(
        windowState = FacadeBlindWindowState(
          position = WindowGroupedValue.Similar(0f),
          tilt0Angle = 0f,
          tilt100Angle = 180f,
        ),
        tiltControlType = SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING,
        tiltingTime = 2000,
        openingTime = 20000,
        closingTime = 20000
      )
    )
  }

  @Test
  fun `should change tilt when tilting changes position`() {
    // given
    val remoteId = 123
    val initialState = FacadeBlindsViewModelState(
      windowState = FacadeBlindWindowState(
        position = WindowGroupedValue.Similar(10f),
        slatTilt = WindowGroupedValue.Similar(33f)
      ),
      tiltControlType = SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING,
      tiltingTime = 2000,
      openingTime = 20000,
      closingTime = 20000,
      lastPosition = 10
    )
    viewModel.setState(initialState)

    // when
    viewModel.handleAction(ShadingSystemAction.TiltTo(95f), remoteId, ItemType.CHANNEL)

    // then
    val finalState = initialState.copy(
      windowState = initialState.windowState.copy(
        slatTilt = WindowGroupedValue.Similar(95f)
      ),
      manualMoving = true
    )
    assertThat(states).containsExactly(initialState, finalState)
  }

  @Test
  fun `should change tilt and position when tilts only when fully closed`() {
    // given
    val remoteId = 123
    val initialState = FacadeBlindsViewModelState(
      windowState = FacadeBlindWindowState(
        position = WindowGroupedValue.Similar(10f),
        slatTilt = WindowGroupedValue.Similar(33f)
      ),
      tiltControlType = SuplaTiltControlType.TILTS_ONLY_WHEN_FULLY_CLOSED,
      tiltingTime = 2000,
      openingTime = 20000,
      closingTime = 20000,
      lastPosition = 10
    )
    viewModel.setState(initialState)

    // when
    viewModel.handleAction(ShadingSystemAction.TiltTo(95f), remoteId, ItemType.CHANNEL)

    // then
    val finalState = initialState.copy(
      windowState = initialState.windowState.copy(
        position = WindowGroupedValue.Similar(100f),
        slatTilt = WindowGroupedValue.Similar(95f)
      ),
      manualMoving = true
    )
    assertThat(states).containsExactly(initialState, finalState)
  }

  @Test
  fun `should set tilt`() {
    // given
    val remoteId = 123

    whenever(executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, tilt = 95f))
      .thenReturn(Completable.complete())

    // when
    viewModel.handleAction(ShadingSystemAction.TiltTo(95f), remoteId, ItemType.CHANNEL)
    viewModel.handleAction(ShadingSystemAction.TiltSetTo(95f), remoteId, ItemType.CHANNEL)

    // then
    val state1 = FacadeBlindsViewModelState(
      windowState = FacadeBlindWindowState(
        position = WindowGroupedValue.Similar(0f),
        slatTilt = WindowGroupedValue.Similar(95f),
        positionTextFormat = WindowGroupedValueFormat.PERCENTAGE
      ),
      manualMoving = true
    )
    val state2 = state1.copy(
      manualMoving = false
    )
    assertThat(states).containsExactly(state1, state2)

    verify(executeShadingSystemActionUseCase).invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, tilt = 95f)
    verifyNoMoreInteractions(executeShadingSystemActionUseCase)
  }

  @Test
  fun `should change position and tilt when tilting changes position`() {
    // given
    val remoteId = 123
    val initialState = FacadeBlindsViewModelState(
      windowState = FacadeBlindWindowState(
        position = WindowGroupedValue.Similar(10f),
        slatTilt = WindowGroupedValue.Similar(33f)
      ),
      tiltControlType = SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING,
      tiltingTime = 2000,
      openingTime = 20000,
      closingTime = 20000,
      lastPosition = 10
    )
    viewModel.setState(initialState)

    // when
    viewModel.handleAction(ShadingSystemAction.MoveAndTiltTo(75f, 50f), remoteId, ItemType.CHANNEL)

    // then
    val finalState = initialState.copy(
      windowState = initialState.windowState.copy(
        position = WindowGroupedValue.Similar(75f),
        slatTilt = WindowGroupedValue.Similar(50f)
      ),
      manualMoving = true
    )
    assertThat(states).containsExactly(initialState, finalState)
  }

  @Test
  fun `should change position and correct tilt when tilts changes position (low position)`() {
    // given
    val remoteId = 123
    val initialState = FacadeBlindsViewModelState(
      windowState = FacadeBlindWindowState(
        position = WindowGroupedValue.Similar(10f),
        slatTilt = WindowGroupedValue.Similar(33f)
      ),
      tiltControlType = SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING,
      tiltingTime = 2000,
      openingTime = 20000,
      closingTime = 20000,
      lastPosition = 10
    )
    viewModel.setState(initialState)

    // when
    viewModel.handleAction(ShadingSystemAction.MoveAndTiltTo(95f, 0f), remoteId, ItemType.CHANNEL)

    // then
    val finalState = initialState.copy(
      windowState = initialState.windowState.copy(
        position = WindowGroupedValue.Similar(95f),
        slatTilt = WindowGroupedValue.Similar(50f)
      ),
      manualMoving = true
    )
    assertThat(states).containsExactly(initialState, finalState)
  }

  @Test
  fun `should change position and correct tilt when tilts changes position (up position)`() {
    // given
    val remoteId = 123
    val initialState = FacadeBlindsViewModelState(
      windowState = FacadeBlindWindowState(
        position = WindowGroupedValue.Similar(10f),
        slatTilt = WindowGroupedValue.Similar(33f)
      ),
      tiltControlType = SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING,
      tiltingTime = 2000,
      openingTime = 20000,
      closingTime = 20000,
      lastPosition = 10
    )
    viewModel.setState(initialState)

    // when
    viewModel.handleAction(ShadingSystemAction.MoveAndTiltTo(2.5f, 80f), remoteId, ItemType.CHANNEL)

    // then
    val finalState = initialState.copy(
      windowState = initialState.windowState.copy(
        position = WindowGroupedValue.Similar(2.5f),
        slatTilt = WindowGroupedValue.Similar(25f)
      ),
      manualMoving = true
    )
    assertThat(states).containsExactly(initialState, finalState)
  }

  @Test
  fun `should change position without tilt when tilting only when fully closed and not closed`() {
    // given
    val remoteId = 123
    val initialState = FacadeBlindsViewModelState(
      windowState = FacadeBlindWindowState(
        position = WindowGroupedValue.Similar(10f),
        slatTilt = WindowGroupedValue.Similar(0f)
      ),
      tiltControlType = SuplaTiltControlType.TILTS_ONLY_WHEN_FULLY_CLOSED,
      tiltingTime = 2000,
      openingTime = 20000,
      closingTime = 20000,
      lastPosition = 10
    )
    viewModel.setState(initialState)

    // when
    viewModel.handleAction(ShadingSystemAction.MoveAndTiltTo(50f, 80f), remoteId, ItemType.CHANNEL)

    // then
    val finalState = initialState.copy(
      windowState = initialState.windowState.copy(
        position = WindowGroupedValue.Similar(50f),
      ),
      manualMoving = true
    )
    assertThat(states).containsExactly(initialState, finalState)
  }

  @Test
  fun `should change position and tilt when tilting only when fully closed and closed`() {
    // given
    val remoteId = 123
    val initialState = FacadeBlindsViewModelState(
      windowState = FacadeBlindWindowState(
        position = WindowGroupedValue.Similar(10f),
        slatTilt = WindowGroupedValue.Similar(0f)
      ),
      tiltControlType = SuplaTiltControlType.TILTS_ONLY_WHEN_FULLY_CLOSED,
      tiltingTime = 2000,
      openingTime = 20000,
      closingTime = 20000,
      lastPosition = 10
    )
    viewModel.setState(initialState)

    // when
    viewModel.handleAction(ShadingSystemAction.MoveAndTiltTo(100f, 80f), remoteId, ItemType.CHANNEL)

    // then
    val finalState = initialState.copy(
      windowState = initialState.windowState.copy(
        position = WindowGroupedValue.Similar(100f),
        slatTilt = WindowGroupedValue.Similar(80f)
      ),
      manualMoving = true
    )
    assertThat(states).containsExactly(initialState, finalState)
  }

  @Test
  fun `should set position and tilt when tilting changes position`() {
    // given
    val remoteId = 123
    val initialState = FacadeBlindsViewModelState(
      windowState = FacadeBlindWindowState(
        position = WindowGroupedValue.Similar(10f),
        slatTilt = WindowGroupedValue.Similar(33f)
      ),
      tiltControlType = SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING,
      tiltingTime = 2000,
      openingTime = 20000,
      closingTime = 20000,
      lastPosition = 10,
      manualMoving = true
    )
    viewModel.setState(initialState)
    whenever(executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, percentage = 75f, tilt = 50f))
      .thenReturn(Completable.complete())

    // when
    viewModel.handleAction(ShadingSystemAction.MoveAndTiltSetTo(75f, 50f), remoteId, ItemType.CHANNEL)

    // then
    val finalState = initialState.copy(manualMoving = false)
    assertThat(states).containsExactly(initialState, finalState)

    verify(executeShadingSystemActionUseCase).invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, percentage = 75f, tilt = 50f)
  }

  @Test
  fun `should set position and correct tilt when tilts changes position (low position)`() {
    // given
    val remoteId = 123
    val initialState = FacadeBlindsViewModelState(
      windowState = FacadeBlindWindowState(
        position = WindowGroupedValue.Similar(10f),
        slatTilt = WindowGroupedValue.Similar(33f)
      ),
      tiltControlType = SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING,
      tiltingTime = 2000,
      openingTime = 20000,
      closingTime = 20000,
      lastPosition = 10,
      manualMoving = true
    )
    viewModel.setState(initialState)
    whenever(executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, percentage = 95f, tilt = 50f))
      .thenReturn(Completable.complete())

    // when
    viewModel.handleAction(ShadingSystemAction.MoveAndTiltSetTo(95f, 0f), remoteId, ItemType.CHANNEL)

    // then
    val finalState = initialState.copy(
      manualMoving = false
    )
    assertThat(states).containsExactly(initialState, finalState)

    verify(executeShadingSystemActionUseCase).invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, percentage = 95f, tilt = 50f)
  }

  @Test
  fun `should set position and correct tilt when tilts changes position (up position)`() {
    // given
    val remoteId = 123
    val initialState = FacadeBlindsViewModelState(
      windowState = FacadeBlindWindowState(
        position = WindowGroupedValue.Similar(10f),
        slatTilt = WindowGroupedValue.Similar(33f)
      ),
      tiltControlType = SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING,
      tiltingTime = 2000,
      openingTime = 20000,
      closingTime = 20000,
      lastPosition = 10,
      manualMoving = true
    )
    viewModel.setState(initialState)
    whenever(executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 2.5f, tilt = 25f))
      .thenReturn(Completable.complete())

    // when
    viewModel.handleAction(ShadingSystemAction.MoveAndTiltSetTo(2.5f, 80f), remoteId, ItemType.CHANNEL)

    // then
    val finalState = initialState.copy(
      manualMoving = false
    )
    assertThat(states).containsExactly(initialState, finalState)
    verify(executeShadingSystemActionUseCase).invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 2.5f, tilt = 25f)
  }

  @Test
  fun `should set position without tilt when tilting only when fully closed and not closed`() {
    // given
    val remoteId = 123
    val initialState = FacadeBlindsViewModelState(
      windowState = FacadeBlindWindowState(
        position = WindowGroupedValue.Similar(10f),
        slatTilt = WindowGroupedValue.Similar(0f)
      ),
      tiltControlType = SuplaTiltControlType.TILTS_ONLY_WHEN_FULLY_CLOSED,
      tiltingTime = 2000,
      openingTime = 20000,
      closingTime = 20000,
      lastPosition = 10,
      manualMoving = true
    )
    viewModel.setState(initialState)
    whenever(executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 50f, tilt = 0f))
      .thenReturn(Completable.complete())

    // when
    viewModel.handleAction(ShadingSystemAction.MoveAndTiltSetTo(50f, 80f), remoteId, ItemType.CHANNEL)

    // then

    val finalState = initialState.copy(
      manualMoving = false
    )
    assertThat(states).containsExactly(initialState, finalState)
    verify(executeShadingSystemActionUseCase).invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 50f, tilt = 0f)
  }

  @Test
  fun `should set position and tilt when tilting only when fully closed and closed`() {
    // given
    val remoteId = 123
    val initialState = FacadeBlindsViewModelState(
      windowState = FacadeBlindWindowState(
        position = WindowGroupedValue.Similar(10f),
        slatTilt = WindowGroupedValue.Similar(0f)
      ),
      tiltControlType = SuplaTiltControlType.TILTS_ONLY_WHEN_FULLY_CLOSED,
      tiltingTime = 2000,
      openingTime = 20000,
      closingTime = 20000,
      lastPosition = 10,
      manualMoving = true
    )
    viewModel.setState(initialState)
    whenever(executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 100f, tilt = 80f))
      .thenReturn(Completable.complete())

    // when
    viewModel.handleAction(ShadingSystemAction.MoveAndTiltSetTo(100f, 80f), remoteId, ItemType.CHANNEL)

    // then
    val finalState = initialState.copy(
      manualMoving = false
    )
    assertThat(states).containsExactly(initialState, finalState)
    verify(executeShadingSystemActionUseCase).invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 100f, tilt = 80f)
  }

  @Test
  fun `should load group`() {
    // given
    val remoteId = 123
    val group = mockGroup(remoteId, online = true)
    val onlineSummary = GroupOnlineSummary(2, 5)

    whenever(readChannelGroupByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(group))
    whenever(getGroupOnlineSummaryUseCase.invoke(321L)).thenReturn(Maybe.just(onlineSummary))
    whenever(preferences.isShowOpeningPercent).thenReturn(false)

    // when
    viewModel.loadData(remoteId, ItemType.GROUP)

    // then
    assertThat(states).containsExactly(
      FacadeBlindsViewModelState(
        remoteId = remoteId,
        windowState = FacadeBlindWindowState(
          position = WindowGroupedValue.Different(30f, 50f),
          slatTilt = WindowGroupedValue.Similar(85f),
          markers = listOf(ShadingBlindMarker(50f, 85f), ShadingBlindMarker(30f, 25f)),
          positionTextFormat = WindowGroupedValueFormat.PERCENTAGE
        ),
        viewState = WindowViewState(
          enabled = true,
          positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED,
          isGroup = true,
          onlineStatusString = "2/5"
        ),
        lastPosition = 0
      )
    )
  }

  @Test
  fun `should load offline group`() {
    // given
    val remoteId = 1234
    val group = mockGroup(remoteId, online = false)
    val onlineSummary = GroupOnlineSummary(2, 5)

    whenever(readChannelGroupByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(group))
    whenever(getGroupOnlineSummaryUseCase.invoke(321L)).thenReturn(Maybe.just(onlineSummary))
    whenever(preferences.isShowOpeningPercent).thenReturn(false)

    // when
    viewModel.loadData(remoteId, ItemType.GROUP)

    // then
    assertThat(states).containsExactly(
      FacadeBlindsViewModelState(
        remoteId = remoteId,
        windowState = FacadeBlindWindowState(
          position = WindowGroupedValue.Similar(25f),
          slatTilt = WindowGroupedValue.Similar(50f),
          markers = emptyList(),
          positionTextFormat = WindowGroupedValueFormat.PERCENTAGE
        ),
        viewState = WindowViewState(
          positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED,
          isGroup = true,
          onlineStatusString = "2/5"
        ),
        lastPosition = 0
      )
    )
  }

  @Test
  fun `should load config for channel`() {
    // given
    val remoteId = 123

    val suplaClient: SuplaClientApi = mockk()
    every { suplaClient.getChannelConfig(remoteId, ChannelConfigType.DEFAULT) } returns true
    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    viewModel.loadConfig(remoteId, ItemType.CHANNEL)

    // then
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()

    verify(suplaClientProvider).provide()
    io.mockk.verify {
      suplaClient.getChannelConfig(remoteId, ChannelConfigType.DEFAULT)
    }

    verifyNoMoreInteractions(suplaClientProvider)
    verifyNoInteractions(readGroupTiltingDetailsUseCase)
  }

  @Test
  fun `should load config for group and do nothing for unknown`() {
    // given
    val remoteId = 123
    whenever(readGroupTiltingDetailsUseCase.invoke(remoteId)).thenReturn(Single.just(TiltingDetails.Unknown))

    // when
    viewModel.loadConfig(remoteId, ItemType.GROUP)

    // then
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()

    verify(readGroupTiltingDetailsUseCase).invoke(remoteId)
    verifyNoMoreInteractions(readGroupTiltingDetailsUseCase)
    verifyNoInteractions(suplaClientProvider)
  }

  @Test
  fun `should load config for group and update state for similar`() {
    // given
    val remoteId = 123
    val details = TiltingDetails.Similar(0, 90, SuplaTiltControlType.TILTS_ONLY_WHEN_FULLY_CLOSED)
    whenever(readGroupTiltingDetailsUseCase.invoke(remoteId)).thenReturn(Single.just(details))

    // when
    viewModel.loadConfig(remoteId, ItemType.GROUP)

    // then
    assertThat(states).containsExactly(
      FacadeBlindsViewModelState(
        tiltControlType = SuplaTiltControlType.TILTS_ONLY_WHEN_FULLY_CLOSED,
        windowState = FacadeBlindWindowState(
          position = WindowGroupedValue.Similar(0f),
          tilt0Angle = 0f,
          tilt100Angle = 90f
        )
      )
    )
    assertThat(events).isEmpty()

    verify(readGroupTiltingDetailsUseCase).invoke(remoteId)
    verifyNoMoreInteractions(readGroupTiltingDetailsUseCase)
    verifyNoInteractions(suplaClientProvider)
  }

  private fun mockChannel(
    remoteId: Int,
    position: Int,
    tilt: Int,
    online: Boolean = true,
    flags: List<SuplaShadingSystemFlag> = listOf(SuplaShadingSystemFlag.TILT_IS_SET)
  ): ChannelDataEntity {
    val facadeBlindValue: FacadeBlindValue = mockk {
      every { this@mockk.position } returns position
      every { this@mockk.tilt } returns tilt
      every { this@mockk.online } returns online
      every { this@mockk.flags } returns flags
      every { hasValidPosition() } returns (position != -1)
      every { hasValidTilt() } returns (tilt != -1)
    }
    val value: ChannelValueEntity = mockk {
      every { asFacadeBlindValue() } returns facadeBlindValue
    }
    return mockk {
      every { this@mockk.remoteId } returns remoteId
      every { channelValueEntity } returns value
      every { this@mockk.flags } returns 0
    }
  }

  private fun mockGroup(remoteId: Int, online: Boolean): ChannelGroupDataEntity {
    val group: ChannelGroupEntity = mockk {
      every { groupTotalValues } returns listOf(ShadowingBlindGroupValue(50, 85), ShadowingBlindGroupValue(30, 25))
    }
    return mockk {
      every { id } returns 321L
      every { this@mockk.remoteId } returns remoteId
      every { channelGroupEntity } returns group
      every { this@mockk.isOnline() } returns online
    }
  }

  private fun mockConfig(
    tilt0Angle: Int = 0,
    tilt100Angle: Int = 180,
    type: SuplaTiltControlType = SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING,
    tiltingTime: Int = 2000,
    openingTime: Int = 20000,
    closingTime: Int = 20000
  ): SuplaChannelFacadeBlindConfig =
    SuplaChannelFacadeBlindConfig(
      remoteId = 123,
      func = SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND,
      crc32 = 0L,
      closingTimeMs = closingTime,
      openingTimeMs = openingTime,
      tiltingTimeMs = tiltingTime,
      motorUpsideDown = false,
      buttonsUpsideDown = false,
      timeMargin = 0,
      tilt0Angle = tilt0Angle,
      tilt100Angle = tilt100Angle,
      type = type
    )
}
