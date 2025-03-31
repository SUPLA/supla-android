package org.supla.android.data.source.remote.valve
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

import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.core.shared.data.model.valve.SuplaValveFlag
import org.supla.core.shared.data.model.valve.ValveValue

@RunWith(MockitoJUnitRunner::class)
class ValveValueTest {
  @Test
  fun `should load valve value from byte array`() {
    // given
    val status = SuplaChannelAvailabilityStatus.ONLINE_BUT_NOT_AVAILABLE
    val bytes = byteArrayOf(1, 2)

    // when
    val value = ValveValue.from(status, bytes)

    // then
    Assertions.assertThat(value).isEqualTo(ValveValue(status, 1, listOf(SuplaValveFlag.MANUALLY_CLOSED)))
  }
}
