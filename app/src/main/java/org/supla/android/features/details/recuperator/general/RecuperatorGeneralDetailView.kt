package org.supla.android.features.details.recuperator.general
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.ViewState
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.details.detailbase.electricitymeter.suplaCard
import org.supla.android.features.details.recuperator.general.view.RecuperatorCanvas
import org.supla.android.features.details.recuperator.general.view.SteppedSlider
import org.supla.android.features.details.recuperator.general.view.drawCrossLines
import org.supla.android.features.details.recuperator.general.view.drawHouse
import org.supla.android.features.details.recuperator.general.view.drawTextInRoundRect
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.tools.SuplaSizeClassPreview
import org.supla.android.ui.views.buttons.supla.SuplaButton
import org.supla.android.ui.views.buttons.supla.SuplaButtonDefaults
import org.supla.android.ui.views.texts.BodyLarge
import org.supla.android.ui.views.texts.BodySmall
import org.supla.android.ui.views.texts.LabelLarge
import org.supla.core.shared.infrastructure.LocalizedString

data class RecuperatorGeneralDetailViewState(
  val isOff: Boolean = false,
  val mode: WorkingMode? = null,
  val supplyOutsideTemperature: String = "",
  val supplyInsideTemperature: String = "",
  val exhaustOutsideTemperature: String = "",
  val exhaustInsideTemperature: String = "",
  val supplyPowerPercent: String = "",
  val exhaustPowerPercent: String = ""
) : ViewState()

enum class WorkingMode {
  MANUAL, PROGRAM
}

interface RecuperatorGeneralDetailViewScope {
  fun onVentilationClick()
  fun onEmptyHouseClick()
  fun onOpenWindowClick()
  fun onPowerClick()
  fun onManualClick()
  fun onProgramClick()
}

@Composable
fun RecuperatorGeneralDetailViewScope.View(
  state: RecuperatorGeneralDetailViewState
) {
  if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
    LandscapeView(state)
  } else {
    PortraitView(state)
  }
}

@Composable
private fun RecuperatorGeneralDetailViewScope.LandscapeView(state: RecuperatorGeneralDetailViewState) {
  Row(
    modifier = Modifier
      .padding(Distance.default),
    horizontalArrangement = Arrangement.spacedBy(Distance.default)
  ) {
    Column(
      modifier = Modifier
        .weight(1f)
    ) {
      StateCard(state)
      Spacer(modifier = Modifier.weight(1f))
      OptionButtons(state)
    }
    Column(
      modifier = Modifier.weight(1f)
    ) {
      ModeCard(state)
      Spacer(modifier = Modifier.weight(1f))
      ModeButtons(state)
    }
  }
}

@Composable
private fun RecuperatorGeneralDetailViewScope.PortraitView(state: RecuperatorGeneralDetailViewState) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(Distance.default)
  ) {
    Column(
      modifier = Modifier.weight(1f).verticalScroll(state = rememberScrollState())
    ) {
      StateCard(state)
      Spacer(modifier = Modifier.height(Distance.default))
      ModeCard(state)
    }

    Column(
      verticalArrangement = Arrangement.spacedBy(Distance.small),
      modifier = Modifier.fillMaxWidth().padding(top = Distance.default)
    ) {
      OptionButtons(state)
      ModeButtons(state)
    }
  }
}

@Composable
private fun StateCard(state: RecuperatorGeneralDetailViewState) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(190.dp)
      .suplaCard()
      .padding(horizontal = Distance.default, vertical = Distance.small)
  ) {
    RecuperatorDiagram(state = state, modifier = Modifier.align(Alignment.Center))
  }
}

@Composable
private fun ModeCard(state: RecuperatorGeneralDetailViewState) {
  state.mode?.let { mode ->
    when (mode) {
      WorkingMode.MANUAL -> ManualModeCard()
      WorkingMode.PROGRAM -> ProgramModeCard()
    }
  }
}

@Composable
private fun ManualModeCard() {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .suplaCard()
      .padding(horizontal = Distance.default, vertical = Distance.small),
    verticalArrangement = Arrangement.spacedBy(Distance.small)
  ) {
    var value by remember { mutableFloatStateOf(3f) }
    BodyLarge(
      text = stringResource(R.string.details_recuperator_manual_mode_label).uppercase(),
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    SteppedSlider(
      value = value,
      onValueChange = { value = it },
      modifier = Modifier.fillMaxWidth(),
      steps = 4,
      labels = listOf(
        LocalizedString.Constant("Bieg 1"),
        LocalizedString.Constant("Bieg 2"),
        LocalizedString.Constant("Bieg 3"),
        LocalizedString.Constant("Bieg 4")
      )
    )
  }
}

