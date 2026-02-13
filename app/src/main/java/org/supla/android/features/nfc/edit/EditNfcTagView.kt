package org.supla.android.features.nfc.edit
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.model.general.SingleOptionalSelectionList
import org.supla.android.data.model.spinner.ProfileItem
import org.supla.android.data.model.spinner.SubjectItem
import org.supla.android.features.nfc.shared.edit.EditNfcTagViewState
import org.supla.android.features.nfc.shared.edit.NfcActions
import org.supla.android.images.ImageId
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.android.ui.views.configuration.ActionConfigurationScope
import org.supla.core.shared.infrastructure.LocalizedString
import java.util.UUID

interface EditNfcTagViewScope : ActionConfigurationScope {
  fun onSave()
  fun onDelete()
}

@Composable
fun EditNfcTagViewScope.View(viewState: EditNfcTagViewState) {
  Box(modifier = Modifier.fillMaxSize()) {
    NfcActions(viewState)

    Row(
      modifier = Modifier
        .padding(Distance.default)
        .align(Alignment.BottomCenter),
      horizontalArrangement = Arrangement.spacedBy(Distance.default)
    ) {
      OutlinedButton(
        text = stringResource(if (viewState.newTag) R.string.cancel else R.string.delete_account),
        modifier = Modifier.weight(1f),
        onClick = { onDelete() }
      )
      Button(
        text = stringResource(R.string.save),
        onClick = { onSave() },
        modifier = Modifier.weight(1f)
      )
    }
  }
}

private val emptyScope = object : EditNfcTagViewScope {
  override fun onProfileSelected(profileItem: ProfileItem) {}
  override fun onSubjectTypeSelected(subjectType: SubjectType) {}
  override fun onSubjectSelected(subjectItem: SubjectItem) {}
  override fun onCaptionChange(caption: String) {}
  override fun onActionChange(actionId: ActionId) {}
  override fun onSave() {}
  override fun onDelete() {}
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
      EditNfcTagViewState(
        tagName = "Open door tag",
        tagUuid = UUID.randomUUID().toString(),
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
        actions = SingleOptionalSelectionList(
          selected = ActionId.OPEN,
          label = R.string.widget_configure_action_label,
          items = listOf(ActionId.OPEN)
        )
      )
    )
  }
}
