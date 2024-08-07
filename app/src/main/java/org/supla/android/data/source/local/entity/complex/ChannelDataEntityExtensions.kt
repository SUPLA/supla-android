package org.supla.android.data.source.local.entity.complex
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

import org.supla.android.data.source.local.entity.hasMeasurements
import org.supla.android.data.source.local.entity.hasValue
import org.supla.android.data.source.local.entity.isFacadeBlind
import org.supla.android.data.source.local.entity.isGpMeasurement
import org.supla.android.data.source.local.entity.isGpMeter
import org.supla.android.data.source.local.entity.isGpm
import org.supla.android.data.source.local.entity.isHvacThermostat
import org.supla.android.data.source.local.entity.isMeasurement
import org.supla.android.data.source.local.entity.isShadingSystem
import org.supla.android.data.source.local.entity.isThermometer
import org.supla.android.data.source.local.entity.isVerticalBlind

fun ChannelDataEntity.isMeasurement() = channelEntity.isMeasurement()

fun ChannelDataEntity.isGpm() = channelEntity.isGpm()

fun ChannelDataEntity.isGpMeasurement() = channelEntity.isGpMeasurement()

fun ChannelDataEntity.isGpMeter() = channelEntity.isGpMeter()

fun ChannelDataEntity.isHvacThermostat() = channelEntity.isHvacThermostat()

fun ChannelDataEntity.hasMeasurements() = channelEntity.hasMeasurements()

fun ChannelDataEntity.hasValue() = channelEntity.hasValue()

fun ChannelDataEntity.isThermometer() = channelEntity.isThermometer()

fun ChannelDataEntity.isShadingSystem() = channelEntity.isShadingSystem()

fun ChannelDataEntity.isFacadeBlind() = channelEntity.isFacadeBlind()

fun ChannelDataEntity.isVerticalBlind() = channelEntity.isVerticalBlind()
