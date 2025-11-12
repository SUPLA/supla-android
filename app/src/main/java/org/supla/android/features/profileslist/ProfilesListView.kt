package org.supla.android.features.profileslist
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

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.tools.BACKGROUND_COLOR
import org.supla.android.ui.extensions.ifTrue
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.buttons.FloatingAddButton
import org.supla.android.ui.views.buttons.IconButton
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

data class ProfilesListViewState(
  var profiles: List<ProfileEntity> = emptyList(),
)

interface ProfilesListScope {
  fun onEditClicked(id: Long)
  fun onProfileSelected(profile: ProfileEntity)
  fun onAddAccount()
  fun onMove(from: Int, to: Int)
  fun onMoveFinished()
}

@Composable
fun ProfilesListScope.View(state: ProfilesListViewState) {
  val view = LocalView.current
  Box {
    Column {
      TopRow()
      val lazyListState = rememberLazyListState()
      val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onMove(from.index, to.index)
        ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK)
      }
      LazyColumn(
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(1.dp),
        contentPadding = PaddingValues(bottom = 104.dp),
        modifier = Modifier
          .padding(top = Distance.small)
          .weight(1f)
      ) {
        items(
          items = state.profiles,
          key = { it.id!! }
        ) { profile ->
          ReorderableItem(reorderableLazyListState, key = profile.id!!) {
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
              ProfileRowPortrait(profile = profile, scope = this)
            } else {
              ProfileRowLandscape(profile = profile, scope = this)
            }
          }
        }
      }
    }

    FloatingAddButton(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(Distance.default),
      contentDescription = stringResource(R.string.android_auto_add),
      onClick = { onAddAccount() }
    )
  }
}

@Composable
private fun TopRow() =
  Row(
    horizontalArrangement = Arrangement.spacedBy(Distance.small),
    modifier = Modifier.padding(top = Distance.default)
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(4.dp),
      modifier = Modifier
        .padding(start = Distance.default)
        .weight(1f)
    ) {
      Text(
        text = stringResource(R.string.profiles_title).uppercase(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = stringResource(R.string.profile_act_info),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onBackground
      )
    }
  }

@Composable
private fun ProfilesListScope.ProfileRowPortrait(
  profile: ProfileEntity,
  scope: ReorderableCollectionItemScope
) =
  Row(
    horizontalArrangement = Arrangement.spacedBy(Distance.tiny),
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .background(color = MaterialTheme.colorScheme.surface)
      .clickable(onClick = { onProfileSelected(profile) })
  ) {
    ProfileIcon(profile.active == true)
    Text(
      text = profile.name,
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier.weight(1f)
    )
    profile.active?.ifTrue { ActiveLabel() }
    Row {
      EditButton(profile.id!!)
      Image(
        drawableId = R.drawable.move_holder,
        modifier = with(scope) {
          Modifier.draggableHandle(onDragStopped = { onMoveFinished() })
        }
      )
    }
  }

@Composable
private fun ProfilesListScope.ProfileRowLandscape(
  profile: ProfileEntity,
  scope: ReorderableCollectionItemScope
) =
  Row(
    horizontalArrangement = Arrangement.spacedBy(Distance.tiny),
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .background(color = MaterialTheme.colorScheme.surface)
      .clickable(onClick = { onProfileSelected(profile) })
  ) {
    ProfileIcon(profile.active == true)
    Text(
      text = profile.name,
      style = MaterialTheme.typography.bodyMedium,
    )
    if (profile.emailAuth) {
      profile.email?.let { ProfileDetail(it) }
    } else {
      profile.accessId?.let { ProfileDetail("ID: $it") }
    }
    profile.active?.ifTrue { ActiveLabel() }

    Row {
      EditButton(profile.id!!)
      Image(
        drawableId = R.drawable.move_holder,
        modifier = with(scope) {
          Modifier.draggableHandle(onDragStopped = { onMoveFinished() })
        }
      )
    }
  }

@Composable
private fun RowScope.ProfileDetail(text: String) =
  Text(
    text = "($text)",
    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier.weight(1f)
  )

@Composable
private fun ActiveLabel() =
  Text(
    text = stringResource(R.string.cfg_profile_active_indicator),
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant
  )

@Composable
private fun ProfileIcon(active: Boolean) =
  Image(
    drawableId = if (active) R.drawable.profile_selected else R.drawable.profile_unselected,
    modifier = Modifier.padding(start = Distance.default, top = Distance.tiny, bottom = Distance.tiny)
  )

@Composable
private fun ProfilesListScope.EditButton(profileId: Long) =
  IconButton(
    icon = R.drawable.pencil,
    onClick = { onEditClicked(profileId) }
  )

private val previewScope = object : ProfilesListScope {
  override fun onEditClicked(id: Long) {}
  override fun onProfileSelected(profile: ProfileEntity) {}
  override fun onAddAccount() {}
  override fun onMove(from: Int, to: Int) {}
  override fun onMoveFinished() {}
}

@Preview(backgroundColor = BACKGROUND_COLOR, showBackground = true)
@PreviewScreenSizes
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.View(
      state = ProfilesListViewState(
        profiles = listOf(
          mockProfile(1L, "Default", true),
          mockProfile(2L, "Custom", false)
        )
      )
    )
  }
}

private fun mockProfile(id: Long, name: String, active: Boolean): ProfileEntity =
  ProfileEntity(
    id = id,
    name = name,
    email = "test@email.pl",
    serverForAccessId = null,
    serverForEmail = null,
    serverAutoDetect = false,
    emailAuth = true,
    accessId = null,
    accessIdPassword = null,
    preferredProtocolVersion = null,
    active = active,
    advancedMode = false,
    position = 0,
    guid = null,
    authKey = null,
  )
