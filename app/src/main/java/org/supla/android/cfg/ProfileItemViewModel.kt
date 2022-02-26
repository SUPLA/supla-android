package org.supla.android.cfg

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable

class ProfileItemViewModel(val t: String, val isActive: Boolean): BaseObservable() {
    @Bindable var title: String? = t
    @Bindable var active: Boolean? = isActive
}
