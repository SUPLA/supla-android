package org.supla.android.features.nfc.shared.edit
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

import org.supla.android.data.model.general.SingleOptionalSelectionList
import org.supla.android.data.model.spinner.ProfileItem
import org.supla.android.data.model.spinner.SubjectItem
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType

data class EditNfcTagViewState(
  val tagName: String = "",
  val tagUuid: String = "",
  val profiles: SingleOptionalSelectionList<ProfileItem>? = null,
  val subjectType: SubjectType = SubjectType.CHANNEL,

  val isError: Boolean = false,
  val newTag: Boolean = false,

  val subjects: SingleOptionalSelectionList<SubjectItem>? = null,
  val actions: SingleOptionalSelectionList<ActionId>? = null,
)
