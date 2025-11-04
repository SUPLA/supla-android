package org.supla.core.shared.data.model.suplaclient
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

import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.LocalizedStringId
import org.supla.core.shared.infrastructure.localizedString

enum class SuplaResultCode(val value: Int) {
  UNKNOWN(-1),
  NONE(0),
  UNSUPPORTED(1),
  FALSE(2),
  TRUE(3),
  TEMPORARILY_UNAVAILABLE(4),
  BAD_CREDENTIALS(5),
  LOCATION_CONFLICT(6),
  CHANNEL_CONFLICT(7),
  DEVICE_DISABLED(8),
  ACCESS_ID_DISABLED(9),
  LOCATION_DISABLED(10),
  CLIENT_DISABLED(11),
  CLIENT_LIMIT_EXCEEDED(12),
  DEVICE_LIMIT_EXCEEDED(13),
  GUID_ERROR(14),
  DEVICE_LOCKED(15),
  REGISTRATION_DISABLED(17),
  ACCESS_ID_NOT_ASSIGNED(18),
  AUTH_KEY_ERROR(19),
  NO_LOCATION_AVAILABLE(20),
  USER_CONFLICT(21),
  UNAUTHORIZED(22),
  AUTHORIZED(23),
  NOT_ALLOWED(24),
  CHANNEL_NOT_FOUND(25),
  UNKNOWN_ERROR(26),
  DENY_CHANNEL_BELONG_TO_GROUP(27),
  DENY_CHANNEL_HAS_SCHEDULE(28),
  DENY_CHANNEL_IS_ASSOCIATED_WITH_SCENE(28),
  DENY_CHANNEL_IS_ASSOCIATED_WITH_ACTION_TRIGGER(30),
  INACTIVE(31),
  CFG_MODE_REQUESTED(32),
  ACTION_UNSUPPORTED(33),
  SUBJECT_NOT_FOUND(34),
  INCORRECT_PARAMETERS(35),
  CLIENT_NOT_EXISTS(36),
  COUNTRY_REJECTED(37),
  CHANNEL_IS_OFFLINE(38),
  NOT_REGISTERED(39),
  DENY_CHANNEL_IS_ASSOCIATED_WITH_VBT(40),
  DENY_CHANNEL_IS_ASSOCIATED_WITH_PUSH(41),
  RESTART_REQUESTED(42),
  IDENTIFY_REQUESTED(43),
  MALFORMED_EMAIL(44);

  fun message(isLogin: Boolean): LocalizedString =
    when (this) {
      TEMPORARILY_UNAVAILABLE -> localizedString(LocalizedStringId.RESULT_CODE_TEMPORARILY_UNAVAILABLE)
      CLIENT_LIMIT_EXCEEDED -> localizedString(LocalizedStringId.RESULT_CODE_CLIENT_LIMIT_EXCEEDED)
      CLIENT_DISABLED -> localizedString(LocalizedStringId.RESULT_CODE_DEVICE_DISABLED)
      ACCESS_ID_DISABLED -> localizedString(LocalizedStringId.RESULT_CODE_ACCESS_ID_DISABLED)
      REGISTRATION_DISABLED -> localizedString(LocalizedStringId.RESULT_CODE_REGISTRATION_DISABLED)
      ACCESS_ID_NOT_ASSIGNED -> localizedString(LocalizedStringId.RESULT_CODE_ACCESS_ID_NOT_ASSIGNED)
      INACTIVE -> localizedString(LocalizedStringId.RESULT_CODE_INACTIVE)
      UNAUTHORIZED -> localizedString(LocalizedStringId.RESULT_CODE_INCORRECT_EMAIL_OR_PASSWORD)
      BAD_CREDENTIALS ->
        localizedString(
          if (isLogin) LocalizedStringId.RESULT_CODE_INCORRECT_EMAIL_OR_PASSWORD else LocalizedStringId.RESULT_CODE_BAD_CREDENTIALS
        )
      else -> localizedString("%s ($value)", localizedString(LocalizedStringId.RESULT_CODE_UNKNOWN_ERROR))
    }

  companion object {
    fun from(value: Int): SuplaResultCode {
      val code = entries.firstOrNull { it.value == value }
      if (code != null) {
        return code
      }

      return UNKNOWN
    }
  }
}
