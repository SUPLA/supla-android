@file:Suppress("SameParameterValue")

package org.supla.android.features.details.windowdetail.curtain
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
import io.reactivex.rxjava3.core.Maybe
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.supla.android.Preferences
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.local.entity.custom.GroupOnlineSummary
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.features.details.windowdetail.base.BaseWindowViewEvent
import org.supla.android.features.details.windowdetail.base.data.CurtainWindowState
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.ui.ShadingSystemPositionPresentation
import org.supla.android.features.details.windowdetail.base.ui.WindowViewState
import org.supla.android.features.details.windowdetail.base.ui.windowview.ShadingSystemOrientation
import org.supla.android.lib.SuplaConst
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.tools.VibrationHelper
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.CallSuplaClientOperationUseCase
import org.supla.android.usecases.client.ExecuteShadingSystemActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.android.usecases.group.GetGroupOnlineSummaryUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.group.totalvalue.GroupTotalValue
import org.supla.core.shared.data.model.function.rollershutter.RollerShutterValue
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.data.model.shadingsystem.SuplaShadingSystemFlag
import org.supla.core.shared.infrastructure.LocalizedStringId

@RunWith(MockitoJUnitRunner::class)
class CurtainViewModelTest : BaseViewModelTest<CurtainViewModelState, BaseWindowViewEvent, CurtainViewModel>() {

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
  override lateinit var viewModel: CurtainViewModel

  @Before
  override fun setUp() {
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
    viewModel.loadData(remoteId, ItemType.CHANNEL)

    // then
    Assertions.assertThat(states).containsExactly(
      CurtainViewModelState(
        remoteId = remoteId,
        windowState = CurtainWindowState(WindowGroupedValue.Similar(position.toFloat())),
        viewState = WindowViewState(
          issues = listOf(ChannelIssueItem.Error(LocalizedStringId.MOTOR_PROBLEM)),
          enabled = true,
          positionPresentation = ShadingSystemPositionPresentation.AS_CLOSED,
          positionUnknown = false,
          calibrationPossible = true,
          calibrating = false,
          orientation = ShadingSystemOrientation.HORIZONTAL
        )
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
    Assertions.assertThat(states).containsExactly(
      CurtainViewModelState(
        remoteId = remoteId,
        windowState = CurtainWindowState(
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
          orientation = ShadingSystemOrientation.HORIZONTAL
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
    function: SuplaFunction = SuplaFunction.CURTAIN
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

    whenever(readChannelByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(channelData))
  }

  private fun mockOnlineGroup(
    remoteId: Int,
    totalValue: String
  ) {
    val groupId = 123L
    val group: ChannelGroupEntity = mockk {
      every {
        this@mockk.groupTotalValues
      } returns GroupTotalValue.parse(SuplaConst.SUPLA_CHANNELFNC_CURTAIN, totalValue)
    }
    val groupData: ChannelGroupDataEntity = mockk {
      every { id } returns groupId
      every { this@mockk.remoteId } returns remoteId
      every { this@mockk.function } returns SuplaFunction.CURTAIN
      every { channelGroupEntity } returns group
      every { status } returns SuplaChannelAvailabilityStatus.ONLINE
    }

    whenever(readChannelGroupByRemoteIdUseCase.invoke(remoteId)).thenReturn(Maybe.just(groupData))
    whenever(getGroupOnlineSummaryUseCase.invoke(groupId)).thenReturn(Maybe.just(GroupOnlineSummary(2, 4)))
  }
}
