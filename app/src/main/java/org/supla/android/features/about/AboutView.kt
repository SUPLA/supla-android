package org.supla.android.features.about
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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import org.supla.android.BuildConfig
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme

data class AboutViewState(
  val version: String = BuildConfig.VERSION_NAME,
  val buildTime: String = ""
)

@Composable
fun AboutView(
  viewState: AboutViewState,
  onSuplaUrlClick: () -> Unit = {}
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(Distance.default),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(all = Distance.default)
    ) {
      Image(
        painter = painterResource(id = R.drawable.logo_light),
        contentDescription = stringResource(id = R.string.app_name),
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
        modifier = Modifier.size(100.dp),
        contentScale = ContentScale.Fit
      )
      Text(
        text = stringResource(id = R.string.menubar_title),
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onBackground
      )
      Text(
        text = "${stringResource(id = R.string.version)} ${viewState.version}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = stringResource(id = R.string.about_app),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .verticalScroll(rememberScrollState())
          .weight(1f)
      )
      TextButton(onClick = onSuplaUrlClick) {
        Text(text = stringResource(id = R.string.homepage))
      }
      Text(
        text = "${stringResource(id = R.string.about_build_time)} ${viewState.buildTime}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Light
      )
    }
  }
}

@PreviewScreenSizes
@PreviewFontScale
@Composable
private fun Preview() {
  SuplaTheme {
    AboutView(AboutViewState(buildTime = "24.06.2024 08:48 (224)"))
  }
}
