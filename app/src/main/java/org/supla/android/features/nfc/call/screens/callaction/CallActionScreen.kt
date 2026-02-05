package org.supla.android.features.nfc.call.screens.callaction
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

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.delay
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.addWizardButton
import org.supla.android.features.addwizard.view.components.AddWizardActionButton
import org.supla.android.features.addwizard.view.components.AddWizardNavigationButton
import org.supla.android.features.addwizard.view.components.addWizardButtonColors
import org.supla.android.features.nfc.add.NfcScanningAnimation
import org.supla.android.features.nfc.call.CallActionFromData
import org.supla.android.features.nfc.call.CallActionFromUrl
import org.supla.android.features.nfc.call.screens.Navigator
import org.supla.android.features.nfc.call.screens.ScreenScaffold
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.buttons.OutlinedButton
import kotlin.time.Duration.Companion.milliseconds

private const val NUMBER_OF_DOTS = 3

interface CallActionScreenScope {
  fun close()
  fun addNewTag(uuid: String)
  fun configureTag(id: Long)
}

data class CallActionScreenState(
  val steps: List<TagProcessingStep> = listOf(TagProcessingStep.ResolvingTag)
) {
  val currentStep: TagProcessingStep
    get() = steps.lastOrNull() ?: TagProcessingStep.ResolvingTag

  fun push(step: TagProcessingStep): CallActionScreenState {
    steps.lastOrNull()?.succeeded = step !is TagProcessingStep.Failure
    return copy(steps = steps + step)
  }
}

sealed class TagProcessingStep {
  var succeeded: Boolean = false

  val isFailure: Boolean
    get() = this is Failure

  data object ResolvingTag : TagProcessingStep()
  data object ExecutingAction : TagProcessingStep()
  data object Success : TagProcessingStep()
  data class Failure(val type: FailureType) : TagProcessingStep()

  sealed interface FailureType {
    data object IllegalIntent : FailureType
    data object UnknownUrl : FailureType
    data class TagNotFound(val uuid: String) : FailureType
    data class TagNotConfigured(val id: Long) : FailureType
    data object ActionFailed : FailureType
    data object ChannelNotFound : FailureType
    data object ChannelOffline : FailureType
  }
}

@Composable
fun CallActionScreen(
  key: CallActionFromUrl,
  navigator: Navigator,
  viewModel: CallActionViewModel = hiltViewModel()
) {
  LaunchedEffect(key.url) { viewModel.onLaunchWithUrl(key.url) }
  ScreenScaffold(
    viewModel = viewModel,
    eventHandler = { handleEvent(it, navigator) },
    content = { viewModel.View(it) }
  )
}

@Composable
fun CallActionScreen(
  key: CallActionFromData,
  navigator: Navigator,
  viewModel: CallActionViewModel = hiltViewModel()
) {
  LaunchedEffect(key.id) { viewModel.onLaunchWithId(key.id) }
  ScreenScaffold(
    viewModel = viewModel,
    eventHandler = { handleEvent(it, navigator) },
    content = { viewModel.View(it) }
  )
}

@Composable
private fun CallActionScreenScope.View(screenState: CallActionScreenState) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.primaryContainer)
      .padding(Distance.default),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(Distance.tiny)
  ) {
    Text(
      text = stringResource(R.string.app_name),
      style = MaterialTheme.typography.displaySmall,
      color = MaterialTheme.colorScheme.onPrimaryContainer
    )
    Text(
      text = stringResource(R.string.app_sentence),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onPrimaryContainer
    )
    Spacer(modifier = Modifier.weight(0.5f))

    HeaderIcon(screenState.currentStep)
    Spacer(modifier = Modifier.height(Distance.default))
    Steps(screenState.steps, screenState.currentStep)

    Spacer(modifier = Modifier.weight(1f))

    if (screenState.currentStep.isFailure) {
      AddWizardNavigationButton(
        textRes = R.string.exit,
        modifier = Modifier.align(Alignment.End),
        onClick = { close() }
      )
    }
  }
}

@Composable
private fun HeaderIcon(step: TagProcessingStep) {
  when (step) {
    TagProcessingStep.Success ->
      Image(
        drawableId = R.drawable.add_wizard_success,
        modifier = Modifier.size(140.dp)
      )

    is TagProcessingStep.Failure ->
      Image(
        drawableId = R.drawable.add_wizard_error,
        modifier = Modifier.size(140.dp)
      )

    TagProcessingStep.ResolvingTag,
    TagProcessingStep.ExecutingAction ->
      NfcScanningAnimation(
        modifier = Modifier.size(140.dp)
      )
  }
}

