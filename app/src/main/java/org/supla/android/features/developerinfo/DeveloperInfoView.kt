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

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.local.entity.ChannelStateEntity
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.forms.TextField
import org.supla.android.ui.views.forms.TextFieldLabel
import org.supla.android.ui.views.settings.SettingCheckboxItem
import org.supla.android.ui.views.settings.SettingsList
import org.supla.android.usecases.developerinfo.TableDetail

data class DeveloperInfoViewState(
  val developerOptions: Boolean = false,
  val rotationEnabled: Boolean = false,
  val debugLoggingEnabled: Boolean = false,
  val debugLoggingFilter: String = "",
  val debugLogSize: String? = null,
  val suplaTableDetails: List<TableDetail> = emptyList(),
  val measurementTableDetails: List<TableDetail> = emptyList(),
  val zwaveAvailable: Boolean = false
)

interface DeveloperInfoScope {
  fun setDeveloperOptionEnabled(enabled: Boolean)
  fun setRotationEnabled(enabled: Boolean)
  fun setDebugLoggingEnabled(enabled: Boolean)
  fun onFilterChanged(filter: String)
  fun downloadLogFile()
  fun deleteLogFile()
  fun refreshLogFileSize()
  fun sendTestNotification()
  fun sendChannelEvent()
  fun sendZwaveErrorEvent()
  fun exportSuplaDatabase()
  fun exportMeasurementsDatabase()
  fun showLoading()
}

@Composable
fun DeveloperInfoScope.View(
  viewState: DeveloperInfoViewState
) {
  val context = LocalContext.current

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
      SettingCheckboxItem(
        label = stringResource(R.string.developer_option),
        checked = viewState.developerOptions
      ) { setDeveloperOptionEnabled(it) }
      SettingCheckboxItem(
        label = stringResource(R.string.developer_info_screen_orientation),
        checked = viewState.rotationEnabled
      ) { setRotationEnabled(it) }
    }

    HeaderLarge(
      text = "Logging",
      modifier = Modifier.padding(top = Distance.small)
    )
    SettingsList {
      SettingCheckboxItem(
        label = "Debug logging",
        checked = viewState.debugLoggingEnabled,
        description = viewState.debugLogSize
      ) { setDebugLoggingEnabled(it) }
    }

    if (viewState.debugLoggingEnabled) {
      TextField(
        value = viewState.debugLoggingFilter,
        label = { TextFieldLabel("Filtering string") },
        onValueChange = { onFilterChanged(it) },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = Distance.default)
      )
      Row(
        horizontalArrangement = Arrangement.spacedBy(Distance.default),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = Distance.default)
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
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = Distance.default),
      onClick = { sendTestNotification() }
    )
    Button(
      text = "Show loader",
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = Distance.default),
      onClick = {
        showLoading()
        Toast.makeText(context, "Loading shown for 5 seconds", Toast.LENGTH_SHORT).show()
      }
    )
    Button(
      text = "Show channel event",
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = Distance.default),
      onClick = { sendChannelEvent() }
    )
    if (viewState.zwaveAvailable) {
      Button(
        text = "Show Z-Wave error event",
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = Distance.default),
        onClick = { sendZwaveErrorEvent() }
      )
    }

    HeaderLarge(
      text = stringResource(R.string.developer_info_database_section),
      modifier = Modifier.padding(top = Distance.small)
    )
    Button(
      text = "Export Supla database",
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = Distance.default),
      onClick = { exportSuplaDatabase() }
    )
    Button(
      text = "Export measurements database",
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = Distance.default),
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
    modifier = modifier
      .padding(horizontal = Distance.default)
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
  override fun onFilterChanged(filter: String) {}
  override fun downloadLogFile() {}
  override fun deleteLogFile() {}
  override fun refreshLogFileSize() {}
  override fun sendTestNotification() {}
  override fun sendChannelEvent() {}
  override fun sendZwaveErrorEvent() {}
  override fun exportSuplaDatabase() {}
  override fun exportMeasurementsDatabase() {}
  override fun showLoading() {}
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
