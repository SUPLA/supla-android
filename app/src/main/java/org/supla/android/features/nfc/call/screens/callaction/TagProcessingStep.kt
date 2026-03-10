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
import org.supla.android.R
import org.supla.android.features.nfc.call.screens.callaction.TagProcessingStep.FailureType.ActionFailed
import org.supla.android.features.nfc.call.screens.callaction.TagProcessingStep.FailureType.ChannelNotFound
import org.supla.android.features.nfc.call.screens.callaction.TagProcessingStep.FailureType.ChannelOffline
import org.supla.android.features.nfc.call.screens.callaction.TagProcessingStep.FailureType.IllegalIntent
import org.supla.android.features.nfc.call.screens.callaction.TagProcessingStep.FailureType.SceneInactive
import org.supla.android.features.nfc.call.screens.callaction.TagProcessingStep.FailureType.TagNotConfigured
import org.supla.android.features.nfc.call.screens.callaction.TagProcessingStep.FailureType.TagNotFound
import org.supla.android.features.nfc.call.screens.callaction.TagProcessingStep.FailureType.UnknownUrl

sealed class TagProcessingStep {
  val asFailure: Failure?
    get() = this as? Failure

  data object Processing : TagProcessingStep()
  data object Success : TagProcessingStep()
  data class Failure(val type: FailureType) : TagProcessingStep()

  sealed interface FailureType {
    data object IllegalIntent : FailureType
    data object UnknownUrl : FailureType
    data class TagNotFound(val uuid: String) : FailureType
    data class TagNotConfigured(val id: Long) : FailureType
    data object ActionFailed : FailureType
    data class ChannelNotFound(val id: Long) : FailureType
    data object ChannelOffline : FailureType
    data object SceneInactive : FailureType
  }
}

val TagProcessingStep.FailureType.titleRes: Int
  @StringRes
  get() = when (this) {
    IllegalIntent -> R.string.call_nfc_action_failure_illegal_intent_title
    UnknownUrl -> R.string.call_nfc_action_failure_unknown_url_title
    is TagNotFound -> R.string.call_nfc_action_failure_not_found_title
    is TagNotConfigured -> R.string.call_nfc_action_not_configured_title
    ActionFailed, SceneInactive -> R.string.call_nfc_action_call_failed_title
    is ChannelNotFound -> R.string.call_nfc_action_channel_not_found_title
    ChannelOffline -> R.string.call_nfc_action_channel_offline_title
  }

val TagProcessingStep.FailureType.messageRes: Int
  @StringRes
  get() = when (this) {
    IllegalIntent -> R.string.call_nfc_action_failure_illegal_intent_message
    UnknownUrl -> R.string.call_nfc_action_failure_unknown_url_message
    is TagNotFound -> R.string.call_nfc_action_failure_not_found_message
    is TagNotConfigured -> R.string.call_nfc_action_not_configured_message
    ActionFailed -> R.string.call_nfc_action_call_failed_message
    is ChannelNotFound -> R.string.call_nfc_action_channel_not_found_message
    ChannelOffline -> R.string.call_nfc_action_channel_offline_message
    SceneInactive -> R.string.scene_inactive
  }

val TagProcessingStep.FailureType.primaryRes: Int?
  @StringRes
  get() = when (this) {
    IllegalIntent,
    UnknownUrl,
    ActionFailed,
    ChannelOffline,
    SceneInactive -> null

    is TagNotFound -> R.string.nfc_add_tag
    is TagNotConfigured -> R.string.nfc_assign_action
    is ChannelNotFound -> R.string.nfc_update_action
  }

val TagProcessingStep.FailureType.secondaryRes: Int
  @StringRes
  get() = when (this) {
    IllegalIntent,
    UnknownUrl,
    is TagNotConfigured,
    ActionFailed,
    is ChannelNotFound,
    ChannelOffline,
    SceneInactive -> R.string.exit

    is TagNotFound -> R.string.cancel
  }
