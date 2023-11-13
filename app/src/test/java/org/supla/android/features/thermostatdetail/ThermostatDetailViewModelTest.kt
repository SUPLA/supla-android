package org.supla.android.features.thermostatdetail
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
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.supla.android.core.BaseViewModelTest
import org.supla.android.data.source.local.entity.ThermostatValue
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.db.Channel
import org.supla.android.db.ChannelValue
import org.supla.android.events.UpdateEventsManager
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.channel.ReadChannelGroupByRemoteIdUseCase

@RunWith(MockitoJUnitRunner::class)
class ThermostatDetailViewModelTest :
  BaseViewModelTest<ThermostatDetailViewState, ThermostatDetailViewEvent, ThermostatDetailViewModel>() {

  @Mock
  private lateinit var readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase

  @Mock
  private lateinit var readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase

  @Mock
  private lateinit var updateEventsManager: UpdateEventsManager

  @Mock
  override lateinit var schedulers: SuplaSchedulers

  @InjectMocks
  override lateinit var viewModel: ThermostatDetailViewModel

  @Before
  override fun setUp() {
    super.setUp()
  }

  @Test
  fun `should close detail when thermostat changed subfunction`() {
    // given
    val remoteId = 123
    val channelBase1: Channel = mockChannel(ThermostatSubfunction.HEAT)
    val channelBase2: Channel = mockChannel(ThermostatSubfunction.COOL)
    whenever(readChannelByRemoteIdUseCase.invoke(remoteId))
      .thenReturn(Maybe.just(channelBase1), Maybe.just(channelBase2))

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL, SUPLA_CHANNELFNC_HVAC_THERMOSTAT)
    viewModel.loadData(remoteId, ItemType.CHANNEL, SUPLA_CHANNELFNC_HVAC_THERMOSTAT)

    // then
    assertThat(states).containsExactly(
      ThermostatDetailViewState(channelBase1)
    )
    assertThat(events).containsExactly(
      ThermostatDetailViewEvent.Close
    )
  }

  @Test
  fun `should close detail when thermostat changed function`() {
    // given
    val remoteId = 123
    val channelBase1: Channel = mockChannel(ThermostatSubfunction.HEAT)
    whenever(readChannelByRemoteIdUseCase.invoke(remoteId))
      .thenReturn(Maybe.just(channelBase1))

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL, SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ThermostatDetailViewEvent.Close
    )
  }

  @Test
  fun `should close detail when thermostat was deleted`() {
    // given
    val remoteId = 123
    val channelBase1: Channel = mockChannel(ThermostatSubfunction.HEAT, visible = 0)
    whenever(readChannelByRemoteIdUseCase.invoke(remoteId))
      .thenReturn(Maybe.just(channelBase1))

    // when
    viewModel.loadData(remoteId, ItemType.CHANNEL, SUPLA_CHANNELFNC_HVAC_THERMOSTAT)

    // then
    assertThat(states).isEmpty()
    assertThat(events).containsExactly(
      ThermostatDetailViewEvent.Close
    )
  }

  private fun mockChannel(subfunction: ThermostatSubfunction, visible: Int = 1): Channel {
    val thermostatValue: ThermostatValue = mockk()
    every { thermostatValue.subfunction } returns subfunction
    val value: ChannelValue = mockk()
    every { value.asThermostatValue() } returns thermostatValue
    return mockk<Channel>().also {
      every { it.func } returns SUPLA_CHANNELFNC_HVAC_THERMOSTAT
      every { it.visible } returns visible
      every { it.value } returns value
    }
  }
}
