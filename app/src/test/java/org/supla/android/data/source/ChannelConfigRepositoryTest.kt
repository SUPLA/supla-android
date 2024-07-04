package org.supla.android.data.source
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

import com.google.gson.GsonBuilder
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.source.local.dao.ChannelConfigDao
import org.supla.android.data.source.local.entity.ChannelConfigEntity
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.gpm.SuplaChannelConfigMeasurementChartType
import org.supla.android.data.source.remote.gpm.SuplaChannelConfigMeterChartType
import org.supla.android.data.source.remote.gpm.SuplaChannelConfigMeterCounterType
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeMeasurementConfig
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeMeterConfig
import org.supla.android.data.source.remote.rollershutter.SuplaChannelFacadeBlindConfig
import org.supla.android.data.source.remote.rollershutter.SuplaTiltControlType
import org.supla.android.lib.SuplaConst

@RunWith(MockitoJUnitRunner::class)
class ChannelConfigRepositoryTest {

  @Mock
  private lateinit var channelConfigDao: ChannelConfigDao

  private val gson = GsonBuilder().create()

  private lateinit var repository: ChannelConfigRepository

  @Before
  fun setUp() {
    repository = ChannelConfigRepository(channelConfigDao, gson)
  }

  @Test
  fun `should insert measurement config to database`() {
    // given
    val profileId = 324L
    val config = SuplaChannelGeneralPurposeMeasurementConfig(
      remoteId = 123,
      func = SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER,
      crc32 = 111L,
      valueDivider = 1,
      valueMultiplier = 2,
      valueAdded = 3,
      valuePrecision = 10,
      unitBeforeValue = "pref",
      unitAfterValue = "suf",
      noSpaceBeforeValue = true,
      noSpaceAfterValue = false,
      keepHistory = true,
      defaultValueDivider = 4,
      defaultValueMultiplier = 5,
      defaultValueAdded = 10L,
      defaultValuePrecision = 11,
      defaultUnitBeforeValue = "def pref",
      defaultUnitAfterValue = "def suf",
      refreshIntervalMs = 123,
      chartType = SuplaChannelConfigMeasurementChartType.BAR
    )

    // when
    repository.insertOrUpdate(profileId, config)

    // then
    val captor = argumentCaptor<ChannelConfigEntity>()
    verify(channelConfigDao).insertOrUpdate(captor.capture())

    val entity = captor.firstValue
    assertThat(entity.channelId).isEqualTo(123)
    assertThat(entity.profileId).isEqualTo(profileId)
    assertThat(entity.configType).isEqualTo(ChannelConfigType.GENERAL_PURPOSE_MEASUREMENT)
    assertThat(entity.config).isEqualTo(
      "{\"remoteId\":123," +
        "\"func\":530," +
        "\"crc32\":111," +
        "\"valueDivider\":1," +
        "\"valueMultiplier\":2," +
        "\"valueAdded\":3," +
        "\"valuePrecision\":10," +
        "\"unitBeforeValue\":\"pref\"," +
        "\"unitAfterValue\":\"suf\"," +
        "\"noSpaceBeforeValue\":true," +
        "\"noSpaceAfterValue\":false," +
        "\"keepHistory\":true," +
        "\"defaultValueDivider\":4," +
        "\"defaultValueMultiplier\":5," +
        "\"defaultValueAdded\":10," +
        "\"defaultValuePrecision\":11," +
        "\"defaultUnitBeforeValue\":\"def pref\"," +
        "\"defaultUnitAfterValue\":\"def suf\"," +
        "\"refreshIntervalMs\":123," +
        "\"chartType\":\"BAR\"}"
    )
    assertThat(gson.fromJson(entity.config, SuplaChannelGeneralPurposeMeasurementConfig::class.java)).isEqualTo(config)
  }

