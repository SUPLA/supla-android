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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.branding.Configuration
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.views.texts.TitleSmall

@Composable
fun LogoWithSentence(modifier: Modifier = Modifier) =
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(4.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Image(Configuration.General.LOGO_WITH_IMAGE_RESOURCE, modifier = Modifier.widthIn(max = 144.dp))
    TitleSmall(stringRes = R.string.app_sentence)
  }

@SuplaPreview
@Composable
private fun Preview() {
  SuplaTheme {
    LogoWithSentence()
  }
}
