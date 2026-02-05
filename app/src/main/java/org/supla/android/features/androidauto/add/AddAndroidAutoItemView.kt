package org.supla.android.features.androidauto.add
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.model.general.SingleOptionalSelectionList
import org.supla.android.data.model.spinner.ProfileItem
import org.supla.android.data.model.spinner.SubjectItem
import org.supla.android.images.ImageId
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.BACKGROUND_COLOR
import org.supla.android.ui.views.EmptyListInfoView
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.android.ui.views.configuration.ActionConfigurationScope
import org.supla.android.ui.views.configuration.Actions
import org.supla.android.ui.views.configuration.Caption
import org.supla.android.ui.views.configuration.Profiles
import org.supla.android.ui.views.configuration.SubjectTypes
import org.supla.android.ui.views.configuration.Subjects
import org.supla.core.shared.infrastructure.LocalizedString

data class AddAndroidAutoItemViewState(
  val profiles: SingleOptionalSelectionList<ProfileItem>? = null,
  val subjectType: SubjectType = SubjectType.CHANNEL,

  val subjects: SingleOptionalSelectionList<SubjectItem>? = null,
  val caption: String? = null,
  val actions: SingleOptionalSelectionList<ActionId>? = null,

  val showDelete: Boolean = false
) {
  val profilesEnabled: Boolean = !showDelete
  val subjectsEnabled: Boolean = !showDelete

  val saveEnabled: Boolean
    get() = caption?.isNotEmpty() == true && subjects?.selected != null && actions?.selected != null
}

interface AddAndroidAutoItemScope : ActionConfigurationScope {
  fun onSave()
  fun onDelete()
}

@Composable
fun AddAndroidAutoItemScope.View(viewState: AddAndroidAutoItemViewState) {
  if (viewState.caption == null && viewState.subjects?.selected != null) {
    onCaptionChange(viewState.subjects.selected.caption(LocalContext.current))
  }

  Box {
    Column(
      modifier = Modifier
        .padding(Distance.default)
        .padding(bottom = 80.dp)
        .verticalScroll(state = rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(Distance.small),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      viewState.profiles?.let { profiles ->
        Profiles(profiles, viewState.profilesEnabled)
        SubjectTypes(viewState.subjectType, viewState.profilesEnabled)
      }
      viewState.subjects?.let { subjects ->
        Subjects(subjects, viewState.subjectsEnabled)
        viewState.caption?.let { Caption(R.string.widget_configure_name_label, it) }
        viewState.actions?.let { Actions(it) }
      }
      if (viewState.subjects == null) {
        EmptyListInfoView(modifier = Modifier.padding(Distance.default))
      }
    }

    if (viewState.showDelete) {
      Row(
        modifier = Modifier
          .padding(Distance.default)
          .align(Alignment.BottomCenter),
        horizontalArrangement = Arrangement.spacedBy(Distance.default)
      ) {
        OutlinedButton(
          text = stringResource(R.string.delete_account),
          modifier = Modifier.weight(1f),
          onClick = { onDelete() }
        )
        Button(
          text = stringResource(R.string.save),
          onClick = { onSave() },
          enabled = viewState.saveEnabled,
          modifier = Modifier.weight(1f)
        )
      }
    } else {
      Button(
        text = stringResource(R.string.save),
        onClick = { onSave() },
        enabled = viewState.saveEnabled,
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(Distance.default)
          .fillMaxWidth()
      )
    }
  }
}

private val emptyScope = object : AddAndroidAutoItemScope {
  override fun onProfileSelected(profileItem: ProfileItem) {}
  override fun onSubjectTypeSelected(subjectType: SubjectType) {}
  override fun onSubjectSelected(subjectItem: SubjectItem) {}
  override fun onCaptionChange(caption: String) {}
  override fun onActionChange(actionId: ActionId) {}
  override fun onSave() {}
  override fun onDelete() {}
}

@Preview(showBackground = true, backgroundColor = BACKGROUND_COLOR)
@Composable
private fun Preview() {
  val firstProfile = ProfileItem(1, LocalizedString.Constant("Default"), true)
  val firstSubject = SubjectItem.create(
    id = 1,
    caption = LocalizedString.Constant("Thermostat"),
    icon = ImageId(R.drawable.fnc_thermostat_dhw)
  )
  SuplaTheme {
    emptyScope.View(
      AddAndroidAutoItemViewState(
        profiles = SingleOptionalSelectionList(
          selected = firstProfile,
          label = R.string.widget_configure_profile_label,
          items = listOf(
            firstProfile,
            ProfileItem(2, LocalizedString.Constant("Test"), true)
          )
        ),
        subjects = SingleOptionalSelectionList(
          selected = firstSubject,
          label = R.string.widget_channel,
          items = listOf(
            firstSubject
          )
        ),
        caption = "Thermostat",
        actions = SingleOptionalSelectionList(
          selected = ActionId.OPEN,
          label = R.string.widget_configure_action_label,
          items = listOf(ActionId.OPEN)
        )
      )
    )
  }
}

@Preview(showBackground = true, backgroundColor = BACKGROUND_COLOR)
@Composable
private fun Preview_NoSubjects() {
  val firstProfile = ProfileItem(1, LocalizedString.Constant("Default"), true)
  SuplaTheme {
    emptyScope.View(
      AddAndroidAutoItemViewState(
        profiles = SingleOptionalSelectionList(
          selected = firstProfile,
          label = R.string.widget_configure_profile_label,
          items = listOf(
            firstProfile,
            ProfileItem(2, LocalizedString.Constant("Test"), true)
          )
        )
      )
    )
  }
}

@Preview(showBackground = true, backgroundColor = BACKGROUND_COLOR)
@Composable
private fun Preview_SubjectsWithoutSelection() {
  val firstProfile = ProfileItem(1, LocalizedString.Constant("Default"), true)
  val firstSubject = SubjectItem.create(
    id = 1,
    caption = LocalizedString.Constant("Thermostat"),
    icon = ImageId(R.drawable.fnc_thermostat_dhw)
  )
  SuplaTheme {
    emptyScope.View(
      AddAndroidAutoItemViewState(
        profiles = SingleOptionalSelectionList(
          selected = firstProfile,
          label = R.string.widget_configure_profile_label,
          items = listOf(
            firstProfile,
            ProfileItem(2, LocalizedString.Constant("Test"), true)
          )
        ),
        subjects = SingleOptionalSelectionList(
          selected = null,
          label = R.string.widget_channel,
          items = listOf(
            firstSubject
          )
        )
      )
    )
  }
}
