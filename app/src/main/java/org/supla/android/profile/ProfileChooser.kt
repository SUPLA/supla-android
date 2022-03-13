package org.supla.android.profile

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

import org.supla.android.R
import org.supla.android.db.AuthProfileItem


class ProfileChooser(private val context: Context,
                     private val profileManager: ProfileManager) {

    private val profiles: Array<AuthProfileItem>
    interface Listener {
        fun onProfileChanged()
    }

    var listener: Listener? = null

    init {
        profiles = profileManager.getAllProfiles().toTypedArray()
    }

    fun show() {
        val builder = AlertDialog.Builder(context)
        val items: Array<String> = profiles.mapIndexed {
            index, itm -> if(itm.isActive) itm.name + " ✓" else itm.name
        }.toTypedArray()
        with(builder) {

            setTitle(R.string.profile_select_active)
            setCancelable(true)
            setItems(items) { dlg,which -> selectProfile(which) }
            show()
        }
    }

    private fun selectProfile(idx: Int) {
        if(profileManager.activateProfile(profiles[idx].id)) {
            listener?.onProfileChanged()
        }
    }
}
