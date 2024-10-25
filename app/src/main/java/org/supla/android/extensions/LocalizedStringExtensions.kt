package org.supla.android.extensions
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

import org.supla.android.R
import org.supla.core.shared.infrastructure.LocalizedString

val LocalizedString.resourceId: Int
  get() = when (this) {
    LocalizedString.GENERAL_TURN_ON -> R.string.channel_btn_on
    LocalizedString.GENERAL_TURN_OFF -> R.string.channel_btn_off
    LocalizedString.GENERAL_OPEN -> R.string.channel_btn_open
    LocalizedString.GENERAL_CLOSE -> R.string.channel_btn_close
    LocalizedString.GENERAL_SHUT -> R.string.channel_btn_shut
    LocalizedString.GENERAL_REVEAL -> R.string.channel_btn_reveal
    LocalizedString.GENERAL_COLLAPSE -> R.string.channel_btn_collapse
    LocalizedString.GENERAL_EXPAND -> R.string.channel_btn_expand
  }
