package org.supla.android.usecases.group.totalvalue
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

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.lib.SuplaConst

@RunWith(MockitoJUnitRunner::class)
class GroupTotalValueTest {

  @Test
  fun `should convert to string and back - roller shutter group`() {
    // given
    val totalValue = GroupTotalValue()
    totalValue.add(ShadingSystemGroupValue(position = 80, closeSensorActive = true), online = true)
    totalValue.add(ShadingSystemGroupValue(position = 50, closeSensorActive = false), online = true)
    // offline items should be not added
    totalValue.add(ShadingSystemGroupValue(position = 50, closeSensorActive = false), online = false)
    totalValue.add(ShadingSystemGroupValue(position = 50, closeSensorActive = false), online = false)

    // when
    val totalValueString = totalValue.asString()
    val values = GroupTotalValue.parse(SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER, totalValueString)

    // then
    assertThat(totalValue.online).isEqualTo(50)
    assertThat(totalValueString).isEqualTo("80:1|50:0")
    assertThat(values).containsExactly(
      ShadingSystemGroupValue(position = 80, closeSensorActive = true),
      ShadingSystemGroupValue(position = 50, closeSensorActive = false)
    )
  }

  @Test
  fun `should convert to string and back - roof window group`() {
    // given
    val totalValue = GroupTotalValue()
    totalValue.add(ShadingSystemGroupValue(position = 80, closeSensorActive = true), online = true)
    totalValue.add(ShadingSystemGroupValue(position = 50, closeSensorActive = false), online = true)
    // offline items should be not added
    totalValue.add(ShadingSystemGroupValue(position = 50, closeSensorActive = false), online = false)
    totalValue.add(ShadingSystemGroupValue(position = 50, closeSensorActive = false), online = false)

    // when
    val totalValueString = totalValue.asString()
    val values = GroupTotalValue.parse(SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW, totalValueString)

    // then
    assertThat(totalValue.online).isEqualTo(50)
    assertThat(totalValueString).isEqualTo("80:1|50:0")
    assertThat(values).containsExactly(
      ShadingSystemGroupValue(position = 80, closeSensorActive = true),
      ShadingSystemGroupValue(position = 50, closeSensorActive = false)
    )
  }

  @Test
  fun `should convert to string and back - facade blind group`() {
    // given
    val totalValue = GroupTotalValue()
    totalValue.add(ShadowingBlindGroupValue(position = 80, tilt = 50), online = true)
    totalValue.add(ShadowingBlindGroupValue(position = 50, tilt = 15), online = true)
    // offline items should be not added
    totalValue.add(ShadowingBlindGroupValue(position = 50, tilt = 0), online = false)
    totalValue.add(ShadowingBlindGroupValue(position = 50, tilt = 10), online = false)

    // when
    val totalValueString = totalValue.asString()
    val values = GroupTotalValue.parse(SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND, totalValueString)

    // then
    assertThat(totalValue.online).isEqualTo(50)
    assertThat(totalValueString).isEqualTo("80:50|50:15")
    assertThat(values).containsExactly(
      ShadowingBlindGroupValue(position = 80, tilt = 50),
      ShadowingBlindGroupValue(position = 50, tilt = 15)
    )
  }

  @Test
  fun `should convert to string and back - terrace awning group`() {
    // given
    val totalValue = GroupTotalValue()
    totalValue.add(ShadingSystemGroupValue(position = 80, closeSensorActive = true), online = true)
    totalValue.add(ShadingSystemGroupValue(position = 50, closeSensorActive = false), online = true)
    // offline items should be not added
    totalValue.add(ShadingSystemGroupValue(position = 50, closeSensorActive = false), online = false)
    totalValue.add(ShadingSystemGroupValue(position = 50, closeSensorActive = false), online = false)

    // when
    val totalValueString = totalValue.asString()
    val values = GroupTotalValue.parse(SuplaConst.SUPLA_CHANNELFNC_TERRACE_AWNING, totalValueString)

    // then
    assertThat(totalValue.online).isEqualTo(50)
    assertThat(totalValueString).isEqualTo("80:1|50:0")
    assertThat(values).containsExactly(
      ShadingSystemGroupValue(position = 80, closeSensorActive = true),
      ShadingSystemGroupValue(position = 50, closeSensorActive = false)
    )
  }

