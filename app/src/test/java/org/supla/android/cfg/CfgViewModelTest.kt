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
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import org.supla.android.data.presenter.TemperaturePresenter

@RunWith(MockitoJUnitRunner::class)
class CfgViewModelTest : TestCase() {

  @Mock
  private lateinit var temperaturePresenter: TemperaturePresenter

  @Mock
  private lateinit var mockRepository: CfgRepository

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
    val repository: CfgRepository = mock {
      on { getCfg() } doReturn fakeCfg
    }
    val nc = NavCoordinator()
    CfgViewModel(repository, nc, temperaturePresenter)
    assertNull(
      "should not be set initially",
      nc.navAction.value
    )
  }

  @Test
  fun saveIsOneShot() {
    Mockito.`when`(mockRepository.getCfg())
      .thenReturn(fakeCfg)
    val viewModel = CfgViewModel(mockRepository, NavCoordinator(), temperaturePresenter)
    assertTrue(viewModel.saveEnabled.value!!)
    viewModel.onSaveConfig()
    assertFalse(viewModel.saveEnabled.value!!)
    verify(temperaturePresenter, never()).reloadConfig()
  }

  @Test
  fun savedChangePropagatesToRepository() {
    val initialData = fakeCfg
    var storedData: CfgData? = null
    val repository: CfgRepository = mock {
      on { getCfg() } doReturn initialData
      on { storeCfg(any()) } doAnswer { storedData = it.getArgument(0) }
    }
    val viewModel = CfgViewModel(repository, NavCoordinator(), temperaturePresenter)
    viewModel.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
    viewModel.onSaveConfig()
    assertEquals(TemperatureUnit.FAHRENHEIT, storedData!!.temperatureUnit.value)
    assertEquals(fakeCfg.buttonAutohide.value, storedData!!.buttonAutohide.value)
    assertEquals(fakeCfg.channelHeight.value, storedData!!.channelHeight.value)
    assertEquals(fakeCfg.showChannelInfo.value, storedData!!.showChannelInfo.value)
    verify(temperaturePresenter).reloadConfig()
  }

  @Test
  fun temperatureUnitChangePropagatesToRepository() {
    val initialData = fakeCfg
    var storedData: CfgData? = null
    val repository: CfgRepository = mock {
      on { getCfg() } doReturn initialData
      on { storeCfg(any()) } doAnswer { storedData = it.getArgument(0) }
    }
    val viewModel = CfgViewModel(repository, NavCoordinator(), temperaturePresenter)
    assertFalse(viewModel.isDirty.value!!)
    viewModel.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
    assertTrue(viewModel.isDirty.value!!)
    viewModel.onSaveConfig()
    assertEquals(initialData.temperatureUnit, storedData!!.temperatureUnit)
    assertEquals(initialData.buttonAutohide, storedData!!.buttonAutohide)
    assertEquals(initialData.channelHeight, storedData!!.channelHeight)
    assertEquals(initialData.showChannelInfo, storedData!!.showChannelInfo)
    assertEquals(TemperatureUnit.FAHRENHEIT, storedData!!.temperatureUnit.value)
    verify(temperaturePresenter).reloadConfig()
  }

  @Test
  fun defaultSettingForButtonAutohideIsTrue() {
    val initialData = fakeCfg
    val repository: CfgRepository = mock {
      on { getCfg() } doReturn initialData
    }
    val vm = CfgViewModel(repository, NavCoordinator(), temperaturePresenter)
    assertEquals(true, vm.cfgData.buttonAutohide.value)
  }

  @Test
  fun buttonAutohidePropagatesToRepository() {
    val initialData = fakeCfg
    var storedData: CfgData? = null
    val repository: CfgRepository = mock {
      on { getCfg() } doReturn initialData
      on { storeCfg(any()) } doAnswer { storedData = it.getArgument(0) }
    }
    val viewModel = CfgViewModel(repository, NavCoordinator(), temperaturePresenter)
    val origVal = viewModel.cfgData.buttonAutohide.value!!
    val newVal = !origVal
    viewModel.setButtonAutohide(newVal)
    viewModel.onSaveConfig()
    assertEquals(newVal, storedData!!.buttonAutohide.value)
    verify(temperaturePresenter).reloadConfig()
  }

  @Test
  fun `default channel height is 100 percent`() {
    val initialData = fakeCfg
    val repository: CfgRepository = mock {
      on { getCfg() } doReturn initialData
    }
    val viewModel = CfgViewModel(repository, NavCoordinator(), temperaturePresenter)
    assertEquals(ChannelHeight.HEIGHT_100, viewModel.cfgData.channelHeight.value)
  }

  @Test
  fun `setting channel height to 60`() {
    val initialData = fakeCfg
    var storedData: CfgData? = null
    val repository: CfgRepository = mock {
      on { getCfg() } doReturn initialData
      on { storeCfg(any()) } doAnswer { storedData = it.getArgument(0) }
    }
    val viewModel = CfgViewModel(repository, NavCoordinator(), temperaturePresenter)
    viewModel.setChannelHeight(ChannelHeight.HEIGHT_60)
    viewModel.onSaveConfig()
    assertEquals(ChannelHeight.HEIGHT_60, storedData!!.channelHeight.value)
    verify(temperaturePresenter).reloadConfig()
  }

  @Test
  fun `setting channel height to 150`() {
    val initialData = fakeCfg
    var storedData: CfgData? = null
    val repository: CfgRepository = mock {
      on { getCfg() } doReturn initialData
      on { storeCfg(any()) } doAnswer { storedData = it.getArgument(0) }
    }
    val viewModel = CfgViewModel(repository, NavCoordinator(), temperaturePresenter)
    viewModel.setChannelHeight(ChannelHeight.HEIGHT_150)
    viewModel.onSaveConfig()
    assertEquals(ChannelHeight.HEIGHT_150, storedData!!.channelHeight.value)
    verify(temperaturePresenter).reloadConfig()
  }
}
