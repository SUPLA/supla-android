package org.supla.android.core.ui
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

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.supla.android.core.ui.SizeClass.LANDSCAPE_BIG
import org.supla.android.core.ui.SizeClass.LANDSCAPE_MEDIUM
import org.supla.android.core.ui.SizeClass.LANDSCAPE_SMALL
import org.supla.android.core.ui.SizeClass.PORTRAIT_BIG
import org.supla.android.core.ui.SizeClass.PORTRAIT_MEDIUM
import org.supla.android.core.ui.SizeClass.PORTRAIT_SMALL
import org.supla.android.core.ui.SizeClass.SQUARE_BIG
import org.supla.android.core.ui.SizeClass.SQUARE_MEDIUM
import org.supla.android.core.ui.SizeClass.SQUARE_SMALL
import org.supla.android.core.ui.theme.Distance
import timber.log.Timber

@Composable
fun SizeClassBox(
  modifier: Modifier = Modifier,
  content: @Composable (sizeClass: SizeClass) -> Unit
) = BoxWithConstraints(modifier = modifier) {
  val sizeClass = sizeClass
  Timber.d("Size class for screen: $sizeClass ($maxWidth x $maxHeight)")

  CompositionLocalProvider(LocalSizeClassProvider provides sizeClass) {
    content(sizeClass)
  }
}

enum class SizeClass() {
  SQUARE_SMALL,
  SQUARE_MEDIUM,
  SQUARE_BIG,

  PORTRAIT_SMALL,
  PORTRAIT_MEDIUM,
  PORTRAIT_BIG,

  LANDSCAPE_SMALL,
  LANDSCAPE_MEDIUM,
  LANDSCAPE_BIG
}

val SizeClass?.isSquare: Boolean
  get() = when (this) {
    SQUARE_SMALL, SQUARE_MEDIUM, SQUARE_BIG -> true
    else -> false
  }

val SizeClass?.isPortrait: Boolean
  get() = when (this) {
    PORTRAIT_SMALL, PORTRAIT_MEDIUM, PORTRAIT_BIG -> true
    else -> false
  }

val SizeClass?.isLandscape: Boolean
  get() = when (this) {
    LANDSCAPE_SMALL, LANDSCAPE_MEDIUM, LANDSCAPE_BIG -> true
    else -> false
  }

val SizeClass?.padding: Dp
  @Composable
  get() =
    if (this == null) {
      0.dp
    } else if (isSmall) {
      Distance.tiny
    } else if (isMedium) {
      Distance.small
    } else {
      Distance.default
    }

val SizeClass?.isSmall: Boolean
  get() = when (this) {
    SQUARE_SMALL, PORTRAIT_SMALL, LANDSCAPE_SMALL -> true
    else -> false
  }

val SizeClass?.isMedium: Boolean
  get() = when (this) {
    SQUARE_MEDIUM, PORTRAIT_MEDIUM, LANDSCAPE_MEDIUM -> true
    else -> false
  }

val SizeClass?.isBig: Boolean
  get() = when (this) {
    SQUARE_BIG, PORTRAIT_BIG, LANDSCAPE_BIG -> true
    else -> false
  }

val LocalSizeClassProvider = compositionLocalOf<SizeClass?> { null }

private val BoxWithConstraintsScope.sizeClass: SizeClass
  get() =
    if (maxWidth > maxHeight) {
      if (maxWidth / maxHeight < 1.1f) {
        calculateSquareSize(maxWidth)
      } else {
        calculateLandscapeSize(maxWidth)
      }
    } else {
      if (maxHeight / maxWidth < 1.1f) {
        calculateSquareSize(maxHeight)
      } else {
        calculatePortraitSize(maxHeight)
      }
    }

private fun calculateSquareSize(size: Dp): SizeClass =
  if (size <= 430.dp) {
    SQUARE_SMALL
  } else if (size <= 550.dp) {
    SQUARE_MEDIUM
  } else {
    SQUARE_BIG
  }

private fun calculateLandscapeSize(width: Dp): SizeClass =
  if (width <= 430.dp) {
    LANDSCAPE_SMALL
  } else if (width <= 550.dp) {
    LANDSCAPE_MEDIUM
  } else {
    LANDSCAPE_BIG
  }

private fun calculatePortraitSize(height: Dp): SizeClass =
  if (height <= 430.dp) {
    PORTRAIT_SMALL
  } else if (height <= 550.dp) {
    PORTRAIT_MEDIUM
  } else {
    PORTRAIT_BIG
  }