  @Test
  fun `should insert meter config to database`() {
    // given
    val profileId = 324L
    val config = SuplaChannelGeneralPurposeMeterConfig(
      remoteId = 123,
      func = SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER,
      crc32 = 222L,
      valueDivider = 1,
      valueMultiplier = 2,
      valueAdded = 3,
      valuePrecision = 10,
      unitBeforeValue = "pref",
      unitAfterValue = "suf",
      noSpaceBeforeValue = false,
      noSpaceAfterValue = true,
      keepHistory = true,
      defaultValueDivider = 4,
      defaultValueMultiplier = 5,
      defaultValueAdded = 10L,
      defaultValuePrecision = 11,
      defaultUnitBeforeValue = "def pref",
      defaultUnitAfterValue = "def suf",
      refreshIntervalMs = 321,
      counterType = SuplaChannelConfigMeterCounterType.ALWAYS_DECREMENT,
      chartType = SuplaChannelConfigMeterChartType.BAR,
      includeValueAddedInHistory = true,
      fillMissingData = false,
    )

    // when
    repository.insertOrUpdate(profileId, config)

    // then
    val captor = argumentCaptor<ChannelConfigEntity>()
    verify(channelConfigDao).insertOrUpdate(captor.capture())

    val entity = captor.firstValue
    assertThat(entity.channelId).isEqualTo(123)
    assertThat(entity.profileId).isEqualTo(profileId)
    assertThat(entity.configType).isEqualTo(ChannelConfigType.GENERAL_PURPOSE_METER)
    assertThat(entity.config).isEqualTo(
      "{\"remoteId\":123," +
        "\"func\":530," +
        "\"crc32\":222," +
        "\"valueDivider\":1," +
        "\"valueMultiplier\":2," +
        "\"valueAdded\":3," +
        "\"valuePrecision\":10," +
        "\"unitBeforeValue\":\"pref\"," +
        "\"unitAfterValue\":\"suf\"," +
        "\"noSpaceBeforeValue\":false," +
        "\"noSpaceAfterValue\":true," +
        "\"keepHistory\":true," +
        "\"defaultValueDivider\":4," +
        "\"defaultValueMultiplier\":5," +
        "\"defaultValueAdded\":10," +
        "\"defaultValuePrecision\":11," +
        "\"defaultUnitBeforeValue\":\"def pref\"," +
        "\"defaultUnitAfterValue\":\"def suf\"," +
        "\"refreshIntervalMs\":321," +
        "\"counterType\":\"ALWAYS_DECREMENT\"," +
        "\"chartType\":\"BAR\"," +
        "\"includeValueAddedInHistory\":true," +
        "\"fillMissingData\":false}"
    )
    assertThat(gson.fromJson(entity.config, SuplaChannelGeneralPurposeMeterConfig::class.java)).isEqualTo(config)
  }

  @Test
  fun `should insert facade blind config to database`() {
    // given
    val profileId = 324L
    val config = SuplaChannelFacadeBlindConfig(
      remoteId = 123,
      func = SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND,
      crc32 = 1L,
      closingTimeMs = 100,
      openingTimeMs = 200,
      tiltingTimeMs = 30,
      motorUpsideDown = true,
      buttonsUpsideDown = false,
      timeMargin = 14,
      tilt0Angle = 50,
      tilt100Angle = 60,
      type = SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING
    )

    // when
    repository.insertOrUpdate(profileId, config)

    // then
    val captor = argumentCaptor<ChannelConfigEntity>()
    verify(channelConfigDao).insertOrUpdate(captor.capture())

    val entity = captor.firstValue
    assertThat(entity.channelId).isEqualTo(123)
    assertThat(entity.profileId).isEqualTo(profileId)
    assertThat(entity.configType).isEqualTo(ChannelConfigType.FACADE_BLIND)
    assertThat(entity.config).isEqualTo(
      "{" +
        "\"remoteId\":123," +
        "\"func\":900," +
        "\"crc32\":1," +
        "\"closingTimeMs\":100," +
        "\"openingTimeMs\":200," +
        "\"tiltingTimeMs\":30," +
        "\"motorUpsideDown\":true," +
        "\"buttonsUpsideDown\":false," +
        "\"timeMargin\":14," +
        "\"tilt0Angle\":50," +
        "\"tilt100Angle\":60," +
        "\"type\":\"CHANGES_POSITION_WHILE_TILTING\"" +
        "}"
    )
    assertThat(gson.fromJson(entity.config, SuplaChannelFacadeBlindConfig::class.java)).isEqualTo(config)
  }

