package org.supla.android.cfg

import org.supla.android.db.AuthProfileItem

class EditableProfileItemViewModel(private val item: AuthProfileItem):
    ProfileItemViewModel(item.name, item.isActive) {

    interface EditActionHandler {
        fun onEditProfile(profileId: Long)
    }

    var editActionHandler: EditActionHandler? = null

    fun onEditProfile() {
        editActionHandler?.onEditProfile(item.id)
    }
}
