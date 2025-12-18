package org.supla.android.features.details.detailbase.history.ui

import androidx.annotation.DimenRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.images.ImageId
import org.supla.android.tools.SuplaComponentPreview
import org.supla.android.ui.views.Image
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.channel.valueformatter.formatters.HumidityValueFormatter

@Composable
fun DataSetContainerRow(
  data: ChannelChartSets,
  showHistory: Boolean,
  modifier: Modifier = Modifier,
  onClick: (Int, ChartEntryType) -> Unit = { _, _ -> }
) =
  Column(
    modifier = modifier
      .padding(start = Distance.default, end = Distance.default)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        enabled = data.dataSets.size == 1,
        indication = ripple(),
        onClick = { onClick(data.remoteId, data.dataSets[0].type) }
      ),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    data.typeName?.let {
      Text(text = it(LocalContext.current), style = MaterialTheme.typography.labelSmall)
    }
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      data.dataSets.forEach { set ->
        DataSetItemsRow(
          channelIcon = data.icon,
          label = set.label,
          active = set.active,
          historyEnabled = showHistory,
          clickEnabled = data.dataSets.size > 1,
          onClick = { onClick(data.remoteId, set.type) }
        )
      }
    }
  }

@Composable
fun DataSetContainerColumn(
  data: ChannelChartSets,
  showHistory: Boolean,
  modifier: Modifier = Modifier,
  onClick: (Int, ChartEntryType) -> Unit = { _, _ -> }
) =
  Column(
    modifier = modifier
      .padding(start = Distance.vertical, end = Distance.vertical)
      .fillMaxWidth()
      .width(IntrinsicSize.Max)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        enabled = data.dataSets.size == 1,
        indication = ripple(),
        onClick = { onClick(data.remoteId, data.dataSets[0].type) }
      ),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(Distance.small)
  ) {
    data.typeName?.let {
      Text(
        text = it(LocalContext.current),
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
      )
    }
    Row(
      horizontalArrangement = Arrangement.spacedBy(Distance.tiny),
      verticalAlignment = Alignment.CenterVertically
    ) {
      data.icon?.let { DataSetIcon(imageId = it, null) }
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Distance.small)
      ) {
        data.dataSets.forEach { set ->
          DataSetItemsColumn(
            label = set.label,
            active = set.active,
            historyEnabled = showHistory,
            clickEnabled = data.dataSets.size > 1,
            onClick = { onClick(data.remoteId, set.type) }
          )
        }
      }
    }
  }

@Composable
private fun DataSetItemsRow(
  channelIcon: ImageId?,
  label: HistoryDataSet.Label,
  active: Boolean,
  historyEnabled: Boolean,
  clickEnabled: Boolean,
  onClick: () -> Unit
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .defaultMinSize(minHeight = dimensionResource(id = R.dimen.button_small_height))
      .let {
        if (clickEnabled) {
          it.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(),
            onClick = onClick
          )
        } else {
          it
        }
      }

  ) {
    channelIcon?.let { DataSetIcon(it, null) }
    when (label) {
      is HistoryDataSet.Label.Single -> DataSetItem(
        value = label.value,
        showColor = label.value.presentColor,
        active = historyEnabled && active
      )

      is HistoryDataSet.Label.Multiple -> {
        label.values.forEach {
          if (!it.justColor) {
            DataSetItem(value = it, showColor = it.presentColor, active = historyEnabled && active)
          }
        }
      }
    }
  }
}

@Composable
private fun DataSetItemsColumn(
  label: HistoryDataSet.Label,
  active: Boolean,
  historyEnabled: Boolean,
  clickEnabled: Boolean,
  onClick: () -> Unit
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.End,
    modifier = Modifier
      .defaultMinSize(minHeight = dimensionResource(id = R.dimen.button_small_height))
      .let {
        if (clickEnabled) {
          it.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(),
            onClick = onClick
          )
        } else {
          it
        }
      }

  ) {
    when (label) {
      is HistoryDataSet.Label.Single -> DataSetItemInRow(
        value = label.value,
        showColor = label.value.presentColor,
        active = historyEnabled && active
      )

      is HistoryDataSet.Label.Multiple ->
        label.values.forEach {
          if (!it.justColor) {
            DataSetItemInRow(value = it, showColor = it.presentColor, active = historyEnabled && active)
          }
        }
    }
  }
}

@Composable
private fun DataSetItemInRow(value: HistoryDataSet.LabelData, showColor: Boolean, active: Boolean) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    DataSetItem(
      value = value,
      showColor = showColor,
      active = active
    )
  }
}

@Composable
private fun DataSetItem(value: HistoryDataSet.LabelData, showColor: Boolean, active: Boolean) {
  value.imageId?.let { DataSetIcon(imageId = it, value.iconSize) }
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    DataSetText(text = value.value)
    if (showColor) {
      Box(
        modifier = Modifier
          .width(50.dp)
          .height(4.dp)
          .let {
            if (active) {
              it.background(colorResource(id = value.color), shape = RoundedCornerShape(2.dp))
            } else {
              it
            }
          }
          .border(1.dp, colorResource(id = value.color), RoundedCornerShape(2.dp))
      )
    }
    value.description?.let {
      Text(
        text = it(LocalContext.current),
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
          .padding(top = 4.dp)
          .padding(horizontal = Distance.tiny)
      )
    }
  }
}

@Composable
private fun DataSetIcon(imageId: ImageId, @DimenRes iconSize: Int?) =
  Image(
    imageId = imageId,
    contentDescription = null,
    alignment = Alignment.Center,
    modifier = Modifier.size(dimensionResource(id = iconSize ?: R.dimen.button_small_height))
  )

