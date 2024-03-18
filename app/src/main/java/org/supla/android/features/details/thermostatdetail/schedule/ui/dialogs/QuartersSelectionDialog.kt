package org.supla.android.features.details.thermostatdetail.schedule.ui.dialogs
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

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.features.details.thermostatdetail.schedule.data.QuartersSelectionData
import org.supla.android.features.details.thermostatdetail.schedule.data.ScheduleDetailEntryBoxKey
import org.supla.android.features.details.thermostatdetail.schedule.data.ScheduleDetailEntryBoxValue
import org.supla.android.features.details.thermostatdetail.schedule.data.ScheduleDetailProgramBox
import org.supla.android.features.details.thermostatdetail.schedule.extensions.colorRes
import org.supla.android.features.details.thermostatdetail.schedule.ui.PreviewProxy
import org.supla.android.features.details.thermostatdetail.schedule.ui.ScheduleDetailViewProxy
import org.supla.android.features.details.thermostatdetail.schedule.ui.components.ScheduleHourCaption
import org.supla.android.features.details.thermostatdetail.schedule.ui.components.ScheduleProgramButton
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.dialogs.DialogButtonsRow
import org.supla.android.ui.views.Separator
import org.supla.android.ui.views.SeparatorStyle
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.OutlinedButton

@Composable
fun QuartersSelectionDialog(
  data: QuartersSelectionData,
  programs: List<ScheduleDetailProgramBox>,
  viewProxy: ScheduleDetailViewProxy,
  onDismiss: () -> Unit,
  onNegativeClick: () -> Unit,
  onPositiveClick: () -> Unit
) {
  val key = data.entryKey
  val value = data.entryValue

  Dialog(onDismiss = onDismiss, usePlatformDefaultWidth = true) {
    DialogHeader(hour = key.hour)
    ScheduleProgramsRow {
      for (programBox in programs) {
        ScheduleProgramButton(
          programBox = programBox,
          modifier = Modifier.padding(vertical = 4.dp),
          active = programBox.scheduleProgram.program == data.activeProgram,
          onClick = { viewProxy.onQuartersDialogProgramChange(programBox.scheduleProgram.program) }
        )
      }
    }

    DayLabel(textRes = key.dayOfWeek.fullText)

    QuarterRow(key = key, program = value.firstQuarterProgram, quarterOfHour = QuarterOfHour.FIRST, viewProxy = viewProxy)
    QuarterRow(key = key, program = value.secondQuarterProgram, quarterOfHour = QuarterOfHour.SECOND, viewProxy = viewProxy)
    QuarterRow(key = key, program = value.thirdQuarterProgram, quarterOfHour = QuarterOfHour.THIRD, viewProxy = viewProxy)
    QuarterRow(key = key, program = value.fourthQuarterProgram, quarterOfHour = QuarterOfHour.FOURTH, viewProxy = viewProxy)

    Separator(style = SeparatorStyle.LIGHT, modifier = Modifier.padding(top = dimensionResource(id = R.dimen.distance_default)))
    DialogButtonsRow {
      OutlinedButton(onClick = onNegativeClick, text = stringResource(id = R.string.cancel), modifier = Modifier.weight(1f))
      Button(onClick = onPositiveClick, text = stringResource(id = R.string.save), modifier = Modifier.weight(1f))
    }
  }
}

@Composable
private fun QuarterRow(
  key: ScheduleDetailEntryBoxKey,
  program: SuplaScheduleProgram,
  quarterOfHour: QuarterOfHour = QuarterOfHour.FIRST,
  viewProxy: ScheduleDetailViewProxy
) =
  Row(
    modifier = Modifier
      .padding(
        start = dimensionResource(id = R.dimen.distance_default),
        top = dimensionResource(id = R.dimen.distance_tiny),
        end = dimensionResource(id = R.dimen.distance_default)
      ),
    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.distance_default)),
    verticalAlignment = Alignment.CenterVertically
  ) {
    ScheduleHourCaption(hour = key.hour, withQuarter = quarterOfHour)
    ScheduleBoxSingleColor(
      colorRes = program.colorRes(),
      modifier = Modifier
        .weight(1f)
        .height(36.dp)
        .clickable(
          interactionSource = MutableInteractionSource(),
          indication = null,
          onClick = { viewProxy.onQueartersDialogQuarterChange(quarterOfHour) }
        )
    )
  }

@Composable
private fun DialogHeader(hour: Short) =
  Text(
    text = stringResource(id = R.string.schedule_detail_quarters_dialog_header, hour),
    modifier = Modifier
      .padding(all = dimensionResource(id = R.dimen.distance_default))
      .fillMaxWidth(),
    style = MaterialTheme.typography.h6,
    textAlign = TextAlign.Center
  )

@Composable
private fun DayLabel(@StringRes textRes: Int) =
  Text(
    text = stringResource(id = textRes).uppercase(),
    style = MaterialTheme.typography.body2,
    textAlign = TextAlign.Center,
    modifier = Modifier
      .padding(
        start = dimensionResource(id = R.dimen.distance_default),
        top = dimensionResource(id = R.dimen.distance_default),
        end = dimensionResource(id = R.dimen.distance_default)
      )
      .fillMaxWidth()
  )

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScheduleProgramsRow(content: @Composable RowScope.() -> Unit) =
  FlowRow(
    modifier = Modifier
      .fillMaxWidth()
      .background(color = colorResource(id = R.color.gray_light))
      .padding(
        horizontal = dimensionResource(id = R.dimen.distance_default),
        vertical = dimensionResource(id = R.dimen.distance_tiny)
      ),
    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.distance_tiny)),
    content = content
  )

@Composable
private fun ScheduleBoxSingleColor(modifier: Modifier = Modifier, @ColorRes colorRes: Int) =
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_small)))
      .background(colorResource(id = colorRes))
  )

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    QuartersSelectionDialog(
      QuartersSelectionData(
        ScheduleDetailEntryBoxKey(DayOfWeek.FRIDAY, 6),
        ScheduleDetailEntryBoxValue(SuplaScheduleProgram.PROGRAM_1),
        SuplaScheduleProgram.PROGRAM_1
      ),
      ScheduleDetailProgramBox.default(),
      PreviewProxy(emptyMap()),
      {},
      {},
      {}
    )
  }
}
