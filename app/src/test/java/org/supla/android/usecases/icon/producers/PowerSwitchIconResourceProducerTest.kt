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
import org.supla.android.lib.SuplaConst

@RunWith(MockitoJUnitRunner::class)
class PowerSwitchIconResourceProducerTest : BaseIconResourceProducerTest() {

  @InjectMocks
  override lateinit var producer: PowerSwitchIconResourceProducer

  @Test
  fun `should produce on icon`() {
    test(
      state = ChannelState.Value.ON,
      function = SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
      expectedIcon = R.drawable.poweron
    )
  }

  @Test
  fun `should produce off icon`() {
    test(
      state = ChannelState.Value.OFF,
      function = SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
      expectedIcon = R.drawable.poweroff
    )
  }

  @Test
  fun `should produce on icon (night mode)`() {
    test(
      state = ChannelState.Value.ON,
      function = SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
      expectedIcon = R.drawable.poweron_nightmode,
      nightMode = true
    )
  }

  @Test
  fun `should produce off icon (night mode)`() {
    test(
      state = ChannelState.Value.OFF,
      function = SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
      expectedIcon = R.drawable.poweroff_nightmode,
      nightMode = true
    )
  }

  @Test
  fun `should produce on icon (alt1)`() {
    test(
      state = ChannelState.Value.ON,
      function = SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
      altIcon = 1,
      expectedIcon = R.drawable.tvon
    )
  }

  @Test
  fun `should produce off icon (alt1)`() {
    test(
      state = ChannelState.Value.OFF,
      function = SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
      altIcon = 1,
      expectedIcon = R.drawable.tvoff
    )
  }

  @Test
  fun `should produce on icon (alt1, night mode)`() {
    test(
      state = ChannelState.Value.ON,
      function = SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
      altIcon = 1,
      expectedIcon = R.drawable.tvon_nightmode,
      nightMode = true
    )
  }

  @Test
  fun `should produce off icon (alt1, night mode)`() {
    test(
      state = ChannelState.Value.OFF,
      function = SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
      altIcon = 1,
      expectedIcon = R.drawable.tvoff_nightmode,
      nightMode = true
    )
  }

  @Test
  fun `should produce on icon (alt2)`() {
    test(
      state = ChannelState.Value.ON,
      function = SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
      altIcon = 2,
      expectedIcon = R.drawable.radioon
    )
  }

  @Test
  fun `should produce off icon (alt2)`() {
    test(
      state = ChannelState.Value.OFF,
      function = SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
      altIcon = 2,
      expectedIcon = R.drawable.radiooff
    )
  }

  @Test
  fun `should produce on icon (alt2, night mode)`() {
    test(
      state = ChannelState.Value.ON,
      function = SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
      altIcon = 2,
      expectedIcon = R.drawable.radioon_nightmode,
      nightMode = true
    )
  }

  @Test
  fun `should produce off icon (alt2, night mode)`() {
    test(
      state = ChannelState.Value.OFF,
      function = SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
      altIcon = 2,
      expectedIcon = R.drawable.radiooff_nightmode,
      nightMode = true
    )
  }

  @Test
  fun `should produce on icon (alt3)`() {
    test(
      state = ChannelState.Value.ON,
      function = SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
      altIcon = 3,
      expectedIcon = R.drawable.pcon
    )
  }

  @Test
  fun `should produce off icon (alt3)`() {
    test(
      state = ChannelState.Value.OFF,
      function = SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
      altIcon = 3,
      expectedIcon = R.drawable.pcoff
    )
  }

  @Test
  fun `should produce on icon (alt3, night mode)`() {
    test(
      state = ChannelState.Value.ON,
      function = SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
      altIcon = 3,
      expectedIcon = R.drawable.pcon_nightmode,
      nightMode = true
    )
  }

  @Test
  fun `should produce off icon (alt3, night mode)`() {
    test(
      state = ChannelState.Value.OFF,
      function = SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
      altIcon = 3,
      expectedIcon = R.drawable.pcoff_nightmode,
      nightMode = true
    )
  }

  @Test
  fun `should produce on icon (alt4)`() {
    test(
      state = ChannelState.Value.ON,
      function = SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
      altIcon = 4,
      expectedIcon = R.drawable.fanon
    )
  }

  @Test
  fun `should produce off icon (alt4)`() {
    test(
      state = ChannelState.Value.OFF,
      function = SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
      altIcon = 4,
      expectedIcon = R.drawable.fanoff
    )
  }

  @Test
  fun `should produce on icon (alt4, night mode)`() {
    test(
      state = ChannelState.Value.ON,
      function = SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
      altIcon = 4,
      expectedIcon = R.drawable.fanon_nightmode,
      nightMode = true
    )
  }

  @Test
  fun `should produce off icon (alt4, night mode)`() {
    test(
      state = ChannelState.Value.OFF,
      function = SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
      altIcon = 4,
      expectedIcon = R.drawable.fanoff_nightmode,
      nightMode = true
    )
  }
}
