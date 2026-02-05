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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.ui.views.EmptyListInfoView
import org.supla.android.ui.views.configuration.ActionConfigurationScope
import org.supla.android.ui.views.configuration.Actions
import org.supla.android.ui.views.configuration.Caption
import org.supla.android.ui.views.configuration.Profiles
import org.supla.android.ui.views.configuration.SubjectTypes
import org.supla.android.ui.views.configuration.Subjects

@Composable
fun ActionConfigurationScope.NfcActions(viewState: EditNfcTagViewState) {
  Column(
    modifier = Modifier
      .padding(Distance.default)
      .padding(bottom = 80.dp)
      .verticalScroll(state = rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(Distance.small),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Caption(
      label = R.string.edit_nfc_tag_name,
      caption = viewState.tagName,
      isError = viewState.isError
    )

    viewState.profiles?.let { profiles ->
      Profiles(profiles)
      SubjectTypes(viewState.subjectType)
    }
    viewState.subjects?.let { subjects ->
      Subjects(subjects)
      viewState.actions?.let { Actions(it) }
    }
    if (viewState.subjects == null) {
      EmptyListInfoView(modifier = Modifier.padding(Distance.default))
    }
  }
}
