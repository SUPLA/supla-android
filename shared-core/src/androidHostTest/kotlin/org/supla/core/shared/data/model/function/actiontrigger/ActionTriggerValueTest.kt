package org.supla.core.shared.data.model.function.actiontrigger
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
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.data.source.remote.hvac.SuplaButtonMode

class ActionTriggerValueTest {

  @Test
  fun `should parse all fields from byte array`() {
    // given
    val status = SuplaChannelAvailabilityStatus.ONLINE
    val bytes = byteArrayOf(
      42, // related channel number
      0x78,
      0x56,
      0x34,
      0x12, // disables local operation, little-endian
      SuplaButtonMode.CMD_WEEKLY_SCHEDULE.rawValue,
      (
        SuplaActionTriggerFlag.WEEKLY_SCHEDULE_ENABLED.value.toInt() or
          SuplaActionTriggerFlag.TEMPORARY_UNLOCK_ACTIVE.value.toInt()
        ).toByte()
    )

    // when
    val value = ActionTriggerValue.from(status, bytes)

    // then
    assertThat(value.status).isEqualTo(status)
    assertThat(value.relatedChannelNumber).isEqualTo(42)
    assertThat(value.disablesLocalOperation).isEqualTo(0x12345678)
    assertThat(value.buttonMode).isEqualTo(SuplaButtonMode.CMD_WEEKLY_SCHEDULE)
    assertThat(value.flags).containsExactly(
      SuplaActionTriggerFlag.WEEKLY_SCHEDULE_ENABLED,
      SuplaActionTriggerFlag.TEMPORARY_UNLOCK_ACTIVE
    )
  }

  @Test
  fun `should parse every button mode`() {
    SuplaButtonMode.entries.forEach { mode ->
      // when
      val value = ActionTriggerValue.from(
        SuplaChannelAvailabilityStatus.OFFLINE,
        byteArrayOf(1, 0, 0, 0, 0, mode.rawValue, 0)
      )

      // then
      assertThat(value.buttonMode).isEqualTo(mode)
    }
  }

  @Test
  fun `should parse each flag independently`() {
    mapOf(
      SuplaActionTriggerFlag.WEEKLY_SCHEDULE_ENABLED to 0x1,
      SuplaActionTriggerFlag.TEMPORARY_UNLOCK_ACTIVE to 0x2
    ).forEach { (flag, byteValue) ->
      // when
      val value = ActionTriggerValue.from(
        SuplaChannelAvailabilityStatus.ONLINE,
        byteArrayOf(1, 0, 0, 0, 0, 0, byteValue.toByte())
      )

      // then
      assertThat(value.flags).containsExactly(flag)
    }
  }

  @Test
  fun `should return defaults when byte array is too short`() {
    // given
    val status = SuplaChannelAvailabilityStatus.OFFLINE

    // when
    val value = ActionTriggerValue.from(status, byteArrayOf(42, 1, 2, 3, 4, 5))

    // then
    assertThat(value.status).isEqualTo(status)
    assertThat(value.relatedChannelNumber).isZero
    assertThat(value.disablesLocalOperation).isZero
    assertThat(value.buttonMode).isEqualTo(SuplaButtonMode.NOT_SET)
    assertThat(value.flags).isEmpty()
  }
}
