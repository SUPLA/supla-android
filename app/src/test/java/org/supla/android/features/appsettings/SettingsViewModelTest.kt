package org.supla.android.features.appsettings

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.NotificationManager
import android.app.UiModeManager
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.Before
import org.junit.Test
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.permissions.PermissionsHelper
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.data.model.general.LockScreenScope
import org.supla.android.data.model.general.LockScreenSettings
import org.supla.android.data.model.general.NightModeSetting
import org.supla.android.data.source.runtime.appsettings.ChannelHeight
import org.supla.android.data.source.runtime.appsettings.TemperatureUnit
import org.supla.android.features.lockscreen.UnlockAction
import org.supla.android.tools.SuplaSchedulers

class SettingsViewModelTest : BaseViewModelTest<SettingsViewState, SettingsViewEvent, SettingsViewModel>(MockSchedulers.MOCKK) {

  @MockK
  private lateinit var preferences: Preferences

  @MockK
  private lateinit var notificationManager: NotificationManager

  @MockK
  private lateinit var permissionsHelper: PermissionsHelper

  @MockK
  private lateinit var modeManager: UiModeManager

  @MockK
  private lateinit var encryptedPreferences: EncryptedPreferences

  @MockK
  private lateinit var applicationPreferences: ApplicationPreferences

  @MockK
  override lateinit var schedulers: SuplaSchedulers

