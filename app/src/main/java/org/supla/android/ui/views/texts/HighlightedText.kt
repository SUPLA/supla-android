package org.supla.android.ui.views.texts
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

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import org.supla.android.main.topbar.searchable

@Composable
fun buildHighlightedText(
  text: String,
  searched: String,
  highlightColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
): AnnotatedString {
  if (!searched.searchable) {
    return AnnotatedString(text)
  }

  return buildAnnotatedString {
    var currentIndex = 0
    var matchIndex = text.indexOf(searched, ignoreCase = true)

    while (matchIndex >= 0) {
      append(text.substring(currentIndex, matchIndex))

      withStyle(SpanStyle(background = highlightColor)) {
        append(text.substring(matchIndex, matchIndex + searched.length))
      }

      currentIndex = matchIndex + searched.length
      matchIndex = text.indexOf(
        searched,
        startIndex = currentIndex,
        ignoreCase = true
      )
    }

    append(text.substring(currentIndex))
  }
}
