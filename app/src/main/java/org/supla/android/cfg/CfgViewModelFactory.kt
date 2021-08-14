package org.supla.android.cfg

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel

class CfgViewModelFactory(private val repository: CfgRepository): ViewModelProvider.Factory {
    override fun <T: ViewModel?> create(modelClass: Class<T>): T {
	if(modelClass.isAssignableFrom(CfgViewModel::class.java)) {
	    return CfgViewModel(repository) as T
	} else {
	    throw IllegalArgumentException("unknown view model class")
	}
    }
}
