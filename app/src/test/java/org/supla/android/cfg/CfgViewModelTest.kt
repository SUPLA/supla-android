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

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    @Before
    override public fun setUp() {

    }

    @Test
    fun beforeInteactionNoNavigationDecision() {
        val viewModel = CfgViewModel(mockRepository)
        assertNull("should not be set initially",
                    viewModel.nextAction.value)
    }

    @Test
    fun createAccountActionResultsInNavigationToCreateAccountView() {
        val viewModel = CfgViewModel(mockRepository)
        viewModel.onCreateAccount()
        viewModel.nextAction.observeForever { }
        assertEquals(viewModel.nextAction.value,
            CfgViewModel.NavigationFlow.CREATE_ACCOUNT)
    }

    @Test
    fun startsInBasicView() {
        val viewModel = CfgViewModel(mockRepository)
        assertFalse(viewModel.advanced.value!!)
    }

    @Test
    fun saveIsOneShot() {
        Mockito.`when`(mockRepository.getCfg())
            .thenReturn(CfgData("localhost", 6666, "pwd", ""))
        val viewModel = CfgViewModel(mockRepository)
        assertTrue(viewModel.saveEnabled.value!!)
        viewModel.onSaveConfig()
        assertFalse(viewModel.saveEnabled.value!!)
    }

    @Test
    fun savedChangePropagatesToRepository() {
        val initialData = CfgData("localhost", 6666, "pwd", "")
        var storedData: CfgData? = null
        val repository: CfgRepository = mock {
            on { getCfg() } doReturn initialData
            on { storeCfg(any()) } doAnswer { storedData = it.getArgument(0) }
        }
        val viewModel = CfgViewModel(repository)
        viewModel.cfgData.serverAddr = "otherhost"
        viewModel.onSaveConfig()
        assertEquals("otherhost", storedData!!.serverAddr)
        assertEquals(initialData.accessID, storedData!!.accessID)
        assertEquals(initialData.accessIDpwd, storedData!!.accessIDpwd)
        assertEquals(initialData.email, storedData!!.email)
    }

    @Test
    fun temperatureUnitChangePropagatesToRepository() {
        val initialData = CfgData("localhost", 6666, "pwd", "")
        var storedData: CfgData? = null
        val repository: CfgRepository = mock {
            on { getCfg() } doReturn initialData
            on { storeCfg(any()) } doAnswer { storedData = it.getArgument(0) }
        }
        val viewModel = CfgViewModel(repository)
        assertFalse(viewModel.cfgData.isDirty.value!!)
        viewModel.cfgData.temperatureUnit = TemperatureUnit.FAHRENHEIT
        assertTrue(viewModel.cfgData.isDirty.value!!)
        viewModel.onSaveConfig()
        assertEquals(initialData.serverAddr, storedData!!.serverAddr)
        assertEquals(initialData.accessID, storedData!!.accessID)
        assertEquals(initialData.accessIDpwd, storedData!!.accessIDpwd)
        assertEquals(initialData.email, storedData!!.email)
        assertEquals(TemperatureUnit.FAHRENHEIT, storedData!!.temperatureUnit)

    }

}