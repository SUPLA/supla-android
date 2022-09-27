package org.supla.android.lib.singlecall

import org.supla.android.tools.UsedFromNativeCode

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

/**
 * Possible codes:
 * SUPLA_RESULT_VERSION_ERROR - It applies when the protocol version is beyond the range
 * accepted by the server.
 *
 * SUPLA_RESULT_HOST_NOT_FOUND
 * SUPLA_RESULT_CANT_CONNECT_TO_HOST
 * SUPLA_RESULT_RESPONSE_TIMEOUT
 *
 * SUPLA_RESULTCODE_GUID_ERROR
 * SUPLA_RESULTCODE_AUTHKEY_ERROR
 * SUPLA_RESULTCODE_TEMPORARILY_UNAVAILABLE
 * SUPLA_RESULTCODE_CLIENT_NOT_EXISTS
 * SUPLA_RESULTCODE_CLIENT_DISABLED
 * SUPLA_RESULTCODE_BAD_CREDENTIALS
 * SUPLA_RESULTCODE_ACCESSID_NOT_ASSIGNED
 * SUPLA_RESULTCODE_ACCESSID_DISABLED
 * SUPLA_RESULTCODE_ACCESSID_INACTIVE
 * SUPLA_RESULTCODE_INCORRECT_PARAMETERS
 * SUPLA_RESULTCODE_SUBJECT_NOT_FOUND
 * SUPLA_RESULTCODE_CHANNEL_IS_OFFLINE
 */

@UsedFromNativeCode
class ResultException(val result: Int) : Exception("The server returned a result number $result")

