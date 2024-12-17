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
class DigiglassIconResourceProducerTest : BaseIconResourceProducerTest() {

  @InjectMocks
  override lateinit var producer: DigiglassIconResourceProducer

  @Test
  fun `should produce opaque icon`() {
    test(
      state = ChannelState.Value.OPAQUE,
      function = SuplaFunction.DIGIGLASS_HORIZONTAL,
      expectedIcon = R.drawable.digiglass
    )
  }

  @Test
  fun `should produce transparent icon`() {
    test(
      state = ChannelState.Value.TRANSPARENT,
      function = SuplaFunction.DIGIGLASS_HORIZONTAL,
      expectedIcon = R.drawable.digiglasstransparent
    )
  }

  @Test
  fun `should produce opaque icon (alt1)`() {
    test(
      state = ChannelState.Value.OPAQUE,
      altIcon = 1,
      function = SuplaFunction.DIGIGLASS_VERTICAL,
      expectedIcon = R.drawable.digiglass1
    )
  }

  @Test
  fun `should produce transparent icon (alt1)`() {
    test(
      state = ChannelState.Value.TRANSPARENT,
      altIcon = 1,
      function = SuplaFunction.DIGIGLASS_VERTICAL,
      expectedIcon = R.drawable.digiglasstransparent1
    )
  }
}
