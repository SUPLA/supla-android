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

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.extensions.toPx
import org.supla.android.ui.views.ChannelOfflineView
import org.supla.android.ui.views.buttons.IconButton

@Composable
fun ElectricityMeterMetricsView(
  state: ElectricityMeterState,
  onIntroductionClose: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val horizontalScrollState = rememberScrollState()
  val verticalScrollState = rememberScrollState()

  val defaultDistanceInPx = Distance.small.toPx().toInt()
  var tableStartY by remember { mutableIntStateOf(0) }
  var infoHeight by remember { mutableIntStateOf(0) }
  var scrollableHeight by remember { mutableIntStateOf(0) }

  Box(
    modifier = Modifier.onGloballyPositioned { scrollableHeight = it.size.height }
  ) {
    Column(
      modifier = modifier
        .background(MaterialTheme.colorScheme.background)
        .verticalScroll(verticalScrollState)
    ) {
      EnergySummaryBox(
        state.totalForwardActiveEnergy,
        state.totalReversedActiveEnergy,
        modifier = Modifier.padding(start = Distance.default, top = Distance.default, end = Distance.default),
        labelSuffix = stringResource(id = R.string.details_em_total_suffix)
      )
      EnergySummaryBox(
        state.currentMonthForwardActiveEnergy,
        state.currentMonthReversedActiveEnergy,
        modifier = Modifier
          .padding(start = Distance.default, top = Distance.small, end = Distance.default)
          .onGloballyPositioned { tableStartY = it.positionInParent().y.toInt() + it.size.height + defaultDistanceInPx },
        labelSuffix = stringResource(id = R.string.details_em_current_month_suffix),
        loading = state.currentMonthDownloading
      )
      when (state.online) {
        true -> {
          PhasesData(state.phaseMeasurementTypes, state.phaseMeasurementValues, horizontalScrollState)
          state.vectorBalancedValues?.let { SingleValueTable(headerRes = R.string.em_phase_to_phase_balance, data = it) }
          state.electricGridParameters?.let { SingleValueTable(headerRes = R.string.em_electric_grid_parameters, data = it) }
        }

        false -> ChannelOfflineView()
        null -> {}
      }
    }

    if (state.showIntroduction) {
      Info(
        scrollState = horizontalScrollState,
        onClose = onIntroductionClose,
        modifier = Modifier
          .offset {
            val area = scrollableHeight + verticalScrollState.value - tableStartY
            if (area > infoHeight) {
              IntOffset(0, 0)
            } else if (area > 0) {
              IntOffset(0, infoHeight - area)
            } else {
              IntOffset(0, infoHeight)
            }
          }
          .align(Alignment.BottomCenter)
          .onGloballyPositioned { infoHeight = it.size.height }
      )
    }
  }
}

@Composable
private fun PhasesData(
  measurementTypes: List<SuplaElectricityMeasurementType>,
  phaseValues: List<PhaseWithMeasurements>,
  scrollState: ScrollState
) {
  BoxWithConstraints(
    modifier = Modifier
      .padding(top = Distance.small)
      .background(color = MaterialTheme.colorScheme.surface)
      .fillMaxWidth()
  ) {
    var freeSpace by remember { mutableStateOf<Dp?>(null) }
    var selectedType by remember { mutableStateOf<SuplaElectricityMeasurementType?>(null) }
    val density = LocalDensity.current
    val width = maxWidth

    Row(
      modifier = Modifier
        .onGloballyPositioned {
          if (freeSpace == null) {
            freeSpace = with(density) { width - it.size.width.toDp() }
          }
        }
        .horizontalScroll(scrollState)
    ) {
      val showPhaseName = phaseValues.size > 1
      PhaseDataLabels(
        measurementTypes = measurementTypes,
        withHeader = showPhaseName,
        selectedLabel = selectedType,
        onSelectedLabelChanged = { selectedType = if (selectedType == it) null else it }
      )
      OptionalSpace(
        freeSpace = freeSpace,
        showPhaseName = showPhaseName,
        measurements = measurementTypes,
        selectedLabel = selectedType,
        onSelectedLabelChanged = { selectedType = if (selectedType == it) null else it }
      )
      phaseValues.forEachIndexed { index, phase ->
        PhaseDataSinglePhase(
          phase = phase,
          types = measurementTypes,
          showPhaseName = showPhaseName,
          selectedLabel = selectedType,
          onSelectedLabelChanged = { selectedType = if (selectedType == it) null else it },
          withMargin = index + 1 == phaseValues.size
        )
      }
    }
  }
}

@Composable
private fun PhaseDataLabels(
  measurementTypes: List<SuplaElectricityMeasurementType>,
  withHeader: Boolean = true,
  withLabel: Boolean = true,
  selectedLabel: SuplaElectricityMeasurementType? = null,
  onSelectedLabelChanged: ((SuplaElectricityMeasurementType) -> Unit)? = null
) =
  Column(
    modifier = Modifier
      .width(IntrinsicSize.Max)
      .background(MaterialTheme.colorScheme.background)
  ) {
    if (withHeader) {
      PhaseHeader()
    }
    var energyShown = false
    measurementTypes.forEach {
      if (!energyShown && it.showEnergyLabel && withLabel) {
        EnergyLabel(text = stringResource(id = R.string.details_em_energy_label))
        energyShown = true
      }
      TypeLabel(stringResource(id = it.shortLabel), selected = it == selectedLabel, onClick = onSelectedLabelChanged?.click(it))
    }
  }

