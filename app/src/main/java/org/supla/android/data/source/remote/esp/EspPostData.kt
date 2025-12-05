package org.supla.android.data.source.remote.esp
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

import org.supla.android.extensions.isNotNull
import org.supla.core.shared.extensions.ifTrue

class EspPostData(
  val fieldMap: MutableMap<String, String>
) {

  var ssid: String?
    get() = fieldMap[FIELD_SSID]
    set(value) {
      fieldMap.putOrRemove(FIELD_SSID, value)
    }

  var password: String?
    get() = fieldMap[FIELD_PASSWORD]
    set(value) {
      fieldMap.putOrRemove(FIELD_PASSWORD, value)
    }

  var server: String?
    get() = fieldMap[FIELD_SERVER]
    set(value) {
      fieldMap.putOrRemove(FIELD_SERVER, value)
    }

  var email: String?
    get() = fieldMap[FIELD_EMAIL]
    set(value) {
      fieldMap.putOrRemove(FIELD_EMAIL, value)
    }

  var softwareUpdate: Boolean?
    get() = fieldMap[FIELD_UPDATE]?.let { it == "1" }
    set(value) {
      fieldMap.putOrRemove(FIELD_UPDATE, value)
    }

  var protocol: EspDeviceProtocol?
    get() = fieldMap[FIELD_PROTO]?.toIntOrNull()?.let { EspDeviceProtocol.from(it) }
    set(value) {
      fieldMap.putOrRemove(FIELD_PROTO, value?.id)
    }

  var reboot: Boolean?
    get() = fieldMap[FIELD_REBOOT]?.let { it == "1" }
    set(value) {
      fieldMap.putOrRemove(FIELD_REBOOT, value)
    }

  val isCompatible: Boolean
    get() = ssid.isNotNull && password.isNotNull && server.isNotNull && email.isNotNull

  companion object {
    const val FIELD_SSID = "sid"
    const val FIELD_PASSWORD = "wpw"
    const val FIELD_SERVER = "svr"
    const val FIELD_EMAIL = "eml"
    const val FIELD_UPDATE = "upd"
    const val FIELD_PROTO = "pro"
    const val FIELD_REBOOT = "rbt"
  }
}

sealed interface EspDeviceProtocol {
  val id: Int

  data object Supla : EspDeviceProtocol {
    override val id: Int = 0
  }

  data object MQTT : EspDeviceProtocol {
    override val id: Int = 1
  }

  data class Unknown(override val id: Int) : EspDeviceProtocol

  companion object {
    fun from(id: Int): EspDeviceProtocol =
      when (id) {
        0 -> Supla
        1 -> MQTT
        else -> Unknown(id)
      }
  }
}

private fun MutableMap<String, String>.putOrRemove(key: String, value: String?) {
  if (value != null) {
    put(key, value)
  } else {
    remove(key)
  }
}

private fun MutableMap<String, String>.putOrRemove(key: String, value: Boolean?) {
  if (value != null) {
    put(key, value.ifTrue { "1" } ?: "0")
  } else {
    remove(key)
  }
}

private fun MutableMap<String, String>.putOrRemove(key: String, value: Int?) {
  if (value != null) {
    put(key, value.toString())
  } else {
    remove(key)
  }
}
