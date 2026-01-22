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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.addwizard.view.components.AddWizardContentText
import org.supla.android.features.addwizard.view.components.AddWizardEmptyScaffold
import org.supla.android.features.addwizard.view.components.AddWizardScaffold
import org.supla.android.features.addwizard.view.components.AddWizardTextFieldContainer
import org.supla.android.features.addwizard.view.components.addWizardButtonColors
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.android.ui.views.forms.TextField
import java.util.UUID

data class AddNfcTagViewState(
  val tagName: String = "",
  val error: Boolean = false,
  val loading: Boolean = false,
  val step: AddNfcStep = AddNfcStep.Preconditions
)

sealed interface AddNfcStep {
  data object Preconditions : AddNfcStep
  data object TagConfiguration : AddNfcStep
  data class Summary(val result: AddNfcSummary) : AddNfcStep
}

sealed interface AddNfcSummary {
  data object Failure : AddNfcSummary
  data object NotUsable : AddNfcSummary
  data object Timeout : AddNfcSummary
  data class Success(val tagId: Long, val tagUuid: String) : AddNfcSummary
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
}

@Composable
fun AddNfcTagScope.View(viewState: AddNfcTagViewState) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.primaryContainer)
  ) {
    when (viewState.step) {
      AddNfcStep.Preconditions -> Preconditions(viewState)
      AddNfcStep.TagConfiguration -> TagConfiguration()
      is AddNfcStep.Summary -> Summary(viewState.step.result)
    }
  }
}

@Composable
private fun AddNfcTagScope.Preconditions(viewState: AddNfcTagViewState) =
  AddWizardScaffold(
    buttonTextId = R.string.next,
    onNext = { onStepFinished(AddNfcStep.Preconditions) }
  ) {
    Image(
      drawableId = R.drawable.ic_menu_nfc,
      modifier = Modifier
        .size(140.dp)
        .rotate(45f)
    )

    AddWizardTextFieldContainer(R.string.add_nfc_name_label) {
      TextField(
        value = viewState.tagName,
        modifier = Modifier.fillMaxWidth(),
        onValueChange = { onTagNameChange(it) },
        isError = viewState.error,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
      )
    }
  }

@Composable
private fun TagConfiguration() =
  AddWizardEmptyScaffold(
    buttonTextId = R.string.next,
    processing = true,
    onNext = { },
  ) {
    Box(modifier = Modifier.weight(1f)) {
      Column(
        modifier = Modifier.align(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Distance.default)
      ) {
        NfcScanningAnimation(modifier = Modifier.size(220.dp))
        Text(
          text = stringResource(R.string.add_nfc_scanning_hint),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onPrimary
        )
      }
    }
  }

@Composable
private fun AddNfcTagScope.Summary(summary: AddNfcSummary) =
  AddWizardScaffold(
    iconRes = summary.iconRes,
    buttonTextId = R.string.exit,
    onNext = { onStepFinished(AddNfcStep.Summary(summary)) }
  ) {
    when (summary) {
      AddNfcSummary.Failure -> AddWizardContentText(stringResource(R.string.add_nfc_general_error))
      AddNfcSummary.NotUsable -> AddWizardContentText(stringResource(R.string.add_nfc_not_usable))
      AddNfcSummary.Timeout -> AddWizardContentText(stringResource(R.string.add_nfc_timeout))
      is AddNfcSummary.Success -> SuccessScreen(
        message = stringResource(R.string.add_nfc_success, summary.tagUuid),
        onConfigureTagAction = { onConfigureTagAction(summary.tagId) }
      )
      is AddNfcSummary.Duplicate -> SuccessScreen(
        message = stringResource(R.string.add_nfc_duplicate, summary.tagUuid),
        onConfigureTagAction = { onConfigureTagAction(summary.tagId) }
      )
    }
  }

@Composable
private fun ColumnScope.SuccessScreen(
  message: String,
  onConfigureTagAction: () -> Unit
) {
  Spacer(modifier = Modifier.weight(1f))

  AddWizardContentText(message)

  Spacer(modifier = Modifier.weight(1f))

  OutlinedButton(
    text = stringResource(R.string.add_nfc_configure_tag),
    colors = ButtonDefaults.addWizardButtonColors(),
    onClick = { onConfigureTagAction() },
    modifier = Modifier
  )

  Spacer(modifier = Modifier.weight(1f))
}

private val previewScope = object : AddNfcTagScope {
  override fun onTagNameChange(name: String) {}
  override fun onStepFinished(step: AddNfcStep) {}
  override fun onConfigureTagAction(tagId: Long) {}
}

@Preview
@Composable
private fun PreviewPrecondition() {
  SuplaTheme {
    previewScope.View(AddNfcTagViewState())
  }
}

@Preview
@Composable
private fun PreviewTagConfiguration() {
  SuplaTheme {
    previewScope.View(AddNfcTagViewState(step = AddNfcStep.TagConfiguration))
  }
}

@Preview
@Composable
private fun PreviewSummary() {
  SuplaTheme {
    previewScope.View(
      viewState = AddNfcTagViewState(
        step = AddNfcStep.Summary(
          result = AddNfcSummary.Success(0L, UUID.randomUUID().toString())
        )
      )
    )
  }
}
