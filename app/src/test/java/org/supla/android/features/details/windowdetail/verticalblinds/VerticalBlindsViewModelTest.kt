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

import com.google.gson.Gson
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ChannelConfigEntity
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.local.entity.custom.GroupOnlineSummary
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.ConfigResult
import org.supla.android.data.source.remote.SuplaChannelConfig
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
import org.supla.android.tools.SuplaThreading
import org.supla.android.usecases.channel.ObserveChannelWithChildrenUseCase
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.CallSuplaClientOperationUseCase
import org.supla.android.usecases.client.ExecuteShadingSystemActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.android.usecases.group.CalculateGroupTiltingDetailsUseCase
import org.supla.android.usecases.group.GetGroupOnlineSummaryUseCase
import org.supla.android.usecases.group.ObserveChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.group.TiltingDetails
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
  private lateinit var calculateGroupTiltingDetailsUseCase: CalculateGroupTiltingDetailsUseCase

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
  private lateinit var preferences: ApplicationPreferences

  @RelaxedMockK
  private lateinit var dateProvider: DateProvider

  @RelaxedMockK
  private lateinit var profileRepository: ProfileRepository

  @RelaxedMockK
  private lateinit var channelRepository: ChannelRepository

  @RelaxedMockK
  private lateinit var gson: Gson

  @RelaxedMockK
  private lateinit var loginUseCase: LoginUseCase

  @RelaxedMockK
  private lateinit var authorizeUseCase: AuthorizeUseCase

  @RelaxedMockK
  override lateinit var threading: SuplaThreading

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
          windowTilt = WindowGroupedValue.Similar(tilt.toFloat()),
          sliderTilt = WindowGroupedValue.Similar(tilt.toFloat()),
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
          windowTilt = WindowGroupedValue.Similar(50f),
          sliderTilt = WindowGroupedValue.Similar(50f),
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
          windowTilt = null,
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
        windowTilt = WindowGroupedValue.Similar(33f)
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
        windowTilt = WindowGroupedValue.Similar(95f),
        sliderTilt = WindowGroupedValue.Similar(95f)
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
        windowTilt = WindowGroupedValue.Similar(33f)
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
        windowTilt = WindowGroupedValue.Similar(95f),
        sliderTilt = WindowGroupedValue.Similar(95f)
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
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, tilt = 95)
    } returns Completable.complete()

    // when
    viewModel.handleAction(ShadingSystemAction.TiltTo(95f), remoteId, ItemType.CHANNEL)
    viewModel.handleAction(ShadingSystemAction.TiltSetTo(95f), remoteId, ItemType.CHANNEL)

    // then
    val state1 = VerticalBlindsViewModelState(
      windowState = VerticalBlindWindowState(
        position = WindowGroupedValue.Similar(0f),
        windowTilt = WindowGroupedValue.Similar(95f),
        sliderTilt = WindowGroupedValue.Similar(95f),
        positionTextFormat = WindowGroupedValueFormat.PERCENTAGE
      ),
      manualMoving = true
    )
    val state2 = state1.copy(
      manualMoving = false
    )
    assertThat(states).containsExactly(state1, state2)

    verify {
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, tilt = 95)
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
        windowTilt = WindowGroupedValue.Similar(33f)
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
        windowTilt = WindowGroupedValue.Similar(50f),
        sliderTilt = WindowGroupedValue.Similar(50f)
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
        windowTilt = WindowGroupedValue.Similar(33f),
        sliderTilt = WindowGroupedValue.Similar(33f)
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
        windowTilt = WindowGroupedValue.Similar(50f),
        sliderTilt = WindowGroupedValue.Similar(50f)
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
        windowTilt = WindowGroupedValue.Similar(33f)
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
        windowTilt = WindowGroupedValue.Similar(25f),
        sliderTilt = WindowGroupedValue.Similar(25f)
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
        windowTilt = WindowGroupedValue.Similar(0f),
        sliderTilt = WindowGroupedValue.Similar(0f)
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
        windowTilt = WindowGroupedValue.Similar(0f)
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
        windowTilt = WindowGroupedValue.Similar(80f),
        sliderTilt = WindowGroupedValue.Similar(80f)
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
        windowTilt = WindowGroupedValue.Similar(33f)
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
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, percentage = 75, tilt = 50)
    } returns Completable.complete()

    // when
    viewModel.handleAction(ShadingSystemAction.MoveAndTiltSetTo(75f, 50f), remoteId, ItemType.CHANNEL)

    // then
    val finalState = initialState.copy(manualMoving = false)
    assertThat(states).containsExactly(initialState, finalState)

    verify {
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, percentage = 75, tilt = 50)
    }
  }

  @Test
  fun `should set position and correct tilt when tilts changes position (low position)`() {
    // given
    val remoteId = 123
    val initialState = VerticalBlindsViewModelState(
      windowState = VerticalBlindWindowState(
        position = WindowGroupedValue.Similar(10f),
        windowTilt = WindowGroupedValue.Similar(33f),
        sliderTilt = WindowGroupedValue.Similar(33f)
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
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, percentage = 95, tilt = 50)
    } returns Completable.complete()

    // when
    viewModel.handleAction(ShadingSystemAction.MoveAndTiltSetTo(95f, 0f), remoteId, ItemType.CHANNEL)

    // then
    val finalState = initialState.copy(
      manualMoving = false
    )
    assertThat(states).containsExactly(initialState, finalState)

    verify {
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, percentage = 95, tilt = 50)
    }
  }

  @Test
  fun `should set position and correct tilt when tilts changes position (up position)`() {
    // given
    val remoteId = 123
    val initialState = VerticalBlindsViewModelState(
      windowState = VerticalBlindWindowState(
        position = WindowGroupedValue.Similar(10f),
        windowTilt = WindowGroupedValue.Similar(33f),
        sliderTilt = WindowGroupedValue.Similar(33f)
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
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 3, tilt = 25)
    } returns Completable.complete()

    // when
    viewModel.handleAction(ShadingSystemAction.MoveAndTiltSetTo(2.5f, 80f), remoteId, ItemType.CHANNEL)

    // then
    val finalState = initialState.copy(
      manualMoving = false
    )
    assertThat(states).containsExactly(initialState, finalState)
    verify {
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 3, tilt = 25)
    }
  }

  @Test
  fun `should set position without tilt when tilting only when fully closed and not closed`() {
    // given
    val remoteId = 123
    val initialState = VerticalBlindsViewModelState(
      windowState = VerticalBlindWindowState(
        position = WindowGroupedValue.Similar(10f),
        windowTilt = WindowGroupedValue.Similar(0f)
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
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 50, tilt = 0)
    } returns Completable.complete()

    // when
    viewModel.handleAction(ShadingSystemAction.MoveAndTiltSetTo(50f, 80f), remoteId, ItemType.CHANNEL)

    // then

    val finalState = initialState.copy(
      manualMoving = false
    )
    assertThat(states).containsExactly(initialState, finalState)
    verify {
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 50, tilt = 0)
    }
  }

  @Test
  fun `should set position and tilt when tilting only when fully closed and closed`() {
    // given
    val remoteId = 123
    val initialState = VerticalBlindsViewModelState(
      windowState = VerticalBlindWindowState(
        position = WindowGroupedValue.Similar(10f),
        windowTilt = WindowGroupedValue.Similar(0f)
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
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 100, tilt = 80)
    } returns Completable.complete()

    // when
    viewModel.handleAction(ShadingSystemAction.MoveAndTiltSetTo(100f, 80f), remoteId, ItemType.CHANNEL)

    // then
    val finalState = initialState.copy(
      manualMoving = false
    )
    assertThat(states).containsExactly(initialState, finalState)
    verify {
      executeShadingSystemActionUseCase.invoke(ActionId.SHUT_PARTIALLY, SubjectType.CHANNEL, remoteId, 100, tilt = 80)
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
    every { channelRepository.findChannelListForGroup(remoteId) } returns Single.just(
      listOf(mockGroupChannel(50, 85), mockGroupChannel(30, 25))
    )
    every { gson.fromJson(any<String>(), SuplaChannelFacadeBlindConfig::class.java) } returns mockConfig()
    every { calculateGroupTiltingDetailsUseCase.invoke(any()) } returns
      TiltingDetails.Similar(0, 180, SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING)
    every { preferences.isShowOpeningPercent } returns false

    // when
    viewModel.observeData(remoteId, ItemType.GROUP)

    // then
    assertThat(states).containsExactly(
      VerticalBlindsViewModelState(
        remoteId = remoteId,
        tiltControlType = SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING,
        windowState = VerticalBlindWindowState(
          position = WindowGroupedValue.Different(30f, 50f),
          windowTilt = WindowGroupedValue.Similar(85f),
          sliderTilt = WindowGroupedValue.Different(25f, 85f),
          tilt0Angle = 0f,
          tilt100Angle = 180f,
          markers = listOf(ShadingBlindMarker(30f, 25f, 0f, 180f), ShadingBlindMarker(50f, 85f, 0f, 180f)),
          positionTextFormat = WindowGroupedValueFormat.PERCENTAGE
        ),
        viewState = WindowViewState(
          enabled = true,
          positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED,
          isGroup = true,
          onlineStatusString = "2/5",
          orientation = ShadingSystemOrientation.HORIZONTAL
        ),
        lastPosition = 50
      )
    )
  }

  @Test
  fun `should pass missing channel config when calculating group tilting details`() {
    // given
    val remoteId = 123
    val group = mockGroup(remoteId, status = SuplaChannelAvailabilityStatus.ONLINE)
    val onlineSummary = GroupOnlineSummary(2, 5)
    val configs = slot<List<SuplaChannelConfig?>>()

    every { observeChannelGroupByRemoteIdUseCase.invoke(remoteId) } returns Observable.just(group)
    every { getGroupOnlineSummaryUseCase.invoke(321L) } returns Maybe.just(onlineSummary)
    every { channelRepository.findChannelListForGroup(remoteId) } returns Single.just(
      listOf(mockGroupChannel(50, 85, configEntity = null), mockGroupChannel(30, 25))
    )
    every { gson.fromJson(any<String>(), SuplaChannelFacadeBlindConfig::class.java) } returns mockConfig()
    every { calculateGroupTiltingDetailsUseCase.invoke(capture(configs)) } returns TiltingDetails.Unknown
    every { preferences.isShowOpeningPercent } returns false

    // when
    viewModel.observeData(remoteId, ItemType.GROUP)

    // then
    assertThat(configs.captured).hasSize(2).containsNull()
    assertThat(states.single().windowState.tilt0Angle).isNull()
    assertThat(states.single().windowState.tilt100Angle).isNull()
    assertThat(states.single().tiltControlType).isNull()
  }

  @Test
  fun `should load offline group`() {
    // given
    val remoteId = 1234
    val group = mockGroup(remoteId, status = SuplaChannelAvailabilityStatus.OFFLINE)
    val onlineSummary = GroupOnlineSummary(2, 5)

    every { observeChannelGroupByRemoteIdUseCase.invoke(remoteId) } returns Observable.just(group)
    every { getGroupOnlineSummaryUseCase.invoke(321L) } returns Maybe.just(onlineSummary)
    every { channelRepository.findChannelListForGroup(remoteId) } returns Single.just(emptyList())
    every { calculateGroupTiltingDetailsUseCase.invoke(any()) } returns TiltingDetails.Unknown
    every { preferences.isShowOpeningPercent } returns false

    // when
    viewModel.observeData(remoteId, ItemType.GROUP)

    // then
    assertThat(states).containsExactly(
      VerticalBlindsViewModelState(
        remoteId = remoteId,
        windowState = VerticalBlindWindowState(
          position = WindowGroupedValue.Similar(25f),
          windowTilt = WindowGroupedValue.Similar(50f),
          sliderTilt = WindowGroupedValue.Similar(50f),
          markers = emptyList(),
          positionTextFormat = WindowGroupedValueFormat.PERCENTAGE
        ),
        viewState = WindowViewState(
          positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED,
          isGroup = true,
          onlineStatusString = "2/5",
          orientation = ShadingSystemOrientation.HORIZONTAL
        ),
        lastPosition = null
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
      every { configEntity } returns null
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

  private fun mockGroupChannel(
    position: Int,
    tilt: Int,
    configEntity: ChannelConfigEntity? = ChannelConfigEntity(1, 1, "{}", ChannelConfigType.FACADE_BLIND, 0L)
  ): ChannelDataEntity {
    val facadeBlindValue: FacadeBlindValue = mockk {
      every { this@mockk.position } returns position
      every { this@mockk.tilt } returns tilt
      every { this@mockk.flags } returns listOf(SuplaShadingSystemFlag.TILT_IS_SET)
      every { hasValidPosition() } returns (position != -1)
      every { hasValidTilt() } returns (tilt != -1)
    }
    val value = mockk<ChannelValueEntity> {
      every { asFacadeBlindValue() } returns facadeBlindValue
    }
    return mockk {
      every { channelValueEntity } returns value
      every { this@mockk.configEntity } returns configEntity
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
