package org.supla.android.features.details.detailbase.electricitymeter
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.ui.views.ChannelOfflineView

@Composable
fun ElectricityMeterMetricsView(
  state: ElectricityMeterState,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
  ) {
    EnergySummaryBox(
      state.totalForwardActiveEnergy,
      state.totalReversedActiveEnergy,
      modifier = Modifier.padding(start = Distance.default, top = Distance.default, end = Distance.default),
      labelSuffix = stringResource(id = R.string.details_em_total_suffix)
    )
//    RangeSelectionBox(modifier = Modifier.padding(start = Distance.default, top = Distance.small, end = Distance.default)) {}
    EnergySummaryBox(
      state.currentMonthForwardActiveEnergy,
      state.currentMonthReversedActiveEnergy,
      modifier = Modifier.padding(start = Distance.default, top = Distance.small, end = Distance.default),
      labelSuffix = stringResource(id = R.string.details_em_current_month_suffix),
      loading = state.currentMonthDownloading
    )
    when (state.online) {
      true -> {
        PhasesData(state.phaseMeasurementTypes, state.phaseMeasurementValues)
        state.vectorBalancedValues?.let { VectorBalancedData(data = it) }
      }

      false -> ChannelOfflineView()
      null -> {}
    }
  }
}

@Composable
private fun RangeSelectionBox(
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Row(
    modifier = modifier
      .suplaCard()
      .clickable(interactionSource = remember { MutableInteractionSource() }, indication = ripple(), onClick = onClick)
      .padding(horizontal = Distance.small, vertical = Distance.tiny),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = stringResource(id = R.string.details_em_select_range),
      style = MaterialTheme.typography.bodyMedium
    )
    Spacer(modifier = Modifier.weight(1f))
    Icon(
      painter = painterResource(id = R.drawable.ic_dropdown),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onBackground
    )
  }
}

@Composable
private fun PhasesData(measurementTypes: List<SuplaElectricityMeasurementType>, phaseValues: List<PhaseWithMeasurements>) {
  BoxWithConstraints(
    modifier = Modifier
      .padding(top = Distance.small)
      .background(color = MaterialTheme.colorScheme.surface)
      .fillMaxWidth()
  ) {
    var freeSpace by remember { mutableStateOf<Dp?>(null) }
    val density = LocalDensity.current
    val width = maxWidth

    Row(
      modifier = Modifier
        .onGloballyPositioned {
          if (freeSpace == null) {
            freeSpace = with(density) { width - it.size.width.toDp() }
          }
        }
        .horizontalScroll(rememberScrollState())
    ) {
      val showPhaseName = phaseValues.size > 1
      PhaseDataLabels(measurementTypes = measurementTypes, withHeader = showPhaseName)
      OptionalSpace(freeSpace = freeSpace, showPhaseName = showPhaseName, measurementsCount = measurementTypes.size)
      phaseValues.forEach { PhaseDataSinglePhase(phase = it, types = measurementTypes, showPhaseName = showPhaseName) }
      Spacer(modifier = Modifier.width(Distance.small))
    }
  }
}

@Composable
private fun PhaseDataLabels(measurementTypes: List<SuplaElectricityMeasurementType>, withHeader: Boolean = true) =
  Column(
    modifier = Modifier
      .width(IntrinsicSize.Max)
      .padding(start = Distance.small)
      .background(MaterialTheme.colorScheme.background)
  ) {
    if (withHeader) {
      PhaseHeader()
    }
    measurementTypes.forEach {
      TypeLabel(stringResource(id = it.labelRes))
    }
  }

