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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.local.calendar.toHour
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.dialogs.DialogButtons
import org.supla.android.ui.dialogs.DialogHeader
import org.supla.android.ui.extensions.isPhoneLandscape
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

  Dialog(onDismiss = { onQuartersSelectionDismiss() }) {
    DialogHeader(title = stringResource(id = R.string.schedule_detail_quarters_dialog_header, key.hour))

    if (LocalConfiguration.current.isPhoneLandscape) {
      LandscapeViewComponents(data, programs)
    } else {
      PortraitViewComponents(data, programs)
    }

    DialogButtons(
      onNegativeClick = ::onQuartersSelectionDismiss,
      onPositiveClick = ::onQuartersSelectionFinish,
      negativeTextRes = R.string.cancel,
      positiveTextRes = R.string.save
    )
  }
}

@Composable
private fun QuartersSelectionDialogScope.PortraitViewComponents(
  data: QuartersSelectionData,
  programs: List<WeeklyScheduleProgram>
) {
  val key = data.entryKey
  val value = data.entryValue

  ScheduleProgramsRow(modifier = Modifier.fillMaxWidth()) {
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
}

@Composable
private fun QuartersSelectionDialogScope.LandscapeViewComponents(
  data: QuartersSelectionData,
  programs: List<WeeklyScheduleProgram>
) {
  val key = data.entryKey
  val value = data.entryValue

  DayLabel(
    textRes = key.dayOfWeek.fullText,
    padding = PaddingValues(bottom = Distance.small)
  )

  Row(
    horizontalArrangement = Arrangement.spacedBy(Distance.small),
    modifier = Modifier.padding(horizontal = Distance.horizontal)
  ) {
    ScheduleProgramsRow(modifier = Modifier.weight(1f)) {
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

    Column(modifier = Modifier.weight(1f)) {
      QuarterRow(key = key, program = value.firstQuarterProgram, quarterOfHour = QuarterOfHour.FIRST, padding = PaddingValues.Zero)
      QuarterRow(
        key = key,
        program = value.secondQuarterProgram,
        quarterOfHour = QuarterOfHour.SECOND,
        padding = PaddingValues(top = Distance.tiny)
      )
      QuarterRow(
        key = key,
        program = value.thirdQuarterProgram,
        quarterOfHour = QuarterOfHour.THIRD,
        padding = PaddingValues(top = Distance.tiny)
      )
      QuarterRow(
        key = key,
        program = value.fourthQuarterProgram,
        quarterOfHour = QuarterOfHour.FOURTH,
        padding = PaddingValues(top = Distance.tiny)
      )
    }
  }
}

@Composable
private fun QuartersSelectionDialogScope.QuarterRow(
  key: ScheduleDetailEntryBoxKey,
  program: SuplaScheduleProgram,
  quarterOfHour: QuarterOfHour,
  padding: PaddingValues = PaddingValues(start = Distance.default, top = Distance.tiny, end = Distance.default)
) = Row(
  modifier = Modifier.padding(padding),
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
private fun DayLabel(
  @StringRes textRes: Int,
  padding: PaddingValues = PaddingValues(start = Distance.default, top = Distance.default, end = Distance.default)
) = Text(
  text = stringResource(id = textRes).uppercase(),
  style = MaterialTheme.typography.bodyMedium,
  textAlign = TextAlign.Center,
  modifier = Modifier
    .padding(padding)
    .fillMaxWidth()
)

@Composable
private fun ScheduleProgramsRow(
  modifier: Modifier,
  content: @Composable RowScope.() -> Unit
) = FlowRow(
  modifier = modifier
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
@SuplaPreviewLandscape
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
