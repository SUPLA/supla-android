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
class LightSwitchIconResourceProducerTest : BaseIconResourceProducerTest() {

  @InjectMocks
  override lateinit var producer: LightSwitchIconResourceProducer

  @Test
  fun `should produce on icon`() {
    test(
      state = ChannelState.Value.ON,
      function = SuplaFunction.LIGHTSWITCH,
      expectedIcon = R.drawable.fnc_light_on
    )
  }

  @Test
  fun `should produce off icon`() {
    test(
      state = ChannelState.Value.OFF,
      function = SuplaFunction.LIGHTSWITCH,
      expectedIcon = R.drawable.fnc_light_off
    )
  }

  @Test
  fun `should produce on icon (alt1)`() {
    test(
      state = ChannelState.Value.ON,
      altIcon = 1,
      function = SuplaFunction.LIGHTSWITCH,
      expectedIcon = R.drawable.fnc_light_1_on
    )
  }

  @Test
  fun `should produce off icon (alt1)`() {
    test(
      state = ChannelState.Value.OFF,
      altIcon = 1,
      function = SuplaFunction.LIGHTSWITCH,
      expectedIcon = R.drawable.fnc_light_1_off
    )
  }

  @Test
  fun `should produce on icon (alt2)`() {
    test(
      state = ChannelState.Value.ON,
      altIcon = 2,
      function = SuplaFunction.LIGHTSWITCH,
      expectedIcon = R.drawable.fnc_light_2_on
    )
  }

  @Test
  fun `should produce off icon (alt2)`() {
    test(
      state = ChannelState.Value.OFF,
      altIcon = 2,
      function = SuplaFunction.LIGHTSWITCH,
      expectedIcon = R.drawable.fnc_light_2_off
    )
  }
}
