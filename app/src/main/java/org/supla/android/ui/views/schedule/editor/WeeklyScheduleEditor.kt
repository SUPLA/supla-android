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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.ui.extensions.isPhoneLandscape
import org.supla.android.ui.views.LoadingScrim
import org.supla.android.ui.views.schedule.ScheduleDetailEntryBoxKey
import org.supla.android.ui.views.schedule.colorRes
import org.supla.android.ui.views.tools.Shadow
import org.supla.android.ui.views.tools.ShadowOrientation
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString

interface WeeklyScheduleProgram {
  val program: SuplaScheduleProgram
  val label: LocalizedString

  @get:DrawableRes
  val iconRes: Int?
}

data class WeeklyScheduleEditorState<Program : WeeklyScheduleProgram>(
  val programs: List<Program> = emptyList(),
  val activeProgram: SuplaScheduleProgram? = null,
  val scheduleTableState: ScheduleTableState = ScheduleTableState()
)

interface WeeklyScheduleEditorScope : ScheduleTableScope {
  fun onScheduleProgramClick(program: SuplaScheduleProgram)
  fun onScheduleProgramLongClick(program: SuplaScheduleProgram)
}

@Composable
fun WeeklyScheduleEditorScope.WeeklyScheduleEditor(
  state: WeeklyScheduleEditorState<out WeeklyScheduleProgram>,
  loading: Boolean,
  modifier: Modifier = Modifier,
  dialogs: @Composable BoxScope.() -> Unit = {},
  overlay: @Composable BoxScope.(boxSize: Size) -> Unit = {}
) {
  var boxSize by remember { mutableStateOf(Size.Zero) }

  Box(modifier = modifier) {
    dialogs()

    WeeklyScheduleEditorContainer {
      Shadow(orientation = ShadowOrientation.STARTING_TOP)
      if (LocalConfiguration.current.isPhoneLandscape) {
        WeeklyScheduleEditorLandscape(state = state, onBoxSizeChanged = { boxSize = it })
      } else {
        WeeklyScheduleEditorPortrait(
          state = state,
          editorScope = this@WeeklyScheduleEditor,
          onBoxSizeChanged = { boxSize = it }
        )
      }
    }

    overlay(boxSize)
    if (loading) {
      LoadingScrim()
    }
  }
}

@Composable
private fun WeeklyScheduleEditorScope.WeeklyScheduleEditorLandscape(
  state: WeeklyScheduleEditorState<out WeeklyScheduleProgram>,
  onBoxSizeChanged: (Size) -> Unit
) {
  Row(horizontalArrangement = Arrangement.spacedBy(Distance.tiny)) {
    ScheduleProgramsColumn {
      SchedulePrograms(state)
    }
    ScheduleTable(
      state = state.scheduleTableState,
      modifier = Modifier
        .fillMaxHeight()
        .weight(1f)
        .padding(top = Distance.small, end = Distance.default, bottom = Distance.small),
      onBoxSizeChanged = onBoxSizeChanged
    )
  }
}

@Composable
private fun ColumnScope.WeeklyScheduleEditorPortrait(
  state: WeeklyScheduleEditorState<out WeeklyScheduleProgram>,
  editorScope: WeeklyScheduleEditorScope,
  onBoxSizeChanged: (Size) -> Unit
) {
  ScheduleProgramsRow {
    with(editorScope) {
      SchedulePrograms(state)
    }
  }
  editorScope.ScheduleTable(
    state = state.scheduleTableState,
    modifier = Modifier
      .fillMaxWidth()
      .weight(1f)
      .padding(
        bottom = dimensionResource(id = R.dimen.distance_small),
        start = dimensionResource(id = R.dimen.distance_default),
        end = dimensionResource(id = R.dimen.distance_default)
      ),
    onBoxSizeChanged = onBoxSizeChanged
  )
}

