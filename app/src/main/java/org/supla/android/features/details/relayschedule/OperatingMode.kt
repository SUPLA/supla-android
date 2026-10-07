package org.supla.android.features.details.relayschedule
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

import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.data.source.remote.hvac.SuplaRelayMode
import org.supla.core.shared.data.model.function.relay.RelayValue
import org.supla.core.shared.data.model.function.relay.SuplaRelayFlag

@JvmInline
value class OperatingMode(val value: Int) {
  val manualAllowed: Boolean
    get() = value and MASK_MANUAL_ALLOWED == MASK_MANUAL_ALLOWED
  val weeklyAllowed: Boolean
    get() = value and MASK_WEEKLY_ALLOWED == MASK_WEEKLY_ALLOWED
  val autoAllowed: Boolean
    get() = value and MASK_AUTO_ALLOWED == MASK_AUTO_ALLOWED

  val manualActive: Boolean
    get() = value and MASK_MANUAL_ACTIVE == MASK_MANUAL_ACTIVE
  val weeklyActive: Boolean
    get() = value and MASK_WEEKLY_ACTIVE == MASK_WEEKLY_ACTIVE
  val autoActive: Boolean
    get() = value and MASK_AUTO_ACTIVE == MASK_AUTO_ACTIVE

  companion object {
    private const val MASK_MANUAL_ALLOWED = 0x01
    private const val MASK_WEEKLY_ALLOWED = 0x02
    private const val MASK_AUTO_ALLOWED = 0x04
    private const val MASK_MANUAL_ACTIVE = 0x08
    private const val MASK_WEEKLY_ACTIVE = 0x10
    private const val MASK_AUTO_ACTIVE = 0x20

    operator fun invoke(channelFlags: Long, relayValue: RelayValue): OperatingMode? {
      val weeklyAllowed = SuplaChannelFlag.WEEKLY_SCHEDULE inside channelFlags
      val autoAllowed = SuplaChannelFlag.RELAY_MODE_AUTOMATIC_SUPPORTED inside channelFlags

      if (!weeklyAllowed && !autoAllowed) return null

      val weeklyActive = relayValue.flags.contains(SuplaRelayFlag.WEEKLY_SCHEDULE_ENABLED)
      val autoActive = relayValue.mode == SuplaRelayMode.AUTOMATIC && !weeklyActive

      return OperatingMode(
        value =
        MASK_MANUAL_ALLOWED or
          (if (weeklyAllowed) MASK_WEEKLY_ALLOWED else 0) or
          (if (autoAllowed) MASK_AUTO_ALLOWED else 0) or
          (if (!weeklyActive && !autoActive) MASK_MANUAL_ACTIVE else 0) or
          (if (weeklyActive) MASK_WEEKLY_ACTIVE else 0) or
          (if (autoActive) MASK_AUTO_ACTIVE else 0)
      )
    }

    operator fun invoke(
      manualAllowed: Boolean = false,
      weeklyAllowed: Boolean = false,
      autoAllowed: Boolean = false,
      manualActive: Boolean = false,
      weeklyActive: Boolean = false,
      autoActive: Boolean = false
    ) = OperatingMode(
      value =
      (if (manualAllowed) MASK_MANUAL_ALLOWED else 0) or
        (if (weeklyAllowed) MASK_WEEKLY_ALLOWED else 0) or
        (if (autoAllowed) MASK_AUTO_ALLOWED else 0) or
        (if (manualActive) MASK_MANUAL_ACTIVE else 0) or
        (if (weeklyActive) MASK_WEEKLY_ACTIVE else 0) or
        (if (autoActive) MASK_AUTO_ACTIVE else 0)
    )
  }
}
