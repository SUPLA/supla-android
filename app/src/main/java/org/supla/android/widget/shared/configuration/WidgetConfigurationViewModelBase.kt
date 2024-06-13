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

import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.supla.android.Preferences
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.SceneRepository
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.Scene
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeBaseConfig
import org.supla.android.db.AuthProfileItem
import org.supla.android.db.Channel
import org.supla.android.db.ChannelBase
import org.supla.android.db.ChannelGroup
import org.supla.android.db.DbItem
import org.supla.android.db.Location
import org.supla.android.di.CoroutineDispatchers
import org.supla.android.extensions.isGpm
import org.supla.android.extensions.isThermometer
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
import org.supla.android.lib.singlecall.DoubleValue
import org.supla.android.lib.singlecall.SingleCall
import org.supla.android.lib.singlecall.TemperatureAndHumidity
import org.supla.android.profile.ProfileManager
import org.supla.android.usecases.channel.valueformatter.GpmValueFormatter
import org.supla.android.usecases.channel.valueprovider.GpmValueProvider
import org.supla.android.usecases.channelconfig.LoadChannelConfigUseCase
import org.supla.android.widget.WidgetConfiguration
import org.supla.android.widget.WidgetPreferences
import org.supla.android.widget.shared.loadTemperatureAndHumidity
import java.security.InvalidParameterException

