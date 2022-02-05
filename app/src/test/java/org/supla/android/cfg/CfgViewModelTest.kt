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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import org.supla.android.profile.ProfileManager
import org.supla.android.profile.AuthInfo
import org.supla.android.db.AuthProfileItem

@RunWith(MockitoJUnitRunner::class)
class CfgViewModelTest: TestCase() {

    private val mockRepository = Mockito.mock(CfgRepository::class.java)
    private val fakeCfg = CfgData(TemperatureUnit.CELSIUS,
                                  true,
                                  ChannelHeight.HEIGHT_100, 
                                  true, false)

    private val fakePM = DummyProfileManager()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    @Before
    override public fun setUp() {

    }

    @Test
    fun beforeInteactionNoNavigationDecision() {
        val repository: CfgRepository = mock {
            on { getCfg() } doReturn fakeCfg
        }
        val viewModel = CfgViewModel(repository, fakePM)
        assertNull("should not be set initially",
                    viewModel.nextAction.value)
    }

    @Test
    fun createAccountActionResultsInNavigationToCreateAccountView() {
        val repository: CfgRepository = mock {
            on { getCfg() } doReturn fakeCfg
        }
        val viewModel = CfgViewModel(repository, fakePM)
        viewModel.onCreateAccount()
        viewModel.nextAction.observeForever { }
        assertEquals(viewModel.nextAction.value,
            CfgViewModel.NavigationFlow.CREATE_ACCOUNT)
    }

    @Test
    fun startsInBasicView() {
        val repository: CfgRepository = mock {
            on { getCfg() } doReturn fakeCfg
        }

        val viewModel = CfgViewModel(repository, fakePM)
        assertFalse(viewModel.isAdvancedMode.value!!)
    }

    @Test
    fun saveIsOneShot() {
        Mockito.`when`(mockRepository.getCfg())
            .thenReturn(fakeCfg)
        val viewModel = CfgViewModel(mockRepository, fakePM)
        assertTrue(viewModel.saveEnabled.value!!)
        viewModel.onSaveConfig()
        assertFalse(viewModel.saveEnabled.value!!)
    }

    @Test
    fun savedChangePropagatesToRepository() {
        val initialData = fakeCfg
        var storedData: CfgData? = null
        val repository: CfgRepository = mock {
            on { getCfg() } doReturn initialData
            on { storeCfg(any()) } doAnswer { storedData = it.getArgument(0) }
        }
        val viewModel = CfgViewModel(repository, fakePM)
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
        val repository: CfgRepository = mock {
            on { getCfg() } doReturn initialData
            on { storeCfg(any()) } doAnswer { storedData = it.getArgument(0) }
        }
        val viewModel = CfgViewModel(repository, fakePM)
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
    fun changingEmailAddressClearsServerDataInConfig() {
        val initialData = fakeCfg
        val repository: CfgRepository = mock {
            on { getCfg() } doReturn initialData
        }
        val vm = CfgViewModel(repository, fakePM)
        vm.emailAddress.value = "test@test.here"
        vm.serverAddrEmail.value = "test.here"
        assertEquals("test.here", vm.serverAddrEmail.value)
        assertEquals("test@test.here", vm.emailAddress.value)
        vm.onEmailChange("", 0, 0, 0)
        assertEquals("", vm.serverAddrEmail.value)
    }

    @Test
    fun disablingAutoDetectionPrefillsServerField() {
        val initialData = fakeCfg
        val repository: CfgRepository = mock {
            on { getCfg() } doReturn initialData
        }
        val vm = CfgViewModel(repository, fakePM)
        vm.emailAddress.value = "not.available@gmail.com"
        vm.serverAddrEmail.value = ""
        assertEquals("", vm.serverAddrEmail.value)
        assertEquals("not.available@gmail.com", vm.emailAddress.value)
        vm.toggleServerAutoDiscovery()
        assertEquals("gmail.com", vm.serverAddrEmail.value)
    }

    @Test
    fun defaultSettingForButtonAutohideIsTrue() {
        val initialData = fakeCfg
        val repository: CfgRepository = mock {
            on { getCfg() } doReturn initialData
        }
        val vm = CfgViewModel(repository, fakePM)
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
        val viewModel = CfgViewModel(repository, fakePM)
        val origVal = viewModel.cfgData.buttonAutohide.value!!
        val newVal = !origVal
        viewModel.setButtonAutohide(newVal)
        viewModel.onSaveConfig()
        assertEquals(newVal, storedData!!.buttonAutohide.value)
    }

    @Test
    fun `default channel height is 100 percent`() {
        val initialData = fakeCfg
        val repository: CfgRepository = mock {
            on { getCfg() } doReturn initialData
        }
        val viewModel = CfgViewModel(repository, fakePM)
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
        val viewModel = CfgViewModel(repository, fakePM)
        viewModel.setChannelHeight(ChannelHeight.HEIGHT_60)
        viewModel.onSaveConfig()
        assertEquals(ChannelHeight.HEIGHT_60, storedData!!.channelHeight.value)
    }

    @Test
    fun `setting channel height to 150`() {
        val initialData = fakeCfg
        var storedData: CfgData? = null
        val repository: CfgRepository = mock {
            on { getCfg() } doReturn initialData
            on { storeCfg(any()) } doAnswer { storedData = it.getArgument(0) }
        }
        val viewModel = CfgViewModel(repository, fakePM)
        viewModel.setChannelHeight(ChannelHeight.HEIGHT_150)
        viewModel.onSaveConfig()
        assertEquals(ChannelHeight.HEIGHT_150, storedData!!.channelHeight.value)
        
    }
}

class DummyProfileManager: ProfileManager {
    override fun getCurrentProfile(): AuthProfileItem {
        return AuthProfileItem("noname", 
                               AuthInfo(true, true),
                               false, true)
    }

    override fun updateCurrentProfile(profile: AuthProfileItem) {
        // intentionally left empty
    }

    override fun getCurrentAuthInfo(): AuthInfo {
        return getCurrentProfile().authInfo
    }

    override fun updateCurrentAuthInfo(info: AuthInfo) {
        // intentionally left empty
    }
}
