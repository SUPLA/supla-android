package org.supla.android.core.networking.suplaclient
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

import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.FieldType
import org.supla.android.data.source.remote.SuplaChannelConfig
import org.supla.android.lib.actions.ActionParameters
import org.supla.core.shared.networking.SuplaClientSharedApi
import org.supla.core.shared.networking.SuplaClientSharedProvider
import java.util.EnumSet

interface SuplaClientProvider : SuplaClientSharedProvider {
  override fun provide(): SuplaClientApi?
}

interface SuplaClientApi : SuplaClientSharedApi {
  fun cancel(reason: SuplaClientState.Reason? = null)
  fun canceled(): Boolean

  @Throws(InterruptedException::class)
  fun join()

  fun reconnect()

  fun open(ID: Int, Group: Boolean, Open: Int): Boolean

  fun setRGBW(
    ID: Int,
    Group: Boolean,
    Color: Int,
    ColorBrightness: Int,
    Brightness: Int,
    TurnOnOff: Boolean
  ): Boolean

  fun executeAction(parameters: ActionParameters): Boolean

  fun registerPushNotificationClientToken(appId: Int, token: String, profileName: String): Boolean

  fun registered(): Boolean

  fun timerArm(remoteId: Int, setOn: Boolean, durationInMs: Int): Boolean

  fun getChannelConfig(remoteId: Int, type: ChannelConfigType): Boolean

  fun getChannelState(remoteId: Int): Boolean

  fun setChannelConfig(config: SuplaChannelConfig): Boolean

  fun getDeviceConfig(deviceId: Int, type: EnumSet<FieldType>): Boolean

  fun oAuthTokenRequest()

  fun getDeviceConfig(deviceId: Int) =
    getDeviceConfig(deviceId, EnumSet.allOf(FieldType::class.java))

  fun isSuperUserAuthorized(): Boolean

  fun superUserAuthorizationRequest(email: String, password: String)

  fun deviceCalCfgRequest(remoteId: Int, isGroup: Boolean, command: Int, dataType: Int, data: ByteArray?): Boolean

  fun setChannelCaption(remoteId: Int, caption: String): Boolean

  fun setChannelGroupCaption(remoteId: Int, caption: String): Boolean

  fun setLocationCaption(remoteId: Int, caption: String): Boolean

  fun setSceneCaption(remoteId: Int, caption: String): Boolean
}
