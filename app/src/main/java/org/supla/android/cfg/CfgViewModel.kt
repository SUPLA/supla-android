package org.supla.android.cfg

import androidx.lifecycle.ViewModel
import kotlin.properties.Delegates
import androidx.lifecycle.MutableLiveData

class CfgViewModel(private val repository: CfgRepository): ViewModel() {

    val advanced = MutableLiveData<Boolean>(false)
    val cfgData: CfgData = repository.getCfg()
}
