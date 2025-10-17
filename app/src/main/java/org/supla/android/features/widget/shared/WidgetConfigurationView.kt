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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.model.general.SingleSelectionList
import org.supla.android.data.model.spinner.ProfileItem
import org.supla.android.data.model.spinner.SubjectItem
import org.supla.android.extensions.ucFirst
import org.supla.android.features.widget.shared.subjectdetail.ActionDetail
import org.supla.android.features.widget.shared.subjectdetail.SubjectDetail
import org.supla.android.images.ImageId
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.BACKGROUND_COLOR
import org.supla.android.ui.views.EmptyListInfoView
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.SegmentedComponent
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.IconButton
import org.supla.android.ui.views.forms.TextField
import org.supla.android.ui.views.spinner.LabelledSpinner
import org.supla.android.ui.views.spinner.Spinner
import org.supla.core.shared.infrastructure.LocalizedString

data class WidgetConfigurationViewState(
  val profiles: SingleSelectionList<ProfileItem>? = null,
  val subjectTypes: List<SubjectType>? = null,
  val subjectType: SubjectType = SubjectType.CHANNEL,
  val subjects: SingleSelectionList<SubjectItem>? = null,
  val subjectDetails: SingleSelectionList<SubjectDetail>? = null,
  val caption: String? = null,

  val showWarning: Boolean = false,
  val saveEnabled: Boolean = false,
  val error: LocalizedString? = null
)

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
      }
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
        viewState.caption?.let { Caption(it, !viewState.saveEnabled) }
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
  Row(verticalAlignment = Alignment.Bottom) {
    Text(
      stringResource(R.string.widget_configure_title),
      style = MaterialTheme.typography.titleLarge,
      modifier = Modifier.weight(1f)
    )
    IconButton(
      icon = R.drawable.ic_close,
      onClick = { onClose() },
      iconSize = dimensionResource(R.dimen.icon_default_size),
      tint = MaterialTheme.colorScheme.onBackground
    )
  }

@Composable
private fun WidgetConfigurationScope.Warning() =
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(Distance.small),
    modifier = Modifier
      .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default)))
      .border(
        width = 1.dp,
        color = colorResource(id = R.color.gray_lighter),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default))
      )
      .padding(Distance.small)
      .clickable { onWarningClick() }
  ) {
    Image(
      drawableId = R.drawable.channel_warning_level1,
      modifier = Modifier.size(dimensionResource(R.dimen.icon_big_size))
    )
    Text(
      text = stringResource(R.string.widget_warning_battery_limitations),
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier.weight(1f)
    )
    Image(
      drawableId = R.drawable.ic_arrow_right,
      modifier = Modifier.size(dimensionResource(R.dimen.icon_big_size)),
    )
  }

@Composable
private fun WidgetConfigurationScope.Profiles(profiles: SingleSelectionList<ProfileItem>) =
  Spinner(
    options = profiles,
    fillMaxWidth = true,
    onOptionSelected = { onProfileSelected(it) }
  )

@Composable
private fun WidgetConfigurationScope.Subjects(subjects: SingleSelectionList<SubjectItem>) =
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
private fun WidgetConfigurationScope.SubjectDetails(actions: SingleSelectionList<SubjectDetail>) =
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

@Preview(showBackground = true, backgroundColor = BACKGROUND_COLOR)
@Composable
private fun Preview() {
  val firstProfile = ProfileItem(1, LocalizedString.Constant("Default"))
  val firstSubject = SubjectItem.create(
    id = 1,
    caption = LocalizedString.Constant("Thermostat"),
    icon = ImageId(R.drawable.fnc_thermostat_dhw)
  )
  SuplaTheme {
    emptyScope.View(
      WidgetConfigurationViewState(
        profiles = SingleSelectionList(
          selected = firstProfile,
          label = R.string.widget_configure_profile_label,
          items = listOf(
            firstProfile,
            ProfileItem(2, LocalizedString.Constant("Test"))
          )
        ),
        subjectTypes = SubjectType.entries,
        subjects = SingleSelectionList(
          selected = firstSubject,
          label = R.string.widget_configure_channel_label,
          items = listOf(
            firstSubject
          )
        ),
        caption = "Thermostat",
        subjectDetails = SingleSelectionList(
          selected = ActionDetail(ActionId.OPEN),
          label = R.string.widget_configure_action_label,
          items = listOf(ActionDetail(ActionId.OPEN))
        ),
        showWarning = true,
      )
    )
  }
}
