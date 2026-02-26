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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.nfc.call.CallActionFromData
import org.supla.android.features.nfc.call.CallActionFromUrl
import org.supla.android.features.nfc.call.screens.Navigator
import org.supla.android.features.nfc.call.screens.ScreenScaffold
import org.supla.android.lib.actions.ActionId
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.DotsLoadingIndicator
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.LogoWithSentence
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.android.ui.views.texts.BodyLarge
import org.supla.android.ui.views.texts.TitleLarge
import org.supla.core.shared.infrastructure.LocalizedString

interface CallActionScreenScope {
  fun close()
  fun addNewTag(uuid: String)
  fun configureTag(id: Long)
}

data class CallActionScreenState(
  val step: TagProcessingStep = TagProcessingStep.Processing,
  val tagData: TagData? = null
) {
  data class TagData(
    val name: String,
    val actionId: ActionId?,
    val subjectName: LocalizedString
  )
}

@Composable
fun CallActionScreen(
  key: CallActionFromUrl,
  navigator: Navigator,
  viewModel: CallActionViewModel = hiltViewModel()
) {
  LaunchedEffect(key.url) { viewModel.onLaunchWithUrl(key.url, key.readOnly) }
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
  LaunchedEffect(key.id) { viewModel.onLaunchWithId(key.id, key.readOnly) }
  ScreenScaffold(
    viewModel = viewModel,
    eventHandler = { handleEvent(it, navigator) },
    content = { viewModel.View(it) }
  )
}

@Composable
private fun CallActionScreenScope.View(screenState: CallActionScreenState) =
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(Distance.default),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(Distance.tiny)
  ) {
    LogoWithSentence()
    HeaderIcon(screenState.step)
    StepContent(screenState.step)
    screenState.tagData?.let { TagData(it) }
    Buttons(screenState.step, this@View)
  }

@Composable
private fun HeaderIcon(step: TagProcessingStep) {
  val resource =
    when (step) {
      is TagProcessingStep.Failure -> R.drawable.nfc_scanning_error
      TagProcessingStep.Processing -> R.drawable.nfc_scanning_in_progress
      TagProcessingStep.Success -> R.drawable.nfc_scanning_success
    }
  Box(
    modifier = Modifier
      .padding(vertical = Distance.default)
      .size(200.dp)
      .background(color = MaterialTheme.colorScheme.surface, shape = CircleShape)
  ) {
    Image(
      drawableId = resource,
      modifier = Modifier.align(Alignment.Center)
    )
  }
}

@Composable
private fun TagData(data: CallActionScreenState.TagData) {
  TitleLarge(
    text = stringResource(R.string.nfc_tag_name, data.name),
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    modifier = Modifier.padding(top = Distance.default)
  )
  data.actionId?.nameRes?.let { actionRes ->
    val actionName = stringResource(actionRes)
    val channelName = data.subjectName.invoke(LocalContext.current)
    BodyLarge(
      text = "$actionName - $channelName",
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
private fun StepContent(step: TagProcessingStep) {
  when (step) {
    TagProcessingStep.Processing -> ProcessingInfo()
    TagProcessingStep.Success -> TitleLarge(R.string.call_nfc_action_success)
    is TagProcessingStep.Failure -> FailureInfo(step.type)
  }
}

@Composable
private fun ProcessingInfo() {
  TitleLarge(R.string.general_processing)
  DotsLoadingIndicator(
    modifier = Modifier.padding(top = Distance.small)
  )
}

@Composable
private fun FailureInfo(errorType: TagProcessingStep.FailureType) {
  TitleLarge(
    stringRes = errorType.titleRes,
    textAlign = TextAlign.Center
  )
  BodyLarge(stringRes = errorType.messageRes)
}

@Composable
private fun ColumnScope.Buttons(step: TagProcessingStep, scope: CallActionScreenScope) {
  step.asFailure?.let { failure ->
    Spacer(modifier = Modifier.weight(1f))
    failure.type.primaryRes?.let {
      Button(
        text = stringResource(it),
        onClick = {
          when (val type = failure.type) {
            is TagProcessingStep.FailureType.ChannelNotFound -> scope.configureTag(type.id)
            is TagProcessingStep.FailureType.TagNotConfigured -> scope.configureTag(type.id)
            is TagProcessingStep.FailureType.TagNotFound -> scope.addNewTag(type.uuid)
            else -> Unit
          }
        },
        modifier = Modifier.fillMaxWidth()
      )
    }
    OutlinedButton(
      text = stringResource(failure.type.secondaryRes),
      onClick = { scope.close() },
      modifier = Modifier.fillMaxWidth()
    )
  }
}

private fun handleEvent(event: CallActionViewEvent, navigator: Navigator) {
  when (event) {
    CallActionViewEvent.Close -> navigator.finish()
    is CallActionViewEvent.EditMissingAction -> navigator.navigateToEditMissingAction(event.id)
    is CallActionViewEvent.SaveNewNfcTag -> navigator.navigateToSaveNewNfcTag(event.uuid, event.readOnly)
  }
}

private val previewScope = object : CallActionScreenScope {
  override fun close() {}
  override fun addNewTag(uuid: String) {}
  override fun configureTag(id: Long) {}
}

private val tagData =
  CallActionScreenState.TagData(
    name = "Living room door",
    actionId = ActionId.TOGGLE,
    subjectName = LocalizedString.Constant("Living room light")
  )

@SuplaPreview
@Composable
private fun Preview_Processing() {
  SuplaTheme {
    previewScope.View(
      screenState = CallActionScreenState(
        tagData = tagData
      )
    )
  }
}

@SuplaPreview
@Composable
private fun Preview_Success() {
  SuplaTheme {
    previewScope.View(
      screenState = CallActionScreenState(
        tagData = tagData,
        step = TagProcessingStep.Success
      )
    )
  }
}

@SuplaPreview
@Composable
private fun Preview_Failure() {
  SuplaTheme {
    previewScope.View(
      screenState = CallActionScreenState(
        tagData = tagData,
        step = TagProcessingStep.Failure(TagProcessingStep.FailureType.TagNotFound(""))
      )
    )
  }
}
