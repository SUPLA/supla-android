package org.supla.android.usecases.profile

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.core.SuplaAppApi
import org.supla.android.core.SuplaAppProvider
import org.supla.android.core.networking.suplaclient.SuplaClientEvent
import org.supla.android.core.networking.suplaclient.SuplaClientStateHolder
import org.supla.android.data.source.AndroidAutoItemRepository
import org.supla.android.data.source.ChannelConfigRepository
import org.supla.android.data.source.ChannelExtendedValueRepository
import org.supla.android.data.source.ChannelGroupRelationRepository
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.ChannelStateRepository
import org.supla.android.data.source.ChannelValueRepository
import org.supla.android.data.source.CurrentLogRepository
import org.supla.android.data.source.ElectricityMeterLogRepository
import org.supla.android.data.source.GeneralPurposeMeasurementLogRepository
import org.supla.android.data.source.GeneralPurposeMeterLogRepository
import org.supla.android.data.source.HomePlusThermostatLogRepository
import org.supla.android.data.source.HumidityLogRepository
import org.supla.android.data.source.ImpulseCounterLogRepository
import org.supla.android.data.source.LocationRepository
import org.supla.android.data.source.PowerActiveLogRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.RoomColorListRepository
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.RoomSceneRepository
import org.supla.android.data.source.RoomUserIconRepository
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.android.data.source.VoltageLogRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.lib.singlecall.SingleCall
import org.supla.android.profile.ProfileIdHolder
import org.supla.android.usecases.client.DisconnectUseCase
import org.supla.android.widget.WidgetManager

@RunWith(MockitoJUnitRunner::class)
class DeleteProfileUseCaseTest {
  @MockK
  private lateinit var context: Context

  @MockK
  private lateinit var profileRepository: RoomProfileRepository

  @MockK
  private lateinit var suplaAppProvider: SuplaAppProvider

  @MockK
  private lateinit var profileIdHolder: ProfileIdHolder

  @MockK
  private lateinit var activateProfileUseCase: ActivateProfileUseCase

  @MockK
  private lateinit var suplaClientStateHolder: SuplaClientStateHolder

  @MockK
  private lateinit var disconnectUseCase: DisconnectUseCase

  @MockK
  private lateinit var singleCallProvider: SingleCall.Provider

  @MockK
  private lateinit var widgetManager: WidgetManager

  @MockK private lateinit var androidAutoItemRepository: AndroidAutoItemRepository

  @MockK private lateinit var channelRepository: RoomChannelRepository

  @MockK private lateinit var channelConfigRepository: ChannelConfigRepository

  @MockK private lateinit var channelExtendedValueRepository: ChannelExtendedValueRepository

  @MockK private lateinit var channelRelationRepository: ChannelRelationRepository

  @MockK private lateinit var channelStateRepository: ChannelStateRepository

  @MockK private lateinit var channelValueRepository: ChannelValueRepository

  @MockK private lateinit var channelGroupRepository: ChannelGroupRepository

  @MockK private lateinit var channelGroupRelationRepository: ChannelGroupRelationRepository

  @MockK private lateinit var colorListRepository: RoomColorListRepository

  @MockK private lateinit var locationRepository: LocationRepository

  @MockK private lateinit var sceneRepository: RoomSceneRepository

  @MockK private lateinit var userIconRepository: RoomUserIconRepository

  @MockK private lateinit var currentLogRepository: CurrentLogRepository

  @MockK private lateinit var electricityMeterLogRepository: ElectricityMeterLogRepository

  @MockK private lateinit var generalPurposeMeterLogRepository: GeneralPurposeMeterLogRepository

  @MockK private lateinit var generalPurposeMeasurementLogRepository: GeneralPurposeMeasurementLogRepository

  @MockK private lateinit var humidityLogRepository: HumidityLogRepository

  @MockK private lateinit var impulseCounterLogRepository: ImpulseCounterLogRepository

  @MockK private lateinit var powerActiveLogRepository: PowerActiveLogRepository

  @MockK private lateinit var temperatureLogRepository: TemperatureLogRepository

