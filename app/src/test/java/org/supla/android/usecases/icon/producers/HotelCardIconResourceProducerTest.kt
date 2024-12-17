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
class HotelCardIconResourceProducerTest : BaseIconResourceProducerTest() {

  @InjectMocks
  override lateinit var producer: HotelCardIconResourceProducer

  @Test
  fun `should produce on icon`() {
    test(
      state = ChannelState.Value.ON,
      function = SuplaFunction.HOTEL_CARD_SENSOR,
      expectedIcon = R.drawable.fnc_hotel_card_on
    )
  }

  @Test
  fun `should produce off icon`() {
    test(
      state = ChannelState.Value.OFF,
      function = SuplaFunction.HOTEL_CARD_SENSOR,
      expectedIcon = R.drawable.fnc_hotel_card_off
    )
  }

  @Test
  fun `should produce on icon (night mode)`() {
    test(
      state = ChannelState.Value.ON,
      function = SuplaFunction.HOTEL_CARD_SENSOR,
      expectedIcon = R.drawable.fnc_hotel_card_on
    )
  }

  @Test
  fun `should produce off icon (night mode)`() {
    test(
      state = ChannelState.Value.OFF,
      function = SuplaFunction.HOTEL_CARD_SENSOR,
      expectedIcon = R.drawable.fnc_hotel_card_off
    )
  }
}
