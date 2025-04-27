package org.supla.core.shared.data.model.channel
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

import org.supla.core.shared.data.model.function.container.ContainerValue
import org.supla.core.shared.data.model.function.facadeblind.FacadeBlindValue
import org.supla.core.shared.data.model.function.relay.RelayValue
import org.supla.core.shared.data.model.function.rollershutter.RollerShutterValue
import org.supla.core.shared.data.model.function.thermostat.ThermostatValue
import org.supla.core.shared.data.model.general.Channel
import org.supla.core.shared.data.model.valve.ValveValue

val Channel.facadeBlindValue: FacadeBlindValue?
  get() = value?.let { FacadeBlindValue.from(status, it) }

val Channel.rollerShutterValue: RollerShutterValue?
  get() = value?.let { RollerShutterValue.from(status, it) }

val Channel.thermostatValue: ThermostatValue?
  get() = value?.let { ThermostatValue.from(status, it) }

val Channel.containerValue: ContainerValue?
  get() = value?.let { ContainerValue.from(status, it) }

val Channel.valveValue: ValveValue?
  get() = value?.let { ValveValue.from(status, it) }

val Channel.relayValue: RelayValue?
  get() = value?.let { RelayValue.from(status, it) }
