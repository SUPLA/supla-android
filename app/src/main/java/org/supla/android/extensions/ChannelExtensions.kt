package org.supla.android.extensions

import org.supla.android.db.Channel
import org.supla.android.lib.SuplaTimerState

fun Channel.getTimerStateValue(): SuplaTimerState? = extendedValue?.extendedValue?.TimerStateValue
