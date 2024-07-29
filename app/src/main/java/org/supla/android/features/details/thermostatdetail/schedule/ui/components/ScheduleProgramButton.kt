package org.supla.android.features.details.thermostatdetail.schedule.ui.components
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

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.details.thermostatdetail.schedule.data.ScheduleDetailProgramBox
import org.supla.android.features.details.thermostatdetail.schedule.extensions.colorRes

@Composable
fun ScheduleProgramButton(
  programBox: ScheduleDetailProgramBox,
  modifier: Modifier = Modifier,
  active: Boolean = false,
  onClick: () -> Unit = { },
  onLongClick: () -> Unit = { }
) {
  Button(
    contentColor = colorResource(id = programBox.scheduleProgram.program.colorRes()),
    text = programBox.textProvider(LocalContext.current),
    iconRes = programBox.iconRes,
    active = active,
    onClick = onClick,
    onLongClick = onLongClick,
    modifier = modifier
  )
}

@Composable
private fun Button(
  contentColor: Color,
  text: String,
  iconRes: Int?,
  active: Boolean,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val radius = 16.dp
  val shape = RoundedCornerShape(size = radius)
  val onBackgroundColor = MaterialTheme.colorScheme.onBackground

  var pressed by remember { mutableStateOf(false) }

  var borderColor by remember(active) { mutableStateOf(if (active) onBackgroundColor else contentColor) }

  Box(
    modifier = modifier
      .defaultMinSize(minHeight = radius.times(2))
      .background(contentColor, shape = shape)
      .border(width = 1.5.dp, color = borderColor, shape = shape)
      .pointerInput(onClick) {
        detectTapGestures(
          onTap = { onClick() },
          onLongPress = { onLongClick() },
          onPress = {
            if (!active) {
              pressed = true
              borderColor = onBackgroundColor

              tryAwaitRelease()
              pressed = false
              borderColor = contentColor
            }
          }
        )
      }
  ) {
    Row(
      modifier = Modifier
        .padding(horizontal = Distance.small)
        .align(Alignment.Center),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(Distance.tiny)
    ) {
      iconRes?.let {
        Image(
          painter = painterResource(id = it),
          contentDescription = null,
          alignment = Alignment.Center,
          modifier = Modifier.size(20.dp)
        )
      }
      Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = Color.Black
      )
    }
  }
}

@Preview(showBackground = true, widthDp = 500)
@Preview(showBackground = true, widthDp = 500, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
  SuplaTheme {
    Row(modifier = Modifier.padding(Distance.default), horizontalArrangement = Arrangement.spacedBy(Distance.tiny)) {
      Button(
        contentColor = colorResource(id = R.color.light_blue),
        text = "22.5°",
        iconRes = null,
        active = true,
        onClick = { },
        onLongClick = { }
      )
      Button(
        contentColor = colorResource(id = R.color.light_red),
        text = "22.5°",
        iconRes = null,
        active = false,
        onClick = { },
        onLongClick = { }
      )
      Button(
        contentColor = colorResource(id = R.color.light_green),
        text = "22.5°",
        iconRes = null,
        active = false,
        onClick = { },
        onLongClick = { }
      )
      Button(
        contentColor = colorResource(id = R.color.light_orange),
        text = "22.5°",
        iconRes = null,
        active = false,
        onClick = { },
        onLongClick = { }
      )
      Button(
        contentColor = colorResource(id = R.color.disabled),
        text = "Off",
        iconRes = R.drawable.ic_power_button,
        active = false,
        onClick = { },
        onLongClick = { }
      )
    }
  }
}