  @MockK private lateinit var temperatureAndHumidityLogRepository: TemperatureAndHumidityLogRepository

  @MockK private lateinit var homePlusThermostatLogRepository: HomePlusThermostatLogRepository

  @MockK private lateinit var voltageLogRepository: VoltageLogRepository

  @InjectMockKs
  private lateinit var useCase: DeleteProfileUseCase

  private lateinit var allDependencies: List<DeleteProfileUseCase.ProfileRemover>

  @Before
  fun setUp() {
    MockKAnnotations.init(this)

    allDependencies = listOf(
      androidAutoItemRepository,
      channelRepository,
      channelConfigRepository,
      channelExtendedValueRepository,
      channelRelationRepository,
      channelStateRepository,
      channelValueRepository,
      channelGroupRepository,
      channelGroupRelationRepository,
      colorListRepository,
      locationRepository,
      sceneRepository,
      userIconRepository,
      currentLogRepository,
      electricityMeterLogRepository,
      generalPurposeMeterLogRepository,
      generalPurposeMeasurementLogRepository,
      humidityLogRepository,
      impulseCounterLogRepository,
      powerActiveLogRepository,
      temperatureLogRepository,
      temperatureAndHumidityLogRepository,
      homePlusThermostatLogRepository,
      voltageLogRepository
    )
  }

  @Test
  fun `should delete inactive profile`() {
    // given
    val profileId = 132L
    val profile = profileMock(profileId, false)

    mockRelatedRepositories(profileId)
    every { profileRepository.findProfile(profileId) } returns Single.just(profile)
    every { profileRepository.deleteProfile(profile) } returns Completable.complete()
    every { widgetManager.onProfileRemoved(profileId) } answers {}

    // when
    val testObserver = useCase(profileId).test()

    // then
    testObserver.assertComplete()

    verify {
      profileRepository.findProfile(profileId)
      profileRepository.deleteProfile(profile)
      widgetManager.onProfileRemoved(profileId)

      allDependencies.forEach { it.deleteByProfile(profileId) }
    }
    confirmVerified(
      profileRepository, suplaAppProvider, profileIdHolder, context, activateProfileUseCase, suplaClientStateHolder,
      disconnectUseCase, widgetManager, androidAutoItemRepository, channelRepository, channelConfigRepository,
      channelExtendedValueRepository, channelRelationRepository, channelStateRepository, channelValueRepository,
      channelGroupRepository, channelGroupRelationRepository, colorListRepository, locationRepository, sceneRepository,
      userIconRepository, currentLogRepository, electricityMeterLogRepository, generalPurposeMeterLogRepository,
      generalPurposeMeasurementLogRepository, humidityLogRepository, impulseCounterLogRepository, powerActiveLogRepository,
      temperatureLogRepository, temperatureAndHumidityLogRepository, homePlusThermostatLogRepository, voltageLogRepository
    )
  }

  @Test
  fun `should delete last active profile`() {
    // given
    val profileId = 132L
    val profile = profileMock(profileId, true)

    mockRelatedRepositories(profileId)
    every { profileRepository.findProfile(profileId) } returns Single.just(profile)
    every { profileRepository.deleteProfile(profile) } returns Completable.complete()
    every { profileRepository.findAllProfiles() } returns Observable.just(emptyList())
    every { disconnectUseCase.invoke() } returns Completable.complete()
    every { profileIdHolder.profileId = null } answers {}
    every { suplaClientStateHolder.handleEvent(SuplaClientEvent.NoAccount) } answers {}
    every { widgetManager.onProfileRemoved(profileId) } answers {}

    // when
    val testObserver = useCase(profileId).test()

    // then
    testObserver.assertComplete()

    verify {
      profileRepository.findProfile(profileId)
      profileRepository.deleteProfile(profile)
      profileRepository.findAllProfiles()
      profileIdHolder.profileId = null
      disconnectUseCase.invoke()
      suplaClientStateHolder.handleEvent(SuplaClientEvent.NoAccount)
      widgetManager.onProfileRemoved(profileId)

      allDependencies.forEach { it.deleteByProfile(profileId) }
    }
    confirmVerified(
      profileRepository, suplaAppProvider, profileIdHolder, context, activateProfileUseCase, suplaClientStateHolder,
      disconnectUseCase, widgetManager, androidAutoItemRepository, channelRepository, channelConfigRepository,
      channelExtendedValueRepository, channelRelationRepository, channelStateRepository, channelValueRepository,
      channelGroupRepository, channelGroupRelationRepository, colorListRepository, locationRepository, sceneRepository,
      userIconRepository, currentLogRepository, electricityMeterLogRepository, generalPurposeMeterLogRepository,
      generalPurposeMeasurementLogRepository, humidityLogRepository, impulseCounterLogRepository, powerActiveLogRepository,
      temperatureLogRepository, temperatureAndHumidityLogRepository, homePlusThermostatLogRepository, voltageLogRepository
    )
  }

