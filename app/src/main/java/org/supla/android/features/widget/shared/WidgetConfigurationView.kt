package org.supla.android.features.widget.shared
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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.model.general.SingleOptionalSelectionList
import org.supla.android.data.model.spinner.ProfileItem
import org.supla.android.data.model.spinner.SubjectItem
import org.supla.android.extensions.ucFirst
import org.supla.android.features.widget.shared.subjectdetail.ActionDetail
import org.supla.android.features.widget.shared.subjectdetail.SubjectDetail
import org.supla.android.images.ImageId
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.views.EmptyListInfoView
import org.supla.android.ui.views.SegmentedComponent
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.forms.TextField
import org.supla.android.ui.views.forms.WarningMessage
import org.supla.android.ui.views.spinner.LabelledSpinner
import org.supla.android.ui.views.spinner.Spinner
import org.supla.android.ui.views.texts.Header
import org.supla.core.shared.infrastructure.LocalizedString

data class WidgetConfigurationViewState(
  val profiles: SingleOptionalSelectionList<ProfileItem>? = null,
  val subjectTypes: List<SubjectType>? = null,
  val subjectType: SubjectType = SubjectType.CHANNEL,
  val subjects: SingleOptionalSelectionList<SubjectItem>? = null,
  val subjectDetails: SingleOptionalSelectionList<SubjectDetail>? = null,
  val caption: String? = null,

  val showWarning: Boolean = false,
  val error: LocalizedString? = null
) {
  val saveEnabled: Boolean
    get() = caption?.isNotEmpty() == true && subjects?.selected != null &&
      (subjectDetails?.selected != null || subjectDetails == null)
}

interface WidgetConfigurationScope {
  fun onWarningClick()
  fun onProfileSelected(profileItem: ProfileItem)
  fun onSubjectTypeSelected(subjectType: SubjectType)
  fun onSubjectSelected(subjectItem: SubjectItem)
  fun onCaptionChange(caption: String)
  fun onDetailChange(detail: SubjectDetail)
  fun onClose()
  fun onOk()
}

@Composable
fun WidgetConfigurationScope.View(
  viewState: WidgetConfigurationViewState
) {
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
      Header()
      if (viewState.showWarning) {
        Warning()
      }
      viewState.profiles?.let { profiles ->
        Profiles(profiles)
      } ?: Text(
        text = stringResource(R.string.widget_app_not_initialized),
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(vertical = Distance.default)
      )
      viewState.subjectTypes?.let { types ->
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = stringResource(id = R.string.widget_configure_type_label).uppercase(),
            style = MaterialTheme.typography.bodySmall,
            color = colorResource(id = R.color.on_surface_variant),
            modifier = Modifier.padding(horizontal = 12.dp),
          )
          SegmentedComponent(
            items = types.map { stringResource(it.widgetNameRes).ucFirst() },
            activeItem = viewState.subjectType.value - 1,
            onClick = { onSubjectTypeSelected(SubjectType.from(it + 1)) }
          )
        }
      }
      if (viewState.subjects == null) {
        EmptyListInfoView(modifier = Modifier.padding(Distance.default))
      } else {
        Subjects(viewState.subjects)
        viewState.caption?.let { Caption(it, it.isEmpty()) }
        viewState.subjectDetails?.let { SubjectDetails(it) }
      }
    }

    viewState.error?.let {
      Text(
        text = it(LocalContext.current),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.fillMaxWidth().padding(vertical = Distance.small)
      )
    }

    Button(
      text = stringResource(R.string.ok),
      onClick = { onOk() },
      enabled = viewState.saveEnabled,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(Distance.default)
        .fillMaxWidth()
    )
  }
}

@Composable
private fun WidgetConfigurationScope.Header() =
  Header(
    textRes = R.string.widget_configure_title,
    iconRes = R.drawable.ic_close,
    onClose = { onClose() }
  )

@Composable
private fun WidgetConfigurationScope.Warning() =
  WarningMessage(
    textRes = R.string.widget_warning_battery_limitations,
    onClick = { onWarningClick() }
  )

@Composable
private fun WidgetConfigurationScope.Profiles(profiles: SingleOptionalSelectionList<ProfileItem>) =
  Spinner(
    options = profiles,
    fillMaxWidth = true,
    onOptionSelected = { onProfileSelected(it) }
  )

@Composable
private fun WidgetConfigurationScope.Subjects(subjects: SingleOptionalSelectionList<SubjectItem>) =
  LabelledSpinner(
    options = subjects,
    fillMaxWidth = true,
    onOptionSelected = { onSubjectSelected(it) }
  )

@Composable
private fun WidgetConfigurationScope.Caption(caption: String, isError: Boolean) =
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
      singleLine = true,
      isError = isError
    )
  }

@Composable
private fun WidgetConfigurationScope.SubjectDetails(actions: SingleOptionalSelectionList<SubjectDetail>) =
  Spinner(
    options = actions,
    fillMaxWidth = true,
    onOptionSelected = { onDetailChange(it) }
  )

private val emptyScope = object : WidgetConfigurationScope {
  override fun onWarningClick() {}
  override fun onProfileSelected(profileItem: ProfileItem) {}
  override fun onSubjectTypeSelected(subjectType: SubjectType) {}
  override fun onSubjectSelected(subjectItem: SubjectItem) {}
  override fun onCaptionChange(caption: String) {}
  override fun onDetailChange(detail: SubjectDetail) {}
  override fun onClose() {}
  override fun onOk() {}
}

@SuplaPreview
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
      WidgetConfigurationViewState(
        profiles = SingleOptionalSelectionList(
          selected = firstProfile,
          label = R.string.widget_configure_profile_label,
          items = listOf(
            firstProfile,
            ProfileItem(2, LocalizedString.Constant("Test"), true)
          )
        ),
        subjectTypes = SubjectType.entries,
        subjects = SingleOptionalSelectionList(
          selected = firstSubject,
          label = R.string.widget_configure_channel_label,
          items = listOf(
            firstSubject
          )
        ),
        caption = "Thermostat",
        subjectDetails = SingleOptionalSelectionList(
          selected = ActionDetail(ActionId.OPEN),
          label = R.string.widget_configure_action_label,
          items = listOf(ActionDetail(ActionId.OPEN))
        ),
        showWarning = true,
      )
    )
  }
}

@SuplaPreview
@Composable
private fun PreviewNoProfiles() {
  SuplaTheme {
    emptyScope.View(
      WidgetConfigurationViewState(
        profiles = null,
        subjectTypes = null,
        subjects = null,
        caption = null,
        subjectDetails = null,
        showWarning = true,
      )
    )
  }
}
