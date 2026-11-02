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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import org.supla.android.R
import org.supla.android.lib.actions.SubjectType
import org.supla.android.ui.lists.ListOnlineState
import org.supla.android.ui.views.list.components.ListItemRect
import org.supla.android.ui.views.list.components.RectColors

enum class StatusSide { START, END }

sealed interface ListItemStatus {
  val isGroup: Boolean
  val online: Boolean
  val type: SubjectType

  data class Channel(
    val onlineState: ListOnlineState
  ) : ListItemStatus {
    override val isGroup: Boolean = false
    override val online: Boolean = onlineState.online
    override val type: SubjectType = SubjectType.CHANNEL
  }

  data class Group(
    val onlinePercentage: Float,
    val activePercentage: Float
  ) : ListItemStatus {
    override val isGroup: Boolean = true
    override val online: Boolean = onlinePercentage > 0
    override val type: SubjectType = SubjectType.GROUP
  }

  data object Scene : ListItemStatus {
    override val isGroup: Boolean = false
    override val online: Boolean = true
    override val type: SubjectType = SubjectType.SCENE
  }
}

class StatusIndicator(
  val listItemStatus: ListItemStatus,
  val hasLeftButton: Boolean,
  val hasRightButton: Boolean
) {
  val isGroup: Boolean
    get() = listItemStatus.isGroup

  @Composable
  fun View(side: StatusSide, modifier: Modifier = Modifier) {
    when (val type = listItemStatus) {
      is ListItemStatus.Channel -> DotStatusView(onlineState = type.onlineState, side, modifier)
      is ListItemStatus.Group ->
        RectStatusView(
          onlinePercentage = type.onlinePercentage,
          activePercentage = type.activePercentage,
          side = side,
          modifier = modifier
        )
      ListItemStatus.Scene -> DotStatusView(onlineState = ListOnlineState.ONLINE, side, modifier)
    }
  }

  @Composable
  private fun DotStatusView(onlineState: ListOnlineState, side: StatusSide, modifier: Modifier) {
    ListItemDot(
      onlineState = onlineState,
      withButton = (side == StatusSide.START && hasLeftButton) || (side == StatusSide.END && hasRightButton),
      paddingValues =
      when (side) {
        StatusSide.START -> PaddingValues(start = dimensionResource(id = R.dimen.list_horizontal_spacing))
        StatusSide.END -> PaddingValues(end = dimensionResource(id = R.dimen.list_horizontal_spacing))
      },
      modifier = modifier
    )
  }

  @Composable
  private fun RectStatusView(onlinePercentage: Float, activePercentage: Float, side: StatusSide, modifier: Modifier) {
    when (side) {
      StatusSide.START ->
        if (hasLeftButton) {
          ListItemRect(
            percentage = onlinePercentage,
            colors = RectColors.Online,
            modifier = modifier,
            paddingValues = PaddingValues(start = dimensionResource(id = R.dimen.list_horizontal_spacing))
          )
        } else {
          Box(
            modifier = Modifier
              .size(dimensionResource(R.dimen.channel_dot_size))
              .padding(start = dimensionResource(R.dimen.list_horizontal_spacing))
          )
        }
      StatusSide.END ->
        Row(
          modifier = modifier.padding(end = dimensionResource(id = R.dimen.list_horizontal_spacing))
        ) {
          ListItemRect(
            percentage = activePercentage,
            colors = RectColors.Active,
          )
          ListItemRect(
            percentage = onlinePercentage,
            colors = RectColors.Online,
          )
        }
    }
  }
}
