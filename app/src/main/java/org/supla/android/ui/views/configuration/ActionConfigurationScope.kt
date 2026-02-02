package org.supla.android.ui.views.configuration
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

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.data.model.general.SingleSelectionList
import org.supla.android.data.model.spinner.ProfileItem
import org.supla.android.data.model.spinner.SubjectItem
import org.supla.android.extensions.ucFirst
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.ui.views.SegmentedComponent
import org.supla.android.ui.views.forms.TextField
import org.supla.android.ui.views.spinner.LabelledSpinner
import org.supla.android.ui.views.spinner.Spinner

/**
 * Scope can be used in features where action needs to be configured.
 * Currently used in AndroidAuto and NFC
 */
interface ActionConfigurationScope {
  fun onProfileSelected(profileItem: ProfileItem)
  fun onSubjectTypeSelected(subjectType: SubjectType)
  fun onSubjectSelected(subjectItem: SubjectItem)
  fun onCaptionChange(caption: String)
  fun onActionChange(actionId: ActionId)
}

@Composable
fun ActionConfigurationScope.Profiles(profiles: SingleSelectionList<ProfileItem>, enabled: Boolean = true) =
  Spinner(
    options = profiles,
    fillMaxWidth = true,
    onOptionSelected = { onProfileSelected(it) },
    enabled = enabled
  )

@Composable
fun ActionConfigurationScope.SubjectTypes(active: SubjectType, enabled: Boolean = true) =
  SegmentedComponent(
    items = SubjectType.entries.map { stringResource(it.nameRes).ucFirst() },
    activeItem = active.value - 1,
    onClick = { onSubjectTypeSelected(SubjectType.from(it + 1)) },
    enabled = enabled
  )

@Composable
fun ActionConfigurationScope.Subjects(subjects: SingleSelectionList<SubjectItem>, enabled: Boolean = true) =
  LabelledSpinner(
    options = subjects,
    fillMaxWidth = true,
    onOptionSelected = { onSubjectSelected(it) },
    enabled = enabled
  )

@Composable
fun ActionConfigurationScope.Caption(
  @StringRes label: Int,
  caption: String,
  isError: Boolean = false
) =
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(
      text = stringResource(label).uppercase(),
      style = MaterialTheme.typography.bodySmall,
      modifier = Modifier.padding(horizontal = 12.dp),
      color = colorResource(id = R.color.on_surface_variant)
    )
    TextField(
      value = caption,
      onValueChange = { onCaptionChange(it) },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      isError = isError
    )
  }

@Composable
fun ActionConfigurationScope.Actions(actions: SingleSelectionList<ActionId>) =
  Spinner(
    options = actions,
    fillMaxWidth = true,
    onOptionSelected = { onActionChange(it) }
  )
