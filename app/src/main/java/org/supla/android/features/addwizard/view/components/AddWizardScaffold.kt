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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.ui.views.Image

@Composable
fun AddWizardScaffold(
  @DrawableRes iconRes: Int,
  @StringRes buttonTextId: Int,
  backButton: (@Composable BoxScope.() -> Unit)? = null,
  processing: Boolean = false,
  onNext: () -> Unit = {},
  content: @Composable ColumnScope.() -> Unit
) =
  AddWizardScaffold(
    buttonTextId = buttonTextId,
    backButton = backButton,
    processing = processing,
    onNext = onNext
  ) {
    Image(
      drawableId = iconRes,
      modifier = Modifier.size(140.dp)
    )

    content()
  }

@Composable
fun AddWizardScaffold(
  @StringRes buttonTextId: Int,
  backButton: (@Composable BoxScope.() -> Unit)? = null,
  processing: Boolean = false,
  onNext: () -> Unit = {},
  content: @Composable ColumnScope.() -> Unit
) =
  AddWizardEmptyScaffold(
    buttonTextId = buttonTextId,
    backButton = backButton,
    processing = processing,
    onNext = onNext
  ) {
    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = Distance.small),
      verticalArrangement = Arrangement.spacedBy(Distance.small),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      content()
    }
  }

@Composable
fun AddWizardEmptyScaffold(
  @StringRes buttonTextId: Int,
  backButton: (@Composable BoxScope.() -> Unit)? = null,
  processing: Boolean = false,
  onNext: () -> Unit = {},
  content: @Composable ColumnScope.() -> Unit
) {
  Column(
    modifier = Modifier.padding(Distance.small),
    verticalArrangement = Arrangement.spacedBy(Distance.tiny),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    content()

    NavigationButtons(
      buttonTextId = buttonTextId,
      backButton = backButton,
      onNext = onNext,
      processing = processing
    )
  }
}

@Composable
private fun NavigationButtons(
  @StringRes buttonTextId: Int,
  backButton: (@Composable BoxScope.() -> Unit)? = null,
  processing: Boolean,
  onNext: () -> Unit
) =
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(Distance.tiny)
  ) {
    backButton?.let { it() }
    AddWizardNavigationButton(
      textRes = buttonTextId,
      modifier = Modifier.align(Alignment.CenterEnd),
      onClick = onNext,
      processing = processing
    )
  }

@Composable
fun ButtonDefaults.addWizardButtonColors() =
  outlinedButtonColors(
    containerColor = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface,
    disabledContainerColor = MaterialTheme.colorScheme.surface,
    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
  )

@Preview(backgroundColor = 0xFF12A71E, showBackground = true)
@Composable
private fun Preview() {
  SuplaTheme {
    AddWizardScaffold(
      R.drawable.add_wizard_step_1,
      R.string.next
    ) {
    }
  }
}
