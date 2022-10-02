package org.supla.android.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoroutineDispatchers @Inject constructor() {

  fun io(): CoroutineDispatcher = Dispatchers.IO

  fun main(): CoroutineDispatcher = Dispatchers.Main
}
