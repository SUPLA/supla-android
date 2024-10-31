package org.supla.android.ui.views.list
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.preferences
import org.supla.android.images.ImageId
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.layouts.BaseSlideableContent
import org.supla.android.ui.lists.ListOnlineState
import org.supla.android.ui.lists.data.SlideableListItemData
import org.supla.android.ui.lists.data.default
import org.supla.android.ui.views.list.components.ListItemIcon
import org.supla.android.ui.views.list.components.ListItemMainRow
import org.supla.android.ui.views.list.components.ListItemValue
import org.supla.android.usecases.list.CreateListItemUpdateEventDataUseCase
import org.supla.core.shared.data.model.lists.IssueIcon
import org.supla.core.shared.data.model.lists.ListItemIssues
import org.supla.core.shared.infrastructure.LocalizedString
import javax.inject.Inject

@AndroidEntryPoint
class IconValueListItemView : BaseSlideableContent<SlideableListItemData.Default> {
  constructor(context: Context) : super(context, null, 0)

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0)

  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  @Inject
  override lateinit var createListItemUpdateEventDataUseCase: CreateListItemUpdateEventDataUseCase

  @Inject
  override lateinit var schedulers: SuplaSchedulers

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
  onIssueClick: (ListItemIssues) -> Unit = { },
  onItemClick: () -> Unit = { },
  onTitleLongClick: () -> Unit = { }
) {
  val title = data.title(LocalContext.current)
  ListItemScaffold(
    itemTitle = title,
    itemOnlineState = data.onlineState,
    itemEstimatedEndDate = data.estimatedTimerEndDate,
    onInfoClick = onInfoClick,
    onIssueClick = onIssueClick,
    onTitleLongClick = onTitleLongClick,
    onItemClick = onItemClick,
    issues = data.issues,
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
        .background(MaterialTheme.colorScheme.background)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(dimensionResource(id = R.dimen.channel_layout_height).times(0.75f))
      ) {
        IconValueListItemView(
          data = SlideableListItemData.Default(
            onlineState = ListOnlineState.ONLINE,
            title = LocalizedString.Constant("Channel"),
            icon = ImageId(R.drawable.fnc_gpm_5),
            value = "100 hPa",
            issues = ListItemIssues(IssueIcon.Warning),
            infoSupported = true
          ),
          scale = 0.75f,
          showInfoIcon = true
        )
      }
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(dimensionResource(id = R.dimen.channel_layout_height))
      ) {
        IconValueListItemView(
          data = SlideableListItemData.Default(
            onlineState = ListOnlineState.ONLINE,
            title = LocalizedString.Constant("Channel"),
            icon = ImageId(R.drawable.fnc_gpm_5),
            value = "100 hPa",
            issues = ListItemIssues(IssueIcon.Error),
            infoSupported = true
          ),
          scale = 1f,
          showInfoIcon = true
        )
      }
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(dimensionResource(id = R.dimen.channel_layout_height).times(1.5f))
      ) {
        IconValueListItemView(
          data = SlideableListItemData.Default(
            onlineState = ListOnlineState.ONLINE,
            title = LocalizedString.Constant("Channel"),
            icon = ImageId(R.drawable.fnc_gpm_5),
            value = "100 hPa",
            issues = ListItemIssues(IssueIcon.Error),
            infoSupported = true
          ),
          scale = 1.5f,
          showInfoIcon = true
        )
      }
    }
  }
}
