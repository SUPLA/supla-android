package org.supla.android.features.nfc.add
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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.addwizard.view.components.AddWizardActionButton
import org.supla.android.features.addwizard.view.components.AddWizardContentText
import org.supla.android.features.addwizard.view.components.AddWizardEmptyScaffold
import org.supla.android.features.addwizard.view.components.AddWizardScaffold
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.Switch
import org.supla.core.shared.extensions.ifTrue
import java.util.UUID

data class AddNfcTagViewState(
  val tagName: String = "",
  val error: Boolean = false,
  val lockTag: Boolean = false,
  val step: AddNfcStep = AddNfcStep.TagReading
)

sealed interface AddNfcStep {
  data object TagReading : AddNfcStep
  data class TagSummary(val result: AddNfcSummary) : AddNfcStep
}

sealed interface AddNfcSummary {
  data object Failure : AddNfcSummary
  data object NotUsable : AddNfcSummary
  data object NotEnoughSpace : AddNfcSummary
  data class Success(val tagUuid: String, val readOnly: Boolean) : AddNfcSummary
  data class Duplicate(val tagId: Long, val tagUuid: String, val name: String) : AddNfcSummary

  val iconRes: Int
    get() = when (this) {
      is Success, is Duplicate -> R.drawable.add_wizard_success
      else -> R.drawable.add_wizard_error
    }
}

interface AddNfcTagScope {
  fun onTagNameChange(name: String)
  fun onStepFinished(step: AddNfcStep)
  fun onConfigureTagAction(tagId: Long)
  fun onWriteLockChanged(active: Boolean)
  fun onPrepareAnother()
}

@Composable
fun AddNfcTagScope.View(viewState: AddNfcTagViewState) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.primaryContainer)
  ) {
    when (viewState.step) {
      AddNfcStep.TagReading -> TagConfiguration(viewState.lockTag)
      is AddNfcStep.TagSummary -> Summary(viewState.step.result)
    }
  }
}

@Composable
private fun AddNfcTagScope.TagConfiguration(writingLockActive: Boolean) =
  AddWizardEmptyScaffold(
    buttonTextId = R.string.next,
    processing = true,
    onNext = { },
  ) {
    Box(modifier = Modifier.weight(1f)) {
      Column(
        modifier = Modifier
          .align(Alignment.Center)
          .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
      ) {
        NfcScanningAnimation(modifier = Modifier.size(220.dp))
        Text(
          text = stringResource(R.string.add_nfc_scanning_hint),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onPrimary,
          textAlign = TextAlign.Center
        )
        Column(
          verticalArrangement = Arrangement.spacedBy(Distance.small),
          modifier = Modifier.padding(horizontal = Distance.tiny)
        ) {
          Message(
            iconRes = R.drawable.ic_info,
            messageRes = R.string.add_nfc_override_warning,
            iconTint = colorResource(id = R.color.zwave_info_label_bg)
          )

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = stringResource(R.string.add_nfc_read_only_label),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.weight(1f)
            )
            Switch(
              checked = writingLockActive,
              onCheckedChange = { onWriteLockChanged(it) }
            )
          }

          Message(
            iconRes = R.drawable.channel_warning_level1,
            messageRes = R.string.add_nfc_lock_warning,
            modifier = Modifier.alpha(writingLockActive.ifTrue { 1f } ?: 0f)
          )
        }
      }
    }
  }

@Composable
private fun Message(
  @DrawableRes iconRes: Int,
  @StringRes messageRes: Int,
  modifier: Modifier = Modifier,
  iconTint: Color? = null
) =
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(Distance.small),
    modifier = modifier
      .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default)))
      .border(
        width = 1.dp,
        color = colorResource(id = R.color.gray_lighter),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default))
      )
      .padding(Distance.small)
  ) {
    Image(
      drawableId = iconRes,
      tint = iconTint,
      modifier = Modifier.size(dimensionResource(R.dimen.icon_big_size))
    )
    Text(
      text = stringResource(messageRes),
      style = MaterialTheme.typography.bodySmall,
      modifier = Modifier.weight(1f)
    )
  }

@Composable
private fun AddNfcTagScope.Summary(summary: AddNfcSummary) =
  AddWizardScaffold(
    iconRes = summary.iconRes,
    buttonTextId = R.string.exit,
    onNext = { onStepFinished(AddNfcStep.TagSummary(summary)) }
  ) {
    when (summary) {
      AddNfcSummary.Failure -> Error(R.string.add_nfc_general_error) { onPrepareAnother() }
      AddNfcSummary.NotUsable -> Error(R.string.add_nfc_not_usable) { onPrepareAnother() }
      AddNfcSummary.NotEnoughSpace -> Error(R.string.add_nfc_not_enough_space) { onPrepareAnother() }
      is AddNfcSummary.Success,
      is AddNfcSummary.Duplicate -> {} // No view, because is handled by another fragment
    }
  }

@Composable
private fun ColumnScope.Error(@StringRes textRes: Int, onPrepareAnother: () -> Unit) {
  Spacer(modifier = Modifier.weight(1f))

  AddWizardContentText(stringResource(textRes))

  Spacer(modifier = Modifier.weight(1f))

  AddWizardActionButton(textRes = R.string.add_wizard_repeat) { onPrepareAnother() }

  Spacer(modifier = Modifier.weight(1f))
}

private val previewScope = object : AddNfcTagScope {
  override fun onTagNameChange(name: String) {}
  override fun onStepFinished(step: AddNfcStep) {}
  override fun onConfigureTagAction(tagId: Long) {}
  override fun onWriteLockChanged(active: Boolean) {}
  override fun onPrepareAnother() {}
}

@Preview
@Composable
private fun PreviewTagConfiguration() {
  SuplaTheme {
    previewScope.View(AddNfcTagViewState(lockTag = true))
  }
}

@Preview
@Composable
private fun PreviewSummary() {
  SuplaTheme {
    previewScope.View(
      viewState = AddNfcTagViewState(
        step = AddNfcStep.TagSummary(
          result = AddNfcSummary.Success(UUID.randomUUID().toString(), true)
        ),
        error = true
      )
    )
  }
}

@Preview
@Composable
private fun PreviewDuplicate() {
  SuplaTheme {
    previewScope.View(
      viewState = AddNfcTagViewState(
        step = AddNfcStep.TagSummary(
          result = AddNfcSummary.Duplicate(1L, UUID.randomUUID().toString(), "Test")
        ),
        error = true
      )
    )
  }
}

@Preview
@Composable
private fun PreviewErrorNotUsable() {
  SuplaTheme {
    previewScope.View(
      viewState = AddNfcTagViewState(
        step = AddNfcStep.TagSummary(
          result = AddNfcSummary.NotUsable
        ),
        error = true
      )
    )
  }
}

@Preview
@Composable
private fun PreviewAnotherError() {
  SuplaTheme {
    previewScope.View(
      viewState = AddNfcTagViewState(
        step = AddNfcStep.TagSummary(
          result = AddNfcSummary.Failure
        ),
        error = true
      )
    )
  }
}
