package org.supla.android.cfg

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfilesViewModel: ViewModel() {
    val activeProfile = MutableLiveData<String>("")

    enum class State { IDLE, NEW_PROFILE_INPUT }
    private val _state = MutableLiveData<State>(State.IDLE)
    val state: LiveData<State> = _state
    var profileName: String = ""

    fun onNewProfile() {
        profileName = ""
        _state.value = State.NEW_PROFILE_INPUT
    }
}
