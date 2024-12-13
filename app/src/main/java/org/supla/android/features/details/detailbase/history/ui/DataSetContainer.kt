package org.supla.android.features.details.detailbase.history.ui

import androidx.annotation.DimenRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.images.ImageId
import org.supla.android.ui.views.Image

@Composable
fun DataSetContainer(
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
        DataSetItems(
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
private fun DataSetItems(
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
      .height(dimensionResource(id = R.dimen.button_small_height))
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
      is HistoryDataSet.Label.Single -> DataSetItem(
        value = label.value,
        showColor = label.value.presentColor,
        active = historyEnabled && active
      )

      is HistoryDataSet.Label.Multiple ->
        label.values.forEach {
          if (!it.justColor) {
            DataSetItem(value = it, showColor = it.presentColor, active = historyEnabled && active)
          }
        }
    }
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
