package org.supla.android.features.nfc.call.screens.configureaction
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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.model.spinner.ProfileItem
import org.supla.android.data.model.spinner.SubjectItem
import org.supla.android.features.nfc.call.screens.Navigator
import org.supla.android.features.nfc.call.screens.ScreenScaffold
import org.supla.android.features.nfc.shared.edit.EditNfcTagViewEvent
import org.supla.android.features.nfc.shared.edit.EditNfcTagViewState
import org.supla.android.features.nfc.shared.edit.NfcActions
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.configuration.ActionConfigurationScope
import org.supla.android.ui.views.texts.Header

interface ConfigureActionScreenScope : ActionConfigurationScope {
  fun onSave()
  fun onClose()
}

@Composable
fun ConfigureActionScreen(
  tagId: Long,
  navigator: Navigator,
  viewModel: ConfigureActionViewModel = hiltViewModel()
) {
  LaunchedEffect(tagId) { viewModel.onViewCreated(tagId) }
  ScreenScaffold(
    viewModel = viewModel,
    eventHandler = {
      when (it) {
        EditNfcTagViewEvent.Close -> navigator.finish()
      }
    },
    content = { View(it, viewModel) }
  )
}

@Composable
fun ConfigureActionScreen(
  uuid: String,
  navigator: Navigator,
  viewModel: ConfigureActionViewModel = hiltViewModel()
) {
  LaunchedEffect(uuid) { viewModel.onViewCreated(uuid) }
  ScreenScaffold(
    viewModel = viewModel,
    eventHandler = {
      when (it) {
        EditNfcTagViewEvent.Close -> navigator.finish()
      }
    },
    content = { View(it, viewModel) }
  )
}

@Composable
private fun BoxScope.View(viewState: EditNfcTagViewState, viewScope: ConfigureActionScreenScope) {
  Column {
    viewScope.Header()
    viewScope.NfcActions(viewState)
  }
  viewScope.SaveButton(Modifier.align(Alignment.BottomCenter))
}

@Composable
private fun ConfigureActionScreenScope.Header() =
  Header(
    textRes = R.string.edit_nfc_tag_title,
    iconRes = R.drawable.ic_close,
    onClose = { onClose() },
    modifier = Modifier.padding(
      start = Distance.default,
      top = Distance.small,
      end = Distance.small,
      bottom = Distance.default
    )
  )

@Composable
private fun ConfigureActionScreenScope.SaveButton(modifier: Modifier = Modifier) {
  Button(
    text = stringResource(R.string.save),
    onClick = { onSave() },
    modifier = modifier.padding(Distance.default)
  )
}

private val previewScope = object : ConfigureActionScreenScope {
  override fun onSave() {}
  override fun onClose() {}
  override fun onProfileSelected(profileItem: ProfileItem) {}
  override fun onSubjectTypeSelected(subjectType: SubjectType) {}
  override fun onSubjectSelected(subjectItem: SubjectItem) {}
  override fun onCaptionChange(caption: String) {}
  override fun onActionChange(actionId: ActionId) {}
}

@SuplaPreview
@Composable
private fun Preview() {
  SuplaTheme {
    Box {
      View(EditNfcTagViewState(), previewScope)
    }
  }
}
