package org.supla.android.features.addwizard.view.components
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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.supla.android.core.ui.theme.Distance
import org.supla.android.extensions.addWizardButton
import org.supla.android.ui.views.buttons.OutlinedButton

val AddWizardDefaultPaddings: PaddingValues
  @Composable
  get() = PaddingValues(start = Distance.small, top = Distance.small, end = Distance.small, bottom = Distance.small)

val AddWizardNavigationPaddings: PaddingValues
  @Composable
  get() = PaddingValues(start = Distance.default, top = Distance.small, end = Distance.small, bottom = Distance.small)

@Composable
fun AddWizardButton(
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = AddWizardDefaultPaddings,
  enabled: Boolean = true,
  onClick: () -> Unit,
  content: @Composable RowScope.() -> Unit
) =
  OutlinedButton(
    colors = ButtonDefaults.addWizardButtonColors(),
    contentPadding = contentPadding,
    onClick = onClick,
    modifier = modifier.addWizardButton(),
    enabled = enabled,
    content = content
  )
