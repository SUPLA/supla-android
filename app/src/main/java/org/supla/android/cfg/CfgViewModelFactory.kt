package org.supla.android.cfg

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel

import org.supla.android.profile.ProfileManager

class CfgViewModelFactory(private val repository: CfgRepository,
                          private val profileManager: ProfileManager): ViewModelProvider.Factory {
    override fun <T: ViewModel?> create(modelClass: Class<T>): T {
	if(modelClass.isAssignableFrom(CfgViewModel::class.java)) {
	    return CfgViewModel(repository, profileManager) as T
	} else {
	    throw IllegalArgumentException("unknown view model class")
	}
    }
}