@Composable
private fun WeeklyScheduleEditorScope.SchedulePrograms(state: WeeklyScheduleEditorState<out WeeklyScheduleProgram>) {
  state.programs.forEach { program ->
    ScheduleProgramButton(
      contentColor = colorResource(id = program.program.colorRes()),
      text = program.label(),
      iconRes = program.iconRes,
      active = program.program == state.activeProgram,
      onClick = { onScheduleProgramClick(program.program) },
      onLongClick = { onScheduleProgramLongClick(program.program) }
    )
  }
}

@Composable
private fun WeeklyScheduleEditorContainer(content: @Composable ColumnScope.() -> Unit) =
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight()
      .background(color = MaterialTheme.colorScheme.background),
    content = content
  )

@Composable
private fun ScheduleProgramsRow(content: @Composable RowScope.() -> Unit) =
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .padding(
        horizontal = dimensionResource(id = R.dimen.distance_default),
        vertical = dimensionResource(id = R.dimen.distance_small)
      )
      .height(dimensionResource(id = R.dimen.button_small_height)),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    content = content
  )

@Composable
private fun ScheduleProgramsColumn(content: @Composable ColumnScope.() -> Unit) =
  Column(
    modifier = Modifier
      .fillMaxHeight()
      .verticalScroll(rememberScrollState())
      .padding(start = Distance.default, top = Distance.small, bottom = Distance.small),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    content = content
  )

private val previewWeeklyScheduleEditorScope = object : WeeklyScheduleEditorScope {
  override fun onScheduleProgramClick(program: SuplaScheduleProgram) {}
  override fun onScheduleProgramLongClick(program: SuplaScheduleProgram) {}
  override fun onScheduleTableLongPress(key: ScheduleDetailEntryBoxKey?) {}
  override fun onScheduleTableTouched(key: ScheduleDetailEntryBoxKey) {}
  override fun onScheduleTableReload() {}
  override fun onScheduleTableInvalidate() {}
}

private data class PreviewWeeklyScheduleProgram(
  override val program: SuplaScheduleProgram,
  override val label: LocalizedString,
  @param:DrawableRes override val iconRes: Int? = null
) : WeeklyScheduleProgram

private val previewWeeklyScheduleEditorState = WeeklyScheduleEditorState(
  programs = listOf(
    PreviewWeeklyScheduleProgram(SuplaScheduleProgram.PROGRAM_1, LocalizedString.Constant("19.0°")),
    PreviewWeeklyScheduleProgram(SuplaScheduleProgram.PROGRAM_2, LocalizedString.Constant("21.0°")),
    PreviewWeeklyScheduleProgram(SuplaScheduleProgram.PROGRAM_3, LocalizedString.Constant("18.0°")),
    PreviewWeeklyScheduleProgram(SuplaScheduleProgram.PROGRAM_4, LocalizedString.Constant("24.0°")),
    PreviewWeeklyScheduleProgram(SuplaScheduleProgram.OFF, localizedString(R.string.turn_off), R.drawable.ic_power_button)
  ),
  activeProgram = SuplaScheduleProgram.PROGRAM_1,
  scheduleTableState = ScheduleTableState(
    schedule = mapOf(
      ScheduleDetailEntryBoxKey(DayOfWeek.TUESDAY, 3) to ScheduleTableBox(SuplaScheduleProgram.PROGRAM_4),
      ScheduleDetailEntryBoxKey(DayOfWeek.THURSDAY, 5) to ScheduleTableBox(
        firstQuarterProgram = SuplaScheduleProgram.PROGRAM_1,
        secondQuarterProgram = SuplaScheduleProgram.PROGRAM_2,
        thirdQuarterProgram = SuplaScheduleProgram.OFF,
        fourthQuarterProgram = SuplaScheduleProgram.PROGRAM_3
      )
    ),
    currentDayOfWeek = DayOfWeek.MONDAY,
    currentHour = 12
  )
)

@SuplaPreview
@SuplaPreviewLandscape
@Composable
private fun WeeklyScheduleEditorPreview() {
  SuplaTheme {
    previewWeeklyScheduleEditorScope.WeeklyScheduleEditor(
      state = previewWeeklyScheduleEditorState,
      loading = false,
      modifier = Modifier
        .systemBarsPadding()
        .fillMaxSize()
    )
  }
}
