package org.supla.android.ui.views.schedule.editor

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.local.calendar.toHour
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.dialogs.DialogButtonsRow
import org.supla.android.ui.dialogs.DialogHeader
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.android.ui.views.schedule.ScheduleDetailEntryBoxKey
import org.supla.android.ui.views.schedule.colorRes
import org.supla.core.shared.infrastructure.LocalizedString

interface QuartersSelectionDialogScope {
  fun onQuartersSelectionProgramChange(program: SuplaScheduleProgram)
  fun onQuartersSelectionQuarterChange(quarterOfHour: QuarterOfHour)
  fun onQuartersSelectionDismiss()
  fun onQuartersSelectionFinish()
}

@Composable
fun QuartersSelectionDialogScope.QuartersDialog(
  data: QuartersSelectionData,
  programs: List<WeeklyScheduleProgram>
) {
  val key = data.entryKey
  val value = data.entryValue

  Dialog(onDismiss = { onQuartersSelectionDismiss() }, usePlatformDefaultWidth = true) {
    DialogHeader(title = stringResource(id = R.string.schedule_detail_quarters_dialog_header, key.hour))
    ScheduleProgramsRow {
      for (program in programs) {
        ScheduleProgramButton(
          contentColor = colorResource(id = program.program.colorRes()),
          text = program.label(),
          iconRes = program.iconRes,
          modifier = Modifier.padding(vertical = 4.dp),
          active = program.program == data.activeProgram,
          onClick = { onQuartersSelectionProgramChange(program.program) },
          onLongClick = {}
        )
      }
    }

    DayLabel(textRes = key.dayOfWeek.fullText)

    QuarterRow(key = key, program = value.firstQuarterProgram, quarterOfHour = QuarterOfHour.FIRST)
    QuarterRow(key = key, program = value.secondQuarterProgram, quarterOfHour = QuarterOfHour.SECOND)
    QuarterRow(key = key, program = value.thirdQuarterProgram, quarterOfHour = QuarterOfHour.THIRD)
    QuarterRow(key = key, program = value.fourthQuarterProgram, quarterOfHour = QuarterOfHour.FOURTH)

    DialogButtonsRow {
      OutlinedButton(
        onClick = { onQuartersSelectionDismiss() },
        text = stringResource(id = R.string.cancel),
        modifier = Modifier.weight(1f)
      )
      Button(
        onClick = { onQuartersSelectionFinish() },
        text = stringResource(id = R.string.save),
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun QuartersSelectionDialogScope.QuarterRow(
  key: ScheduleDetailEntryBoxKey,
  program: SuplaScheduleProgram,
  quarterOfHour: QuarterOfHour
) = Row(
  modifier = Modifier.padding(
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
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = { onQuartersSelectionQuarterChange(quarterOfHour) }
      )
  )
}

@Composable
private fun ScheduleHourCaption(hour: Short, withQuarter: QuarterOfHour) = Text(
  text = hour.toInt().toHour(withQuarter),
  style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
  textAlign = TextAlign.Left
)

@Composable
private fun DayLabel(@StringRes textRes: Int) = Text(
  text = stringResource(id = textRes).uppercase(),
  style = MaterialTheme.typography.bodyMedium,
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
private fun ScheduleProgramsRow(content: @Composable RowScope.() -> Unit) = FlowRow(
  modifier = Modifier
    .fillMaxWidth()
    .background(color = colorResource(id = R.color.gray_lighter))
    .padding(
      horizontal = dimensionResource(id = R.dimen.distance_default),
      vertical = dimensionResource(id = R.dimen.distance_tiny)
    ),
  horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.distance_tiny)),
  content = content
)

@Composable
private fun ScheduleBoxSingleColor(modifier: Modifier = Modifier, @ColorRes colorRes: Int) = Box(
  modifier = modifier
    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_small)))
    .background(colorResource(id = colorRes))
)

private data class PreviewProgram(
  override val program: SuplaScheduleProgram,
  override val label: LocalizedString,
  override val iconRes: Int? = null
) : WeeklyScheduleProgram

private val previewScope = object : QuartersSelectionDialogScope {
  override fun onQuartersSelectionProgramChange(program: SuplaScheduleProgram) {}
  override fun onQuartersSelectionQuarterChange(quarterOfHour: QuarterOfHour) {}
  override fun onQuartersSelectionDismiss() {}
  override fun onQuartersSelectionFinish() {}
}

@SuplaPreview
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.QuartersDialog(
      data = QuartersSelectionData(
        ScheduleDetailEntryBoxKey(DayOfWeek.FRIDAY, 6),
        ScheduleTableBox(SuplaScheduleProgram.PROGRAM_1),
        SuplaScheduleProgram.PROGRAM_1
      ),
      programs = listOf(
        PreviewProgram(SuplaScheduleProgram.PROGRAM_1, LocalizedString.Constant("20.0°")),
        PreviewProgram(SuplaScheduleProgram.PROGRAM_2, LocalizedString.Constant("22.5°")),
        PreviewProgram(SuplaScheduleProgram.PROGRAM_3, LocalizedString.Constant("21.0° - 22.5°")),
        PreviewProgram(SuplaScheduleProgram.PROGRAM_4, LocalizedString.Constant("23.5°")),
        PreviewProgram(SuplaScheduleProgram.OFF, LocalizedString.Constant("Default"))
      )
    )
  }
}
