package org.supla.android.widget.shared.configuration
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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.supla.android.Preferences
import org.supla.android.data.source.ChannelRepository
import org.supla.android.db.AuthProfileItem
import org.supla.android.db.Channel
import org.supla.android.db.ChannelBase
import org.supla.android.db.ChannelGroup
import org.supla.android.di.CoroutineDispatchers
import org.supla.android.lib.SuplaConst.*
import org.supla.android.profile.ProfileManager
import org.supla.android.widget.WidgetConfiguration
import org.supla.android.widget.WidgetPreferences
import java.security.InvalidParameterException

abstract class WidgetConfigurationViewModelBase(
        private val preferences: Preferences,
        private val widgetPreferences: WidgetPreferences,
        private val profileManager: ProfileManager,
        private val channelRepository: ChannelRepository,
        private val dispatchers: CoroutineDispatchers
) : ViewModel() {
    private val _userLoggedIn = MutableLiveData<Boolean>()
    val userLoggedIn: LiveData<Boolean> = _userLoggedIn

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _confirmationResult = MutableLiveData<Result<ChannelBase>>()
    val confirmationResult: LiveData<Result<ChannelBase>> = _confirmationResult

    private val _profilesList = MutableLiveData<List<AuthProfileItem>>()
    val profilesList: LiveData<List<AuthProfileItem>> = _profilesList

    private val _itemsList = MutableLiveData<List<ChannelBase>>()
    val itemsList: LiveData<List<ChannelBase>> = _itemsList

    private val _itemsType = MutableLiveData(ItemType.CHANNEL)
    val itemsType: LiveData<ItemType> = _itemsType

    var selectedProfile: AuthProfileItem? = null
    var selectedItem: ChannelBase? = null
    var selectedAction: WidgetAction? = null
    var widgetId: Int? = null
    var displayName: String? = null

    init {
        _dataLoading.value = true
        triggerDataLoad()
    }

    fun confirmSelection() {
        when {
            widgetId == null -> {
                _confirmationResult.value = Result.failure(InvalidParameterException())
            }
            selectedItem == null -> {
                _confirmationResult.value = Result.failure(NoItemSelectedException())
            }
            displayName == null || displayName?.isBlank() == true -> {
                _confirmationResult.value = Result.failure(EmptyDisplayNameException())
            }
            else -> {
                val itemType = itemsType.value ?: ItemType.CHANNEL
                val itemId = if (itemType.isChannel()) (selectedItem as Channel).channelId else (selectedItem as ChannelGroup).groupId
                val color = if (itemType.isChannel()) (selectedItem as Channel).color else 0

                setWidgetConfiguration(widgetId!!, itemId, itemType, displayName!!, selectedItem!!.func, color)
                _confirmationResult.value = Result.success(selectedItem!!)
            }
        }
    }

    fun onDisplayNameChanged(s: CharSequence, `_`: Int, `__`: Int, `___`: Int) {
        displayName = s.toString()
    }

    fun changeProfile(profile: AuthProfileItem?) {
        if (profile == null) {
            return // nothing to do
        }
        selectedProfile = profile

        reloadItems()
    }

    fun changeType(type: ItemType) {
        _itemsType.value = type
        reloadItems()
    }

    open fun changeChannel(channel: ChannelBase?) {
        selectedItem = channel
    }

    protected abstract fun filterItems(channelBase: ChannelBase): Boolean

    private fun reloadItems() {
        _dataLoading.value = true
        changeChannel(null)
        displayName = null

        viewModelScope.launch {
            withContext(dispatchers.io()) {
                loadItems()
                _dataLoading.postValue(false)
            }
        }
    }

    private fun triggerDataLoad() {
        viewModelScope.launch {
            withContext(dispatchers.io()) {
                val configSet = preferences.configIsSet()
                if (configSet) {
                    _profilesList.postValue(profileManager.getAllProfiles())
                    selectedProfile = profileManager.getCurrentProfile()

                    loadItems()
                }

                _dataLoading.postValue(false)
                _userLoggedIn.postValue(configSet)
            }
        }
    }

    private fun loadItems() {
        val items: List<ChannelBase> = if (itemsType.value == ItemType.CHANNEL) {
            getAllChannels().filter { filterItems(it) }
        } else {
            getAllChannelGroups().filter { filterItems(it) }
        }
        _itemsList.postValue(items)
        if (items.isNotEmpty()) {
            selectedItem = items[0]
        }
    }

    private fun getAllChannels(): List<Channel> {
        channelRepository.getAllProfileChannels(selectedProfile?.id).use { cursor ->
            val channels = mutableListOf<Channel>()
            if (!cursor.moveToFirst()) {
                return channels
            }

            do {
                val channel = Channel()
                channel.AssignCursorData(cursor)
                channels.add(channel)
            } while (cursor.moveToNext())

            // As the widgets are stateless it is possible that user creates many widgets for the same channel id
            return channels
        }
    }

    private fun getAllChannelGroups(): List<ChannelGroup> {
        channelRepository.getAllProfileChannelGroups(selectedProfile?.id).use { cursor ->
            val channelGroups = mutableListOf<ChannelGroup>()
            if (!cursor.moveToFirst()) {
                return channelGroups
            }

            do {
                val channelGroup = ChannelGroup()
                channelGroup.AssignCursorData(cursor)
                channelGroups.add(channelGroup)
            } while (cursor.moveToNext())

            // As the widgets are stateless it is possible that user creates many widgets for the same channel id
            return channelGroups
        }
    }

    private fun setWidgetConfiguration(widgetId: Int, itemId: Int,
                                       itemType: ItemType, itemName: String,
                                       channelFunction: Int, channelColor: Int) {
        val configuration = WidgetConfiguration(
                itemId,
                itemType,
                itemName,
                channelFunction,
                channelColor,
                selectedProfile!!.id,
                visibility = true,
                selectedAction?.actionId)
        widgetPreferences.setWidgetConfiguration(widgetId, configuration)
    }
}

enum class ItemType(val id: Int) {
    CHANNEL(0), GROUP(1);

    fun getIntValue() = id

    fun isChannel() = this == CHANNEL

    fun isGroup() = this == GROUP

    companion object {
        fun fromInt(value: Int): ItemType? =
                if (value < 0 || value >= values().size) {
                    null
                } else {
                    values()[value]
                }
    }
}

class NoItemSelectedException : RuntimeException()

class EmptyDisplayNameException : RuntimeException()

internal fun ChannelBase.isSwitch() =
        func == SUPLA_CHANNELFNC_LIGHTSWITCH
                || func == SUPLA_CHANNELFNC_DIMMER
                || func == SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING
                || func == SUPLA_CHANNELFNC_RGBLIGHTING
                || func == SUPLA_CHANNELFNC_POWERSWITCH

internal fun ChannelBase.isRollerShutter() =
        func == SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
                || func == SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW

internal fun ChannelBase.isGateController() =
        func == SUPLA_CHANNELFNC_CONTROLLINGTHEGATE
                || func == SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR

internal fun ChannelBase.isDoorLock() =
        func == SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK
                || func == SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK