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
import org.supla.android.core.ui.LocalizedString
import org.supla.android.core.ui.localizedString

sealed interface ChannelIssueItem {
  val icon: IssueIcon
  val messages: List<LocalizedString>
  val priority: Int

  val message: LocalizedString
    get() = messages.firstOrNull() ?: LocalizedString.Empty

  data class Warning(@StringRes private val stringRes: Int? = null) : ChannelIssueItem {
    override val icon: IssueIcon
      get() = IssueIcon.Warning
    override val messages: List<LocalizedString>
      get() = listOf(localizedString(stringRes))
    override val priority: Int
      get() = 1
  }

  data class Error(@StringRes private val stringRes: Int? = null) : ChannelIssueItem {
    override val icon: IssueIcon
      get() = IssueIcon.Error
    override val messages: List<LocalizedString>
      get() = listOf(localizedString(stringRes))
    override val priority: Int
      get() = 2
  }

  data class LowBattery(override val messages: List<LocalizedString>) : ChannelIssueItem {
    override val icon: IssueIcon
      get() = IssueIcon.Battery0
    override val priority: Int
      get() = 3
  }
}

fun List<ChannelIssueItem>.message(context: Context): String =
  flatMap { it.messages }.fold("") { acc, item -> if (acc.isEmpty()) item(context) else "$acc\n${item(context)}" }
