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
    totalValue.add(RollerShutterGroupValue(position = 80, openSensorActive = true), online = true)
    totalValue.add(RollerShutterGroupValue(position = 50, openSensorActive = false), online = true)
    // offline items should be not added
    totalValue.add(RollerShutterGroupValue(position = 50, openSensorActive = false), online = false)
    totalValue.add(RollerShutterGroupValue(position = 50, openSensorActive = false), online = false)

    // when
    val totalValueString = totalValue.asString()
    val values = GroupTotalValue.parse(SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER, totalValueString)

    // then
    assertThat(totalValue.online).isEqualTo(50)
    assertThat(totalValueString).isEqualTo("80:1|50:0")
    assertThat(values).containsExactly(
      RollerShutterGroupValue(position = 80, openSensorActive = true),
      RollerShutterGroupValue(position = 50, openSensorActive = false)
    )
  }

  @Test
  fun `should convert to string and back - roof window group`() {
    // given
    val totalValue = GroupTotalValue()
    totalValue.add(RollerShutterGroupValue(position = 80, openSensorActive = true), online = true)
    totalValue.add(RollerShutterGroupValue(position = 50, openSensorActive = false), online = true)
    // offline items should be not added
    totalValue.add(RollerShutterGroupValue(position = 50, openSensorActive = false), online = false)
    totalValue.add(RollerShutterGroupValue(position = 50, openSensorActive = false), online = false)

    // when
    val totalValueString = totalValue.asString()
    val values = GroupTotalValue.parse(SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW, totalValueString)

    // then
    assertThat(totalValue.online).isEqualTo(50)
    assertThat(totalValueString).isEqualTo("80:1|50:0")
    assertThat(values).containsExactly(
      RollerShutterGroupValue(position = 80, openSensorActive = true),
      RollerShutterGroupValue(position = 50, openSensorActive = false)
    )
  }

  @Test
  fun `should convert to string and back - facade blind group`() {
    // given
    val totalValue = GroupTotalValue()
    totalValue.add(FacadeBlindGroupValue(position = 80, tilt = 50), online = true)
    totalValue.add(FacadeBlindGroupValue(position = 50, tilt = 15), online = true)
    // offline items should be not added
    totalValue.add(FacadeBlindGroupValue(position = 50, tilt = 0), online = false)
    totalValue.add(FacadeBlindGroupValue(position = 50, tilt = 10), online = false)

    // when
    val totalValueString = totalValue.asString()
    val values = GroupTotalValue.parse(SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND, totalValueString)

    // then
    assertThat(totalValue.online).isEqualTo(50)
    assertThat(totalValueString).isEqualTo("80:50|50:15")
    assertThat(values).containsExactly(
      FacadeBlindGroupValue(position = 80, tilt = 50),
      FacadeBlindGroupValue(position = 50, tilt = 15)
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
