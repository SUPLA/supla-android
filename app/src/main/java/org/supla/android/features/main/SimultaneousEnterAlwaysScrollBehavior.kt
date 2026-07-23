package org.supla.android.features.main
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

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateTo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberSimultaneousEnterAlwaysScrollBehavior(
  state: TopAppBarState = rememberTopAppBarState(),
  canScroll: () -> Boolean = { true }
): TopAppBarScrollBehavior {
  val currentCanScroll by rememberUpdatedState(canScroll)

  return remember(state) {
    object : TopAppBarScrollBehavior {

      override val state: TopAppBarState = state

      override val isPinned: Boolean = false

      override val snapAnimationSpec: AnimationSpec<Float>? = null

      override val flingAnimationSpec: DecayAnimationSpec<Float>? = null

      override val nestedScrollConnection =
        object : NestedScrollConnection {

          @Suppress("SameReturnValue")
          override fun onPreScroll(
            available: Offset,
            source: NestedScrollSource
          ): Offset {
            val expandingCollapsedTopBar = available.y > 0f && state.heightOffset < 0f
            if (!currentCanScroll() && !expandingCollapsedTopBar) {
              return Offset.Zero
            }

            state.heightOffset =
              (state.heightOffset + available.y)
                .coerceIn(
                  minimumValue = state.heightOffsetLimit,
                  maximumValue = 0f
                )
            return Offset.Zero
          }

          override suspend fun onPostFling(
            consumed: Velocity,
            available: Velocity
          ): Velocity {
            if (currentCanScroll() || state.heightOffset < 0f) {
              state.snapToNearestEdge()
            }
            return Velocity.Zero
          }
        }
    }
  }
}

private suspend fun TopAppBarState.snapToNearestEdge() {
  if (heightOffsetLimit == 0f || heightOffset == 0f || heightOffset == heightOffsetLimit) {
    return
  }

  val targetOffset =
    if (heightOffset > heightOffsetLimit / 2f) {
      0f
    } else {
      heightOffsetLimit
    }

  AnimationState(heightOffset)
    .animateTo(targetOffset) {
      heightOffset = value.coerceIn(
        minimumValue = heightOffsetLimit,
        maximumValue = 0f
      )
    }
}
