package org.supla.android.ui.views.slider
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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme

@Composable
fun ThermostatThumb(
  interactionSource: MutableInteractionSource,
  iconRes: Int,
  color: Color,
  modifier: Modifier = Modifier
) {
  val interactions = remember { mutableStateListOf<Interaction>() }
  LaunchedEffect(interactionSource) {
    interactionSource.interactions.collect { interaction ->
      when (interaction) {
        is PressInteraction.Press -> interactions.add(interaction)
        is PressInteraction.Release -> interactions.remove(interaction.press)
        is PressInteraction.Cancel -> interactions.remove(interaction.press)
        is DragInteraction.Start -> interactions.add(interaction)
        is DragInteraction.Stop -> interactions.remove(interaction.start)
        is DragInteraction.Cancel -> interactions.remove(interaction.start)
      }
    }
  }

  Box(
    modifier
      .size(32.dp)
      .indication(
        interactionSource = interactionSource,
        indication = ripple(
          bounded = false,
          radius = 20.dp
        )
      )
      .hoverable(interactionSource = interactionSource)
      .background(
        color = color.copy(alpha = 0.4f),
        shape = CircleShape
      )
  ) {
    Image(
      painter = painterResource(id = iconRes),
      contentDescription = null,
      colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
      contentScale = ContentScale.Inside,
      modifier = Modifier
        .size(24.dp)
        .align(Alignment.Center)
        .background(color = color, shape = CircleShape)
        .padding(all = 3.dp)
    )
  }
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    ThermostatThumb(
      interactionSource = remember { MutableInteractionSource() },
      iconRes = R.drawable.ic_heat,
      color = MaterialTheme.colorScheme.error
    )
  }
}
