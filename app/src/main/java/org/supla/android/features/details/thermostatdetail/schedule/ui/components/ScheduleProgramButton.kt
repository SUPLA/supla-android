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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.supla.android.R
import org.supla.android.features.details.thermostatdetail.schedule.data.ScheduleDetailProgramBox
import org.supla.android.features.details.thermostatdetail.schedule.extensions.color
import org.supla.android.ui.views.buttons.AnimatableButtonType
import org.supla.android.ui.views.buttons.AnimationMode
import org.supla.android.ui.views.buttons.RoundedControlButton

@Composable
fun ScheduleProgramButton(
  programBox: ScheduleDetailProgramBox,
  modifier: Modifier = Modifier,
  active: Boolean = false,
  onClick: () -> Unit = { },
  onLongClick: () -> Unit = { }
) {
  val iconPainter = programBox.iconRes?.let { painterResource(id = programBox.iconRes) }
  RoundedControlButton(
    modifier = modifier,
    height = 32.dp,
    backgroundColor = programBox.scheduleProgram.program.color(),
    animationMode = AnimationMode.Toggle(active = active),
    icon = iconPainter,
    text = programBox.textProvider(LocalContext.current),
    type = AnimatableButtonType.NEUTRAL,
    contentPadding = PaddingValues(horizontal = dimensionResource(id = R.dimen.distance_small)),
    fontSize = 14.sp,
    fontFamily = FontFamily(Font(R.font.open_sans_bold)),
    onClick = onClick,
    onLongClick = onLongClick
  )
}
