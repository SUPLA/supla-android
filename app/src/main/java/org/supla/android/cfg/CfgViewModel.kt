package org.supla.android.cfg

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData

class CfgViewModel(private val repository: CfgRepository): ViewModel() {

    enum class NavigationFlow { CREATE_ACCOUNT, STATUS, MAIN }

    private val _didSaveConfig = MutableLiveData<Boolean>(false)
    val didSaveConfig: LiveData<Boolean> get() = _didSaveConfig
    val advanced = MutableLiveData<Boolean>(false)
    val saveEnabled = MutableLiveData<Boolean>(true)
    val nextAction = MutableLiveData<NavigationFlow?>()
    val cfgData: CfgData = repository.getCfg()

    fun onCreateAccount() {
        android.util.Log.i("SUPLA", "will create account")
        nextAction.value = NavigationFlow.CREATE_ACCOUNT
    }

    fun onSaveConfig() {
        android.util.Log.i("SUPLA", "save config")
        saveEnabled.value = false
        if(cfgData.isDirty.value == true) {
            repository.storeCfg(cfgData)
            nextAction.value = NavigationFlow.STATUS
            _didSaveConfig.value = true
        }
    }
}