@Composable
private fun ProgramModeCard() {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .suplaCard()
      .padding(horizontal = Distance.default, vertical = Distance.small),
    verticalArrangement = Arrangement.spacedBy(Distance.tiny)
  ) {
    BodyLarge(
      text = stringResource(R.string.thermostat_detail_mode_weekly_schedule).uppercase(),
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    BodySmall(
      stringRes = R.string.thermostat_detail_program_current,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    ProgramRow {
      LabelLarge(text = "09:00 - 10:00")
      LabelLarge(text = "A - Tryb wysoki")
    }
    Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(MaterialTheme.colorScheme.outline))
    BodySmall(
      stringRes = R.string.thermostat_detail_program_next,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    ProgramRow {
      BodyLarge(text = "09:00 - 10:00")
      BodyLarge(text = "A - Tryb wysoki")
    }
  }
}

@Composable
inline fun ProgramRow(
  content: @Composable RowScope.() -> Unit
) = Row(
  modifier = Modifier.fillMaxWidth(),
  horizontalArrangement = Arrangement.SpaceBetween,
  content = content
)

@Composable
private fun RecuperatorGeneralDetailViewScope.OptionButtons(state: RecuperatorGeneralDetailViewState) {
  Row(horizontalArrangement = Arrangement.spacedBy(Distance.default), modifier = Modifier.fillMaxWidth()) {
    SuplaButton(
      text = stringResource(R.string.details_recuperator_ventilation),
      modifier = Modifier.weight(1f),
      onClick = { onVentilationClick() }
    )
    SuplaButton(
      text = stringResource(R.string.details_recuperator_empty_house),
      modifier = Modifier.weight(1f),
      onClick = { onEmptyHouseClick() }
    )
    SuplaButton(
      text = stringResource(R.string.details_recuperator_open_window),
      modifier = Modifier.weight(1f),
      onClick = { onOpenWindowClick() }
    )
  }
}

@Composable
private fun RecuperatorGeneralDetailViewScope.ModeButtons(state: RecuperatorGeneralDetailViewState) {
  Row(horizontalArrangement = Arrangement.spacedBy(Distance.default), modifier = Modifier.fillMaxWidth()) {
    val color = if (state.isOff) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    SuplaButton(
      iconRes = R.drawable.ic_power_button,
      colors = SuplaButtonDefaults.buttonColors(content = color, contentPressed = color),
      onClick = { onPowerClick() }
    )
    SuplaButton(
      text = stringResource(R.string.thermostat_detail_mode_manual),
      modifier = Modifier.weight(1f),
      pressed = state.mode == WorkingMode.MANUAL,
      onClick = { onManualClick() }
    )
    SuplaButton(
      text = stringResource(R.string.thermostat_detail_mode_weekly_schedule),
      modifier = Modifier.weight(1f),
      pressed = state.mode == WorkingMode.PROGRAM,
      onClick = { onProgramClick() }
    )
  }
}

@Composable
private fun RecuperatorDiagram(state: RecuperatorGeneralDetailViewState, modifier: Modifier = Modifier) {
  RecuperatorCanvas(modifier = modifier) {
    val vectorHeight = 156f
    val scale = size.height / vectorHeight
    val scaleMatrix = Matrix().apply { scale(scale, scale) }

    drawHouse(scaleMatrix)
    val (arrowLeft, arrowTop, arrowBounds) = drawCrossLines(scaleMatrix)

    var firstRectWidth = 0f
    drawTextInRoundRect(state.supplyPowerPercent) { width, _ ->
      firstRectWidth = width
      Offset(arrowLeft - width - 8 * scale, arrowTop - 3 * scale)
    }

    drawTextInRoundRect(state.supplyInsideTemperature) { width, _ ->
      Offset(arrowLeft - width - 2 * 8 * scale - firstRectWidth, arrowTop - 3 * scale)
    }

    drawTextInRoundRect(state.supplyOutsideTemperature) { _, _ ->
      Offset(arrowLeft + arrowBounds.width + 8 * scale, arrowTop - 3 * scale)
    }

    drawTextInRoundRect(state.exhaustPowerPercent) { width, height ->
      firstRectWidth = width
      Offset(arrowLeft - width - 8 * scale, arrowTop - height / 2 + arrowBounds.height)
    }

    drawTextInRoundRect(state.exhaustInsideTemperature) { width, height ->
      Offset(arrowLeft - width - 2 * 8 * scale - firstRectWidth, arrowTop - height / 2 + arrowBounds.height)
    }

    drawTextInRoundRect(state.exhaustOutsideTemperature) { _, height ->
      Offset(arrowLeft + arrowBounds.width + 8 * scale, arrowTop - height / 2 + arrowBounds.height)
    }
  }
}

private val previewScope = object : RecuperatorGeneralDetailViewScope {
  override fun onVentilationClick() {}
  override fun onEmptyHouseClick() {}
  override fun onOpenWindowClick() {}
  override fun onPowerClick() {}
  override fun onManualClick() {}
  override fun onProgramClick() {}
}

@SuplaPreview
@SuplaPreviewLandscape
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.View(
      state = RecuperatorGeneralDetailViewState(
        mode = WorkingMode.MANUAL,
        supplyOutsideTemperature = "22.7°",
        supplyInsideTemperature = "22.7°",
        exhaustOutsideTemperature = "22.7°",
        exhaustInsideTemperature = "22.7°",
        supplyPowerPercent = "0 %",
        exhaustPowerPercent = "0 %"
      )
    )
  }
}

@SuplaSizeClassPreview
@SuplaPreviewLandscape
@Composable
private fun PreviewProgramMode() {
  SuplaTheme {
    previewScope.View(
      state = RecuperatorGeneralDetailViewState(
        mode = WorkingMode.PROGRAM,
        supplyOutsideTemperature = "22.7°",
        supplyInsideTemperature = "22.7°",
        exhaustOutsideTemperature = "22.7°",
        exhaustInsideTemperature = "22.7°",
        supplyPowerPercent = "0 %",
        exhaustPowerPercent = "0 %"
      )
    )
  }
}