@Composable
private fun DataSetText(text: String) =
  Text(
    text = text,
    fontSize = 16.sp,
    fontFamily = FontFamily(Font(R.font.quicksand_regular)),
  )

private val testSets1 =
  ChannelChartSets(
    remoteId = 1,
    function = SuplaFunction.ELECTRICITY_METER,
    name = LocalizedString.Constant("name"),
    aggregation = ChartDataAggregation.MINUTES,
    icon = ImageId(R.drawable.fnc_electricity_meter),
    dataSets = listOf(
      HistoryDataSet(
        type = ChartEntryType.POWER_ACTIVE,
        label = HistoryDataSet.Label.Multiple(
          values = listOf(
            HistoryDataSet.LabelData(
              imageId = null,
              value = "456 kWh",
              color = R.color.chart_pie_1,
            ),
            HistoryDataSet.LabelData(
              imageId = null,
              value = "345 kWh",
              color = R.color.chart_pie_2,
            ),
            HistoryDataSet.LabelData(
              imageId = null,
              value = "334 kWh",
              color = R.color.chart_pie_4,
            )
          )
        ),
        valueFormatter = HumidityValueFormatter
      )
    ),
    typeName = LocalizedString.Constant("type name")
  )

private val testSets2 =
  ChannelChartSets(
    remoteId = 1,
    function = SuplaFunction.ELECTRICITY_METER,
    name = LocalizedString.Constant("name"),
    aggregation = ChartDataAggregation.MINUTES,
    dataSets = listOf(
      HistoryDataSet(
        type = ChartEntryType.ELECTRICITY,
        label = HistoryDataSet.Label.Multiple(
          values = listOf(
            HistoryDataSet.LabelData(
              imageId = ImageId(R.drawable.ic_forward_energy),
              value = "345 kWh",
              color = R.color.chart_pie_2,
            ),
            HistoryDataSet.LabelData(
              imageId = ImageId(R.drawable.ic_reversed_energy),
              value = "334 kWh",
              color = R.color.chart_pie_4,
            )
          )
        ),
        valueFormatter = HumidityValueFormatter
      ),
    ),
    typeName = LocalizedString.Constant("type name")
  )

private val testSets3 =
  ChannelChartSets(
    remoteId = 1,
    function = SuplaFunction.ELECTRICITY_METER,
    name = LocalizedString.Constant("name"),
    aggregation = ChartDataAggregation.MINUTES,
    dataSets = listOf(
      HistoryDataSet(
        type = ChartEntryType.TEMPERATURE,
        label = HistoryDataSet.Label.Single(
          value = HistoryDataSet.LabelData(
            imageId = ImageId(R.drawable.fnc_thermometer),
            value = "25",
            color = R.color.chart_pie_1,
          )
        ),
        valueFormatter = HumidityValueFormatter
      )
    ),
    typeName = LocalizedString.Constant("type name")
  )

private val testSets4 =
  ChannelChartSets(
    remoteId = 1,
    function = SuplaFunction.ELECTRICITY_METER,
    name = LocalizedString.Constant("name"),
    aggregation = ChartDataAggregation.MINUTES,
    dataSets = listOf(
      HistoryDataSet(
        type = ChartEntryType.HUMIDITY,
        label = HistoryDataSet.Label.Single(
          value = HistoryDataSet.LabelData(
            imageId = ImageId(R.drawable.fnc_humidity),
            value = "75%",
            color = R.color.chart_pie_1,
          )
        ),
        valueFormatter = HumidityValueFormatter
      )
    ),
    typeName = LocalizedString.Constant("type name")
  )

private val testSets5 =
  ChannelChartSets(
    remoteId = 1,
    function = SuplaFunction.HUMIDITY_AND_TEMPERATURE,
    name = LocalizedString.Constant("name"),
    aggregation = ChartDataAggregation.MINUTES,
    dataSets = listOf(
      HistoryDataSet(
        type = ChartEntryType.TEMPERATURE,
        label = HistoryDataSet.Label.Single(
          value = HistoryDataSet.LabelData(
            imageId = ImageId(R.drawable.fnc_humidity),
            value = "25 C",
            color = R.color.chart_pie_1,
          )
        ),
        valueFormatter = HumidityValueFormatter
      ),
      HistoryDataSet(
        type = ChartEntryType.HUMIDITY,
        label = HistoryDataSet.Label.Single(
          value = HistoryDataSet.LabelData(
            imageId = ImageId(R.drawable.fnc_humidity),
            value = "75%",
            color = R.color.chart_pie_2,
          )
        ),
        valueFormatter = HumidityValueFormatter
      )
    ),
    typeName = LocalizedString.Constant("type name")
  )

@SuplaComponentPreview
@Composable
private fun Preview() {
  SuplaTheme {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
      DataSetContainerRow(data = testSets1, showHistory = true)
      DataSetContainerRow(data = testSets2, showHistory = true)
      DataSetContainerRow(data = testSets3, showHistory = true)
      DataSetContainerRow(data = testSets4, showHistory = true)
      DataSetContainerRow(data = testSets5, showHistory = true)
    }
  }
}

@SuplaComponentPreview
@Composable
private fun PreviewLandscape() {
  SuplaTheme {
    Column(modifier = Modifier.width(IntrinsicSize.Max)) {
      DataSetContainerColumn(data = testSets1, showHistory = true)
      DataSetContainerColumn(data = testSets2, showHistory = true)
      DataSetContainerColumn(data = testSets3, showHistory = true)
      DataSetContainerColumn(data = testSets4, showHistory = true)
      DataSetContainerColumn(data = testSets5, showHistory = true)
    }
  }
}
