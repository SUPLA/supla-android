package org.supla.android.features.appsettings

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.NotificationManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.permissions.PermissionsHelper
import org.supla.android.data.source.runtime.appsettings.ChannelHeight
import org.supla.android.data.source.runtime.appsettings.TemperatureUnit
import org.supla.android.tools.SuplaSchedulers

@RunWith(MockitoJUnitRunner::class)
class SettingsViewModelTest : BaseViewModelTest<SettingsViewState, SettingsViewEvent, SettingsViewModel>() {

  @Mock
  private lateinit var preferences: Preferences

  @Mock
  private lateinit var notificationManager: NotificationManager

  @Mock
  private lateinit var permissionsHelper: PermissionsHelper

  @Mock
  override lateinit var schedulers: SuplaSchedulers

  override val viewModel: SettingsViewModel by lazy {
    SettingsViewModel(
      preferences,
      notificationManager,
      permissionsHelper,
      schedulers
    )
  }

  @Before
  override fun setUp() {
    super.setUp()
  }

  @Test
  fun `check if setting options are loaded in proper order with proper values`() {
    // given
    mockPreferences()
    whenever(permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION)).thenReturn(true)

    // when
    viewModel.loadSettings()

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).isEmpty()

    val settingsList = states[0].settingsItems
    assertThat(settingsList).extracting({ it::class.java }).containsExactly(
      tuple(SettingItem.HeaderItem::class.java),
      tuple(SettingItem.ChannelHeightItem::class.java),
      tuple(SettingItem.TemperatureUnitItem::class.java),
      tuple(SettingItem.ButtonAutoHide::class.java),
      tuple(SettingItem.InfoButton::class.java),
      tuple(SettingItem.BottomLabels::class.java),
      tuple(SettingItem.RollerShutterOpenClose::class.java),
      tuple(SettingItem.LocalizationOrdering::class.java),
      tuple(SettingItem.HeaderItem::class.java),
      tuple(SettingItem.NotificationsItem::class.java),
      tuple(SettingItem.LocalizationItem::class.java)
    )

    assertThat((settingsList[0] as SettingItem.HeaderItem).headerResource).isEqualTo(R.string.menubar_appsettings)
    assertThat((settingsList[1] as SettingItem.ChannelHeightItem).height).isEqualTo(ChannelHeight.HEIGHT_100)
    assertThat((settingsList[2] as SettingItem.TemperatureUnitItem).unit).isEqualTo(TemperatureUnit.FAHRENHEIT)
    assertThat((settingsList[3] as SettingItem.ButtonAutoHide).active).isEqualTo(true)
    assertThat((settingsList[4] as SettingItem.InfoButton).visible).isEqualTo(false)
    assertThat((settingsList[5] as SettingItem.BottomLabels).visible).isEqualTo(false)
    assertThat((settingsList[6] as SettingItem.RollerShutterOpenClose).showOpeningPercentage).isEqualTo(true)
    assertThat((settingsList[8] as SettingItem.HeaderItem).headerResource).isEqualTo(R.string.settings_permissions)
    assertThat((settingsList[9] as SettingItem.NotificationsItem).allowed).isEqualTo(true)
    assertThat((settingsList[10] as SettingItem.LocalizationItem).allowed).isEqualTo(true)
  }

  @Test
  fun `check if channel height is saved`() {
    // given
    mockPreferences()
    whenever(permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION)).thenReturn(true)

    // when
    viewModel.loadSettings()
    val channelSettingItem = states[0].settingsItems[1] as SettingItem.ChannelHeightItem
    channelSettingItem.callback(2)

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).isEmpty()
    verifyPreferencesMockedCalls()
    verify(preferences).channelHeight = 150
    verifyNoMoreInteractions(preferences)
  }

  @Test
  fun `check if temperature unit is saved`() {
    // given
    mockPreferences()
    whenever(permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION)).thenReturn(true)

    // when
    viewModel.loadSettings()
    val channelSettingItem = states[0].settingsItems[3] as SettingItem.ButtonAutoHide
    channelSettingItem.callback(false)

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).isEmpty()
    verifyPreferencesMockedCalls()
    verify(preferences).isButtonAutohide = false
    verifyNoMoreInteractions(preferences)
  }

  @Test
  fun `check if button auto hide is saved`() {
    // given
    mockPreferences()
    whenever(permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION)).thenReturn(true)

    // when
    viewModel.loadSettings()
    val channelSettingItem = states[0].settingsItems[2] as SettingItem.TemperatureUnitItem
    channelSettingItem.callback(0)

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).isEmpty()
    verifyPreferencesMockedCalls()
    verify(preferences).temperatureUnit = TemperatureUnit.CELSIUS
    verifyNoMoreInteractions(preferences)
  }

  @Test
  fun `check if info button is saved`() {
    // given
    mockPreferences()
    whenever(permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION)).thenReturn(true)

    // when
    viewModel.loadSettings()
    val channelSettingItem = states[0].settingsItems[4] as SettingItem.InfoButton
    channelSettingItem.callback(true)

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).isEmpty()
    verifyPreferencesMockedCalls()
    verify(preferences).isShowChannelInfo = true
    verifyNoMoreInteractions(preferences)
  }

  @Test
  fun `check if show bottom labels is saved`() {
    // given
    mockPreferences()
    whenever(permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION)).thenReturn(true)

    // when
    viewModel.loadSettings()
    val channelSettingItem = states[0].settingsItems[5] as SettingItem.BottomLabels
    channelSettingItem.callback(true)

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).isEmpty()
    verifyPreferencesMockedCalls()
    verify(preferences).isShowBottomLabel = true
    verifyNoMoreInteractions(preferences)
  }

  @Test
  fun `check if rs showing opening percentage is saved`() {
    // given
    mockPreferences()
    whenever(permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION)).thenReturn(true)

    // when
    viewModel.loadSettings()
    val channelSettingItem = states[0].settingsItems[6] as SettingItem.RollerShutterOpenClose
    channelSettingItem.callback(false)

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).isEmpty()
    verifyPreferencesMockedCalls()
    verify(preferences).isShowOpeningPercent = false
    verifyNoMoreInteractions(preferences)
  }

  @Test
  fun `should open localization ordering when clicked on localization ordering`() {
    mockPreferences()
    whenever(permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION)).thenReturn(true)

    // when
    viewModel.loadSettings()
    val channelSettingItem = states[0].settingsItems[7] as SettingItem.LocalizationOrdering
    channelSettingItem.callback()

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).containsExactly(SettingsViewEvent.NavigateToLocalizationsOrdering)
    verifyPreferencesMockedCalls()
    verifyNoMoreInteractions(preferences)
  }

  @Test
  fun `should open settings when clicked on notification permission`() {
    mockPreferences()
    whenever(permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION)).thenReturn(true)

    // when
    viewModel.loadSettings()
    val channelSettingItem = states[0].settingsItems[9] as SettingItem.NotificationsItem
    channelSettingItem.callback()

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).containsExactly(SettingsViewEvent.NavigateToSettings)
    verifyPreferencesMockedCalls()
    verifyNoMoreInteractions(preferences)
  }

  @Test
  fun `should open settings when clicked on localization permission`() {
    mockPreferences()
    whenever(permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION)).thenReturn(true)

    // when
    viewModel.loadSettings()
    val channelSettingItem = states[0].settingsItems[10] as SettingItem.LocalizationItem
    channelSettingItem.callback()

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).containsExactly(SettingsViewEvent.NavigateToSettings)
    verifyPreferencesMockedCalls()
    verifyNoMoreInteractions(preferences)
  }

  private fun mockPreferences() {
    whenever(preferences.channelHeight).thenReturn(100)
    whenever(preferences.temperatureUnit).thenReturn(TemperatureUnit.FAHRENHEIT)
    whenever(preferences.isButtonAutohide).thenReturn(true)
    whenever(preferences.isShowChannelInfo).thenReturn(false)
    whenever(preferences.isShowOpeningPercent).thenReturn(true)
  }

  private fun verifyPreferencesMockedCalls() {
    verify(preferences, times(2)).channelHeight
    verify(preferences).temperatureUnit
    verify(preferences).isButtonAutohide
    verify(preferences).isShowChannelInfo
    verify(preferences).isShowBottomLabel
    verify(preferences).isShowOpeningPercent
  }
}
