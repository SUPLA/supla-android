package org.supla.android.extensions

import androidx.work.Worker
import dagger.hilt.android.EntryPointAccessors
import org.supla.android.di.ProfileManagerEntryPoint
import org.supla.android.di.SingleCallProviderEntryPoint
import org.supla.android.lib.singlecall.SingleCall
import org.supla.android.profile.ProfileManager

fun Worker.getProfileManager(): ProfileManager =
  EntryPointAccessors.fromApplication(
    applicationContext,
    ProfileManagerEntryPoint::class.java
  ).provideProfileManager()

fun Worker.getSingleCallProvider(): SingleCall.Provider =
  EntryPointAccessors.fromApplication(
    applicationContext,
    SingleCallProviderEntryPoint::class.java
  ).provideSingleCallProvider()
