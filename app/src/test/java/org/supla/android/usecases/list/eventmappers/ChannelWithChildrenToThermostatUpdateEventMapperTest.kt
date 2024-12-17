package org.supla.android.usecases.list.eventmappers
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
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.supla.android.R
import org.supla.android.core.shared.shareable
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.shareable
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.db.Channel
import org.supla.android.extensions.date
import org.supla.android.extensions.toTimestamp
import org.supla.android.images.ImageId
import org.supla.android.lib.SuplaChannelExtendedValue
import org.supla.android.lib.SuplaTimerState
import org.supla.android.testhelpers.extensions.mockShareable
import org.supla.android.ui.lists.ListOnlineState
import org.supla.android.ui.lists.data.SlideableListItemData
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.data.model.function.thermostat.ThermostatValue
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.IssueIcon
import org.supla.core.shared.data.model.lists.ListItemIssues
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.GetChannelIssuesForListUseCase

@RunWith(MockitoJUnitRunner::class)
class ChannelWithChildrenToThermostatUpdateEventMapperTest {

  @Mock
  private lateinit var getCaptionUseCase: GetCaptionUseCase

  @Mock
  private lateinit var getChannelIconUseCase: GetChannelIconUseCase

  @Mock
  private lateinit var getChannelValueStringUseCase: GetChannelValueStringUseCase

  @Mock
  private lateinit var getChannelIssuesForListUseCase: GetChannelIssuesForListUseCase

  @Mock
  lateinit var valuesFormatter: ValuesFormatter

  @InjectMocks
  lateinit var mapper: ChannelWithChildrenToThermostatUpdateEventMapper

  @Test
  fun `should handle channel with children`() {
    // given
    val channel = mockk<ChannelDataEntity> {
      every { channelEntity } returns mockk {
        every { function } returns SuplaFunction.HVAC_THERMOSTAT
      }
    }

    val channelWithChildren = ChannelWithChildren(channel, emptyList())

    // when
    val result = mapper.handle(channelWithChildren)

    // then
    assertThat(result).isTrue
  }

  @Test
  fun `should not handle channel`() {
    // given
    val channel = mockk<Channel>()

    // when
    val result = mapper.handle(channel)

    // then
    assertThat(result).isFalse
  }

  @Test
  fun `should map channel with thermometer to thermostat slideable item`() {
    // given
    val captionString = "some title"
    val caption = LocalizedString.Constant(captionString)
    val icon: ImageId = mockk()
    val value = "some value"
    val subValue = "some sub value"
    val channelIssues = ListItemIssues(IssueIcon.Warning)
    val thermostatValue = mockk<ThermostatValue> {
      every { online } returns true
      every { flags } returns emptyList()
      every { setpointTemperatureHeat } returns 12.5f
      every { setpointTemperatureCool } returns 12.5f
      every { mode } returns SuplaHvacMode.HEAT
    }
    val thermometerChannel = mockk<ChannelDataEntity>()
    every { thermometerChannel.function } returns SuplaFunction.THERMOMETER
    thermometerChannel.mockShareable()
    val thermometerChild = mockk<ChannelChildEntity> {
      every { relationType } returns ChannelRelationType.MAIN_THERMOMETER
      every { channelRelationEntity } returns mockk {
        every { channelId } returns 123
        every { parentId } returns 234
        every { relationType } returns ChannelRelationType.MAIN_THERMOMETER
      }
      every { channelDataEntity } returns thermometerChannel
      every { children } returns emptyList()
    }
    val channel = mockk<ChannelDataEntity> {
      every { remoteId } returns 123
      every { this@mockk.caption } returns captionString
      every { function } returns SuplaFunction.HVAC_THERMOSTAT
      every { isOnline() } returns true
      every { stateEntity } returns null
      every { channelEntity } returns mockk {
        every { function } returns SuplaFunction.HVAC_THERMOSTAT
      }
      every { channelValueEntity } returns mockk {
        every { online } returns true
        every { asThermostatValue() } returns thermostatValue
        every { getValueAsByteArray() } returns byteArrayOf()
      }
      every { channelExtendedValueEntity } returns mockk {
        every { getSuplaValue() } returns null
      }
      every { flags } returns SuplaChannelFlag.CHANNEL_STATE.rawValue
    }
    val channelWithChildren = ChannelWithChildren(channel, listOf(thermometerChild))

    val channelShareable = channel.shareable
    whenever(getCaptionUseCase.invoke(channelShareable)).thenReturn(caption)
    whenever(getChannelIconUseCase.invoke(channel)).thenReturn(icon)
    whenever(getChannelValueStringUseCase(thermometerChannel)).thenReturn(value)
    val channelWithChildrenShareable = channelWithChildren.shareable
    whenever(getChannelIssuesForListUseCase(channelWithChildrenShareable)).thenReturn(channelIssues)
    whenever(valuesFormatter.getTemperatureString(12.5f)).thenReturn(subValue)

    // when
    val result = mapper.map(channelWithChildren) as SlideableListItemData.Thermostat

    // then
    assertThat(result.onlineState).isEqualTo(ListOnlineState.ONLINE)
    assertThat(result.title).isEqualTo(caption)
    assertThat(result.icon).isEqualTo(icon)
    assertThat(result.value).isEqualTo(value)
    assertThat(result.subValue).isEqualTo(subValue)
    assertThat(result.indicatorIcon).isEqualTo(R.drawable.ic_standby)
    assertThat(result.issues).isEqualTo(channelIssues)
    assertThat(result.estimatedTimerEndDate).isNull()
    assertThat(result.infoSupported).isEqualTo(true)
  }

