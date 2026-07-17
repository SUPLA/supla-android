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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.dialogs.DialogHeader
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.Separator
import org.supla.android.ui.views.SeparatorStyle
import org.supla.android.ui.views.texts.BodyMedium

data class ProfileSelectionDialogState(
  val profiles: List<ProfileVo>
)

data class ProfileVo(
  val id: Long,
  val name: String,
  val active: Boolean
)

@Composable
fun ProfileSelectionDialog(
  profiles: List<ProfileVo>,
  onDismiss: () -> Unit,
  onProfileSelected: (Long) -> Unit
) {
  Dialog(
    onDismiss = onDismiss
  ) {
    DialogHeader(title = stringResource(R.string.profile_select_active))

    for (profile in profiles) {
      Separator(style = SeparatorStyle.OUTLINE)
      Row(
        modifier = Modifier
          .clickable(onClick = { onProfileSelected(profile.id) })
          .padding(horizontal = Distance.default)
          .height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(Distance.small),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Image(
          drawableId = if (profile.active) R.drawable.profile_selected else R.drawable.profile_unselected,
        )
        BodyMedium(
          text = profile.name,
          color = if (profile.active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.weight(1f),
          textAlign = TextAlign.Start
        )
        if (profile.active) {
          Image(
            drawableId = R.drawable.check,
            contentDescription = stringResource(R.string.profile_selected)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(Distance.small))
  }
}

@SuplaPreview
@Composable
private fun Preview() {
  SuplaTheme {
    ProfileSelectionDialog(
      profiles = listOf(
        ProfileVo(
          id = 1L,
          name = "Default",
          active = true
        ),
        ProfileVo(
          id = 1L,
          name = "Other",
          active = false
        ),
        ProfileVo(
          id = 1L,
          name = "Unknown",
          active = false
        )
      ),
      onDismiss = {},
      onProfileSelected = {}
    )
  }
}
