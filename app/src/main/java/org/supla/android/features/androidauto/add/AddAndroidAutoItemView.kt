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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.model.general.SingleSelectionList
import org.supla.android.extensions.ucFirst
import org.supla.android.images.ImageId
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.BACKGROUND_COLOR
import org.supla.android.ui.views.EmptyListInfoView
import org.supla.android.ui.views.SegmentedComponent
import org.supla.android.ui.views.TextField
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.android.ui.views.spinner.LabelledSpinner
import org.supla.android.ui.views.spinner.Spinner
import org.supla.android.ui.views.spinner.SpinnerItem
import org.supla.android.ui.views.spinner.SubjectSpinnerItem
import org.supla.core.shared.infrastructure.LocalizedString

data class AddAndroidAutoItemViewState(
  val profiles: SingleSelectionList<ProfileItem>? = null,
  val subjectType: SubjectType = SubjectType.CHANNEL,

  val subjects: SingleSelectionList<SubjectItem>? = null,
  val caption: String? = null,
  val actions: SingleSelectionList<ActionId>? = null,

  val saveEnabled: Boolean = true,
  val showDelete: Boolean = false
) {
  val profilesEnabled: Boolean = !showDelete
  val subjectsEnabled: Boolean = !showDelete
}

data class ProfileItem(
  val id: Long,
  override val label: LocalizedString
) : SpinnerItem

data class SubjectItem(
  val id: Int,
  val caption: LocalizedString,
  val actions: List<ActionId>,
  override val icon: ImageId?,
  override val isLocation: Boolean
) : SubjectSpinnerItem {

  override val label: LocalizedString
    get() = caption

  fun actionsList(selectedAction: ActionId? = null): SingleSelectionList<ActionId>? =
    if (actions.isEmpty()) {
      null
    } else {
      SingleSelectionList(
        selected = actions.firstOrNull { it == selectedAction } ?: actions.first(),
        label = R.string.widget_configure_action_label,
        items = actions
      )
    }
}

interface AddAndroidAutoItemScope {
  fun onProfileSelected(profileItem: ProfileItem)
  fun onSubjectTypeSelected(subjectType: SubjectType)
  fun onSubjectSelected(subjectItem: SubjectItem)
  fun onCaptionChange(caption: String)
  fun onActionChange(actionId: ActionId)
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
        SegmentedComponent(
          items = SubjectType.entries.map { stringResource(it.nameRes).ucFirst() },
          activeItem = viewState.subjectType.value - 1,
          onClick = { onSubjectTypeSelected(SubjectType.from(it + 1)) },
          enabled = viewState.profilesEnabled
        )
      }
      viewState.subjects?.let { subjects ->
        Subjects(subjects, viewState.subjectsEnabled)
        viewState.caption?.let { Caption(it) }
        viewState.actions?.let { Actions(it) }
      } ?: EmptyListInfoView(modifier = Modifier.padding(Distance.default))
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

@Composable
private fun AddAndroidAutoItemScope.Profiles(profiles: SingleSelectionList<ProfileItem>, enabled: Boolean) =
  Spinner(
    options = profiles,
    fillMaxWidth = true,
    onOptionSelected = { onProfileSelected(it) },
    enabled = enabled
  )

@Composable
private fun AddAndroidAutoItemScope.Subjects(subjects: SingleSelectionList<SubjectItem>, enabled: Boolean) =
  LabelledSpinner(
    options = subjects,
    fillMaxWidth = true,
    onOptionSelected = { onSubjectSelected(it) },
    enabled = enabled
  )

@Composable
private fun AddAndroidAutoItemScope.Caption(caption: String) =
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(
      text = stringResource(R.string.widget_configure_name_label).uppercase(),
      style = MaterialTheme.typography.bodySmall,
      modifier = Modifier.padding(horizontal = 12.dp),
      color = colorResource(id = R.color.on_surface_variant)
    )
    TextField(
      value = caption,
      onValueChange = { onCaptionChange(it) },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true
    )
  }

@Composable
private fun AddAndroidAutoItemScope.Actions(actions: SingleSelectionList<ActionId>) =
  Spinner(
    options = actions,
    fillMaxWidth = true,
    onOptionSelected = { onActionChange(it) }
  )

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
  val firstProfile = ProfileItem(1, LocalizedString.Constant("Default"))
  val firstSubject = SubjectItem(1, LocalizedString.Constant("Thermostat"), emptyList(), ImageId(R.drawable.fnc_thermostat_dhw), false)
  SuplaTheme {
    emptyScope.View(
      AddAndroidAutoItemViewState(
        profiles = SingleSelectionList(
          selected = firstProfile,
          label = R.string.widget_configure_profile_label,
          items = listOf(
            firstProfile,
            ProfileItem(2, LocalizedString.Constant("Test"))
          )
        ),
        subjects = SingleSelectionList(
          selected = firstSubject,
          label = R.string.widget_channel,
          items = listOf(
            firstSubject
          )
        ),
        caption = "Thermostat",
        actions = SingleSelectionList(
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
  val firstProfile = ProfileItem(1, LocalizedString.Constant("Default"))
  SuplaTheme {
    emptyScope.View(
      AddAndroidAutoItemViewState(
        profiles = SingleSelectionList(
          selected = firstProfile,
          label = R.string.widget_configure_profile_label,
          items = listOf(
            firstProfile,
            ProfileItem(2, LocalizedString.Constant("Test"))
          )
        )
      )
    )
  }
}