  @Test
  fun `should map channel with thermometer to thermostat slideable item without main thermometer`() {
    // given
    val captionString = "some title"
    val caption = LocalizedString.Constant(captionString)
    val icon: ImageId = mockk()
    val value = ValuesFormatter.NO_VALUE_TEXT
    val subValue = "some sub value"
    val channelIssues = ListItemIssues(IssueIcon.Warning)
    val estimatedEndDate = date(2023, 11, 21)
    val thermostatValue = mockk<ThermostatValue> {
      every { online } returns true
      every { flags } returns emptyList()
      every { setpointTemperatureHeat } returns 12.5f
      every { setpointTemperatureCool } returns 12.5f
      every { mode } returns SuplaHvacMode.HEAT
    }
    val channel = mockk<ChannelDataEntity> {
      every { remoteId } returns 123
      every { this@mockk.caption } returns captionString
      every { function } returns SuplaFunction.HVAC_THERMOSTAT
      every { isOnline() } returns true
      every { stateEntity } returns null
      every { channelEntity } returns mockk {
        every { function } returns SuplaFunction.HVAC_THERMOSTAT
      }
      every { channelValueEntity } returns mockk {
        every { online } returns true
        every { asThermostatValue() } returns thermostatValue
        every { getValueAsByteArray() } returns byteArrayOf()
      }
      every { channelExtendedValueEntity } returns mockk {
        every { getSuplaValue() } returns SuplaChannelExtendedValue().also {
          it.TimerStateValue = SuplaTimerState(estimatedEndDate.toTimestamp(), null, 11, null)
        }
      }
      every { flags } returns 0
    }
    val channelWithChildren = ChannelWithChildren(channel, listOf())

    whenever(getCaptionUseCase.invoke(channel.shareable)).thenReturn(caption)
    whenever(getChannelIconUseCase.invoke(channel)).thenReturn(icon)
    whenever(getChannelIssuesForListUseCase.invoke(channelWithChildren.shareable)).thenReturn(channelIssues)
    whenever(valuesFormatter.getTemperatureString(12.5f)).thenReturn(subValue)

    // when
    val result = mapper.map(channelWithChildren) as SlideableListItemData.Thermostat

    // then
    assertThat(result.onlineState).isEqualTo(ListOnlineState.ONLINE)
    assertThat(result.title).isEqualTo(caption)
    assertThat(result.icon).isEqualTo(icon)
    assertThat(result.value).isEqualTo(value)
    assertThat(result.subValue).isEqualTo(subValue)
    assertThat(result.indicatorIcon).isEqualTo(R.drawable.ic_standby)
    assertThat(result.issues).isEqualTo(channelIssues)
    assertThat(result.estimatedTimerEndDate).isEqualTo(estimatedEndDate)
    assertThat(result.infoSupported).isEqualTo(false)
  }
}
