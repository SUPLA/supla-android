package org.supla.android.ui.views.list

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.preferences
import org.supla.android.images.ImageId
import org.supla.android.ui.layouts.BaseSlideableContent
import org.supla.android.ui.lists.data.SlideableListItemData
import org.supla.android.ui.lists.data.default
import org.supla.android.ui.views.list.components.ListItemIcon
import org.supla.android.ui.views.list.components.ListItemMainRow
import org.supla.android.ui.views.list.components.ListItemValue

class IconValueListItemView : BaseSlideableContent<SlideableListItemData.Default> {
  constructor(context: Context) : super(context, null, 0)

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0)

  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  @Composable
  override fun Content() {
    val data = this.data ?: SlideableListItemData.Default.default()

    SuplaTheme {
      IconValueListItemView(
        data = data,
        hasLeftButton = hasLeftButton,
        hasRightButton = hasRightButton,
        onInfoClick = onInfoClick,
        onIssueClick = onIssueClick,
        onTitleLongClick = onTitleLongClick,
        onItemClick = onItemClick
      )
    }
  }
}

@Composable
fun IconValueListItemView(
  data: SlideableListItemData.Default,
  showInfoIcon: Boolean = LocalContext.current.preferences.isShowChannelInfo,
  hasLeftButton: Boolean = false,
  hasRightButton: Boolean = false,
  scale: Float = LocalContext.current.preferences.scale,
  onInfoClick: () -> Unit = { },
  onIssueClick: () -> Unit = { },
  onItemClick: () -> Unit = { },
  onTitleLongClick: () -> Unit = { }
) {
  val title = data.titleProvider(LocalContext.current)
  ListItemScaffold(
    itemTitle = title,
    itemOnline = data.online,
    itemEstimatedEndDate = data.estimatedTimerEndDate,
    onInfoClick = onInfoClick,
    onIssueClick = onIssueClick,
    onTitleLongClick = onTitleLongClick,
    onItemClick = onItemClick,
    itemIssueIconType = data.issueIconType,
    hasLeftButton = hasLeftButton,
    hasRightButton = hasRightButton,
    scale = scale,
    showInfoIcon = showInfoIcon && data.infoSupported
  ) {
    ListItemMainRow(scale = scale) {
      data.icon?.let {
        ListItemIcon(imageId = it, scale = scale)
      }
      data.value?.let {
        ListItemValue(value = it, scale = scale)
      }
    }
  }
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    Column(
      modifier = Modifier
        .background(MaterialTheme.colors.background)
        .width(600.dp)
        .height(dimensionResource(id = R.dimen.channel_layout_height).times(1.5f))
    ) {
      IconValueListItemView(
        data = SlideableListItemData.Default(
          online = true,
          titleProvider = { "Channel" },
          icon = ImageId(R.drawable.fnc_gpm_5),
          value = "100 hPa",
          issueIconType = null,
          infoSupported = true
        ),
        scale = 1f,
        showInfoIcon = true
      )
    }
  }
}
