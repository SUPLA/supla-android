package org.supla.android.extensions

import org.supla.android.db.Channel
import org.supla.android.db.ChannelBase
import org.supla.android.lib.SuplaConst
import org.supla.android.lib.SuplaTimerState

fun Channel.getTimerStateValue(): SuplaTimerState? = extendedValue?.extendedValue?.TimerStateValue

internal fun ChannelBase.hasSwitchDetail() =
  func == SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH ||
    func == SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH ||
    func == SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER
