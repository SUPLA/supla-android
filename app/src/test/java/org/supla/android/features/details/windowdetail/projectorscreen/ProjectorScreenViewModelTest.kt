@file:Suppress("SameParameterValue")

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

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import org.assertj.core.api.Assertions
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
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.features.details.windowdetail.base.BaseWindowViewEvent
import org.supla.android.features.details.windowdetail.base.data.ProjectorScreenState
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.ui.ShadingSystemPositionPresentation
import org.supla.android.features.details.windowdetail.base.ui.WindowViewState
import org.supla.android.lib.SuplaConst
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.tools.VibrationHelper
import org.supla.android.usecases.channel.ObserveChannelWithChildrenUseCase
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.CallSuplaClientOperationUseCase
import org.supla.android.usecases.client.ExecuteShadingSystemActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.android.usecases.group.GetGroupOnlineSummaryUseCase
import org.supla.android.usecases.group.ObserveChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.group.totalvalue.GroupTotalValue
import org.supla.core.shared.data.model.function.rollershutter.RollerShutterValue
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.data.model.shadingsystem.SuplaShadingSystemFlag
import org.supla.core.shared.infrastructure.LocalizedStringId

class ProjectorScreenViewModelTest : BaseViewModelTest<ProjectorScreenViewModelState, BaseWindowViewEvent, ProjectorScreenViewModel>(
  MockSchedulers.MOCKK
) {

  @RelaxedMockK
  lateinit var loadingTimeoutManager: LoadingTimeoutManager

  @RelaxedMockK
  lateinit var executeShadingSystemActionUseCase: ExecuteShadingSystemActionUseCase

  @RelaxedMockK
  lateinit var executeSimpleActionUseCase: ExecuteSimpleActionUseCase

  @RelaxedMockK
  lateinit var callSuplaClientOperationUseCase: CallSuplaClientOperationUseCase

  @RelaxedMockK
  lateinit var observeChannelWithChildrenUseCase: ObserveChannelWithChildrenUseCase

  @RelaxedMockK
  lateinit var observeChannelGroupByRemoteIdUseCase: ObserveChannelGroupByRemoteIdUseCase

  @RelaxedMockK
  lateinit var getGroupOnlineSummaryUseCase: GetGroupOnlineSummaryUseCase

  @RelaxedMockK
  lateinit var vibrationHelper: VibrationHelper

  @RelaxedMockK
  lateinit var preferences: Preferences

  @RelaxedMockK
  lateinit var dateProvider: DateProvider

  @RelaxedMockK
  lateinit var suplaClientProvider: SuplaClientProvider

  @RelaxedMockK
  lateinit var profileRepository: RoomProfileRepository

  @RelaxedMockK
  lateinit var loginUseCase: LoginUseCase

  @RelaxedMockK
  lateinit var authorizeUseCase: AuthorizeUseCase

  @RelaxedMockK
  override lateinit var schedulers: SuplaSchedulers

  @InjectMockKs
  override lateinit var viewModel: ProjectorScreenViewModel

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    super.setUp()
  }

  @Test
  fun `should load data when online`() {
    // given
    val remoteId = 123
    val position = 33

    mockOnlineChannel(
      remoteId,
      position,
      valueFlags = listOf(SuplaShadingSystemFlag.MOTOR_PROBLEM),
      channelFlags = listOf(SuplaChannelFlag.CALCFG_RECALIBRATE)
    )

    // when
    viewModel.observeData(remoteId, ItemType.CHANNEL)

    // then
    Assertions.assertThat(states).containsExactly(
      ProjectorScreenViewModelState(
        remoteId = remoteId,
        windowState = ProjectorScreenState(WindowGroupedValue.Similar(position.toFloat())),
        viewState = WindowViewState(
          issues = listOf(ChannelIssueItem.Error(LocalizedStringId.MOTOR_PROBLEM)),
          enabled = true,
          positionPresentation = ShadingSystemPositionPresentation.AS_EXTENSION,
          positionUnknown = false,
          calibrationPossible = true,
          calibrating = false
        )
      )
    )
  }

  @Test
  fun `should load group data when online with different positions`() {
    // given
    val remoteId = 133

    mockOnlineGroup(remoteId, "10|80|20")

    // when
    viewModel.observeData(remoteId, ItemType.GROUP)

    // then
    Assertions.assertThat(states).containsExactly(
      ProjectorScreenViewModelState(
        remoteId = remoteId,
        windowState = ProjectorScreenState(
          position = WindowGroupedValue.Different(min = 10.0f, max = 80.0f),
          markers = listOf(10f, 80f, 20f)
        ),
        viewState = WindowViewState(
          enabled = true,
          positionPresentation = ShadingSystemPositionPresentation.AS_EXTENSION,
          positionUnknown = false,
          calibrationPossible = false,
          calibrating = false,
          isGroup = true,
          onlineStatusString = "2/4",
        )
      )
    )
  }

  private fun mockOnlineChannel(
    remoteId: Int,
    position: Int = 0,
    bottomPosition: Int = 100,
    valueFlags: List<SuplaShadingSystemFlag> = emptyList(),
    channelFlags: List<SuplaChannelFlag> = emptyList(),
    hasValidPosition: Boolean = true,
    function: SuplaFunction = SuplaFunction.PROJECTOR_SCREEN
  ) {
    val rollerShutterValue: RollerShutterValue = mockk {
      every { this@mockk.position } returns position
      every { this@mockk.bottomPosition } returns bottomPosition
      every { this@mockk.flags } returns valueFlags
      every { hasValidPosition() } returns hasValidPosition
      every { status } returns SuplaChannelAvailabilityStatus.ONLINE
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
    val channelWithChildren: ChannelWithChildren = mockk {
      every { channel } returns channelData
    }

    every { observeChannelWithChildrenUseCase.invoke(remoteId) } returns Observable.just(channelWithChildren)
  }

  private fun mockOnlineGroup(
    remoteId: Int,
    totalValue: String
  ) {
    val groupId = 123L
    val group: ChannelGroupEntity = mockk {
      every {
        this@mockk.groupTotalValues
      } returns GroupTotalValue.parse(SuplaConst.SUPLA_CHANNELFNC_PROJECTOR_SCREEN, totalValue)
    }
    val groupData: ChannelGroupDataEntity = mockk {
      every { id } returns groupId
      every { this@mockk.remoteId } returns remoteId
      every { this@mockk.function } returns SuplaFunction.PROJECTOR_SCREEN
      every { channelGroupEntity } returns group
      every { status } returns SuplaChannelAvailabilityStatus.ONLINE
    }

    every { observeChannelGroupByRemoteIdUseCase.invoke(remoteId) } returns Observable.just(groupData)
    every { getGroupOnlineSummaryUseCase.invoke(groupId) } returns Maybe.just(GroupOnlineSummary(2, 4))
  }
}
