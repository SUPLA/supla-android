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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.settings.SettingsList
import org.supla.android.ui.views.settings.SettingsListItem
import org.supla.android.usecases.developerinfo.TableDetail

data class DeveloperInfoViewState(
  val developerOptions: Boolean = false,
  val rotationEnabled: Boolean = false,
  val debugLoggingEnabled: Boolean = false,
  val debugLogSize: String? = null,
  val suplaTableDetails: List<TableDetail> = emptyList(),
  val measurementTableDetails: List<TableDetail> = emptyList()
)

interface DeveloperInfoScope {
  fun setDeveloperOptionEnabled(enabled: Boolean)
  fun setRotationEnabled(enabled: Boolean)
  fun setDebugLoggingEnabled(enabled: Boolean)
  fun downloadLogFile()
  fun deleteLogFile()
  fun refreshLogFileSize()
  fun sendTestNotification()
  fun exportSuplaDatabase()
  fun exportMeasurementsDatabase()
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
        label = stringResource(R.string.developer_option),
        checked = viewState.developerOptions
      ) { setDeveloperOptionEnabled(it) }
      SettingsListItem(
        label = stringResource(R.string.developer_info_screen_orientation),
        checked = viewState.rotationEnabled
      ) { setRotationEnabled(it) }
    }

    HeaderLarge(
      text = "Logging",
      modifier = Modifier.padding(top = Distance.small)
    )
    SettingsList {
      SettingsListItem(
        label = "Debug logging",
        checked = viewState.debugLoggingEnabled,
        description = viewState.debugLogSize
      ) { setDebugLoggingEnabled(it) }
    }
    if (viewState.debugLoggingEnabled) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(Distance.default),
        modifier = Modifier.fillMaxWidth().padding(horizontal = Distance.default)
      ) {
        Button(
          text = "Export",
          modifier = Modifier.weight(1f),
          onClick = { downloadLogFile() }
        )
        Button(
          text = "Delete",
          modifier = Modifier.weight(1f),
          onClick = { deleteLogFile() }
        )
        IconButton(onClick = { refreshLogFileSize() }) {
          Icon(imageVector = Icons.Outlined.Refresh, contentDescription = "Refresh")
        }
      }
    }

    HeaderLarge(
      text = "Testing",
      modifier = Modifier.padding(top = Distance.small)
    )
    Button(
      text = "Test notification",
      modifier = Modifier.fillMaxWidth().padding(horizontal = Distance.default),
      onClick = { sendTestNotification() }
    )

    HeaderLarge(
      text = stringResource(R.string.developer_info_database_section),
      modifier = Modifier.padding(top = Distance.small)
    )
    Button(
      text = "Export Supla database",
      modifier = Modifier.fillMaxWidth().padding(horizontal = Distance.default),
      onClick = { exportSuplaDatabase() }
    )
    Button(
      text = "Export measurements database",
      modifier = Modifier.fillMaxWidth().padding(horizontal = Distance.default),
      onClick = { exportMeasurementsDatabase() }
    )
    HeaderSmall(
      text = "Supla",
      modifier = Modifier.padding(top = Distance.tiny)
    )
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
private fun HeaderLarge(text: String, modifier: Modifier = Modifier) =
  Text(
    text = text,
    style = MaterialTheme.typography.titleLarge,
    modifier = modifier.padding(horizontal = Distance.default)
      .padding(bottom = Distance.tiny)
  )

@Composable
private fun HeaderSmall(text: String, modifier: Modifier = Modifier) =
  Text(
    text = text,
    style = MaterialTheme.typography.bodyLarge,
    modifier = modifier.padding(horizontal = Distance.default)
  )

val previewScope = object : DeveloperInfoScope {
  override fun setDeveloperOptionEnabled(enabled: Boolean) {}
  override fun setRotationEnabled(enabled: Boolean) {}
  override fun setDebugLoggingEnabled(enabled: Boolean) {}
  override fun downloadLogFile() {}
  override fun deleteLogFile() {}
  override fun refreshLogFileSize() {}
  override fun sendTestNotification() {}
  override fun exportSuplaDatabase() {}
  override fun exportMeasurementsDatabase() {}
}

@PreviewScreenSizes
@PreviewFontScale
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.View(
      DeveloperInfoViewState(
        debugLoggingEnabled = true,
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
