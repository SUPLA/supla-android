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

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.data.source.remote.thermostat.ThermostatValue
import org.supla.android.db.Channel
import org.supla.android.extensions.date
import org.supla.android.extensions.toTimestamp
import org.supla.android.images.ImageId
import org.supla.android.lib.SuplaChannelExtendedValue
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT
import org.supla.android.lib.SuplaTimerState
import org.supla.android.ui.lists.data.IssueIconType
import org.supla.android.ui.lists.data.SlideableListItemData
import org.supla.android.usecases.channel.ChannelWithChildren
import org.supla.android.usecases.channel.GetChannelCaptionUseCase
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase

@RunWith(MockitoJUnitRunner::class)
class ChannelWithChildrenToThermostatUpdateEventMapperTest {

  @Mock
  private lateinit var getChannelCaptionUseCase: GetChannelCaptionUseCase

  @Mock
  private lateinit var getChannelIconUseCase: GetChannelIconUseCase

  @Mock
  private lateinit var getChannelValueStringUseCase: GetChannelValueStringUseCase

  @Mock
  lateinit var valuesFormatter: ValuesFormatter

  @InjectMocks
  lateinit var mapper: ChannelWithChildrenToThermostatUpdateEventMapper

  @Test
  fun `should handle channel with children`() {
    // given
    val channel = mockk<ChannelDataEntity> {
      every { channelEntity } returns mockk {
        every { function } returns SUPLA_CHANNELFNC_HVAC_THERMOSTAT
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
    val caption = "some title"
    val icon: ImageId = mockk()
    val value = "some value"
    val subValue = "some sub value"
    val indicatorIcon = 123
    val issueIconType = IssueIconType.WARNING
    val thermostatValue = mockk<ThermostatValue> {
      every { getSetpointText(valuesFormatter) } returns subValue
      every { getIndicatorIcon() } returns indicatorIcon
      every { getIssueIconType() } returns issueIconType
    }
    val thermometerChannel = mockk<ChannelDataEntity>()
    val thermometerChild = mockk<ChannelChildEntity> {
      every { relationType } returns ChannelRelationType.MAIN_THERMOMETER
      every { channelDataEntity } returns thermometerChannel
    }
    val channel = mockk<ChannelDataEntity> {
      every { channelEntity } returns mockk {
        every { function } returns SUPLA_CHANNELFNC_HVAC_THERMOSTAT
      }
      every { channelValueEntity } returns mockk {
        every { online } returns true
        every { asThermostatValue() } returns thermostatValue
      }
      every { channelExtendedValueEntity } returns mockk {
        every { getSuplaValue() } returns null
      }
      every { flags } returns SuplaChannelFlag.CHANNEL_STATE.rawValue
    }
    val channelWithChildren = ChannelWithChildren(channel, listOf(thermometerChild))
    val context: Context = mockk()

    whenever(getChannelCaptionUseCase.invoke(channel)).thenReturn { caption }
    whenever(getChannelIconUseCase.invoke(channel)).thenReturn(icon)
    whenever(getChannelValueStringUseCase(thermometerChannel)).thenReturn(value)

    // when
    val result = mapper.map(channelWithChildren) as SlideableListItemData.Thermostat

    // then
    assertThat(result.online).isTrue
    assertThat(result.titleProvider(context)).isEqualTo(caption)
    assertThat(result.icon).isEqualTo(icon)
    assertThat(result.value).isEqualTo(value)
    assertThat(result.subValue).isEqualTo(subValue)
    assertThat(result.indicatorIcon).isEqualTo(indicatorIcon)
    assertThat(result.issueIconType).isEqualTo(issueIconType)
    assertThat(result.estimatedTimerEndDate).isEqualTo(null)
    assertThat(result.infoSupported).isEqualTo(true)
  }

  @Test
  fun `should map channel with thermometer to thermostat slideable item without main thermometer`() {
    // given
    val caption = "some title"
    val icon: ImageId = mockk()
    val value = ValuesFormatter.NO_VALUE_TEXT
    val subValue = "some sub value"
    val indicatorIcon = 123
    val issueIconType = IssueIconType.WARNING
    val estimatedEndDate = date(2023, 11, 21)
    val thermostatValue = mockk<ThermostatValue> {
      every { getSetpointText(valuesFormatter) } returns subValue
      every { getIndicatorIcon() } returns indicatorIcon
      every { getIssueIconType() } returns issueIconType
    }
    val channel = mockk<ChannelDataEntity> {
      every { channelEntity } returns mockk {
        every { function } returns SUPLA_CHANNELFNC_HVAC_THERMOSTAT
      }
      every { channelValueEntity } returns mockk {
        every { online } returns true
        every { asThermostatValue() } returns thermostatValue
      }
      every { channelExtendedValueEntity } returns mockk {
        every { getSuplaValue() } returns SuplaChannelExtendedValue().also {
          it.TimerStateValue = SuplaTimerState(estimatedEndDate.toTimestamp(), null, 11, null)
        }
      }
      every { flags } returns 0
    }
    val channelWithChildren = ChannelWithChildren(channel, listOf())
    val context: Context = mockk()

    whenever(getChannelCaptionUseCase.invoke(channel)).thenReturn { caption }
    whenever(getChannelIconUseCase.invoke(channel)).thenReturn(icon)

    // when
    val result = mapper.map(channelWithChildren) as SlideableListItemData.Thermostat

    // then
    assertThat(result.online).isTrue
    assertThat(result.titleProvider(context)).isEqualTo(caption)
    assertThat(result.icon).isEqualTo(icon)
    assertThat(result.value).isEqualTo(value)
    assertThat(result.subValue).isEqualTo(subValue)
    assertThat(result.indicatorIcon).isEqualTo(indicatorIcon)
    assertThat(result.issueIconType).isEqualTo(issueIconType)
    assertThat(result.estimatedTimerEndDate).isEqualTo(estimatedEndDate)
    assertThat(result.infoSupported).isEqualTo(false)
  }
}