@Composable
private fun PhaseDataSinglePhase(
  phase: PhaseWithMeasurements,
  types: List<SuplaElectricityMeasurementType>,
  showPhaseName: Boolean
) =
  Column(
    modifier = Modifier
      .width(IntrinsicSize.Max)
      .background(MaterialTheme.colorScheme.background),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    if (showPhaseName) {
      PhaseHeader(stringResource(id = phase.phase), alignment = TextAlign.Center)
    }
    Row {
      Column(
        modifier = Modifier.width(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        types.forEach {
          PhaseValue(phase.values[it] ?: "")
        }
      }
      Column(
        modifier = Modifier.width(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        types.forEach {
          PhaseValueUnit(if (phase.values.contains(it)) it.unit else "")
        }
      }
    }
  }

@Composable
private fun OptionalSpace(freeSpace: Dp?, showPhaseName: Boolean, measurementsCount: Int) =
  freeSpace?.let {
    // For the single phase, when there is more space, we want that values are aligned to the right side of screen
    if (it > 0.dp) {
      Column(
        modifier = Modifier
          .width(it)
          .background(MaterialTheme.colorScheme.background)
      ) {
        if (showPhaseName) {
          PhaseHeader()
        }
        repeat(measurementsCount) {
          TypeLabel()
        }
      }
    }
  }

@Composable
private fun PhaseHeader(text: String = "", alignment: TextAlign = TextAlign.Start) =
  Text(
    text = text.uppercase(),
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign = alignment,
    modifier = Modifier
      .height(35.dp)
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface)
      .wrapContentHeight(align = Alignment.CenterVertically)
  )

@Composable
private fun TypeLabel(text: String = "") =
  Text(
    text = text,
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onBackground,
    modifier = Modifier
      .padding(bottom = 1.dp)
      .height(35.dp)
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface)
      .padding(end = Distance.small)
      .wrapContentHeight(align = Alignment.CenterVertically)
  )

@Composable
private fun PhaseValue(text: String) =
  Text(
    text = text,
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onBackground,
    textAlign = TextAlign.End,
    modifier = Modifier
      .padding(bottom = 1.dp)
      .height(35.dp)
      .defaultMinSize(minWidth = 70.dp)
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface)
      .padding(end = 4.dp)
      .wrapContentHeight(align = Alignment.CenterVertically)
  )

@Composable
private fun PhaseValueUnit(text: String = "") =
  Text(
    text = text,
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier
      .padding(bottom = 1.dp)
      .height(35.dp)
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface)
      .padding(end = Distance.small)
      .wrapContentHeight(align = Alignment.CenterVertically)
  )

@Composable
private fun VectorBalancedData(data: Map<SuplaElectricityMeasurementType, String>) {
  Text(
    text = stringResource(id = R.string.em_phase_to_phase_balance),
    style = MaterialTheme.typography.labelMedium,
    modifier = Modifier.padding(start = Distance.small, top = Distance.default, end = Distance.small)
  )
  Row(
    modifier = Modifier
      .padding(top = Distance.small)
      .background(color = MaterialTheme.colorScheme.surface)
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
  ) {
    Row(modifier = Modifier.padding(start = Distance.small, end = Distance.small)) {
      PhaseDataLabels(measurementTypes = data.keys.toList(), withHeader = false)
      VectorBalancedDataValues(values = data.values.toList())
    }
  }
}

@Composable
private fun VectorBalancedDataValues(
  values: List<String>
) =
  Column(
    modifier = Modifier
      .width(IntrinsicSize.Max)
      .background(MaterialTheme.colorScheme.background),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Row {
      Column(
        modifier = Modifier.width(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        values.forEach { PhaseValue(it) }
      }
    }
  }

@Preview(showBackground = true)
@PreviewScreenSizes
@PreviewFontScale
@Composable
private fun Preview() {
  SuplaTheme {
    ElectricityMeterMetricsView(
      ElectricityMeterState(
        online = true,
        phaseMeasurementTypes = listOf(
          SuplaElectricityMeasurementType.FREQUENCY,
          SuplaElectricityMeasurementType.CURRENT,
          SuplaElectricityMeasurementType.VOLTAGE
        ),
        phaseMeasurementValues = listOf(
          PhaseWithMeasurements(
            R.string.details_em_phase1,
            mapOf(
              SuplaElectricityMeasurementType.FREQUENCY to "50",
              SuplaElectricityMeasurementType.CURRENT to "5",
              SuplaElectricityMeasurementType.VOLTAGE to "245"
            )
          ),
          PhaseWithMeasurements(
            R.string.details_em_phase2,
            mapOf(
              SuplaElectricityMeasurementType.FREQUENCY to "50",
              SuplaElectricityMeasurementType.CURRENT to "3",
              SuplaElectricityMeasurementType.VOLTAGE to "243"
            )
          ),
          PhaseWithMeasurements(
            R.string.details_em_phase3,
            mapOf(
              SuplaElectricityMeasurementType.FREQUENCY to "50",
              SuplaElectricityMeasurementType.CURRENT to "4",
              SuplaElectricityMeasurementType.VOLTAGE to "248"
            )
          )
        )
      )
    )
  }
}

@Composable
fun Modifier.suplaCard(): Modifier {
  val shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_default))
  return this then Modifier
    .background(MaterialTheme.colorScheme.surface, shape)
    .border(1.dp, MaterialTheme.colorScheme.outline, shape)
}