  @Test
  fun `should load measurement config from database`() {
    // given
    val channelId = 222
    val profileId = 333L
    val type = ChannelConfigType.GENERAL_PURPOSE_MEASUREMENT
    val config = "{\"remoteId\":$channelId," +
      "\"func\":520," +
      "\"crc32\":333," +
      "\"valueDivider\":1," +
      "\"valueMultiplier\":2," +
      "\"valueAdded\":3," +
      "\"valuePrecision\":10," +
      "\"unitBeforeValue\":\"pref\"," +
      "\"unitAfterValue\":\"suf\"," +
      "\"noSpaceBeforeValue\":true," +
      "\"noSpaceAfterValue\":false," +
      "\"keepHistory\":true," +
      "\"defaultValueDivider\":4," +
      "\"defaultValueMultiplier\":5," +
      "\"defaultValueAdded\":10," +
      "\"defaultValuePrecision\":11," +
      "\"defaultUnitBeforeValue\":\"def pref\"," +
      "\"defaultUnitAfterValue\":\"def suf\"," +
      "\"refreshIntervalMs\":123," +
      "\"chartType\":\"BAR\"}"
    val entity: ChannelConfigEntity = mockk<ChannelConfigEntity>().apply {
      every { this@apply.channelId } returns channelId
      every { this@apply.profileId } returns profileId
      every { this@apply.configType } returns type
      every { this@apply.config } returns config
    }

    whenever(channelConfigDao.read(profileId, channelId, type)).thenReturn(Single.just(entity))

    // when
    val testObserver = repository.findChannelConfig(profileId, channelId, type).test()

    // then
    testObserver.assertComplete()
    val channelConfig = testObserver.values().first()
    assertThat(channelConfig).isEqualTo(
      SuplaChannelGeneralPurposeMeasurementConfig(
        remoteId = channelId,
        func = SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT,
        crc32 = 333,
        valueDivider = 1,
        valueMultiplier = 2,
        valueAdded = 3,
        valuePrecision = 10,
        unitBeforeValue = "pref",
        unitAfterValue = "suf",
        noSpaceBeforeValue = true,
        noSpaceAfterValue = false,
        keepHistory = true,
        defaultValueDivider = 4,
        defaultValueMultiplier = 5,
        defaultValueAdded = 10L,
        defaultValuePrecision = 11,
        defaultUnitBeforeValue = "def pref",
        defaultUnitAfterValue = "def suf",
        refreshIntervalMs = 123,
        chartType = SuplaChannelConfigMeasurementChartType.BAR
      )
    )
  }

