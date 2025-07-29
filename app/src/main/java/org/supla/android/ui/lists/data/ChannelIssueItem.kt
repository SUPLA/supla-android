package org.supla.android.ui.lists.data
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

import android.content.Context
import androidx.annotation.StringRes
import org.supla.android.core.shared.invoke
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.infrastructure.localizedString

fun ChannelIssueItem.Companion.error(@StringRes stringRes: Int): ChannelIssueItem =
  ChannelIssueItem.Error(localizedString(stringRes))

fun ChannelIssueItem.Companion.warning(@StringRes stringRes: Int): ChannelIssueItem =
  ChannelIssueItem.Warning(localizedString(stringRes))

fun List<ChannelIssueItem>.message(context: Context): String =
  flatMap { it.messages }.fold("") { acc, item -> if (acc.isEmpty()) item(context) else "$acc\n${item(context)}" }
