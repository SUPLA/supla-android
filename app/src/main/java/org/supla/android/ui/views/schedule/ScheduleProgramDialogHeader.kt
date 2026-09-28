package org.supla.android.ui.views.schedule

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram

@Composable
fun ScheduleProgramDialogHeader(program: SuplaScheduleProgram) =
  Row(
    modifier = Modifier
      .padding(all = dimensionResource(id = R.dimen.distance_default))
      .fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(16.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(color = colorResource(id = program.colorRes()))
    )
    Text(
      text = stringResource(id = R.string.schedule_detail_program_dialog_header, program.number()),
      style = MaterialTheme.typography.headlineSmall,
      textAlign = TextAlign.Center
    )
  }

private fun SuplaScheduleProgram.number(): Int = when (this) {
  SuplaScheduleProgram.PROGRAM_1 -> 1
  SuplaScheduleProgram.PROGRAM_2 -> 2
  SuplaScheduleProgram.PROGRAM_3 -> 3
  SuplaScheduleProgram.PROGRAM_4 -> 4
  SuplaScheduleProgram.OFF -> 0
}