@Composable
private fun PhaseDataSinglePhase(
  phase: PhaseWithMeasurements,
  types: List<SuplaElectricityMeasurementType>,
  showPhaseName: Boolean,
  withLabel: Boolean = true,
  selectedLabel: SuplaElectricityMeasurementType? = null,
  onSelectedLabelChanged: ((SuplaElectricityMeasurementType) -> Unit)? = null,
  withMargin: Boolean = false
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
        var energyShown = false
        types.forEach {
          if (!energyShown && it.showEnergyLabel && withLabel) {
            EnergyLabel()
            energyShown = true
          }
          PhaseValue(
            text = phase.values[it] ?: ValuesFormatter.NO_VALUE_TEXT,
            selected = it == selectedLabel,
            onClick = onSelectedLabelChanged?.click(it)
          )
        }
      }
      Column(
        modifier = Modifier.width(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        var energyShown = false
        types.forEach {
          if (!energyShown && it.showEnergyLabel && withLabel) {
            EnergyLabel()
            energyShown = true
          }
          PhaseValueUnit(
            text = if (phase.values.contains(it) && phase.values[it] != null) it.unit else "",
            withMargin = withMargin,
            selected = it == selectedLabel,
            onClick = onSelectedLabelChanged?.click(it)
          )
        }
      }
    }
  }

@Composable
private fun OptionalSpace(
  freeSpace: Dp?,
  showPhaseName: Boolean,
  measurements: List<SuplaElectricityMeasurementType>,
  selectedLabel: SuplaElectricityMeasurementType? = null,
  onSelectedLabelChanged: ((SuplaElectricityMeasurementType) -> Unit)? = null
) =
  freeSpace?.let { space ->
    // For the single phase, when there is more space, we want that values are aligned to the right side of screen
    if (space > 0.dp) {
      Column(
        modifier = Modifier
          .width(space)
          .background(MaterialTheme.colorScheme.background)
      ) {
        if (showPhaseName) {
          PhaseHeader()
        }
        var energyLabelShown = false
        measurements.forEach {
          if (it.showEnergyLabel && !energyLabelShown) {
            EnergyLabel()
            energyLabelShown = true
          }
          TypeLabel(selected = it == selectedLabel, onClick = onSelectedLabelChanged?.click(it))
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
private fun EnergyLabel(text: String = "") =
  Text(
    text = text,
    style = MaterialTheme.typography.labelMedium,
    color = MaterialTheme.colorScheme.onBackground,
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface)
      .padding(top = Distance.small, bottom = Distance.tiny, start = Distance.small)
      .wrapContentHeight(align = Alignment.CenterVertically)
  )

@Composable
private fun TypeLabel(text: String = "", selected: Boolean = false, onClick: (() -> Unit)? = null) {
  Text(
    text = text,
    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal),
    color = MaterialTheme.colorScheme.onBackground,
    modifier = Modifier
      .padding(bottom = 1.dp)
      .height(35.dp)
      .fillMaxWidth()
      .background(if (selected) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.surface)
      .padding(start = Distance.small)
      .wrapContentHeight(align = Alignment.CenterVertically)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        enabled = onClick != null,
        onClick = onClick ?: {}
      )
  )
}

@Composable
private fun PhaseValue(text: String, selected: Boolean = false, onClick: (() -> Unit)? = null) =
  Text(
    text = text,
    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal),
    color = MaterialTheme.colorScheme.onBackground,
    textAlign = TextAlign.End,
    modifier = Modifier
      .padding(bottom = 1.dp)
      .height(35.dp)
      .defaultMinSize(minWidth = 70.dp)
      .fillMaxWidth()
      .background(if (selected) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.surface)
      .padding(end = 4.dp, start = Distance.small)
      .wrapContentHeight(align = Alignment.CenterVertically)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        enabled = onClick != null,
        onClick = onClick ?: {}
      )
  )

@Composable
private fun PhaseValueUnit(text: String = "", withMargin: Boolean = false, selected: Boolean = false, onClick: (() -> Unit)? = null) =
  Text(
    text = text,
    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal),
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier
      .padding(bottom = 1.dp)
      .height(35.dp)
      .fillMaxWidth()
      .background(if (selected) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.surface)
      .padding(end = if (withMargin) Distance.small else 0.dp)
      .wrapContentHeight(align = Alignment.CenterVertically)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        enabled = onClick != null,
        onClick = onClick ?: {}
      )
  )

