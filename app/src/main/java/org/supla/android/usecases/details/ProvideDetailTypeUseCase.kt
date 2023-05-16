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
    SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING ->
      LegacyDetailType.RGBW
    SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER,
    SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW ->
      LegacyDetailType.RS
    SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH,
    SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
    SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER -> {
      (channelBase as? Channel)?.run {
        when (value?.subValueType) {
          SuplaChannelValue.SUBV_TYPE_IC_MEASUREMENTS.toShort() -> LegacyDetailType.IC
          SuplaChannelValue.SUBV_TYPE_ELECTRICITY_MEASUREMENTS.toShort() -> LegacyDetailType.EM
          else -> StandardDetailType.SWITCH
        }
      }
    }
    SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER ->
      LegacyDetailType.EM
    SuplaConst.SUPLA_CHANNELFNC_IC_ELECTRICITY_METER,
    SuplaConst.SUPLA_CHANNELFNC_IC_GAS_METER,
    SuplaConst.SUPLA_CHANNELFNC_IC_WATER_METER,
    SuplaConst.SUPLA_CHANNELFNC_IC_HEAT_METER ->
      LegacyDetailType.IC
    SuplaConst.SUPLA_CHANNELFNC_THERMOMETER ->
      LegacyDetailType.TEMPERATURE
    SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE ->
      LegacyDetailType.TEMPERATURE_HUMIDITY
    SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS ->
      LegacyDetailType.THERMOSTAT_HP
    SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL,
    SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL ->
      LegacyDetailType.DIGIGLASS
    else -> null
  }
}

sealed interface DetailType: Serializable

enum class LegacyDetailType : DetailType {
  RGBW,
  RS,
  IC,
  EM,
  TEMPERATURE,
  TEMPERATURE_HUMIDITY,
  THERMOSTAT_HP,
  DIGIGLASS
}

enum class StandardDetailType: DetailType {
  SWITCH
}