package org.supla.android.ui.lists
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
import org.supla.android.core.ui.LocalizedString
import org.supla.android.ui.lists.data.IssueIcon

data class ListItemIssues(
  val icons: List<IssueIcon> = emptyList(),
  private val issuesStrings: List<LocalizedString> = emptyList()
) {
  fun message(context: Context): String {
    if (issuesStrings.isEmpty()) {
      return ""
    }

    return issuesStrings.fold("") { acc, item -> if (acc.isEmpty()) item(context) else "$acc\n${item(context)}" }
  }

  fun isEmpty(): Boolean = icons.isEmpty()

  fun hasMessage(): Boolean = issuesStrings.isNotEmpty()

  companion object {
    val empty = ListItemIssues()

    operator fun invoke(issueIcon: IssueIcon): ListItemIssues = ListItemIssues(listOf(issueIcon))
    operator fun invoke(first: IssueIcon, second: IssueIcon): ListItemIssues = ListItemIssues(listOf(first, second))
  }
}
