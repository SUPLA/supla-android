package org.supla.android.core.networking.esp
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

const val RESULT_PARAM_ERROR = -3
const val RESULT_COMPAT_ERROR = -2
const val RESULT_CONN_ERROR = -1
const val RESULT_FAILED = 0
const val RESULT_SUCCESS = 1

data class EspConfigResult(
  var resultCode: Int = RESULT_FAILED,
  var deviceName: String? = null,
  var deviceLastState: String? = null,
  var deviceFirmwareVersion: String? = null,
  var deviceGUID: String? = null,
  var deviceMAC: String? = null,
  var needsCloudConfig: Boolean = false
) {
  fun merge(result: EspConfigResult) {
    deviceName = result.deviceName
    deviceLastState = result.deviceLastState
    deviceFirmwareVersion = result.deviceFirmwareVersion
    deviceGUID = result.deviceGUID
    deviceMAC = result.deviceMAC
    needsCloudConfig = result.needsCloudConfig
  }
}
