package org.supla.core.shared.infrastructure.messaging
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

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.supla.android.data.source.remote.SuplaEventType
import org.supla.android.lib.SuplaChannelBasicCfg
import org.supla.android.lib.SuplaChannelState
import org.supla.android.lib.ZWaveNode
import org.supla.android.lib.ZWaveWakeUpSettings
import org.supla.core.shared.data.model.suplaclient.SuplaResultCode
import kotlin.time.Duration

sealed interface SuplaClientMessage {
  data class ChannelDataChanged(val channelId: Int, val extendedValueChanged: Boolean, val timerValueChanged: Boolean) : SuplaClientMessage
  data class GroupDataChanged(val groupId: Int) : SuplaClientMessage
  data object ClientRegistered : SuplaClientMessage
  data class ClientRegistrationError(val resultCode: SuplaResultCode) : SuplaClientMessage
  data class Event(
    val owner: Boolean,
    val type: SuplaEventType,
    val channelId: Int,
    val duration: Duration,
    val senderId: Int,
    val senderName: String?
  ) : SuplaClientMessage

  data class RegistrationEnabled(
    val clientRegistrationExpirationTimestamp: Long,
    val deviceRegistrationExpirationTimestamp: Long
  ) : SuplaClientMessage {
    val isClientRegistrationEnabled: Boolean
      get() {
        val expirationInstant = Instant.fromEpochMilliseconds(clientRegistrationExpirationTimestamp)
        val currentInstant = Clock.System.now()
        return expirationInstant.toEpochMilliseconds() > currentInstant.toEpochMilliseconds()
      }

    val isDeviceRegistrationEnabled: Boolean
      get() {
        val expirationInstant = Instant.fromEpochMilliseconds(deviceRegistrationExpirationTimestamp)
        val currentInstant = Clock.System.now()
        return expirationInstant.toEpochMilliseconds() > currentInstant.toEpochMilliseconds()
      }
  }

  data class OAuthToken(
    val url: String,
    val creationTimestamp: Long,
    val resultCode: Int,
    val timeToLive: Int,
    val token: String?
  ) : SuplaClientMessage

  data class CallConfigResult(
    val channelId: Int,
    val command: Int,
    val result: Int,
    val data: ByteArray?
  ) : SuplaClientMessage {
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other == null || this::class != other::class) return false

      other as CallConfigResult

      if (channelId != other.channelId) return false
      if (command != other.command) return false
      if (result != other.result) return false
      if (!data.contentEquals(other.data)) return false

      return true
    }

    override fun hashCode(): Int {
      var result1 = channelId
      result1 = 31 * result1 + command
      result1 = 31 * result1 + result
      result1 = 31 * result1 + data.contentHashCode()
      return result1
    }
  }

  data class AuthorizationResult(
    val authorized: Boolean,
    val code: SuplaResultCode
  ) : SuplaClientMessage

  data class ChannelState(
    val channelState: SuplaChannelState
  ) : SuplaClientMessage

  data class ChannelBasicConfig(
    val channelBasicConfig: SuplaChannelBasicCfg
  ) : SuplaClientMessage

  data class ChannelFunctionSetResult(
    val channelId: Int,
    val function: Int,
    val resultCode: Int
  ) : SuplaClientMessage

  data class ChannelCaptionSetResult(
    val channelId: Int,
    val caption: String?,
    val resultCode: Int
  ) : SuplaClientMessage

  data class SetRegistrationEnabledResult(
    val resultCode: SuplaResultCode
  ) : SuplaClientMessage

  data class ZWaveResetAndClearResult(
    val resultCode: Int
  ) : SuplaClientMessage

  data class ZWaveAddNodeResult(
    val resultCode: Int,
    val node: ZWaveNode?
  ) : SuplaClientMessage

  data class ZWaveRemoveNodeResult(
    val resultCode: Int,
    val nodeId: Short
  ) : SuplaClientMessage

  data class ZWaveGetNodeListResult(
    val resultCode: Int,
    val node: ZWaveNode?
  ) : SuplaClientMessage

  data class ZWaveGetAssignedNodeIdResult(
    val resultCode: Int,
    val nodeId: Short
  ) : SuplaClientMessage

  data class ZWaveAssignNodeIdResult(
    val resultCode: Int,
    val nodeId: Short
  ) : SuplaClientMessage

  data class CallConfigProgressReport(
    val channelId: Int,
    val command: Int,
    val progress: Short
  ) : SuplaClientMessage

  data class ZWaveWakeUpSettingsReport(
    val resultCode: Int,
    val settings: ZWaveWakeUpSettings?
  ) : SuplaClientMessage

  data class ZWaveSetWakeUpTimeResult(
    val result: Int
  ) : SuplaClientMessage

  companion object
}
