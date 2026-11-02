package org.supla.android.main
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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.awaitFirst
import org.supla.android.R
import org.supla.android.core.networking.suplaclient.SuplaClientMessageHandlerWrapper
import org.supla.android.core.shared.shareable
import org.supla.android.core.ui.EventBasedViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.SuplaEventType
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.images.ImageId
import org.supla.android.main.view.EventNotificationState
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.extensions.forTrue
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessage
import org.supla.core.shared.usecase.GetCaptionUseCase
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class MainViewModel @Inject constructor(
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val channelRepository: ChannelRepository,
  private val getCaptionUseCase: GetCaptionUseCase,
  suplaClientMessageHandlerWrapper: SuplaClientMessageHandlerWrapper
) : EventBasedViewModel<MainViewEvent>() {

  var eventNotificationState by mutableStateOf<EventNotificationState?>(null)
    private set
  private var notificationJob: Job? = null

  init {
    setupSuplaClientMessageHandler(suplaClientMessageHandlerWrapper)
  }

  fun hideNotification() {
    notificationJob?.cancel()
    eventNotificationState = null
  }

  override fun handleSuplaMessage(message: SuplaClientMessage) {
    val event = (message as? SuplaClientMessage.Event) ?: return

    if ((!event.owner || event.type == SuplaEventType.SET_BRIDGE_VALUE_FAILED) && event.channelId != 0) {
      showEventNotification(event)
    }
  }

  private fun showEventNotification(event: SuplaClientMessage.Event) {
    notificationJob?.cancel()
    notificationJob = viewModelScope.launch {
      runCatching {
        val channelData = channelRepository.findChannelDataEntity(event.channelId).awaitFirst()
        val actionMessage = event.actionMessage(channelData.channelEntity) ?: return@launch

        eventNotificationState =
          EventNotificationState(
            time = event.time,
            device = event.senderName,
            actionText = actionMessage,
            subjectIcon = event.subjectIcon(channelData),
            subjectName = event.subjectName(channelData)
          )

        delay(5.seconds)

        eventNotificationState = null
      }
    }
  }

  val SuplaClientMessage.Event.time: LocalDateTime?
    get() = when (type) {
      SuplaEventType.SET_BRIDGE_VALUE_FAILED -> null
      else -> LocalDateTime.now()
    }

  fun SuplaClientMessage.Event.actionMessage(channel: ChannelEntity): LocalizedString? =
    when (type) {
      SuplaEventType.UNKNOWN -> null
      SuplaEventType.CONTROLLING_THE_GATEWAY_LOCK -> localizedString(R.string.event_openedthegateway)
      SuplaEventType.CONTROLLING_THE_GATE -> localizedString(R.string.event_openedclosedthegate)
      SuplaEventType.CONTROLLING_THE_GARAGE_DOOR -> localizedString(R.string.event_openedclosedthegatedoors)
      SuplaEventType.CONTROLLING_THE_DOOR_LOCK -> localizedString(R.string.event_openedthedoor)
      SuplaEventType.CONTROLLING_THE_ROLLER_SHUTTER -> localizedString(R.string.event_openedcloserollershutter)
      SuplaEventType.CONTROLLING_THE_ROOF_WINDOW -> localizedString(R.string.event_openedclosedtheroofwindow)
      SuplaEventType.POWER_ON_OFF -> localizedString(R.string.event_poweronoff)
      SuplaEventType.LIGHT_ON_OFF -> localizedString(R.string.event_turnedthelightonoff)
      SuplaEventType.VALVE_OPEN_CLOSE -> localizedString(R.string.event_openedclosedthevalve)
      SuplaEventType.SET_BRIDGE_VALUE_FAILED ->
        (SuplaChannelFlag.ZWAVE_BRIDGE inside channel.flags).forTrue {
          localizedString(R.string.zwave_device_communication_error)
        }
    }

  fun SuplaClientMessage.Event.subjectIcon(channelData: ChannelDataEntity): ImageId =
    when (type) {
      SuplaEventType.SET_BRIDGE_VALUE_FAILED -> ImageId(R.drawable.zwave_device_error)
      else -> getChannelIconUseCase.invoke(channelData)
    }

  fun SuplaClientMessage.Event.subjectName(channelData: ChannelDataEntity): LocalizedString =
    when (type) {
      SuplaEventType.SET_BRIDGE_VALUE_FAILED -> localizedString(R.string.zwave_device_communication_error)
      else -> getCaptionUseCase.invoke(channelData.shareable)
    }
}

sealed interface MainViewEvent : ViewEvent
