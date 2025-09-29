package org.supla.android.features.developerinfo
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.local.entity.ChannelStateEntity
import org.supla.android.ui.views.settings.SettingsList
import org.supla.android.ui.views.settings.SettingsListItem
import org.supla.android.usecases.developerinfo.TableDetail

data class DeveloperInfoViewState(
  val rotationEnabled: Boolean = false,
  val suplaTableDetails: List<TableDetail> = emptyList(),
  val measurementTableDetails: List<TableDetail> = emptyList()
)

interface DeveloperInfoScope {
  fun setRotationEnabled(enabled: Boolean)
}

@Composable
fun DeveloperInfoScope.View(
  viewState: DeveloperInfoViewState
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(Distance.tiny),
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(top = Distance.default)
      .verticalScroll(rememberScrollState())
  ) {
    HeaderLarge(text = stringResource(R.string.developer_info_settings))
    SettingsList {
      SettingsListItem(
        label = stringResource(R.string.developer_info_screen_orientation),
        checked = viewState.rotationEnabled
      ) { setRotationEnabled(it) }
    }

    Spacer(modifier = Modifier.height(Distance.tiny))

    HeaderLarge(text = stringResource(R.string.developer_info_database_section))
    HeaderSmall(text = "Supla")
    viewState.suplaTableDetails.forEach {
      Row(modifier = Modifier.padding(horizontal = Distance.default)) {
        Text("${it.name}: ", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.weight(1f))
        Text(it.count.toString(), style = MaterialTheme.typography.bodyMedium)
      }
    }
    HeaderSmall("Measurements")
    viewState.measurementTableDetails.forEach {
      Row(modifier = Modifier.padding(horizontal = Distance.default)) {
        Text("${it.name}: ", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.weight(1f))
        Text(it.count.toString(), style = MaterialTheme.typography.bodyMedium)
      }
    }
  }
}

@Composable
private fun HeaderLarge(text: String) =
  Text(
    text = text,
    style = MaterialTheme.typography.titleLarge,
    modifier = Modifier.padding(horizontal = Distance.default)
      .padding(bottom = Distance.tiny)
  )

@Composable
private fun HeaderSmall(text: String) =
  Text(
    text = text,
    style = MaterialTheme.typography.bodyLarge,
    modifier = Modifier.padding(horizontal = Distance.default)
  )

val previewScope = object : DeveloperInfoScope {
  override fun setRotationEnabled(enabled: Boolean) {}
}

@PreviewScreenSizes
@PreviewFontScale
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.View(
      DeveloperInfoViewState(
        suplaTableDetails = listOf(
          TableDetail(ChannelStateEntity.TABLE_NAME, 15),
          TableDetail(ChannelStateEntity.TABLE_NAME, 15)
        ),
        measurementTableDetails = listOf(
          TableDetail(ChannelStateEntity.TABLE_NAME, 15),
          TableDetail(ChannelStateEntity.TABLE_NAME, 15)
        )
      )
    )
  }
}