  @Test
  fun `should delete active profile and activate other one`() {
    // given
    val profileId = 133L
    val profileIdToActivate = 234L
    val profile = profileMock(profileId, true)

    mockRelatedRepositories(profileId)
    every { profileRepository.findProfile(profileId) } returns Single.just(profile)
    every { profileRepository.deleteProfile(profile) } returns Completable.complete()
    every { activateProfileUseCase.invoke(profileIdToActivate, true) } returns Completable.complete()
    every { profileRepository.findAllProfiles() } returns Observable.just(listOf(profileMock(profileIdToActivate, false)))
    every { disconnectUseCase.invoke() } returns Completable.complete()
    every { widgetManager.onProfileRemoved(profileId) } answers {}

    val suplaApp = mockk<SuplaAppApi>()
    every { suplaApp.SuplaClientInitIfNeed(any()) } returns null
    every { suplaAppProvider.provide() } returns suplaApp

    // when
    val testObserver = useCase(profileId).test()

    // then
    testObserver.assertComplete()

    verify {
      profileRepository.findProfile(profileId)
      profileRepository.deleteProfile(profile)
      profileRepository.findAllProfiles()
      activateProfileUseCase.invoke(profileIdToActivate, true)
      suplaAppProvider.provide()
      suplaApp.SuplaClientInitIfNeed(context)
      disconnectUseCase.invoke()
      widgetManager.onProfileRemoved(profileId)

      allDependencies.forEach { it.deleteByProfile(profileId) }
    }
    confirmVerified(
      suplaApp, profileRepository, suplaAppProvider, profileIdHolder, context, activateProfileUseCase, suplaClientStateHolder,
      disconnectUseCase, widgetManager, androidAutoItemRepository, channelRepository, channelConfigRepository,
      channelExtendedValueRepository, channelRelationRepository, channelStateRepository, channelValueRepository,
      channelGroupRepository, channelGroupRelationRepository, colorListRepository, locationRepository, sceneRepository,
      userIconRepository, currentLogRepository, electricityMeterLogRepository, generalPurposeMeterLogRepository,
      generalPurposeMeasurementLogRepository, humidityLogRepository, impulseCounterLogRepository, powerActiveLogRepository,
      temperatureLogRepository, temperatureAndHumidityLogRepository, homePlusThermostatLogRepository, voltageLogRepository
    )
  }

  private fun profileMock(profileId: Long, isActive: Boolean): ProfileEntity = mockk {
    every { id } returns profileId
    every { this@mockk.active } returns isActive
  }

  private fun mockRelatedRepositories(profileId: Long) {
    every { androidAutoItemRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { channelRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { channelConfigRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { channelExtendedValueRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { channelRelationRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { channelStateRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { channelValueRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { channelGroupRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { channelGroupRelationRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { colorListRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { locationRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { sceneRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { userIconRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { currentLogRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { electricityMeterLogRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { generalPurposeMeterLogRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { generalPurposeMeasurementLogRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { humidityLogRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { impulseCounterLogRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { powerActiveLogRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { temperatureLogRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { temperatureAndHumidityLogRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { homePlusThermostatLogRepository.deleteByProfile(profileId) } returns Completable.complete()
    every { voltageLogRepository.deleteByProfile(profileId) } returns Completable.complete()
  }
}
