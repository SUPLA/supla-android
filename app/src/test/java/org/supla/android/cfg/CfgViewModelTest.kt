package org.supla.android.cfg

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

}