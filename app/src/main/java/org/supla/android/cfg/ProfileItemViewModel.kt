package org.supla.android.cfg

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable

class ProfileItemViewModel(val t: String): BaseObservable() {
    @Bindable var title: String? = ""
}
