package org.supla.android.cfg
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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import junit.framework.TestCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*

@RunWith(MockitoJUnitRunner::class)
class CfgViewModelTest : TestCase() {

  @Mock
  private lateinit var appConfigurationProvider: AppConfigurationProvider

  private val fakeCfg = CfgData(
    TemperatureUnit.CELSIUS,
    true,
    ChannelHeight.HEIGHT_100,
    _showChannelInfo = true,
    _showOpeningPercent = false
  )

  @get:Rule
  var instantTaskExecutorRule = InstantTaskExecutorRule()

  @Test
  fun beforeInteractionNoNavigationDecision() {
    whenever(appConfigurationProvider.getConfiguration()).thenReturn(fakeCfg)
    val nc = NavCoordinator()
    CfgViewModel(appConfigurationProvider)
    assertNull(
      "should not be set initially",
      nc.navAction.value
    )
  }

  @Test
  fun saveIsOneShot() {
    whenever(appConfigurationProvider.getConfiguration()).thenReturn(fakeCfg)
    val viewModel = CfgViewModel(appConfigurationProvider)
    assertTrue(viewModel.saveEnabled.value!!)
    viewModel.onSaveConfig()
    assertFalse(viewModel.saveEnabled.value!!)
  }

  @Test
  fun savedChangePropagatesToRepository() {
    var storedData: CfgData? = null
    whenever(appConfigurationProvider.getConfiguration()).thenReturn(fakeCfg)
    doAnswer { storedData = it.getArgument(0) }.`when`(appConfigurationProvider).storeConfiguration(any())
    val viewModel = CfgViewModel(appConfigurationProvider)
    viewModel.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
    viewModel.onSaveConfig()
    assertEquals(TemperatureUnit.FAHRENHEIT, storedData!!.temperatureUnit.value)
    assertEquals(fakeCfg.buttonAutohide.value, storedData!!.buttonAutohide.value)
    assertEquals(fakeCfg.channelHeight.value, storedData!!.channelHeight.value)
    assertEquals(fakeCfg.showChannelInfo.value, storedData!!.showChannelInfo.value)
  }

  @Test
  fun temperatureUnitChangePropagatesToRepository() {
    val initialData = fakeCfg
    var storedData: CfgData? = null
    whenever(appConfigurationProvider.getConfiguration()).thenReturn(fakeCfg)
    doAnswer { storedData = it.getArgument(0) }.`when`(appConfigurationProvider).storeConfiguration(any())
    val viewModel = CfgViewModel(appConfigurationProvider)
    assertFalse(viewModel.isDirty.value!!)
    viewModel.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
    assertTrue(viewModel.isDirty.value!!)
    viewModel.onSaveConfig()
    assertEquals(initialData.temperatureUnit, storedData!!.temperatureUnit)
    assertEquals(initialData.buttonAutohide, storedData!!.buttonAutohide)
    assertEquals(initialData.channelHeight, storedData!!.channelHeight)
    assertEquals(initialData.showChannelInfo, storedData!!.showChannelInfo)
    assertEquals(TemperatureUnit.FAHRENHEIT, storedData!!.temperatureUnit.value)
  }

  @Test
  fun defaultSettingForButtonAutohideIsTrue() {
    whenever(appConfigurationProvider.getConfiguration()).thenReturn(fakeCfg)
    val vm = CfgViewModel(appConfigurationProvider)
    assertEquals(true, vm.cfgData.buttonAutohide.value)
  }

  @Test
  fun buttonAutohidePropagatesToRepository() {
    var storedData: CfgData? = null
    whenever(appConfigurationProvider.getConfiguration()).thenReturn(fakeCfg)
    doAnswer { storedData = it.getArgument(0) }.`when`(appConfigurationProvider).storeConfiguration(any())
    val viewModel = CfgViewModel(appConfigurationProvider)
    val origVal = viewModel.cfgData.buttonAutohide.value!!
    val newVal = !origVal
    viewModel.setButtonAutohide(newVal)
    viewModel.onSaveConfig()
    assertEquals(newVal, storedData!!.buttonAutohide.value)
  }

  @Test
  fun `default channel height is 100 percent`() {
    whenever(appConfigurationProvider.getConfiguration()).thenReturn(fakeCfg)
    val viewModel = CfgViewModel(appConfigurationProvider)
    assertEquals(ChannelHeight.HEIGHT_100, viewModel.cfgData.channelHeight.value)
  }

  @Test
  fun `setting channel height to 60`() {
    var storedData: CfgData? = null
    whenever(appConfigurationProvider.getConfiguration()).thenReturn(fakeCfg)
    doAnswer { storedData = it.getArgument(0) }.`when`(appConfigurationProvider).storeConfiguration(any())
    val viewModel = CfgViewModel(appConfigurationProvider)
    viewModel.setChannelHeight(ChannelHeight.HEIGHT_60)
    viewModel.onSaveConfig()
    assertEquals(ChannelHeight.HEIGHT_60, storedData!!.channelHeight.value)
  }

  @Test
  fun `setting channel height to 150`() {
    var storedData: CfgData? = null
    whenever(appConfigurationProvider.getConfiguration()).thenReturn(fakeCfg)
    doAnswer { storedData = it.getArgument(0) }.`when`(appConfigurationProvider).storeConfiguration(any())
    val viewModel = CfgViewModel(appConfigurationProvider)
    viewModel.setChannelHeight(ChannelHeight.HEIGHT_150)
    viewModel.onSaveConfig()
    assertEquals(ChannelHeight.HEIGHT_150, storedData!!.channelHeight.value)
  }
}
