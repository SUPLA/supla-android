package org.supla.android.ui.views

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

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.core.ui.theme.listItemValue
import org.supla.android.extensions.max
import org.supla.android.ui.layouts.BaseSlideableContent
import org.supla.android.ui.lists.data.IssueIconType
import org.supla.android.ui.lists.data.SlideableListItemData
import org.supla.android.ui.lists.data.default
import org.supla.android.ui.views.list.ListItemScaffold
import javax.inject.Inject

@AndroidEntryPoint
class ThermostatListItemView : BaseSlideableContent<SlideableListItemData.Thermostat> {

  constructor(context: Context) : super(context, null, 0)

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0) {
    loadAttributes(context, attrs)
  }

  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    loadAttributes(context, attrs)
  }

  @Inject
  lateinit var preferences: Preferences

  var hasLeftButton: Boolean = false
  var hasRightButton: Boolean = false

  @Composable
  override fun Content() {
    val data = this.data ?: SlideableListItemData.Thermostat.default()
    val scale = preferences.channelHeight.div(100f)

    SuplaTheme {
      ThermostatListItemView(
        data = data,
        showInfoIcon = preferences.isShowChannelInfo,
        scale = scale,
        hasLeftButton = hasLeftButton,
        hasRightButton = hasRightButton,
        onInfoClick = onInfoClick,
        onIssueClick = onIssueClick,
        onTitleLongClick = onTitleLongClick
      )
    }
  }

  private fun loadAttributes(context: Context, attrs: AttributeSet?) {
    context.theme.obtainStyledAttributes(attrs, R.styleable.ThermostatListItemView, 0, 0).apply {
      try {
        hasLeftButton = getBoolean(R.styleable.ThermostatListItemView_hasLeftButton, false)
        hasRightButton = getBoolean(R.styleable.ThermostatListItemView_hasRightButton, false)
      } finally {
        recycle()
      }
    }
  }
}

@Composable
fun ThermostatListItemView(
  data: SlideableListItemData.Thermostat,
  showInfoIcon: Boolean,
  scale: Float = 1f,
  hasLeftButton: Boolean = false,
  hasRightButton: Boolean = false,
  onInfoClick: () -> Unit = { },
  onIssueClick: () -> Unit = { },
  onTitleLongClick: () -> Unit = { }
) {
  ListItemScaffold(
    itemTitle = data.titleProvider(LocalContext.current),
    online = data.online,
    scale = scale,
    hasLeftButton = hasLeftButton,
    hasRightButton = hasRightButton,
    onInfoClick = onInfoClick,
    onTitleLongClick = onTitleLongClick,
    showInfoIcon = showInfoIcon,
    issueIconType = data.issueIconType,
    onIssueClick = onIssueClick
  ) {
    Row(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = dimensionResource(id = R.dimen.distance_default).times(scale)),
      horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.distance_tiny)),
      verticalAlignment = Alignment.CenterVertically
    ) {
      data.iconProvider?.let {
        Image(
          bitmap = it(LocalContext.current).asImageBitmap(),
          contentDescription = null,
          alignment = Alignment.Center,
          modifier = Modifier
            .width(dimensionResource(id = R.dimen.channel_img_width).times(scale))
            .height(dimensionResource(id = R.dimen.channel_img_height).times(scale)),
          contentScale = ContentScale.Fit
        )
      }

      if (scale <= 1f) {
        CurrentTemperature(temperature = data.value, scale = scale)
        Column {
          SetpointTemperature(data = data, scale = scale)
        }
      } else {
        Column {
          CurrentTemperature(temperature = data.value, scale = scale)
          SetpointTemperature(data = data, scale = scale)
        }
      }
    }
  }
}

@Composable
private fun CurrentTemperature(temperature: String, scale: Float) {
  val valueSize = MaterialTheme.typography.listItemValue().fontSize.let { max(it, it.times(scale)) }

  Text(
    text = temperature,
    style = MaterialTheme.typography.listItemValue().copy(fontSize = valueSize),
    fontFamily = FontFamily(Font(R.font.open_sans_regular))
  )
}

@Composable
private fun SetpointTemperature(data: SlideableListItemData.Thermostat, scale: Float) =
  Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
    data.indicatorIcon?.let {
      val indicatorSize = androidx.compose.ui.unit.max(12.dp, 12.dp.times(scale))
      Image(
        painter = painterResource(id = it),
        contentDescription = null,
        modifier = Modifier
          .width(indicatorSize)
          .height(indicatorSize),
        contentScale = ContentScale.Fit
      )
    }

    val subValueSize = MaterialTheme.typography.body2.fontSize.let { max(it, it.times(scale)) }
    Text(
      text = data.subValue,
      style = MaterialTheme.typography.body2.copy(fontSize = subValueSize)
    )
  }

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    Column(modifier = Modifier.background(MaterialTheme.colors.background)) {
      Column(
        modifier = Modifier
          .width(600.dp)
          .height(dimensionResource(id = R.dimen.channel_layout_height).times(1.5f))
      ) {
        ThermostatListItemView(
          data = SlideableListItemData.Thermostat(
            online = true,
            titleProvider = { "Thermostat" },
            iconProvider = { ResourcesCompat.getDrawable(it.resources, R.drawable.ic_thermostat_cool, null)!!.toBitmap() },
            value = "20,7°C",
            subValue = "21,0°",
            indicatorIcon = R.drawable.ic_cooling,
            issueIconType = IssueIconType.WARNING
          ),
          true,
          scale = 1.5f
        )
      }
      Column(
        modifier = Modifier
          .width(600.dp)
          .height(dimensionResource(id = R.dimen.channel_layout_height))
      ) {
        ThermostatListItemView(
          data = SlideableListItemData.Thermostat(
            online = true,
            titleProvider = { "Thermostat" },
            iconProvider = { ResourcesCompat.getDrawable(it.resources, R.drawable.ic_thermostat_cool, null)!!.toBitmap() },
            value = "20,7°C",
            subValue = "21,0°",
            indicatorIcon = R.drawable.ic_cooling,
            issueIconType = IssueIconType.ERROR
          ),
          false
        )
      }
      Column(
        modifier = Modifier
          .width(600.dp)
          .height(dimensionResource(id = R.dimen.channel_layout_height).times(0.6f))
      ) {
        ThermostatListItemView(
          data = SlideableListItemData.Thermostat(
            online = true,
            titleProvider = { "Thermostat" },
            iconProvider = { ResourcesCompat.getDrawable(it.resources, R.drawable.ic_thermostat_cool, null)!!.toBitmap() },
            value = "20,7°C",
            subValue = "21,0°",
            indicatorIcon = R.drawable.ic_cooling,
            issueIconType = null
          ),
          true,
          scale = 0.6f
        )
      }
      Column(
        modifier = Modifier
          .width(600.dp)
          .height(dimensionResource(id = R.dimen.channel_layout_height).times(0.6f))
      ) {
        ThermostatListItemView(
          data = SlideableListItemData.Thermostat(
            online = true,
            titleProvider = { "Thermostat with very long name which goes out of the screen and must be cut" },
            iconProvider = { ResourcesCompat.getDrawable(it.resources, R.drawable.ic_thermostat_cool, null)!!.toBitmap() },
            value = "20,7°C",
            subValue = "21,0°",
            indicatorIcon = R.drawable.ic_cooling,
            issueIconType = IssueIconType.WARNING
          ),
          false,
          scale = 0.6f
        )
      }
    }
  }
}
