package org.supla.android.widget.onoff
/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import android.content.Context
import androidx.work.WorkerParameters
import org.supla.android.lib.SuplaConst
import org.supla.android.widget.shared.WidgetCommandWorkerBase


const val ARG_TURN_ON = "ARG_TURN_ON"

/**
 * Worker which is implemented for turning on/off switches. It supports following channel functions:
 * light switch [SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH],
 * dimmer [SuplaConst.SUPLA_CHANNELFNC_DIMMER],
 * RGB lightning [SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING],
 * dimmer with RGB lightning [SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING]
 * power switch [SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH]
 * It supports also opening and closing of roller shutters
 */
class OnOffWidgetCommandWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : WidgetCommandWorkerBase(appContext, workerParams)