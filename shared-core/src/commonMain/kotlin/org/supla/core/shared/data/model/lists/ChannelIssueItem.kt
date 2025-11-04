package org.supla.core.shared.data.model.lists
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

sealed class ChannelIssueItem(
  val icon: IssueIcon
) {

  abstract val messages: List<LocalizedString>
  abstract val priority: Int

  val message: LocalizedString
    get() = messages.firstOrNull() ?: LocalizedString.Empty

  fun extendOn(remoteId: Int, name: LocalizedString): ChannelIssueItem =
    when (this) {
      is Error -> Error(messages.firstOrNull()?.let { localizedString("%s (%d - %s)", it, remoteId, name) })
      is Warning -> Warning(messages.firstOrNull()?.let { localizedString("%s (%d - %s)", it, remoteId, name) })
      is SoundAlarm -> SoundAlarm(messages.firstOrNull()?.let { localizedString("%s (%d - %s)", it, remoteId, name) })
      is LowBattery -> LowBattery(messages.map { localizedString("%s (%d - %s)", it, remoteId, name) })
    }

  data class Error(private val string: LocalizedString? = null) : ChannelIssueItem(IssueIcon.Error) {
    override val messages: List<LocalizedString>
      get() = string?.let { listOf(it) } ?: emptyList()
    override val priority: Int
      get() = 1

    companion object {
      operator fun invoke(stringId: LocalizedStringId) =
        Error(localizedString(stringId))
    }
  }

  data class Warning(private val string: LocalizedString? = null) : ChannelIssueItem(IssueIcon.Warning) {
    override val messages: List<LocalizedString>
      get() = string?.let { listOf(it) } ?: emptyList()
    override val priority: Int
      get() = 2

    companion object {
      operator fun invoke(stringId: LocalizedStringId) =
        Warning(localizedString(stringId))
    }
  }

  data class SoundAlarm(private val string: LocalizedString? = null) : ChannelIssueItem(IssueIcon.Sound) {
    override val messages: List<LocalizedString>
      get() = string?.let { listOf(it) } ?: emptyList()
    override val priority: Int
      get() = 3

    companion object {
      operator fun invoke(stringId: LocalizedStringId) =
        SoundAlarm(localizedString(stringId))
    }
  }

  data class LowBattery(override val messages: List<LocalizedString>) : ChannelIssueItem(IssueIcon.Battery0) {
    override val priority: Int
      get() = 4
  }

  companion object
}