  @Test
  fun `should convert to string and back - projector screen group`() {
    // given
    val totalValue = GroupTotalValue()
    totalValue.add(ProjectorScreenGroupValue(position = 80), online = true)
    totalValue.add(ProjectorScreenGroupValue(position = 50), online = true)
    // offline items should be not added
    totalValue.add(ProjectorScreenGroupValue(position = 50), online = false)
    totalValue.add(ProjectorScreenGroupValue(position = 50), online = false)

    // when
    val totalValueString = totalValue.asString()
    val values = GroupTotalValue.parse(SuplaConst.SUPLA_CHANNELFNC_PROJECTOR_SCREEN, totalValueString)

    // then
    assertThat(totalValue.online).isEqualTo(50)
    assertThat(totalValueString).isEqualTo("80|50")
    assertThat(values).containsExactly(
      ProjectorScreenGroupValue(position = 80),
      ProjectorScreenGroupValue(position = 50)
    )
  }

  @Test
  fun `should convert to string and back - curtain group`() {
    // given
    val totalValue = GroupTotalValue()
    totalValue.add(ShadingSystemGroupValue(position = 80, closeSensorActive = true), online = true)
    totalValue.add(ShadingSystemGroupValue(position = 50, closeSensorActive = false), online = true)
    // offline items should be not added
    totalValue.add(ShadingSystemGroupValue(position = 50, closeSensorActive = false), online = false)
    totalValue.add(ShadingSystemGroupValue(position = 50, closeSensorActive = false), online = false)

    // when
    val totalValueString = totalValue.asString()
    val values = GroupTotalValue.parse(SuplaConst.SUPLA_CHANNELFNC_CURTAIN, totalValueString)

    // then
    assertThat(totalValue.online).isEqualTo(50)
    assertThat(totalValueString).isEqualTo("80:1|50:0")
    assertThat(values).containsExactly(
      ShadingSystemGroupValue(position = 80, closeSensorActive = true),
      ShadingSystemGroupValue(position = 50, closeSensorActive = false)
    )
  }

  @Test
  fun `should convert to string and back - vertical blind`() {
    // given
    val totalValue = GroupTotalValue()
    totalValue.add(ShadingSystemGroupValue(position = 80, closeSensorActive = true), online = true)
    totalValue.add(ShadingSystemGroupValue(position = 50, closeSensorActive = false), online = true)
    // offline items should be not added
    totalValue.add(ShadingSystemGroupValue(position = 50, closeSensorActive = false), online = false)
    totalValue.add(ShadingSystemGroupValue(position = 50, closeSensorActive = false), online = false)

    // when
    val totalValueString = totalValue.asString()
    val values = GroupTotalValue.parse(SuplaConst.SUPLA_CHANNELFNC_VERTICAL_BLIND, totalValueString)

    // then
    assertThat(totalValue.online).isEqualTo(50)
    assertThat(totalValueString).isEqualTo("80:1|50:0")
    assertThat(values).containsExactly(
      ShadingSystemGroupValue(position = 80, closeSensorActive = true),
      ShadingSystemGroupValue(position = 50, closeSensorActive = false)
    )
  }

  @Test
  fun `should convert to string and back - empty list`() {
    // given
    val totalValue = GroupTotalValue()
    totalValue.add(GeneralGroupValue(), online = true)

    // when
    val totalValueString = totalValue.asString()
    val values = GroupTotalValue.parse(SuplaConst.SUPLA_CHANNELFNC_ALARM, totalValueString)

    // then
    assertThat(totalValue.online).isEqualTo(100)
    assertThat(totalValueString).isEqualTo("")
    assertThat(values).isEmpty()
  }
}
