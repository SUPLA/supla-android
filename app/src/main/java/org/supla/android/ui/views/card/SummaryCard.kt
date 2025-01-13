package org.supla.android.ui.views.card
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.features.details.detailbase.electricitymeter.suplaCard
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.Separator
import org.supla.android.ui.views.SeparatorStyle

@Composable
fun DoubleSummaryCard(
  label: String,
  firstData: SummaryCardData,
  secondData: SummaryCardData,
  modifier: Modifier = Modifier,
  loading: Boolean = false
) {
  val radius = dimensionResource(id = R.dimen.radius_default)
  val density = LocalDensity.current
  var spacerHeight by remember { mutableStateOf<Dp?>(null) }

  Column(
    modifier = modifier.suplaCard(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Label(
      text = label,
      modifier = Modifier.padding(all = Distance.tiny)
    )

    Separator(style = SeparatorStyle.OUTLINE)

    Box(
      modifier = Modifier
        .onGloballyPositioned {
          if (spacerHeight == null || spacerHeight == 0.dp) {
            spacerHeight = with(density) { it.size.height.toDp() }
          }
        }
    ) {
      Row {
        DoubleSummaryBox(
          iconRes = R.drawable.ic_forward_energy,
          label = stringResource(id = R.string.details_em_forwarded_energy),
          value = firstData.value,
          price = firstData.price,
          modifier = Modifier
            .weight(0.5f)
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(bottomStart = radius))
        )
        Spacer(height = spacerHeight)
        DoubleSummaryBox(
          iconRes = R.drawable.ic_reversed_energy,
          label = stringResource(id = R.string.details_em_reversed_energy),
          value = secondData.value,
          price = secondData.price,
          modifier = Modifier
            .weight(0.5f)
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(bottomEnd = radius))
        )
      }
      if (loading) {
        LoadingBox(spacerHeight)
      }
    }
  }
}

@Composable
fun SingleSummaryCard(
  label: String,
  data: SummaryCardData,
  modifier: Modifier = Modifier,
  iconRes: Int? = null,
  loading: Boolean = false
) {
  val radius = dimensionResource(id = R.dimen.radius_default)
  val density = LocalDensity.current
  var spacerHeight by remember { mutableStateOf<Dp?>(null) }

  Column(
    modifier = modifier.suplaCard(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .onGloballyPositioned {
          if (spacerHeight == null || spacerHeight == 0.dp) {
            spacerHeight = with(density) { it.size.height.toDp() }
          }
        }
    ) {
      SingleSummaryBox(
        iconRes = iconRes,
        label = label,
        value = data.value,
        price = data.price,
        modifier = Modifier.background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(radius))
      )
      if (loading) {
        LoadingBox(height = spacerHeight)
      }
    }
  }
}

@Composable
private fun LoadingBox(height: Dp?) =
  Box(
    modifier = Modifier
      .height(height ?: 0.dp)
      .fillMaxWidth()
      .background(colorResource(id = R.color.dialog_scrim))
  ) {
    CircularProgressIndicator(
      modifier = Modifier
        .align(Alignment.Center)
        .padding(Distance.small)
        .size(dimensionResource(id = R.dimen.button_small_height))
    )
  }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DoubleSummaryBox(iconRes: Int, label: String, value: String, price: String?, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.padding(Distance.small),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    IconAndLabel(iconRes = iconRes, label = label)
    Text(
      text = value,
      style = MaterialTheme.typography.labelLarge
    )
    price?.let {
      Separator(style = SeparatorStyle.OUTLINE)
      FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Label(text = stringResource(id = R.string.details_em_cost))
        Text(
          text = it,
          style = MaterialTheme.typography.labelMedium
        )
      }
    }
  }
}

@Composable
private fun SingleSummaryBox(iconRes: Int?, label: String, value: String, price: String?, modifier: Modifier = Modifier) {
  var spacerHeight by remember { mutableStateOf<Dp?>(null) }
  val density = LocalDensity.current
  Row(
    modifier = modifier
      .padding(Distance.small)
      .fillMaxWidth()
      .height(IntrinsicSize.Min)
      .onGloballyPositioned {
        if (spacerHeight == null) {
          spacerHeight = with(density) { it.size.height.toDp() }
        }
      },
    horizontalArrangement = Arrangement.spacedBy(Distance.small),
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.weight(1f)
    ) {
      IconAndLabel(iconRes = iconRes, label = label, maxLines = 2)
      EnergyValue(text = value)
    }
    price?.let {
      Spacer(height = spacerHeight)
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End,
      ) {
        Label(text = stringResource(id = R.string.details_em_cost))
        Spacer(modifier = Modifier.weight(1f))
        PriceValue(text = it)
      }
    }
  }
}

@Composable
private fun IconAndLabel(iconRes: Int?, label: String, maxLines: Int = 1) =
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
    iconRes?.let {
      Image(drawableId = it, modifier = Modifier.size(dimensionResource(id = R.dimen.icon_small_size)))
    }
    Label(text = label, maxLines = maxLines, modifier = Modifier.fillMaxWidth())
  }

@Composable
private fun Label(text: String, maxLines: Int = 1, modifier: Modifier = Modifier) =
  Text(
    text = text,
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = modifier,
    maxLines = maxLines,
    overflow = TextOverflow.Ellipsis,
  )

@Composable
private fun Spacer(height: Dp?) =
  Spacer(
    modifier = Modifier
      .width(1.dp)
      .height(height = height ?: 0.dp)
      .background(MaterialTheme.colorScheme.outline)
  )

@Composable
private fun PriceValue(text: String) =
  Text(
    text = text,
    style = MaterialTheme.typography.labelMedium
  )

@Composable
private fun EnergyValue(text: String) =
  Text(
    text = text,
    style = MaterialTheme.typography.labelLarge
  )