  override val viewModel: SettingsViewModel by lazy {
    SettingsViewModel(
      preferences,
      notificationManager,
      permissionsHelper,
      modeManager,
      encryptedPreferences,
      applicationPreferences,
      schedulers
    )
  }

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    super.setUp()
  }

  @Test
  fun `check if setting options are loaded in proper order with proper values`() {
    // given
    mockPreferences()
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns true

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
      tuple(SettingItem.TemperaturePrecisionItem::class.java),
      tuple(SettingItem.ButtonAutoHide::class.java),
      tuple(SettingItem.InfoButton::class.java),
      tuple(SettingItem.BottomMenu::class.java),
      tuple(SettingItem.BottomLabels::class.java),
      tuple(SettingItem.RollerShutterOpenClose::class.java),
      tuple(SettingItem.NightMode::class.java),
      tuple(SettingItem.LockScreen::class.java),
      tuple(SettingItem.BatteryWarningLevel::class.java),
      tuple(SettingItem.LocalizationOrdering::class.java),
      tuple(SettingItem.AndroidAuto::class.java),
      tuple(SettingItem.HeaderItem::class.java),
      tuple(SettingItem.NotificationsItem::class.java),
      tuple(SettingItem.LocalizationItem::class.java)
    )

    assertThat((settingsList[0] as SettingItem.HeaderItem).headerResource).isEqualTo(R.string.menubar_appsettings)
    assertThat((settingsList[1] as SettingItem.ChannelHeightItem).height).isEqualTo(ChannelHeight.HEIGHT_100)
    assertThat((settingsList[2] as SettingItem.TemperatureUnitItem).unit).isEqualTo(TemperatureUnit.FAHRENHEIT)
    assertThat((settingsList[3] as SettingItem.TemperaturePrecisionItem).precision).isEqualTo(2)
    assertThat((settingsList[4] as SettingItem.ButtonAutoHide).active).isEqualTo(true)
    assertThat((settingsList[5] as SettingItem.InfoButton).visible).isEqualTo(false)
    assertThat((settingsList[6] as SettingItem.BottomMenu).visible).isEqualTo(false)
    assertThat((settingsList[7] as SettingItem.BottomLabels).visible).isEqualTo(false)
    assertThat((settingsList[8] as SettingItem.RollerShutterOpenClose).showOpeningPercentage).isEqualTo(true)
    assertThat((settingsList[9] as SettingItem.NightMode).nightModeSetting).isEqualTo(NightModeSetting.NEVER)
    assertThat((settingsList[10] as SettingItem.LockScreen).lockScreenScope).isEqualTo(LockScreenScope.NONE)
    assertThat((settingsList[14] as SettingItem.HeaderItem).headerResource).isEqualTo(R.string.settings_permissions)
    assertThat((settingsList[15] as SettingItem.NotificationsItem).allowed).isEqualTo(true)
    assertThat((settingsList[16] as SettingItem.LocalizationItem).allowed).isEqualTo(true)
  }

  @Test
  fun `check if channel height is saved`() {
    // given
    mockPreferences()
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns true
    every { preferences.channelHeight = 150 } answers {}

    // when
    viewModel.loadSettings()
    val channelSettingItem = states[0].settingsItems[1] as SettingItem.ChannelHeightItem
    channelSettingItem.callback(2)

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).isEmpty()
    verifyPreferencesMockedCalls()
    verify { preferences.channelHeight = 150 }
    confirmVerified(preferences)
  }

  @Test
  fun `check if temperature unit is saved`() {
    // given
    mockPreferences()
    every { applicationPreferences.temperatureUnit = TemperatureUnit.CELSIUS } answers {}
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns true

    // when
    viewModel.loadSettings()
    val channelSettingItem = states[0].settingsItems[2] as SettingItem.TemperatureUnitItem
    channelSettingItem.callback(0)

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).isEmpty()
    verifyPreferencesMockedCalls()
    verify { applicationPreferences.temperatureUnit = TemperatureUnit.CELSIUS }
    confirmVerified(preferences)
  }

  @Test
  fun `check if temperature precision is saved`() {
    // given
    mockPreferences()
    every { applicationPreferences.temperaturePrecision = 2 } answers {}
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns true

    // when
    viewModel.loadSettings()
    val channelSettingItem = states[0].settingsItems[3] as SettingItem.TemperaturePrecisionItem
    channelSettingItem.callback(1)

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).isEmpty()
    verifyPreferencesMockedCalls()
    verify { applicationPreferences.temperaturePrecision = 2 }
    confirmVerified(preferences)
  }

  @Test
  fun `check if button auto hide is saved`() {
    // given
    mockPreferences()
    every { preferences.isButtonAutohide = false } answers {}
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns true

    // when
    viewModel.loadSettings()
    val channelSettingItem = states[0].settingsItems[4] as SettingItem.ButtonAutoHide
    channelSettingItem.callback(false)

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).isEmpty()
    verifyPreferencesMockedCalls()
    verify { preferences.isButtonAutohide = false }
    confirmVerified(preferences)
  }

  @Test
  fun `check if info button is saved`() {
    // given
    mockPreferences()
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns true
    every { preferences.isShowChannelInfo = true } answers {}

    // when
    viewModel.loadSettings()
    val channelSettingItem = states[0].settingsItems[5] as SettingItem.InfoButton
    channelSettingItem.callback(true)

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).isEmpty()
    verifyPreferencesMockedCalls()
    verify { preferences.isShowChannelInfo = true }
    confirmVerified(preferences)
  }

  @Test
  fun `check if show bottom menu is saved`() {
    // given
    mockPreferences()
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns false
    every { preferences.isShowBottomMenu = true } answers {}

    // when
    viewModel.loadSettings()
    val channelSettingItem = states[0].settingsItems[6] as SettingItem.BottomMenu
    channelSettingItem.callback(true)

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).isEmpty()
    verifyPreferencesMockedCalls()
    verify { preferences.isShowBottomMenu = true }
    confirmVerified(preferences)
  }

  @Test
  fun `check if show bottom labels is saved`() {
    // given
    mockPreferences()
    every { preferences.isShowBottomLabel = true } answers {}
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns true

    // when
    viewModel.loadSettings()
    val channelSettingItem = states[0].settingsItems[7] as SettingItem.BottomLabels
    channelSettingItem.callback(true)

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).isEmpty()
    verifyPreferencesMockedCalls()
    verify { preferences.isShowBottomLabel = true }
    confirmVerified(preferences)
  }

  @Test
  fun `check if rs showing opening percentage is saved`() {
    // given
    mockPreferences()
    every { preferences.isShowOpeningPercent = false } answers {}
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns true

    // when
    viewModel.loadSettings()
    val channelSettingItem = states[0].settingsItems[8] as SettingItem.RollerShutterOpenClose
    channelSettingItem.callback(false)

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).isEmpty()
    verifyPreferencesMockedCalls()
    verify { preferences.isShowOpeningPercent = false }
    confirmVerified(preferences)
  }

  @Test
  fun `check if night mode setting is saved`() {
    // given
    mockPreferences()
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns false
    every { applicationPreferences.nightMode = NightModeSetting.ALWAYS } answers {}

    // when
    viewModel.loadSettings()
    val channelSettingItem = states[0].settingsItems[9] as SettingItem.NightMode
    channelSettingItem.callback(NightModeSetting.ALWAYS)

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).isEmpty()
    verifyPreferencesMockedCalls()
    verify {
      applicationPreferences.nightMode = NightModeSetting.ALWAYS
    }
    confirmVerified(preferences)
  }

  @Test
  fun `should navigate to pin verification when user wants turn off pin`() {
    // given
    mockPreferences()
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns false
    every { applicationPreferences.nightMode = NightModeSetting.ALWAYS } answers {}

    // when
    viewModel.loadSettings()
    val lockScreenItem = states[0].settingsItems[10] as SettingItem.LockScreen
    lockScreenItem.callback(LockScreenScope.NONE)

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).containsExactly(SettingsViewEvent.NavigateToPinVerification(UnlockAction.TurnOffPin))
    verifyPreferencesMockedCalls()
    verify(exactly = 2) {
      encryptedPreferences.lockScreenSettings
    }
    confirmVerified(preferences, encryptedPreferences)
  }

  @Test
  fun `should navigate to pin verification when user wants change scope to accounts`() {
    // given
    mockPreferences()
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns false
    every { encryptedPreferences.lockScreenSettings } returns LockScreenSettings(LockScreenScope.NONE, "sum", false)

    // when
    viewModel.loadSettings()
    val lockScreenItem = states[0].settingsItems[10] as SettingItem.LockScreen
    lockScreenItem.callback(LockScreenScope.ACCOUNTS)

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).containsExactly(SettingsViewEvent.NavigateToPinVerification(UnlockAction.ConfirmAuthorizeAccounts))
    verifyPreferencesMockedCalls()
    verify {
      encryptedPreferences.lockScreenSettings
    }
    confirmVerified(preferences, encryptedPreferences)
  }

  @Test
  fun `should navigate to pin verification when user wants change scope to application`() {
    // given
    mockPreferences()
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns false
    every { encryptedPreferences.lockScreenSettings } returns LockScreenSettings(LockScreenScope.NONE, "sum", false)

    // when
    viewModel.loadSettings()
    val lockScreenItem = states[0].settingsItems[10] as SettingItem.LockScreen
    lockScreenItem.callback(LockScreenScope.APPLICATION)

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).containsExactly(SettingsViewEvent.NavigateToPinVerification(UnlockAction.ConfirmAuthorizeApplication))
    verifyPreferencesMockedCalls()
    verify {
      encryptedPreferences.lockScreenSettings
    }
    confirmVerified(preferences, encryptedPreferences)
  }

  @Test
  fun `should navigate to pin setup`() {
    // given
    mockPreferences()
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns false

    // when
    viewModel.loadSettings()
    val lockScreenItem = states[0].settingsItems[10] as SettingItem.LockScreen
    lockScreenItem.callback(LockScreenScope.APPLICATION)

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).containsExactly(SettingsViewEvent.NavigateToPinSetup(LockScreenScope.APPLICATION))
    verifyPreferencesMockedCalls()
    verify {
      encryptedPreferences.lockScreenSettings
    }
    confirmVerified(preferences, encryptedPreferences)
  }

  @Test
  fun `should open localization ordering when clicked on localization ordering`() {
    mockPreferences()
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns true

    // when
    viewModel.loadSettings()
    val channelSettingItem = states[0].settingsItems[12] as SettingItem.LocalizationOrdering
    channelSettingItem.callback()

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).containsExactly(SettingsViewEvent.NavigateToLocalizationsOrdering)
    verifyPreferencesMockedCalls()
    confirmVerified(preferences)
  }

  @Test
  fun `should open settings when clicked on notification permission`() {
    mockPreferences()
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns true

    // when
    viewModel.loadSettings()
    val channelSettingItem = states[0].settingsItems[15] as SettingItem.NotificationsItem
    channelSettingItem.callback()

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).containsExactly(SettingsViewEvent.NavigateToSettings)
    verifyPreferencesMockedCalls()
    confirmVerified(preferences)
  }

  @Test
  fun `should open settings when clicked on localization permission`() {
    mockPreferences()
    every { permissionsHelper.checkPermissionGranted(ACCESS_FINE_LOCATION) } returns true

    // when
    viewModel.loadSettings()
    val channelSettingItem = states[0].settingsItems[16] as SettingItem.LocalizationItem
    channelSettingItem.callback()

    // then
    assertThat(states.size).isEqualTo(1)
    assertThat(events).containsExactly(SettingsViewEvent.NavigateToSettings)
    verifyPreferencesMockedCalls()
    confirmVerified(preferences)
  }

  private fun mockPreferences() {
    every { preferences.channelHeight } returns 100
    every { applicationPreferences.temperatureUnit } returns TemperatureUnit.FAHRENHEIT
    every { applicationPreferences.temperaturePrecision } returns 2
    every { preferences.isShowBottomMenu } returns false
    every { preferences.isButtonAutohide } returns true
    every { preferences.isShowChannelInfo } returns false
    every { preferences.isShowOpeningPercent } returns true
    every { preferences.isShowBottomLabel } returns false
    every { applicationPreferences.batteryWarningLevel } returns 10
    every { applicationPreferences.nightMode } returns NightModeSetting.NEVER

    every { encryptedPreferences.lockScreenSettings } returns LockScreenSettings.DEFAULT
  }

  private fun verifyPreferencesMockedCalls() {
    verify(exactly = 2) {
      preferences.channelHeight
    }
    verify {
      preferences.isButtonAutohide
      preferences.isShowChannelInfo
      preferences.isShowBottomMenu
      preferences.isShowBottomLabel
      preferences.isShowOpeningPercent
      applicationPreferences.temperatureUnit
      applicationPreferences.temperaturePrecision
      applicationPreferences.nightMode
    }
  }
}
