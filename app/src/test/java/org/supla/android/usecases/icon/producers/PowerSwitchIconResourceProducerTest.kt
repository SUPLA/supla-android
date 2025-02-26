package org.supla.android.usecases.icon.producers
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

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.R
import org.supla.android.data.model.general.ChannelState
import org.supla.core.shared.data.model.general.SuplaFunction

@RunWith(MockitoJUnitRunner::class)
class PowerSwitchIconResourceProducerTest : BaseIconResourceProducerTest() {

  @InjectMocks
  override lateinit var producer: PowerSwitchIconResourceProducer

  @Test
  fun `should produce on icon`() {
    test(
      state = ChannelState.Value.ON,
      function = SuplaFunction.POWER_SWITCH,
      expectedIcon = R.drawable.fnc_switch_on
    )
  }

  @Test
  fun `should produce off icon`() {
    test(
      state = ChannelState.Value.OFF,
      function = SuplaFunction.POWER_SWITCH,
      expectedIcon = R.drawable.fnc_switch_off
    )
  }

  @Test
  fun `should produce on icon (alt1)`() {
    test(
      state = ChannelState.Value.ON,
      altIcon = 1,
      function = SuplaFunction.POWER_SWITCH,
      expectedIcon = R.drawable.fnc_switch_1_on
    )
  }

  @Test
  fun `should produce off icon (alt1)`() {
    test(
      state = ChannelState.Value.OFF,
      altIcon = 1,
      function = SuplaFunction.POWER_SWITCH,
      expectedIcon = R.drawable.fnc_switch_1_off
    )
  }

  @Test
  fun `should produce on icon (alt2)`() {
    test(
      state = ChannelState.Value.ON,
      altIcon = 2,
      function = SuplaFunction.POWER_SWITCH,
      expectedIcon = R.drawable.fnc_switch_2_on
    )
  }

  @Test
  fun `should produce off icon (alt2)`() {
    test(
      state = ChannelState.Value.OFF,
      altIcon = 2,
      function = SuplaFunction.POWER_SWITCH,
      expectedIcon = R.drawable.fnc_switch_2_off
    )
  }

  @Test
  fun `should produce on icon (alt3)`() {
    test(
      state = ChannelState.Value.ON,
      altIcon = 3,
      function = SuplaFunction.POWER_SWITCH,
      expectedIcon = R.drawable.fnc_switch_3_on
    )
  }

  @Test
  fun `should produce off icon (alt3)`() {
    test(
      state = ChannelState.Value.OFF,
      altIcon = 3,
      function = SuplaFunction.POWER_SWITCH,
      expectedIcon = R.drawable.fnc_switch_3_off
    )
  }

  @Test
  fun `should produce on icon (alt4)`() {
    test(
      state = ChannelState.Value.ON,
      altIcon = 4,
      function = SuplaFunction.POWER_SWITCH,
      expectedIcon = R.drawable.fnc_switch_4_on
    )
  }

  @Test
  fun `should produce off icon (alt4)`() {
    test(
      state = ChannelState.Value.OFF,
      altIcon = 4,
      function = SuplaFunction.POWER_SWITCH,
      expectedIcon = R.drawable.fnc_switch_4_off
    )
  }
}
