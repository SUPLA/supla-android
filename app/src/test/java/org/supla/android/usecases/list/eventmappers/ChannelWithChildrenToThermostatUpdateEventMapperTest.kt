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

import com.google.gson.Gson
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.R
import org.supla.android.core.shared.shareable
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.shareable
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
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
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat

class ChannelWithChildrenToThermostatUpdateEventMapperTest {

  @MockK
  private lateinit var getCaptionUseCase: GetCaptionUseCase

  @MockK
  private lateinit var getChannelIconUseCase: GetChannelIconUseCase

  @MockK
  private lateinit var getChannelValueStringUseCase: GetChannelValueStringUseCase

  @MockK
  private lateinit var getChannelIssuesForListUseCase: GetChannelIssuesForListUseCase

  @MockK
  lateinit var valueFormatter: ValueFormatter

  @MockK
  private lateinit var gson: Gson

  @InjectMockKs
  lateinit var mapper: ChannelWithChildrenToThermostatUpdateEventMapper

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

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
      every { status } returns SuplaChannelAvailabilityStatus.ONLINE
      every { flags } returns emptyList()
      every { setpointTemperatureHeat } returns 12.5f
      every { setpointTemperatureCool } returns 12.5f
      every { mode } returns SuplaHvacMode.HEAT
    }
    val thermometerChannel = mockk<ChannelDataEntity>()
    every { thermometerChannel.function } returns SuplaFunction.THERMOMETER
    thermometerChannel.mockShareable()
    val thermometerChannelWithChildren = ChannelWithChildren(thermometerChannel)
    val thermometerChild = mockk<ChannelChildEntity> {
      every { relationType } returns ChannelRelationType.MAIN_THERMOMETER
      every { channelRelationEntity } returns mockk {
        every { channelId } returns 123
        every { parentId } returns 234
        every { relationType } returns ChannelRelationType.MAIN_THERMOMETER
      }
      every { channelDataEntity } returns thermometerChannel
      every { withChildren } returns thermometerChannelWithChildren
      every { children } returns emptyList()
    }
    val channel = mockk<ChannelDataEntity> {
      every { remoteId } returns 123
      every { altIcon } returns 0
      every { this@mockk.caption } returns captionString
      every { function } returns SuplaFunction.HVAC_THERMOSTAT
      every { status } returns SuplaChannelAvailabilityStatus.ONLINE
      every { stateEntity } returns null
      every { channelEntity } returns mockk {
        every { function } returns SuplaFunction.HVAC_THERMOSTAT
      }
      every { channelValueEntity } returns mockk {
        every { status } returns SuplaChannelAvailabilityStatus.ONLINE
        every { asThermostatValue() } returns thermostatValue
        every { getValueAsByteArray() } returns byteArrayOf()
      }
      every { channelExtendedValueEntity } returns mockk {
        every { getSuplaValue() } returns null
      }
      every { flags } returns SuplaChannelFlag.CHANNEL_STATE.rawValue
      every { configEntity } returns null
    }
    val channelWithChildren = ChannelWithChildren(channel, listOf(thermometerChild))

    val channelShareable = channel.shareable
    every { getCaptionUseCase.invoke(channelShareable) } returns caption
    every { getChannelIconUseCase.invoke(channel) } returns icon
    every { getChannelValueStringUseCase(thermometerChannelWithChildren) } returns value
    val channelWithChildrenShareable = channelWithChildren.shareable
    every { getChannelIssuesForListUseCase(channelWithChildrenShareable) } returns channelIssues
    every { valueFormatter.format(12.5f, ValueFormat.WithoutUnit) } returns subValue

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
    val value = NO_VALUE_TEXT
    val subValue = "some sub value"
    val channelIssues = ListItemIssues(IssueIcon.Warning)
    val estimatedEndDate = date(2023, 11, 21)
    val thermostatValue = mockk<ThermostatValue> {
      every { status } returns SuplaChannelAvailabilityStatus.ONLINE
      every { flags } returns emptyList()
      every { setpointTemperatureHeat } returns 12.5f
      every { setpointTemperatureCool } returns 12.5f
      every { mode } returns SuplaHvacMode.HEAT
    }
    val channel = mockk<ChannelDataEntity> {
      every { remoteId } returns 123
      every { altIcon } returns 0
      every { this@mockk.caption } returns captionString
      every { function } returns SuplaFunction.HVAC_THERMOSTAT
      every { status } returns SuplaChannelAvailabilityStatus.ONLINE
      every { stateEntity } returns null
      every { channelEntity } returns mockk {
        every { function } returns SuplaFunction.HVAC_THERMOSTAT
      }
      every { channelValueEntity } returns mockk {
        every { status } returns SuplaChannelAvailabilityStatus.ONLINE
        every { asThermostatValue() } returns thermostatValue
        every { getValueAsByteArray() } returns byteArrayOf()
      }
      every { channelExtendedValueEntity } returns mockk {
        every { getSuplaValue() } returns SuplaChannelExtendedValue().also {
          it.TimerStateValue = SuplaTimerState(estimatedEndDate.toTimestamp(), null, 11, null)
        }
      }
      every { flags } returns 0
      every { configEntity } returns null
    }
    val channelWithChildren = ChannelWithChildren(channel, listOf())

    val channelShareable = channel.shareable
    every { getCaptionUseCase.invoke(channelShareable) } returns caption
    every { getChannelIconUseCase.invoke(channel) } returns icon
    val channelWithChildrenShareable = channelWithChildren.shareable
    every { getChannelIssuesForListUseCase.invoke(channelWithChildrenShareable) } returns channelIssues
    every { valueFormatter.format(12.5f, ValueFormat.WithoutUnit) } returns subValue

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
