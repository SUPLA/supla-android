package org.supla.android.core.shared
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

import org.supla.android.data.source.remote.SuplaEventType
import org.supla.android.lib.SuplaEvent
import org.supla.android.lib.SuplaOAuthToken
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessage
import timber.log.Timber
import java.net.URL
import kotlin.time.Duration.Companion.milliseconds

fun SuplaClientMessage.Companion.from(event: SuplaEvent, clientId: Int): SuplaClientMessage =
  SuplaClientMessage.Event(
    owner = event.SenderID == clientId,
    type = SuplaEventType.from(event.Event) ?: SuplaEventType.UNKNOWN,
    channelId = event.ChannelID,
    duration = event.DurationMS.milliseconds,
    senderId = event.SenderID,
    senderName = event.SenderName
  )

fun SuplaClientMessage.Companion.from(token: SuplaOAuthToken): SuplaClientMessage =
  SuplaClientMessage.OAuthToken(
    url = token.url.toString(),
    creationTimestamp = token.birthday,
    resultCode = token.resultCode,
    timeToLive = token.expiresIn,
    token = token.token
  )

val SuplaClientMessage.Event.event: SuplaEvent
  get() = SuplaEvent().also {
    it.Owner = owner
    it.Event = type.value
    it.ChannelID = channelId
    it.DurationMS = duration.inWholeMilliseconds
    it.SenderID = senderId
    it.SenderName = senderName
  }

val SuplaClientMessage.OAuthToken.suplaToken: SuplaOAuthToken
  get() = SuplaOAuthToken(resultCode, timeToLive, token).also {
    try {
      it.url = URL(url)
    } catch (exception: Exception) {
      Timber.w(exception, "Could not set url")
    }
  }
