package org.supla.android.features.details.windowdetail.verticalblinds
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

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.Preferences
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.local.entity.custom.GroupOnlineSummary
import org.supla.android.data.source.remote.ConfigResult
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.data.source.remote.rollershutter.SuplaChannelFacadeBlindConfig
import org.supla.android.data.source.remote.rollershutter.SuplaTiltControlType
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.features.details.windowdetail.base.BaseWindowViewEvent
import org.supla.android.features.details.windowdetail.base.data.ShadingBlindMarker
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValueFormat
import org.supla.android.features.details.windowdetail.base.data.verticalblinds.VerticalBlindWindowState
import org.supla.android.features.details.windowdetail.base.ui.ShadingSystemAction
import org.supla.android.features.details.windowdetail.base.ui.ShadingSystemPositionPresentation
import org.supla.android.features.details.windowdetail.base.ui.WindowViewState
import org.supla.android.features.details.windowdetail.base.ui.windowview.ShadingSystemOrientation
import org.supla.android.lib.SuplaConst
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ObserveChannelWithChildrenUseCase
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.CallSuplaClientOperationUseCase
import org.supla.android.usecases.client.ExecuteShadingSystemActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.android.usecases.group.GetGroupOnlineSummaryUseCase
import org.supla.android.usecases.group.ObserveChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.group.ReadGroupTiltingDetailsUseCase
import org.supla.android.usecases.group.totalvalue.ShadowingBlindGroupValue
import org.supla.core.shared.data.model.function.facadeblind.FacadeBlindValue
import org.supla.core.shared.data.model.shadingsystem.SuplaShadingSystemFlag

