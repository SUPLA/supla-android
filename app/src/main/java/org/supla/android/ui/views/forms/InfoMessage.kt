package org.supla.android.ui.views.forms
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.views.icons.CheckIcon
import org.supla.android.ui.views.icons.InfoIcon
import org.supla.android.ui.views.texts.BodyMedium

sealed interface MessageType {
  val backgroundColor: Color
    @Composable get

  @Composable
  fun Icon()
}

data object Info : MessageType {
  override val backgroundColor: Color
    @Composable
    get() = MaterialTheme.colorScheme.secondaryContainer

  @Composable
  override fun Icon() = InfoIcon()
}

data object Success : MessageType {
  override val backgroundColor: Color
    @Composable
    get() = MaterialTheme.colorScheme.surfaceVariant

  @Composable
  override fun Icon() = CheckIcon()
}

@Composable
fun InfoMessage(
  text: String,
  modifier: Modifier = Modifier,
  type: MessageType = Info
) =
  Row(
    modifier = modifier
      .fillMaxWidth()
      .background(
        color = type.backgroundColor,
        shape = RoundedCornerShape(size = dimensionResource(R.dimen.radius_default))
      )
      .padding(Distance.tiny),
    horizontalArrangement = Arrangement.spacedBy(Distance.tiny)
  ) {
    type.Icon()
    BodyMedium(
      text = text,
      textAlign = TextAlign.Start
    )
  }

@Composable
@SuplaPreview
private fun Preview() {
  SuplaTheme {
    Column(
      modifier = Modifier.padding(Distance.default),
      verticalArrangement = Arrangement.spacedBy(Distance.default)
    ) {
      InfoMessage(
        text = "This operation is not supported on this device."
      )
      InfoMessage(
        text = "This operation is not supported on this device.",
        type = Success
      )
    }
  }
}
