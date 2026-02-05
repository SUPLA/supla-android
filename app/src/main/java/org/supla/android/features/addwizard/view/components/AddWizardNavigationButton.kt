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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.ui.views.Image
import kotlin.time.Duration.Companion.milliseconds

private const val NUMBER_OF_DOTS = 10

@Composable
fun AddWizardNavigationButton(
  @StringRes textRes: Int,
  modifier: Modifier = Modifier,
  @DrawableRes iconRes: Int? = R.drawable.ic_arrow_right,
  processing: Boolean = false,
  onClick: () -> Unit
) =
  AddWizardButton(
    contentPadding = AddWizardNavigationPaddings,
    onClick = onClick,
    modifier = modifier,
    enabled = !processing
  ) {
    if (processing) {
      ProcessingText()
    } else {
      Text(
        text = stringResource(textRes),
        style = MaterialTheme.typography.labelLarge,
      )
      Spacer(modifier = Modifier.width(Distance.tiny))
      iconRes?.let {
        Image(
          drawableId = it,
          modifier = Modifier.size(dimensionResource(R.dimen.icon_default_size))
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
      if (position >= NUMBER_OF_DOTS) {
        position = 0
      } else {
        position += 1
      }
      text = ""
      for (i in 0..NUMBER_OF_DOTS) {
        text += if (i == position) "|" else "."
      }
    }
  }

  Text(
    text = text,
    style = MaterialTheme.typography.bodySmall,
  )
}