@Composable
private fun SingleValueTable(@StringRes headerRes: Int, data: Map<SuplaElectricityMeasurementType, String>) {
  Text(
    text = stringResource(id = headerRes),
    style = MaterialTheme.typography.labelMedium,
    modifier = Modifier.padding(start = Distance.small, top = Distance.default, end = Distance.small)
  )

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
      PhaseDataLabels(measurementTypes = data.keys.toList(), withHeader = false)
      OptionalSpace(freeSpace = freeSpace, showPhaseName = false, measurements = data.keys.toList())
      VectorBalancedDataValues(types = data.keys.toList(), values = data.values.toList())
    }
  }
}

@Composable
private fun VectorBalancedDataValues(
  types: List<SuplaElectricityMeasurementType>,
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
      Column(
        modifier = Modifier.width(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        types.forEach { PhaseValueUnit(it.unit, withMargin = true) }
      }
    }
  }

private fun ((SuplaElectricityMeasurementType) -> Unit).click(type: SuplaElectricityMeasurementType): () -> Unit = { this(type) }

@Composable
private fun Info(scrollState: ScrollState, onClose: () -> Unit, modifier: Modifier = Modifier) {
  val offset = remember { Animatable(100.dp.toPx()) }
  LaunchedEffect("Swipe animation") {
    delay(2000)
    while (true) {
      launch {
        scrollState.animateScrollBy(
          100.dp.toPx(),
          animationSpec = tween(durationMillis = 750, easing = CubicBezierEasing(0.5f, 0.0f, 0.3f, 1.0f))
        )
      }
      launch {
        offset.animateTo((-100).dp.toPx(), animationSpec = tween(durationMillis = 750, easing = CubicBezierEasing(0.5f, 0.0f, 0.3f, 1.0f)))
      }
      delay(750)
      launch {
        scrollState.animateScrollBy(
          (-100).dp.toPx(),
          animationSpec = tween(durationMillis = 750, easing = CubicBezierEasing(0.5f, 0.0f, 0.3f, 1.0f))
        )
      }
      launch {
        offset.animateTo(100.dp.toPx(), animationSpec = tween(durationMillis = 750, easing = CubicBezierEasing(0.5f, 0.0f, 0.3f, 1.0f)))
      }
      delay(2000)
    }
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(30.dp)
        .background(Brush.verticalGradient(listOf(Color.Transparent, colorResource(R.color.info_scrim))))
    )
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(colorResource(R.color.info_scrim))
    ) {
      IconButton(
        R.drawable.ic_close,
        modifier = Modifier
          .padding(Distance.small)
          .size(dimensionResource(R.dimen.icon_default_size))
          .align(Alignment.TopEnd),
        tint = MaterialTheme.colorScheme.onPrimary,
        onClick = onClose
      )
      Box(
        modifier = Modifier
          .padding(bottom = 42.dp)
          .width(200.dp)
          .height(10.dp)
          .background(
            Brush.horizontalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.onPrimary)),
            RoundedCornerShape(size = 5.dp)
          )
          .align(Alignment.BottomCenter)
      )
      androidx.compose.foundation.Image(
        imageVector = Icons.Filled.TouchApp,
        contentDescription = "",
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
        modifier = Modifier
          .offset { IntOffset(offset.value.toInt(), 0) }
          .padding(bottom = Distance.default, top = Distance.default)
          .size(dimensionResource(R.dimen.icon_big_size))
          .align(Alignment.BottomCenter)
      )
    }
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(colorResource(R.color.info_scrim))
    ) {
      Text(
        stringResource(R.string.details_em_info_swipe),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = Distance.default, end = Distance.default, bottom = Distance.small)
      )
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
          SuplaElectricityMeasurementType.VOLTAGE,
          SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY
        ),
        phaseMeasurementValues = listOf(
          PhaseWithMeasurements(
            R.string.details_em_phase1,
            mapOf(
              SuplaElectricityMeasurementType.FREQUENCY to "50",
              SuplaElectricityMeasurementType.CURRENT to "5",
              SuplaElectricityMeasurementType.VOLTAGE to "245",
              SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY to "245"
            )
          ),
          PhaseWithMeasurements(
            R.string.details_em_phase2,
            mapOf(
              SuplaElectricityMeasurementType.FREQUENCY to "50",
              SuplaElectricityMeasurementType.CURRENT to "3",
              SuplaElectricityMeasurementType.VOLTAGE to "243",
              SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY to "245"
            )
          ),
          PhaseWithMeasurements(
            R.string.details_em_phase3,
            mapOf(
              SuplaElectricityMeasurementType.FREQUENCY to "50",
              SuplaElectricityMeasurementType.CURRENT to "4",
              SuplaElectricityMeasurementType.VOLTAGE to "248",
              SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY to "245"
            )
          )
        ),
        vectorBalancedValues = mapOf(
          SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY_BALANCED to "1234,56",
          SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY_BALANCED to "2345,67"
        ),
        electricGridParameters = mapOf(
          SuplaElectricityMeasurementType.VOLTAGE_PHASE_ANGLE_12 to "33",
          SuplaElectricityMeasurementType.VOLTAGE_PHASE_ANGLE_13 to "34",
          SuplaElectricityMeasurementType.CURRENT_PHASE_SEQUENCE to "1-2-3",
          SuplaElectricityMeasurementType.VOLTAGE_PHASE_SEQUENCE to "1-3-2"
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
