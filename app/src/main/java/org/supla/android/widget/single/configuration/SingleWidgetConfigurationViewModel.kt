package org.supla.android.widget.single.configuration
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

import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.Preferences
import org.supla.android.data.source.ChannelRepository
import org.supla.android.db.Channel
import org.supla.android.profile.ProfileManager
import org.supla.android.widget.WidgetPreferences
import org.supla.android.widget.shared.configuration.*
import javax.inject.Inject

@HiltViewModel
class SingleWidgetConfigurationViewModel @Inject constructor(
        preferences: Preferences,
        widgetPreferences: WidgetPreferences,
        profileManager: ProfileManager,
        channelRepository: ChannelRepository
) : WidgetConfigurationViewModelBase(
        preferences,
        widgetPreferences,
        profileManager,
        channelRepository
) {
    override fun filterChannels(channel: Channel): Boolean {
        return channel.isGateController()
                || channel.isDoorLock()
                || channel.isSwitch()
                || channel.isRollerShutter()
    }

    override fun updateActions() {
        if (selectedChannel?.isSwitch() == true) {
            postActions(listOf(
                    WidgetAction.TURN_ON,
                    WidgetAction.TURN_OFF))
        } else if (selectedChannel?.isRollerShutter() == true) {
            postActions(listOf(
                    WidgetAction.MOVE_UP,
                    WidgetAction.MOVE_DOWN))
        } else {
            postActions(listOf())
        }
    }
}
