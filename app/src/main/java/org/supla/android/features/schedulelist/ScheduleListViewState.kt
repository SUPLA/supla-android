package org.supla.android.features.schedulelist
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

import org.supla.android.features.captionchangedialog.CaptionChangeDialogState
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.ui.dialogs.authorize.AuthorizationModelState
import org.supla.android.ui.lists.ListItem

data class ScheduleListViewState(
  val items: List<ListItem>? = null,
  val loading: Boolean = true,
  val loadingError: Boolean = false,
  val captionChangeDialogState: CaptionChangeDialogState? = null,
  val authorizationUserName: String? = null,
  val authorizationIsCloudAccount: Boolean? = null,
  override val authorizationDialogState: AuthorizationDialogState? = null
) : AuthorizationModelState()
