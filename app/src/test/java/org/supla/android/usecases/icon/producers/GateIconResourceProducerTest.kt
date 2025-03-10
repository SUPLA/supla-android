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
class GateIconResourceProducerTest : BaseIconResourceProducerTest() {

  @InjectMocks
  override lateinit var producer: GateIconResourceProducer

  @Test
  fun `should produce open icon`() {
    test(
      state = ChannelState.Value.OPEN,
      function = SuplaFunction.OPEN_SENSOR_GATE,
      expectedIcon = R.drawable.fnc_gate_opened
    )
  }

  @Test
  fun `should produce closed icon`() {
    test(
      state = ChannelState.Value.CLOSED,
      function = SuplaFunction.CONTROLLING_THE_GATE,
      expectedIcon = R.drawable.fnc_gate_closed
    )
  }

  @Test
  fun `should produce partially opened icon`() {
    test(
      state = ChannelState.Value.PARTIALLY_OPENED,
      function = SuplaFunction.CONTROLLING_THE_GATE,
      expectedIcon = R.drawable.fnc_gate_closed_50_percent
    )
  }

  @Test
  fun `should produce open icon (alt 1)`() {
    test(
      state = ChannelState.Value.OPEN,
      altIcon = 1,
      function = SuplaFunction.CONTROLLING_THE_GATE,
      expectedIcon = R.drawable.fnc_gate_1_opened
    )
  }

  @Test
  fun `should produce closed icon (alt 1)`() {
    test(
      state = ChannelState.Value.CLOSED,
      altIcon = 1,
      function = SuplaFunction.CONTROLLING_THE_GATE,
      expectedIcon = R.drawable.fnc_gate_1_closed
    )
  }

  @Test
  fun `should produce partially opened icon (alt 1)`() {
    test(
      state = ChannelState.Value.PARTIALLY_OPENED,
      altIcon = 1,
      function = SuplaFunction.CONTROLLING_THE_GATE,
      expectedIcon = R.drawable.fnc_gate_1_closed_50_percent
    )
  }

  @Test
  fun `should produce open icon (alt 2)`() {
    test(
      state = ChannelState.Value.OPEN,
      altIcon = 2,
      function = SuplaFunction.OPEN_SENSOR_GATE,
      expectedIcon = R.drawable.fnc_barier_opened
    )
  }

  @Test
  fun `should produce closed icon (alt 2)`() {
    test(
      state = ChannelState.Value.CLOSED,
      altIcon = 2,
      function = SuplaFunction.CONTROLLING_THE_GATE,
      expectedIcon = R.drawable.fnc_barier_closed
    )
  }
}
