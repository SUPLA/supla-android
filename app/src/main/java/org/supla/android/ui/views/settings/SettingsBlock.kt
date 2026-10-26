package org.supla.android.ui.views.settings
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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.settingsSurface
import org.supla.android.tools.SuplaPreview

interface SettingsColumnScope : ColumnScope {
  val index: Int
  val count: Int
}

@Composable
fun SettingsBlock(
  modifier: Modifier = Modifier,
  content: SettingsScope.() -> Unit
) {
  val items = mutableListOf<@Composable SettingsColumnScope.() -> Unit>()
  SettingsScope(items).content()

  Column(modifier.settingsSurface()) {
    val itemCount = items.size

    items.forEachIndexed { index, item ->
      item(
        SettingsColumnScopeImpl(
          columnScope = this,
          index = index,
          count = itemCount
        )
      )
    }
  }
}

@Composable
fun <T> SettingsBlock(
  items: List<T>,
  modifier: Modifier = Modifier,
  itemContent: @Composable SettingsColumnScope.(T) -> Unit
) {
  Column(modifier = modifier.settingsSurface()) {
    val itemCount = items.size

    items.forEachIndexed { index, item ->
      itemContent(
        SettingsColumnScopeImpl(
          columnScope = this,
          index = index,
          count = itemCount
        ),
        item
      )
    }
  }
}

@Composable
fun SettingBlock(
  modifier: Modifier = Modifier,
  itemContent: @Composable SettingsColumnScope.() -> Unit
) {
  Column(modifier = modifier.settingsSurface()) {
    itemContent(
      SettingsColumnScopeImpl(
        columnScope = this,
        index = 0,
        count = 1
      )
    )
  }
}

private class SettingsColumnScopeImpl(
  columnScope: ColumnScope,
  override val index: Int,
  override val count: Int
) : SettingsColumnScope, ColumnScope by columnScope

class SettingsScope internal constructor(
  private val items: MutableList<@Composable SettingsColumnScope.() -> Unit>
) {
  fun item(content: @Composable SettingsColumnScope.() -> Unit) {
    items += content
  }
}

@SuplaPreview
@PreviewScreenSizes
@PreviewFontScale
@Composable
private fun Preview() {
  SuplaTheme {
    SettingsBlock {
      item {
        SettingCheckboxItem(
          label = "Screen rotation enabled",
          checked = false,
          onCheckedChanged = {}
        )
      }
      item {
        SettingCheckboxItem(
          label = "Screen rotation enabled",
          checked = false,
          description = "App restart is needed",
          onCheckedChanged = {}
        )
      }
      item {
        SettingTextFieldItem(
          label = "Reset hour",
          value = "1",
          onValueChanged = {},
          unit = ":00",
        )
      }
    }
  }
}
