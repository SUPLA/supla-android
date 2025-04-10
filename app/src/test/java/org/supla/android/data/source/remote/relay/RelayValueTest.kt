package org.supla.android.data.source.remote.relay
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
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.core.shared.data.model.function.relay.RelayValue
import org.supla.core.shared.data.model.function.relay.SuplaRelayFlag

@RunWith(MockitoJUnitRunner::class)
class RelayValueTest {
  @Test
  fun `should load relay value from byte array`() {
    // given
    val status = SuplaChannelAvailabilityStatus.ONLINE
    val bytes = byteArrayOf(1, 1, 0)

    // when
    val value = RelayValue.from(status, bytes)

    // then
    assertThat(value).isEqualTo(RelayValue(status = status, on = true, flags = listOf(SuplaRelayFlag.OVERCURRENT_RELAY_OFF)))
  }
}
