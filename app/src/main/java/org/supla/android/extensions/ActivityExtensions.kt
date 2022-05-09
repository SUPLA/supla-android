package org.supla.android.extensions

import android.app.Activity
import org.supla.android.SuplaApp
import org.supla.android.profile.ProfileManager

/**
 * Temporary extensions to get profile manager from the application. Should be removed when dependency injection is introduced.
 */
fun Activity.getProfileManager(): ProfileManager {
    return (application as SuplaApp).getProfileManager(applicationContext)
}