class VerticalBlindsViewModelTest : BaseViewModelTest<VerticalBlindsViewModelState, BaseWindowViewEvent, VerticalBlindsViewModel>(
  MockSchedulers.MOCKK
) {

  @RelaxedMockK
  private lateinit var channelConfigEventsManager: ChannelConfigEventsManager

  @RelaxedMockK
  private lateinit var executeShadingSystemActionUseCase: ExecuteShadingSystemActionUseCase

  @RelaxedMockK
  private lateinit var readGroupTiltingDetailsUseCase: ReadGroupTiltingDetailsUseCase

  @RelaxedMockK
  private lateinit var suplaClientProvider: SuplaClientProvider

  @RelaxedMockK
  private lateinit var executeSimpleActionUseCase: ExecuteSimpleActionUseCase

  @RelaxedMockK
  private lateinit var callSuplaClientOperationUseCase: CallSuplaClientOperationUseCase

  @RelaxedMockK
  private lateinit var observeChannelWithChildrenUseCase: ObserveChannelWithChildrenUseCase

  @RelaxedMockK
  private lateinit var observeChannelGroupByRemoteIdUseCase: ObserveChannelGroupByRemoteIdUseCase

  @RelaxedMockK
  private lateinit var getGroupOnlineSummaryUseCase: GetGroupOnlineSummaryUseCase

  @RelaxedMockK
  private lateinit var preferences: Preferences

  @RelaxedMockK
  private lateinit var dateProvider: DateProvider

  @RelaxedMockK
  private lateinit var profileRepository: RoomProfileRepository

  @RelaxedMockK
  private lateinit var loginUseCase: LoginUseCase

  @RelaxedMockK
  private lateinit var authorizeUseCase: AuthorizeUseCase

  @RelaxedMockK
  override lateinit var schedulers: SuplaSchedulers

  @InjectMockKs
  override lateinit var viewModel: VerticalBlindsViewModel

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    super.setUp()
  }

  @Test
  fun `should load channel`() {
    // given
    val remoteId = 123
    val position = 10
    val tilt = 33
    val channelData = mockChannel(remoteId, position, tilt)

    every { observeChannelWithChildrenUseCase.invoke(remoteId) } returns Observable.just(channelData)
    every { preferences.isShowOpeningPercent } returns false

    // when
    viewModel.observeData(remoteId, ItemType.CHANNEL)

    // then
    assertThat(states).containsExactly(
      VerticalBlindsViewModelState(
        remoteId = remoteId,
        windowState = VerticalBlindWindowState(
          position = WindowGroupedValue.Similar(position.toFloat()),
          slatTilt = WindowGroupedValue.Similar(tilt.toFloat()),
          positionTextFormat = WindowGroupedValueFormat.PERCENTAGE
        ),
        viewState = WindowViewState(
          enabled = true,
          positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED,
          orientation = ShadingSystemOrientation.HORIZONTAL
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
    val channelData = mockChannel(remoteId, position, tilt, status = SuplaChannelAvailabilityStatus.OFFLINE)

    every { observeChannelWithChildrenUseCase.invoke(remoteId) } returns Observable.just(channelData)
    every { preferences.isShowOpeningPercent } returns false

    // when
    viewModel.observeData(remoteId, ItemType.CHANNEL)

    // then
    assertThat(states).containsExactly(
      VerticalBlindsViewModelState(
        remoteId = remoteId,
        windowState = VerticalBlindWindowState(
          position = WindowGroupedValue.Similar(25f),
          slatTilt = WindowGroupedValue.Similar(50f),
          positionTextFormat = WindowGroupedValueFormat.PERCENTAGE
        ),
        viewState = WindowViewState(
          enabled = false,
          positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED,
          orientation = ShadingSystemOrientation.HORIZONTAL
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

    every { observeChannelWithChildrenUseCase.invoke(remoteId) } returns Observable.just(channelData)
    every { preferences.isShowOpeningPercent } returns false

    // when
    viewModel.observeData(remoteId, ItemType.CHANNEL)

    // then
    assertThat(states).containsExactly(
      VerticalBlindsViewModelState(
        remoteId = remoteId,
        windowState = VerticalBlindWindowState(
          position = WindowGroupedValue.Similar(10f),
          slatTilt = null,
          positionTextFormat = WindowGroupedValueFormat.PERCENTAGE
        ),
        viewState = WindowViewState(
          enabled = true,
          positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED,
          orientation = ShadingSystemOrientation.HORIZONTAL
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

    every {
      channelConfigEventsManager.observerConfig(remoteId)
    } returns Observable.just(ChannelConfigEventsManager.ConfigEvent(ConfigResult.RESULT_TRUE, config))

    // when
    viewModel.observeConfig(remoteId, ItemType.CHANNEL)

    // then
    assertThat(states).containsExactly(
      VerticalBlindsViewModelState(
        windowState = VerticalBlindWindowState(
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
    val initialState = VerticalBlindsViewModelState(
      windowState = VerticalBlindWindowState(
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
    val initialState = VerticalBlindsViewModelState(
      windowState = VerticalBlindWindowState(
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

    every {
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, tilt = 95f)
    } returns Completable.complete()

    // when
    viewModel.handleAction(ShadingSystemAction.TiltTo(95f), remoteId, ItemType.CHANNEL)
    viewModel.handleAction(ShadingSystemAction.TiltSetTo(95f), remoteId, ItemType.CHANNEL)

    // then
    val state1 = VerticalBlindsViewModelState(
      windowState = VerticalBlindWindowState(
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

    verify {
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, tilt = 95f)
    }
    confirmVerified(executeShadingSystemActionUseCase)
  }

  @Test
  fun `should change position and tilt when tilting changes position`() {
    // given
    val remoteId = 123
    val initialState = VerticalBlindsViewModelState(
      windowState = VerticalBlindWindowState(
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
    val initialState = VerticalBlindsViewModelState(
      windowState = VerticalBlindWindowState(
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
    val initialState = VerticalBlindsViewModelState(
      windowState = VerticalBlindWindowState(
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
    val initialState = VerticalBlindsViewModelState(
      windowState = VerticalBlindWindowState(
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
    val initialState = VerticalBlindsViewModelState(
      windowState = VerticalBlindWindowState(
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
    val initialState = VerticalBlindsViewModelState(
      windowState = VerticalBlindWindowState(
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
    every {
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, percentage = 75f, tilt = 50f)
    } returns Completable.complete()

    // when
    viewModel.handleAction(ShadingSystemAction.MoveAndTiltSetTo(75f, 50f), remoteId, ItemType.CHANNEL)

    // then
    val finalState = initialState.copy(manualMoving = false)
    assertThat(states).containsExactly(initialState, finalState)

    verify {
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, percentage = 75f, tilt = 50f)
    }
  }

  @Test
  fun `should set position and correct tilt when tilts changes position (low position)`() {
    // given
    val remoteId = 123
    val initialState = VerticalBlindsViewModelState(
      windowState = VerticalBlindWindowState(
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
    every {
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, percentage = 95f, tilt = 50f)
    } returns Completable.complete()

    // when
    viewModel.handleAction(ShadingSystemAction.MoveAndTiltSetTo(95f, 0f), remoteId, ItemType.CHANNEL)

    // then
    val finalState = initialState.copy(
      manualMoving = false
    )
    assertThat(states).containsExactly(initialState, finalState)

    verify {
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, percentage = 95f, tilt = 50f)
    }
  }

  @Test
  fun `should set position and correct tilt when tilts changes position (up position)`() {
    // given
    val remoteId = 123
    val initialState = VerticalBlindsViewModelState(
      windowState = VerticalBlindWindowState(
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
    every {
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 2.5f, tilt = 25f)
    } returns Completable.complete()

    // when
    viewModel.handleAction(ShadingSystemAction.MoveAndTiltSetTo(2.5f, 80f), remoteId, ItemType.CHANNEL)

    // then
    val finalState = initialState.copy(
      manualMoving = false
    )
    assertThat(states).containsExactly(initialState, finalState)
    verify {
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 2.5f, tilt = 25f)
    }
  }

  @Test
  fun `should set position without tilt when tilting only when fully closed and not closed`() {
    // given
    val remoteId = 123
    val initialState = VerticalBlindsViewModelState(
      windowState = VerticalBlindWindowState(
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
    every {
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 50f, tilt = 0f)
    } returns Completable.complete()

    // when
    viewModel.handleAction(ShadingSystemAction.MoveAndTiltSetTo(50f, 80f), remoteId, ItemType.CHANNEL)

    // then

    val finalState = initialState.copy(
      manualMoving = false
    )
    assertThat(states).containsExactly(initialState, finalState)
    verify {
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 50f, tilt = 0f)
    }
  }

  @Test
  fun `should set position and tilt when tilting only when fully closed and closed`() {
    // given
    val remoteId = 123
    val initialState = VerticalBlindsViewModelState(
      windowState = VerticalBlindWindowState(
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
    every {
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 100f, tilt = 80f)
    } returns Completable.complete()

    // when
    viewModel.handleAction(ShadingSystemAction.MoveAndTiltSetTo(100f, 80f), remoteId, ItemType.CHANNEL)

    // then
    val finalState = initialState.copy(
      manualMoving = false
    )
    assertThat(states).containsExactly(initialState, finalState)
    verify {
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 100f, tilt = 80f)
    }
  }

  @Test
  fun `should load group`() {
    // given
    val remoteId = 123
    val group = mockGroup(remoteId, status = SuplaChannelAvailabilityStatus.ONLINE)
    val onlineSummary = GroupOnlineSummary(2, 5)

    every { observeChannelGroupByRemoteIdUseCase.invoke(remoteId) } returns Observable.just(group)
    every { getGroupOnlineSummaryUseCase.invoke(321L) } returns Maybe.just(onlineSummary)
    every { preferences.isShowOpeningPercent } returns false

    // when
    viewModel.observeData(remoteId, ItemType.GROUP)

    // then
    assertThat(states).containsExactly(
      VerticalBlindsViewModelState(
        remoteId = remoteId,
        windowState = VerticalBlindWindowState(
          position = WindowGroupedValue.Different(30f, 50f),
          slatTilt = WindowGroupedValue.Similar(85f),
          markers = listOf(ShadingBlindMarker(50f, 85f), ShadingBlindMarker(30f, 25f)),
          positionTextFormat = WindowGroupedValueFormat.PERCENTAGE
        ),
        viewState = WindowViewState(
          enabled = true,
          positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED,
          isGroup = true,
          onlineStatusString = "2/5",
          orientation = ShadingSystemOrientation.HORIZONTAL
        ),
        lastPosition = 0
      )
    )
  }

  @Test
  fun `should load offline group`() {
    // given
    val remoteId = 1234
    val group = mockGroup(remoteId, status = SuplaChannelAvailabilityStatus.OFFLINE)
    val onlineSummary = GroupOnlineSummary(2, 5)

    every { observeChannelGroupByRemoteIdUseCase.invoke(remoteId) } returns Observable.just(group)
    every { getGroupOnlineSummaryUseCase.invoke(321L) } returns Maybe.just(onlineSummary)
    every { preferences.isShowOpeningPercent } returns false

    // when
    viewModel.observeData(remoteId, ItemType.GROUP)

    // then
    assertThat(states).containsExactly(
      VerticalBlindsViewModelState(
        remoteId = remoteId,
        windowState = VerticalBlindWindowState(
          position = WindowGroupedValue.Similar(25f),
          slatTilt = WindowGroupedValue.Similar(50f),
          markers = emptyList(),
          positionTextFormat = WindowGroupedValueFormat.PERCENTAGE
        ),
        viewState = WindowViewState(
          positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED,
          isGroup = true,
          onlineStatusString = "2/5",
          orientation = ShadingSystemOrientation.HORIZONTAL
        ),
        lastPosition = 0
      )
    )
  }

  private fun mockChannel(
    remoteId: Int,
    position: Int,
    tilt: Int,
    status: SuplaChannelAvailabilityStatus = SuplaChannelAvailabilityStatus.ONLINE,
    flags: List<SuplaShadingSystemFlag> = listOf(SuplaShadingSystemFlag.TILT_IS_SET)
  ): ChannelWithChildren {
    val facadeBlindValue: FacadeBlindValue = mockk {
      every { this@mockk.position } returns position
      every { this@mockk.tilt } returns tilt
      every { this@mockk.status } returns status
      every { this@mockk.flags } returns flags
      every { hasValidPosition() } returns (position != -1)
      every { hasValidTilt() } returns (tilt != -1)
    }
    val value: ChannelValueEntity = mockk {
      every { asFacadeBlindValue() } returns facadeBlindValue
    }
    val channelData: ChannelDataEntity = mockk {
      every { this@mockk.remoteId } returns remoteId
      every { channelValueEntity } returns value
      every { this@mockk.flags } returns 0
    }

    return mockk {
      every { channel } returns channelData
    }
  }

  private fun mockGroup(remoteId: Int, status: SuplaChannelAvailabilityStatus): ChannelGroupDataEntity {
    val group: ChannelGroupEntity = mockk {
      every { groupTotalValues } returns listOf(ShadowingBlindGroupValue(50, 85), ShadowingBlindGroupValue(30, 25))
    }
    return mockk {
      every { id } returns 321L
      every { this@mockk.remoteId } returns remoteId
      every { channelGroupEntity } returns group
      every { this@mockk.status } returns status
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
