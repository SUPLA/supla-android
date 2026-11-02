package org.supla.android.features.notificationinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.android.ui.views.texts.BodyMedium
import org.supla.android.ui.views.texts.HeadlineMedium

@Composable
fun NotificationInfoDialog(
  onTurnOn: () -> Unit = {},
  onSkip: () -> Unit = {}
) {
  Dialog(
    onDismiss = {}
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(Distance.small),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(Distance.default)
    ) {
      Image(
        drawableId = R.drawable.notification_info,
        modifier = Modifier.size(80.dp)
      )
      HeadlineMedium(stringRes = R.string.notification_info_title)
      BodyMedium(stringRes = R.string.notification_info_message)

      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        OutlinedButton(
          text = stringResource(R.string.skip),
          onClick = onSkip
        )

        Button(
          text = stringResource(R.string.turn_on),
          onClick = onTurnOn
        )
      }
    }
  }
}

@Composable
@SuplaPreview
private fun Preview() {
  SuplaTheme {
    NotificationInfoDialog()
  }
}