abstract class WidgetConfigurationViewModelBase(
  private val preferences: Preferences,
  private val widgetPreferences: WidgetPreferences,
  private val profileManager: ProfileManager,
  private val channelRepository: ChannelRepository,
  private val sceneRepository: SceneRepository,
  private val dispatchers: CoroutineDispatchers,
  private val singleCallProvider: SingleCall.Provider,
  private val valuesFormatter: ValuesFormatter,
  private val loadChannelConfigUseCase: LoadChannelConfigUseCase
) : ViewModel() {
  private val _userLoggedIn = MutableLiveData<Boolean>()
  val userLoggedIn: LiveData<Boolean> = _userLoggedIn

  private val _dataLoading = MutableLiveData<Boolean>()
  val dataLoading: LiveData<Boolean> = _dataLoading

  private val _confirmationResult = MutableLiveData<Result<DbItem>>()
  val confirmationResult: LiveData<Result<DbItem>> = _confirmationResult

  private val _profilesList = MutableLiveData<List<AuthProfileItem>>()
  val profilesList: LiveData<List<AuthProfileItem>> = _profilesList

  private val _itemsList = MutableLiveData<List<SpinnerItem<DbItem>>>()
  val itemsList: LiveData<List<SpinnerItem<DbItem>>> = _itemsList

  private val _itemsType = MutableLiveData(ItemType.CHANNEL)
  val itemsType: LiveData<ItemType> = _itemsType

  private var selectedProfile: AuthProfileItem? = null
  var selectedItem: DbItem? = null
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
        viewModelScope.launch {
          withContext(dispatchers.io()) {
            _dataLoading.postValue(true)
            storeWidgetConfiguration()
            _dataLoading.postValue(false)
          }
        }
      }
    }
  }

  @Suppress("UNUSED_PARAMETER")
  fun onDisplayNameChanged(s: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
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

  open fun changeItem(channel: DbItem?) {
    selectedItem = channel
  }

  protected abstract fun filterItems(channelBase: DbItem): Boolean
  protected abstract fun valueWithUnit(): Boolean

  private fun reloadItems() {
    _dataLoading.value = true
    changeItem(null)
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
        val configSet = preferences.isAnyAccountRegistered
        if (configSet) {
          _profilesList.postValue(profileManager.getAllProfiles().blockingFirst())
          selectedProfile = profileManager.getCurrentProfile().blockingGet()

          loadItems()
        }

        _dataLoading.postValue(false)
        _userLoggedIn.postValue(configSet)
      }
    }
  }

  private fun loadItems() {
    val items: List<SpinnerItem<DbItem>> = when (itemsType.value) {
      ItemType.CHANNEL -> getChannels()
      ItemType.GROUP -> getAllChannelGroups()
      ItemType.SCENE -> getAllScenes()
      else -> emptyList()
    }

    _itemsList.postValue(items)
    if (items.isNotEmpty()) {
      selectedItem = items[0].value
    }
  }

  private fun getChannels(): List<SpinnerItem<DbItem>> {
    channelRepository.getAllProfileChannels(selectedProfile?.id).use { cursor ->
      val channels = mutableListOf<SpinnerItem<DbItem>>()
      if (!cursor.moveToFirst()) {
        return channels
      }

      var lastLocationId = -1L
      do {
        val channel = Channel()
        channel.AssignCursorData(cursor)

        if (filterItems(channel)) {
          getLocationFromChannelCursor(cursor).let {
            if (it.value.id != lastLocationId) {
              lastLocationId = it.value.id
              channels.add(it)
            }
          }

          channels.add(SpinnerItem(channel))
        }
      } while (cursor.moveToNext())

      // As the widgets are stateless it is possible that user creates many widgets for the same channel id
      return channels
    }
  }

  private fun getAllChannelGroups(): List<SpinnerItem<DbItem>> {
    channelRepository.getAllProfileChannelGroups(selectedProfile?.id).use { cursor ->
      val channelGroups = mutableListOf<SpinnerItem<DbItem>>()
      if (!cursor.moveToFirst()) {
        return channelGroups
      }

      var lastLocationId = -1L
      do {
        val channelGroup = ChannelGroup()
        channelGroup.AssignCursorData(cursor)
        if (filterItems(channelGroup)) {
          getLocationFromChannelCursor(cursor).let {
            if (it.value.id != lastLocationId) {
              lastLocationId = it.value.id
              channelGroups.add(it)
            }
          }

          channelGroups.add(SpinnerItem(channelGroup))
        }
      } while (cursor.moveToNext())

      // As the widgets are stateless it is possible that user creates many widgets for the same channel id
      return channelGroups
    }
  }

  private fun getLocationFromChannelCursor(cursor: Cursor): SpinnerItem<DbItem> {
    val captionIndex = cursor.getColumnIndex("section")
    val idIndex = cursor.getColumnIndex(ChannelEntity.COLUMN_LOCATION_ID)

    val location = Location()
    location.id = cursor.getInt(idIndex).toLong()
    location.caption = cursor.getString(captionIndex)
    return SpinnerItem(location)
  }

  private fun getAllScenes(): List<SpinnerItem<DbItem>> {
    val profileId = selectedProfile?.id
    return if (profileId != null) {
      val listWithLocations: MutableList<SpinnerItem<DbItem>> = mutableListOf()

      var lastLocationId = -1
      sceneRepository.getAllScenesForProfile(profileId).forEach {
        val location = it.second
        if (lastLocationId != location.locationId) {
          lastLocationId = location.locationId
          listWithLocations.add(SpinnerItem(location))
        }

        listWithLocations.add(SpinnerItem(it.first))
      }

      listWithLocations
    } else {
      emptyList()
    }
  }

  private fun storeWidgetConfiguration() {
    val itemType = itemsType.value ?: ItemType.CHANNEL
    val itemId = when (itemType) {
      ItemType.CHANNEL -> (selectedItem as Channel).channelId
      ItemType.GROUP -> (selectedItem as ChannelGroup).groupId
      ItemType.SCENE -> (selectedItem as Scene).sceneId
    }
    val value = when {
      itemType.isChannel() && (selectedItem as Channel).isThermometer() ->
        getThermometerWidgetValue(selectedItem as Channel)

      itemType.isChannel() && (selectedItem as Channel).isGpm() ->
        getGpmWidgetValue(selectedItem as Channel)

      itemType.isChannel() -> (selectedItem as Channel).color.toString()
      else -> "0"
    }

    setWidgetConfiguration(
      itemId,
      itemType,
      value
    )
    _confirmationResult.postValue(Result.success(selectedItem!!))
  }

  private fun getThermometerWidgetValue(channel: Channel): String {
    val formatter: (temperatureAndHumidity: TemperatureAndHumidity?) -> String =
      if (channel.func == SUPLA_CHANNELFNC_THERMOMETER) {
        { valuesFormatter.getTemperatureString(it?.temperature, valueWithUnit()) }
      } else {
        { valuesFormatter.getTemperatureAndHumidityString(it, valueWithUnit()) }
      }
    return loadTemperatureAndHumidity(
      { (loadChannelValue(channel.channelId) as TemperatureAndHumidity) },
      formatter
    )
  }

  private fun getGpmWidgetValue(channel: Channel): String {
    val channelConfig = try {
      loadChannelConfigUseCase(channel.profileId, channel.remoteId).blockingGet()
    } catch (ex: Exception) {
      null
    }
    val doubleValue = try {
      (loadChannelValue(channel.remoteId) as DoubleValue).value
    } catch (ex: Exception) {
      null
    } ?: GpmValueProvider.UNKNOWN_VALUE

    val formatter = GpmValueFormatter(channelConfig as? SuplaChannelGeneralPurposeBaseConfig)
    return formatter.format(doubleValue, valueWithUnit())
  }

  private fun setWidgetConfiguration(
    itemId: Int,
    itemType: ItemType,
    value: String
  ) {
    val channelFunction = if (itemType == ItemType.SCENE) 0 else (selectedItem as ChannelBase).func
    val altIcon = when (itemType) {
      ItemType.SCENE -> (selectedItem as Scene).altIcon
      else -> (selectedItem as ChannelBase).altIcon
    }
    val userIcon = when (itemType) {
      ItemType.SCENE -> (selectedItem as Scene).userIcon
      else -> (selectedItem as ChannelBase).userIconId
    }

    val configuration = WidgetConfiguration(
      itemId,
      itemType,
      displayName!!,
      channelFunction,
      value,
      selectedProfile!!.id,
      visibility = true,
      selectedAction?.actionId,
      altIcon,
      userIcon
    )
    widgetPreferences.setWidgetConfiguration(widgetId!!, configuration)
  }

  private fun loadChannelValue(itemId: Int): org.supla.android.lib.singlecall.ChannelValue =
    singleCallProvider.provide(selectedProfile!!.id).getChannelValue(itemId)
}

enum class ItemType(val id: Int) {
  CHANNEL(0), GROUP(1), SCENE(2);

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
