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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.buttons.OutlinedButton
import kotlin.time.Duration.Companion.milliseconds

private const val numberOfDots = 10

@Composable
fun AddWizardScaffold(
  @DrawableRes iconRes: Int,
  @StringRes buttonTextId: Int,
  backButton: (@Composable BoxScope.() -> Unit)? = null,
  processing: Boolean = false,
  onNext: () -> Unit = {},
  onClose: () -> Unit = {},
  content: @Composable ColumnScope.() -> Unit
) {
  Column(
    modifier = Modifier.padding(Distance.small),
    verticalArrangement = Arrangement.spacedBy(Distance.default),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = Distance.small),
      verticalArrangement = Arrangement.spacedBy(Distance.default),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      TitleBar(onClose = onClose)

      Image(
        drawableId = iconRes,
        modifier = Modifier.size(140.dp)
      )

      content()
    }

    NavigationButtons(
      buttonTextId = buttonTextId,
      backButton = backButton,
      onNext = onNext,
      processing = processing
    )
  }
}

@Composable
private fun TitleBar(onClose: () -> Unit) =
  Box(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = stringResource(R.string.app_name).lowercase(),
      style = MaterialTheme.typography.titleLarge,
      modifier = Modifier.align(Alignment.Center),
      color = MaterialTheme.colorScheme.onPrimaryContainer
    )
    IconButton(
      onClick = onClose,
      modifier = Modifier.align(Alignment.CenterEnd)
    ) {
      Image(
        drawableId = R.drawable.ic_close,
        tint = MaterialTheme.colorScheme.onPrimaryContainer
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
  Box(modifier = Modifier.fillMaxWidth().padding(Distance.tiny)) {
    backButton?.let { it() }
    OutlinedButton(
      colors = ButtonDefaults.addWizardButtonColors(),
      contentPadding = PaddingValues(start = Distance.default, top = Distance.small, end = Distance.small, bottom = Distance.small),
      onClick = onNext,
      modifier = Modifier.align(Alignment.CenterEnd).defaultMinSize(minWidth = 130.dp, minHeight = 56.dp),
      enabled = !processing
    ) {
      if (processing) {
        ProcessingText()
      } else {
        Text(
          text = stringResource(buttonTextId),
          style = MaterialTheme.typography.labelLarge,
        )
        Spacer(modifier = Modifier.width(Distance.tiny))
        Image(
          drawableId = R.drawable.ic_arrow_right,
        )
      }
    }
  }

@Composable
private fun ProcessingText() {
  var position by remember { mutableIntStateOf(0) }
  var text by remember { mutableStateOf("..........") }
  LaunchedEffect(Any()) {
    while (true) {
      delay(100.milliseconds)
      if (position >= numberOfDots) {
        position = 0
      } else {
        position += 1
      }
      text = ""
      for (i in 0..numberOfDots) {
        text += if (i == position) "|" else "."
      }
    }
  }

  Text(
    text = text,
    style = MaterialTheme.typography.bodySmall,
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
