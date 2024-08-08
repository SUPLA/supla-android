package org.supla.android.data.source.remote.channel
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

enum class SuplaChannelFlag(val rawValue: Long) {
  RS_SBS_AND_STOP_ACTIONS(0x0080),
  RS_AUTO_CALIBRATION(0x1000),
  CALCFG_RESET_COUNTERS(0x2000),
  CALCFG_RECALIBRATE(0x4000),
  CHANNEL_STATE(0x10000),
  PHASE1_UNSUPPORTED(0x20000),
  PHASE2_UNSUPPORTED(0x40000),
  PHASE3_UNSUPPORTED(0x80000),
  TIME_SETTING_NOT_AVAILABLE(0x100000),
  RSA_ENCRYPTED_PIN_REQUIRED(0x200000),
  OFFLINE_DURING_REGISTRATION(0x400000),
  ZIGBEE_BRIDGE(0x800000),
  COUNTDOWN_TIMER_SUPPORTED(0x1000000),
  LIGHT_SOURCE_LIFESPAN_SETTABLE(0x2000000),
  HAS_PARENT(0x20000000);

  infix fun inside(flags: Long): Boolean = (flags and rawValue) > 0L

  infix fun notInside(flags: Long): Boolean = (flags and rawValue) == 0L

  companion object {
    fun from(value: Long): List<SuplaChannelFlag> =
      mutableListOf<SuplaChannelFlag>().apply {
        SuplaChannelFlag.entries.forEach {
          if (it.rawValue.and(value) > 0) {
            add(it)
          }
        }
      }
  }
}

val Long.suplaFlags: List<SuplaChannelFlag>
  get() = SuplaChannelFlag.from(this)
