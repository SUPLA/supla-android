package org.supla.android.usecases.group
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
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_PROJECTOR_SCREEN
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS

@RunWith(MockitoJUnitRunner::class)
class GetGroupActivePercentageUseCaseTest {

  @InjectMocks
  private lateinit var useCase: GetGroupActivePercentageUseCase

  @Test
  fun `should get active percentage for power switch group`() {
    // given
    val group: ChannelGroupEntity = mockk {
      every { function } returns SUPLA_CHANNELFNC_POWERSWITCH
      every { totalValue } returns "1|0|1|1"
    }

    // when
    val activePercentage = useCase.invoke(group)

    // then
    assertThat(activePercentage).isEqualTo(75)
  }

  @Test
  fun `should get active percentage for roller shutter`() {
    // given
    val group: ChannelGroupEntity = mockk {
      every { function } returns SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
      every { totalValue } returns "80:1|50:0|100:0|100:0"
    }

    // when
    val activePercentage = useCase.invoke(group)

    // then
    assertThat(activePercentage).isEqualTo(75)
  }

  @Test
  fun `should get active percentage for facade blind`() {
    // given
    val group: ChannelGroupEntity = mockk {
      every { function } returns SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND
      every { totalValue } returns "80:50|50:10|100:0|100:0"
    }

    // when
    val activePercentage = useCase.invoke(group)

    // then
    assertThat(activePercentage).isEqualTo(50)
  }

  @Test
  fun `should get active percentage for projector screen`() {
    // given
    val group: ChannelGroupEntity = mockk {
      every { function } returns SUPLA_CHANNELFNC_PROJECTOR_SCREEN
      every { totalValue } returns "100|50|100|100"
    }

    // when
    val activePercentage = useCase.invoke(group)

    // then
    assertThat(activePercentage).isEqualTo(75)
  }

  @Test
  fun `should get active percentage for dimmer`() {
    // given
    val group: ChannelGroupEntity = mockk {
      every { function } returns SUPLA_CHANNELFNC_DIMMER
      every { totalValue } returns "100|0|100|100"
    }

    // when
    val activePercentage = useCase.invoke(group)

    // then
    assertThat(activePercentage).isEqualTo(75)
  }

  @Test
  fun `should get active percentage for rgb`() {
    // given
    val group: ChannelGroupEntity = mockk {
      every { function } returns SUPLA_CHANNELFNC_RGBLIGHTING
      every { totalValue } returns "20:100|20:0|40:100|10:100"
    }

    // when
    val activePercentage = useCase.invoke(group)

    // then
    assertThat(activePercentage).isEqualTo(75)
  }

  @Test
  fun `should get active percentage for dimmer and rgb`() {
    // given
    val group: ChannelGroupEntity = mockk {
      every { function } returns SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING
      every { totalValue } returns "20:100:40|20:0:0|40:100:20|10:100:10"
    }

    // when
    val activePercentage = useCase.invoke(group)

    // then
    assertThat(activePercentage).isEqualTo(75)
  }

  @Test
  fun `should get active percentage for heatpol thermostat`() {
    // given
    val group: ChannelGroupEntity = mockk {
      every { function } returns SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS
      every { totalValue } returns "1:10:40|0:0:0|1:10:20|0:10:10"
    }

    // when
    val activePercentage = useCase.invoke(group)

    // then
    assertThat(activePercentage).isEqualTo(50)
  }
}
