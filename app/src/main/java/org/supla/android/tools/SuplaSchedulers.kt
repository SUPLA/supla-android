package org.supla.android.tools

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuplaSchedulers @Inject constructor() {
  val io: Scheduler
    get() = Schedulers.io()
  val ui: Scheduler
    get() = AndroidSchedulers.mainThread()
  val computation: Scheduler
    get() = Schedulers.computation()
}
