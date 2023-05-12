package org.supla.android.usecases.details

import org.supla.android.db.Channel
import org.supla.android.db.ChannelBase
import org.supla.android.lib.SuplaChannelValue
import org.supla.android.lib.SuplaConst
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProvideDetailTypeUseCase @Inject constructor() {

  operator fun invoke(channelBase: ChannelBase): DetailType? = when (channelBase.func) {
    SuplaConst.SUPLA_CHANNELFNC_DIMMER,
    SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING,
    SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING -> DetailType.RGBW
    SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER,
    SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW -> DetailType.RS
    SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH,
    SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
    SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER -> {
      (channelBase as? Channel)?.run {
        when (value?.subValueType) {
          SuplaChannelValue.SUBV_TYPE_IC_MEASUREMENTS.toShort() -> DetailType.IC
          SuplaChannelValue.SUBV_TYPE_ELECTRICITY_MEASUREMENTS.toShort() -> DetailType.EM
          else -> null
        }
      }
    }
    SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER,
    SuplaConst.SUPLA_CHANNELFNC_IC_ELECTRICITY_METER,
    SuplaConst.SUPLA_CHANNELFNC_IC_GAS_METER,
    SuplaConst.SUPLA_CHANNELFNC_IC_WATER_METER,
    SuplaConst.SUPLA_CHANNELFNC_IC_HEAT_METER -> {
      if (channelBase.type == SuplaConst.SUPLA_CHANNELTYPE_IMPULSE_COUNTER) {
        DetailType.IC
      } else {
        DetailType.EM
      }
    }
    SuplaConst.SUPLA_CHANNELFNC_THERMOMETER -> DetailType.TEMPERATURE
    SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE -> DetailType.TEMPERATURE_HUMIDITY
    SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS -> DetailType.THERMOSTAT_HP
    SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL,
    SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL -> DetailType.DIGIGLASS
    else -> null
  }
}

enum class DetailType : Serializable {
  RGBW,
  RS,
  IC,
  EM,
  TEMPERATURE,
  TEMPERATURE_HUMIDITY,
  THERMOSTAT_HP,
  DIGIGLASS
}
