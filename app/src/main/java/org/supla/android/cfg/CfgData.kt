package org.supla.android.cfg

import androidx.lifecycle.MutableLiveData
import kotlin.properties.Delegates


data class CfgData(private var _serverAddr: String,
                   private var _accessID: Int,
                   private var _accessIDpwd: String,
                   private var _email: String) {
    val isDirty = MutableLiveData<Boolean>(false)

    var serverAddr: String by Delegates.observable(_serverAddr) { p, o, n ->
        if (o != n) {
            _serverAddr = n
            isDirty.value = true
        }
    }

    var accessID: Int by Delegates.observable(_accessID) { p, o, n ->
        if (o != n) {
            _accessID = n
            isDirty.value = true
        }
    }

    var accessIDpwd: String by Delegates.observable(_accessIDpwd) { p, o, n ->
        if (o != n) {
            _accessIDpwd = n
            isDirty.value = true
        }
    }

    var email: String by Delegates.observable(_email) {p,o,n ->
        if(o != n) {
            _email = n
            isDirty.value = true
        }
    }
}