@Composable
private fun CallActionScreenScope.Steps(steps: List<TagProcessingStep>, currentStep: TagProcessingStep) {
  for (step in steps) {
    when (step) {
      TagProcessingStep.ResolvingTag -> {
        InformationRow(
          message = R.string.call_nfc_action_resolving_tag,
          processing = currentStep == TagProcessingStep.ResolvingTag,
          successful = step.succeeded
        )
      }

      TagProcessingStep.ExecutingAction -> {
        InformationRow(
          message = R.string.call_nfc_action_calling,
          processing = currentStep == TagProcessingStep.ExecutingAction,
          successful = step.succeeded
        )
      }

      is TagProcessingStep.Failure -> FailureStep(step.type)
      TagProcessingStep.Success -> {
        Text(
          text = stringResource(R.string.call_nfc_action_success),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
      }
    }
  }
}

@Composable
private fun CallActionScreenScope.FailureStep(type: TagProcessingStep.FailureType) {
  val message = when (type) {
    TagProcessingStep.FailureType.IllegalIntent -> R.string.call_nfc_action_failure_illegal_intent
    TagProcessingStep.FailureType.UnknownUrl -> R.string.call_nfc_action_failure_unknown_url
    is TagProcessingStep.FailureType.TagNotFound -> R.string.call_nfc_action_failure_not_found
    is TagProcessingStep.FailureType.TagNotConfigured -> R.string.call_nfc_action_not_configured
    TagProcessingStep.FailureType.ActionFailed -> R.string.call_nfc_action_call_failed
    TagProcessingStep.FailureType.ChannelNotFound -> R.string.call_nfc_action_channel_not_found
    TagProcessingStep.FailureType.ChannelOffline -> R.string.call_nfc_action_channel_offline
  }

  Text(
    text = stringResource(message),
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onPrimaryContainer,
    textAlign = TextAlign.Center
  )

  Spacer(modifier = Modifier.height(Distance.default))
  when (type) {
    is TagProcessingStep.FailureType.TagNotFound ->
      AddWizardActionButton(textRes = R.string.nfc_list_add, icon = Icons.Default.Nfc) { addNewTag(type.uuid) }

    is TagProcessingStep.FailureType.TagNotConfigured ->
      AddWizardActionButton(textRes = R.string.add_nfc_configure_tag, icon = Icons.Default.Edit) { configureTag(type.id) }

    else -> {}
  }
}

@Composable
private fun InformationRow(@StringRes message: Int, processing: Boolean, successful: Boolean) {
  Row(
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = stringResource(message),
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onPrimaryContainer
    )

    Box(modifier = Modifier.width(30.dp)) {
      if (processing) {
        ProcessingText()
      } else {
        if (successful) {
          Text(
            text = stringResource(R.string.notifications_active),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.align(Alignment.Center)
          )
        } else {
          Text(
            text = stringResource(R.string.notifications_inactive),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.align(Alignment.Center)
          )
        }
      }
    }
  }
}

@Composable
private fun ProcessingText() {
  var count by remember { mutableIntStateOf(0) }
  var text by remember { mutableStateOf("") }
  LaunchedEffect(Any()) {
    while (true) {
      delay(200.milliseconds)
      if (count > NUMBER_OF_DOTS) {
        count = 0
      } else {
        count += 1
      }
      text = ""
      for (i in 1 until count) {
        text += "."
      }
    }
  }

  Text(
    text = text,
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onPrimaryContainer
  )
}

@Composable
private fun Button(
  @StringRes buttonTextId: Int,
  onClick: () -> Unit
) =
  OutlinedButton(
    colors = ButtonDefaults.addWizardButtonColors(),
    contentPadding = PaddingValues(start = Distance.default, top = Distance.tiny, end = Distance.default, bottom = Distance.tiny),
    onClick = onClick,
    modifier = Modifier
      .addWizardButton(),
  ) {
    Text(
      text = stringResource(buttonTextId),
      style = MaterialTheme.typography.labelLarge,
    )
  }

private fun handleEvent(event: CallActionViewEvent, navigator: Navigator) {
  when (event) {
    CallActionViewEvent.Close -> navigator.finish()
    is CallActionViewEvent.EditMissingAction -> navigator.navigateToEditMissingAction(event.id)
    is CallActionViewEvent.SaveNewNfcTag -> navigator.navigateToSaveNewNfcTag(event.uuid)
  }
}

private val previewScope = object : CallActionScreenScope {
  override fun close() {}
  override fun addNewTag(uuid: String) {}
  override fun configureTag(id: Long) {}
}

@SuplaPreview
@Composable
private fun Preview_Searching() {
  SuplaTheme {
    previewScope.View(CallActionScreenState())
  }
}

@SuplaPreview
@Composable
private fun Preview_SearchFailed() {
  val steps = listOf(
    TagProcessingStep.ResolvingTag,
    TagProcessingStep.Failure(TagProcessingStep.FailureType.TagNotFound(""))
  )
  SuplaTheme {
    previewScope.View(CallActionScreenState(steps = steps))
  }
}
