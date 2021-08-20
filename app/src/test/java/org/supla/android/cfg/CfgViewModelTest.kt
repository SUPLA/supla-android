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

@RunWith(MockitoJUnitRunner::class)
class CfgViewModelTest: TestCase() {

    private val mockRepository = Mockito.mock(CfgRepository::class.java)
    private val fakeCfg = CfgData("localhost", 0, "****", "noone@nowhere", false)

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
        val viewModel = CfgViewModel(repository)
        assertNull("should not be set initially",
                    viewModel.nextAction.value)
    }

    @Test
    fun createAccountActionResultsInNavigationToCreateAccountView() {
        val repository: CfgRepository = mock {
            on { getCfg() } doReturn fakeCfg
        }
        val viewModel = CfgViewModel(repository)
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

        val viewModel = CfgViewModel(repository)
        assertFalse(viewModel.cfgData.isAdvanced.value!!)
    }

    @Test
    fun saveIsOneShot() {
        Mockito.`when`(mockRepository.getCfg())
            .thenReturn(CfgData("localhost", 6666, "pwd", "", true))
        val viewModel = CfgViewModel(mockRepository)
        assertTrue(viewModel.saveEnabled.value!!)
        viewModel.onSaveConfig()
        assertFalse(viewModel.saveEnabled.value!!)
    }

    @Test
    fun savedChangePropagatesToRepository() {
        val initialData = CfgData("localhost", 6666, "pwd", "", true)
        var storedData: CfgData? = null
        val repository: CfgRepository = mock {
            on { getCfg() } doReturn initialData
            on { storeCfg(any()) } doAnswer { storedData = it.getArgument(0) }
        }
        val viewModel = CfgViewModel(repository)
        viewModel.cfgData.serverAddr.value = "otherhost"
        viewModel.onSaveConfig()
        assertEquals("otherhost", storedData!!.serverAddr.value)
        assertEquals(initialData.accessID.value, storedData!!.accessID.value)
        assertEquals(initialData.accessIDpwd.value, storedData!!.accessIDpwd.value)
        assertEquals(initialData.email.value, storedData!!.email.value)
    }

    @Test
    fun temperatureUnitChangePropagatesToRepository() {
        val initialData = CfgData("localhost", 6666, "pwd", "", true)
        var storedData: CfgData? = null
        val repository: CfgRepository = mock {
            on { getCfg() } doReturn initialData
            on { storeCfg(any()) } doAnswer { storedData = it.getArgument(0) }
        }
        val viewModel = CfgViewModel(repository)
        assertFalse(viewModel.isDirty.value!!)
        viewModel.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
        assertTrue(viewModel.isDirty.value!!)
        viewModel.onSaveConfig()
        assertEquals(initialData.serverAddr, storedData!!.serverAddr)
        assertEquals(initialData.accessID, storedData!!.accessID)
        assertEquals(initialData.accessIDpwd, storedData!!.accessIDpwd)
        assertEquals(initialData.email, storedData!!.email)
        assertEquals(TemperatureUnit.FAHRENHEIT, storedData!!.temperatureUnit.value)

    }

    @Test
    fun changingEmailAddressClearsServerDataInConfig() {
        val initialData = CfgData("localhost", 6666, "pwd",
            "whatever@email.com", true)
        val repository: CfgRepository = mock {
            on { getCfg() } doReturn initialData
        }
        val vm = CfgViewModel(repository)
        assertEquals("localhost", vm.cfgData.serverAddr.value)
        assertEquals(6666, vm.cfgData.accessID.value)
        assertEquals("pwd", vm.cfgData.accessIDpwd.value)
        vm.onEmailChange("", 0, 0, 0)
        assertEquals("", vm.cfgData.serverAddr.value)
        assertEquals(0, vm.cfgData.accessID.value)
        assertEquals("", vm.cfgData.accessIDpwd.value)
    }

    @Test
    fun defaultSettingForButtonAutohideIsTrue() {
        val initialData = CfgData("localhost", 6666, "pwd",
                                  "whatever@email.com", false)
        val repository: CfgRepository = mock {
            on { getCfg() } doReturn initialData
        }
        val vm = CfgViewModel(repository)
        assertEquals(true, vm.cfgData.buttonAutohide.value)
    }
    
    @Test
    fun buttonAutohidePropagatesToRepository() {
        val initialData = CfgData("localhost", 6666, "pwd",
                                  "dont@ca.re", false)
        var storedData: CfgData? = null
        val repository: CfgRepository = mock {
            on { getCfg() } doReturn initialData
            on { storeCfg(any()) } doAnswer { storedData = it.getArgument(0) }
        }
        val viewModel = CfgViewModel(repository)
        val origVal = viewModel.cfgData.buttonAutohide.value!!
        val newVal = !origVal
        viewModel.cfgData.buttonAutohide.value = newVal
        viewModel.onSaveConfig()
        assertEquals(newVal, storedData!!.buttonAutohide.value)
    }
}