  @Test
  fun `should load meter config from database`() {
    // given
    val channelId = 222
    val profileId = 333L
    val type = ChannelConfigType.GENERAL_PURPOSE_METER
    val config = "{\"remoteId\":$channelId," +
      "\"func\":530," +
      "\"crc32\":444," +
      "\"valueDivider\":1," +
      "\"valueMultiplier\":2," +
      "\"valueAdded\":3," +
      "\"valuePrecision\":10," +
      "\"unitBeforeValue\":\"pref\"," +
      "\"unitAfterValue\":\"suf\"," +
      "\"noSpaceBeforeValue\":true," +
      "\"noSpaceAfterValue\":false," +
      "\"keepHistory\":true," +
      "\"defaultValueDivider\":4," +
      "\"defaultValueMultiplier\":5," +
      "\"defaultValueAdded\":10," +
      "\"defaultValuePrecision\":11," +
      "\"defaultUnitBeforeValue\":\"def pref\"," +
      "\"defaultUnitAfterValue\":\"def suf\"," +
      "\"refreshIntervalMs\":321," +
      "\"counterType\":\"ALWAYS_INCREMENT\"," +
      "\"chartType\":\"BAR\"," +
      "\"includeValueAddedInHistory\":false," +
      "\"fillMissingData\":true}"
    val entity: ChannelConfigEntity = mockk<ChannelConfigEntity>().apply {
      every { this@apply.channelId } returns channelId
      every { this@apply.profileId } returns profileId
      every { this@apply.configType } returns type
      every { this@apply.config } returns config
    }

    whenever(channelConfigDao.read(profileId, channelId, type)).thenReturn(Single.just(entity))

    // when
    val testObserver = repository.findChannelConfig(profileId, channelId, type).test()

    // then
    testObserver.assertComplete()
    val channelConfig = testObserver.values().first()
    assertThat(channelConfig).isEqualTo(
      SuplaChannelGeneralPurposeMeterConfig(
        remoteId = channelId,
        func = SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER,
        crc32 = 444,
        valueDivider = 1,
        valueMultiplier = 2,
        valueAdded = 3,
        valuePrecision = 10,
        unitBeforeValue = "pref",
        unitAfterValue = "suf",
        noSpaceBeforeValue = true,
        noSpaceAfterValue = false,
        keepHistory = true,
        defaultValueDivider = 4,
        defaultValueMultiplier = 5,
        defaultValueAdded = 10L,
        defaultValuePrecision = 11,
        defaultUnitBeforeValue = "def pref",
        defaultUnitAfterValue = "def suf",
        refreshIntervalMs = 321,
        counterType = SuplaChannelConfigMeterCounterType.ALWAYS_INCREMENT,
        chartType = SuplaChannelConfigMeterChartType.BAR,
        includeValueAddedInHistory = false,
        fillMissingData = true
      )
    )
  }

  @Test
  fun `should load facade blind config from database`() {
    // given
    val channelId = 222
    val profileId = 333L
    val type = ChannelConfigType.FACADE_BLIND
    val config = "{" +
      "\"remoteId\":$channelId," +
      "\"func\":900," +
      "\"crc32\":1," +
      "\"closingTimeMs\":100," +
      "\"openingTimeMs\":200," +
      "\"tiltingTimeMs\":30," +
      "\"motorUpsideDown\":true," +
      "\"buttonsUpsideDown\":false," +
      "\"timeMargin\":14," +
      "\"tilt0Angle\":50," +
      "\"tilt100Angle\":60," +
      "\"type\":\"TILTS_ONLY_WHEN_FULLY_CLOSED\"" +
      "}"
    val entity: ChannelConfigEntity = mockk<ChannelConfigEntity>().apply {
      every { this@apply.channelId } returns channelId
      every { this@apply.profileId } returns profileId
      every { this@apply.configType } returns type
      every { this@apply.config } returns config
    }

    whenever(channelConfigDao.read(profileId, channelId, type)).thenReturn(Single.just(entity))

    // when
    val testObserver = repository.findChannelConfig(profileId, channelId, type).test()

    // then
    testObserver.assertComplete()
    val channelConfig = testObserver.values().first()
    assertThat(channelConfig).isEqualTo(
      SuplaChannelFacadeBlindConfig(
        remoteId = channelId,
        func = SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND,
        crc32 = 1,
        closingTimeMs = 100,
        openingTimeMs = 200,
        tiltingTimeMs = 30,
        motorUpsideDown = true,
        buttonsUpsideDown = false,
        timeMargin = 14,
        tilt0Angle = 50,
        tilt100Angle = 60,
        type = SuplaTiltControlType.TILTS_ONLY_WHEN_FULLY_CLOSED
      )
    )
  }

  @Test
  fun `should delete`() {
    // given
    val profileId = 123L
    val channelId = 321
    whenever(channelConfigDao.delete(profileId, channelId)).thenReturn(Completable.complete())

    // when
    val result = repository.delete(profileId, channelId).test()

    // then
    result.assertComplete()
    verify(channelConfigDao).delete(profileId, channelId)
    verifyNoMoreInteractions(channelConfigDao)
  }
}
