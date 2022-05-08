package org.supla.android.extensions

import androidx.work.Worker
import dagger.hilt.android.EntryPointAccessors
import org.supla.android.di.ProfileManagerEntryPoint
import org.supla.android.di.WidgetVisibilityHandlerEntryPoint
import org.supla.android.profile.ProfileManager
import org.supla.android.widget.WidgetVisibilityHandler

fun Worker.getProfileManager(): ProfileManager =
        EntryPointAccessors.fromApplication(
                applicationContext,
                ProfileManagerEntryPoint::class.java
        ).provideProfileManager()
