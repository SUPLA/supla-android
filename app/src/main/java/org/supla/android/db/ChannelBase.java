package org.supla.android.db;

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

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import org.supla.android.R;
import org.supla.android.SuplaApp;
import org.supla.android.ValuesFormatterProvider;
import org.supla.android.data.ValuesFormatter;
import org.supla.android.images.ImageCache;
import org.supla.android.images.ImageId;
import org.supla.android.lib.SuplaChannelBase;
import org.supla.android.lib.SuplaConst;

public abstract class ChannelBase extends DbItem {

  private int RemoteId; // SuplaChannelBase.Id
  private String Caption;
  private int Func;
  private int Visible;
  private long LocationId;
  private int AltIcon;
  private int UserIconId;
  private int Flags;
  private long profileId;

  private ValuesFormatterProvider valuesFormatterProvider;

  public ChannelBase() {
    profileId = -1;
    valuesFormatterProvider = SuplaApp.getApp();
  }

  public ChannelBase(ValuesFormatterProvider tpFact) {
    profileId = -1;
    valuesFormatterProvider = tpFact;
  }

  @SuppressLint("DefaultLocale")
  public CharSequence getHumanReadableThermostatTemperature(
      Double measuredTempFrom,
      Double measuredTempTo,
      Double presetTempFrom,
      Double presetTempTo,
      float measuredRelativeSize,
      float presetdRelativeSize) {

    if (measuredTempFrom != null && measuredTempTo != null && measuredTempFrom > measuredTempTo) {
      Double f = measuredTempFrom;
      measuredTempFrom = measuredTempTo;
      measuredTempTo = f;
    }

    if (presetTempFrom != null && presetTempTo != null && presetTempFrom > presetTempTo) {
      Double f = presetTempFrom;
      presetTempFrom = presetTempTo;
      presetTempTo = f;
    }

    String measured;
    String preset;
    ValuesFormatter tp = getTemperaturePresenter();

    measured = tp.getTemperatureString(measuredTempFrom, true, true);
    if (tp.isTemperatureDefined(measuredTempTo)) {
      measured += " - " + tp.getTemperatureString(measuredTempTo, true, true);
    }

    preset = "/" + tp.getTemperatureString(presetTempFrom, true, true);
    if (tp.isTemperatureDefined(presetTempTo)) {
      preset += " - " + tp.getTemperatureString(presetTempTo, true, true);
    }

    SpannableString ss = new SpannableString(measured + preset);
    ss.setSpan(new RelativeSizeSpan(measuredRelativeSize), 0, measured.length(), 0);

    ss.setSpan(
        new RelativeSizeSpan(presetdRelativeSize),
        measured.length(),
        measured.length() + preset.length(),
        0);

    return ss;
  }

  @SuppressLint("DefaultLocale")
  public CharSequence getHumanReadableThermostatTemperature(
      Double measuredTempFrom, Double measuredTempTo, Double presetTempFrom, Double presetTempTo) {

    return getHumanReadableThermostatTemperature(
        measuredTempFrom, measuredTempTo, presetTempFrom, presetTempTo, 1.0f, 0.7f);
  }

  @SuppressLint("DefaultLocale")
  public CharSequence getHumanReadableThermostatTemperature(
      Double measuredTemp, Double presetTemp) {
    return getHumanReadableThermostatTemperature(measuredTemp, null, presetTemp, null);
  }

  public int getRemoteId() {
    return RemoteId;
  }

  public void setRemoteId(int id) {
    RemoteId = id;
  }

  public String getCaption() {
    return Caption;
  }

  public void setCaption(String caption) {
    Caption = caption;
  }

  public int getVisible() {
    return Visible;
  }

  public void setVisible(int visible) {
    Visible = visible;
  }

  public long getLocationId() {
    return LocationId;
  }

  public void setLocationId(long locationId) {
    LocationId = locationId;
  }

  public int getType() {
    return 0;
  }

  public int getFunc() {
    return Func;
  }

  public void setFunc(int func) {
    Func = func;
  }

  protected abstract int _getOnLine();

  public int getOnLinePercent() {
    int result = _getOnLine();
    if (result > 100) {
      result = 100;
    } else if (result < 0) {
      result = 0;
    }
    return result;
  }

  public boolean getOnLine() {
    return getOnLinePercent() > 0;
  }

  public int getAltIcon() {
    return AltIcon;
  }

  public void setAltIcon(int altIcon) {
    AltIcon = altIcon;
  }

  public int getUserIconId() {
    return UserIconId;
  }

  public void setUserIconId(int userIconId) {
    UserIconId = userIconId;
  }

  public int getFlags() {
    return Flags;
  }

  public void setFlags(int flags) {
    Flags = flags;
  }

  public long getProfileId() {
    return profileId;
  }

  public void setProfileId(long pid) {
    profileId = pid;
  }

  public String getNotEmptyCaption(Context context) {
    return SuplaConst.getNotEmptyCaption(getCaption(), getFunc(), context);
  }

  protected int imgActive(ChannelValue value) {

    if (value == null || !getOnLine()) {
      return 0;
    }

    switch (getFunc()) {
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW:
        return value.getSubValueHi();

      case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY:
      case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATE:
      case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GARAGEDOOR:
      case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_DOOR:
      case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_ROLLERSHUTTER:
      case SuplaConst.SUPLA_CHANNELFNC_MAILSENSOR:
      case SuplaConst.SUPLA_CHANNELFNC_OPENINGSENSOR_WINDOW:
      case SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH:
      case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:
      case SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER:
      case SuplaConst.SUPLA_CHANNELFNC_NOLIQUIDSENSOR:
      case SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS:
        return value.hiValue() ? 1 : 0;

      case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
        return value.getBrightness() > 0 ? 1 : 0;

      case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
        return value.getColorBrightness() > 0 ? 1 : 0;

      case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
        {
          int result = 0;

          if (value.getBrightness() > 0) {
            result |= 0x1;
          }

          if (value.getColorBrightness() > 0) {
            result |= 0x2;
          }

          return result;
        }
      case SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE:
        return value.isClosed() ? 1 : 0;
      case SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL:
      case SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL:
        return value.getDigiglassValue().isAnySectionTransparent() ? 1 : 0;
    }

    return 0;
  }

  // We intentionally specify icons with the _nighthtmode
  // suffix for night mode instead of using the default icons
  // from the drawable-night directory because not every
  // part of the application is night mode enabled yet.
  private int getImageResourceIdForNightMode(WhichOne whichImage, int active, boolean _50percent) {
    int img_idx = -1;

    switch (getFunc()) {
      case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK:
        img_idx =
            active == 1 ? R.drawable.gatewayclosed_nightmode : R.drawable.gatewayopen_nightmode;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATE:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
        switch (getAltIcon()) {
          case 1:
            if (_50percent) {
              img_idx = R.drawable.gatealt1closed50percent_nightmode;
            } else {
              img_idx =
                  active > 0
                      ? R.drawable.gatealt1closed_nightmode
                      : R.drawable.gatealt1open_nightmode;
            }
            break;
          case 2:
            img_idx =
                active > 0 ? R.drawable.barierclosed_nightmode : R.drawable.barieropen_nightmode;
            break;
          default:
            if (_50percent) {
              img_idx = R.drawable.gateclosed50percent_nightmode;
            } else {
              img_idx =
                  active > 0 ? R.drawable.gateclosed_nightmode : R.drawable.gateopen_nightmode;
            }
        }
        break;
      case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GARAGEDOOR:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:
        if (_50percent) {
          img_idx = R.drawable.garagedoorclosed50percent_nightmode;
        } else {
          img_idx =
              active > 0
                  ? R.drawable.garagedoorclosed_nightmode
                  : R.drawable.garagedooropen_nightmode;
        }
        break;
      case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_DOOR:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK:
        img_idx = active == 1 ? R.drawable.doorclosed_nightmode : R.drawable.dooropen_nightmode;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_ROLLERSHUTTER:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:
        img_idx =
            active == 1
                ? R.drawable.rollershutterclosed_nightmode
                : R.drawable.rollershutteropen_nightmode;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_ROOFWINDOW:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW:
        img_idx =
            active == 1
                ? R.drawable.roofwindowclosed_nightmode
                : R.drawable.roofwindowopen_nightmode;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH:
        switch (getAltIcon()) {
          case 1:
            img_idx = active == 1 ? R.drawable.tvon_nightmode : R.drawable.tvoff_nightmode;
            break;
          case 2:
            img_idx = active == 1 ? R.drawable.radioon_nightmode : R.drawable.radiooff_nightmode;
            break;
          case 3:
            img_idx = active == 1 ? R.drawable.pcon_nightmode : R.drawable.pcoff_nightmode;
            break;
          case 4:
            img_idx = active == 1 ? R.drawable.fanon_nightmode : R.drawable.fanoff_nightmode;
            break;
          default:
            img_idx = active == 1 ? R.drawable.poweron_nightmode : R.drawable.poweroff_nightmode;
        }

        break;
      case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:
        switch (getAltIcon()) {
          case 1:
            img_idx =
                active == 1 ? R.drawable.xmastreeon_nightmode : R.drawable.xmastreeoff_nightmode;
            break;
          case 2:
            img_idx = active == 1 ? R.drawable.uvon_nightmode : R.drawable.uvoff_nightmode;
            break;
          default:
            img_idx = active == 1 ? R.drawable.lighton_nightmode : R.drawable.lightoff_nightmode;
        }

        break;
      case SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER:
        switch (getAltIcon()) {
          case 1:
            img_idx =
                active == 1
                    ? R.drawable.staircasetimeron_1_nightmode
                    : R.drawable.staircasetimeroff_1_nightmode;
            break;
          default:
            img_idx =
                active == 1
                    ? R.drawable.staircasetimeron_nightmode
                    : R.drawable.staircasetimeroff_nightmode;
        }

        break;
      case SuplaConst.SUPLA_CHANNELFNC_THERMOMETER:
        img_idx = R.drawable.thermometer_nightmode;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_HUMIDITY:
        img_idx = R.drawable.humidity_nightmode;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE:
        img_idx =
            whichImage == WhichOne.First
                ? R.drawable.thermometer_nightmode
                : R.drawable.humidity_nightmode;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_WINDSENSOR:
        img_idx = R.drawable.wind_nightmode;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_PRESSURESENSOR:
        img_idx = R.drawable.pressure_nightmode;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_RAINSENSOR:
        img_idx = R.drawable.rain_nightmode;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_WEIGHTSENSOR:
        img_idx = R.drawable.weight_nightmode;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_NOLIQUIDSENSOR:
        img_idx = active == 1 ? R.drawable.liquid_nightmode : R.drawable.noliquid_nightmode;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
        img_idx = active == 1 ? R.drawable.dimmeron_nightmode : R.drawable.dimmeroff_nightmode;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
        img_idx = active == 1 ? R.drawable.rgbon_nightmode : R.drawable.rgboff_nightmode;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
        switch (active) {
          case 0:
            img_idx = R.drawable.dimmerrgboffoff_nightmode;
            break;
          case 1:
            img_idx = R.drawable.dimmerrgbonoff_nightmode;
            break;
          case 2:
            img_idx = R.drawable.dimmerrgboffon_nightmode;
            break;
          case 3:
            img_idx = R.drawable.dimmerrgbonon_nightmode;
            break;
        }

        break;

      case SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR:
        img_idx = R.drawable.depthsensor_nightmode;
        break;

      case SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR:
        img_idx = R.drawable.distancesensor_nightmode;
        break;

      case SuplaConst.SUPLA_CHANNELFNC_OPENINGSENSOR_WINDOW:
        img_idx = active == 1 ? R.drawable.windowclosed_nightmode : R.drawable.windowopen_nightmode;
        break;

      case SuplaConst.SUPLA_CHANNELFNC_MAILSENSOR:
        img_idx = active == 1 ? R.drawable.mail_nightmode : R.drawable.nomail_nightmode;
        break;

      case SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER:
      case SuplaConst.SUPLA_CHANNELFNC_IC_ELECTRICITY_METER:
        switch (getAltIcon()) {
          case 1:
            img_idx = R.drawable.powerstation_nightmode;
            break;
          default:
            img_idx = R.drawable.electricitymeter_nightmode;
        }
        break;

      case SuplaConst.SUPLA_CHANNELFNC_IC_GAS_METER:
        img_idx = R.drawable.gasmeter_nightmode;
        break;

      case SuplaConst.SUPLA_CHANNELFNC_IC_WATER_METER:
        img_idx = R.drawable.watermeter_nightmode;
        break;

      case SuplaConst.SUPLA_CHANNELFNC_IC_HEAT_METER:
        img_idx = R.drawable.heatmeter_nightmode;
        break;

      case SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT:
        switch (getAltIcon()) {
          case 1:
            img_idx =
                active == 1
                    ? R.drawable.thermostaton_1_nightmode
                    : R.drawable.thermostatoff_1_nightmode;
            break;
          case 2:
            img_idx =
                active == 1
                    ? R.drawable.thermostaton_2_nightmode
                    : R.drawable.thermostatoff_2_nightmode;
            break;
          case 3:
            img_idx =
                active == 1
                    ? R.drawable.thermostaton_3_nightmode
                    : R.drawable.thermostatoff_3_nightmode;
            break;
          default:
            img_idx =
                active == 1
                    ? R.drawable.thermostaton_nightmode
                    : R.drawable.thermostatoff_nightmode;
        }

        break;

      case SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS:
        switch (getAltIcon()) {
          case 1:
            img_idx =
                active == 1
                    ? R.drawable.thermostat_hp_homepluson_1_nightmode
                    : R.drawable.thermostat_hp_homeplusoff_1_nightmode;
            break;
          case 2:
            img_idx =
                active == 1
                    ? R.drawable.thermostat_hp_homepluson_2_nightmode
                    : R.drawable.thermostat_hp_homeplusoff_2_nightmode;
            break;
          case 3:
            img_idx =
                active == 1
                    ? R.drawable.thermostat_hp_homepluson_3_nightmode
                    : R.drawable.thermostat_hp_homeplusoff_3_nightmode;
            break;
          default:
            img_idx =
                active == 1
                    ? R.drawable.thermostat_hp_homepluson_nightmode
                    : R.drawable.thermostat_hp_homeplusoff_nightmode;
        }

        break;
      case SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE:
        img_idx = active == 1 ? R.drawable.valveclosed_nightmode : R.drawable.valveopen_nightmode;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL:
        switch (getAltIcon()) {
          case 1:
            img_idx =
                active == 1
                    ? R.drawable.digiglasstransparent1_nightmode
                    : R.drawable.digiglass1_nightmode;
            break;
          default:
            img_idx =
                active == 1
                    ? R.drawable.digiglasstransparent_nightmode
                    : R.drawable.digiglass_nightmode;
        }
        break;
      case SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL:
        switch (getAltIcon()) {
          case 1:
            img_idx =
                active == 1
                    ? R.drawable.digiglassvtransparent1_nightmode
                    : R.drawable.digiglass1_nightmode;
            break;
          default:
            img_idx =
                active == 1
                    ? R.drawable.digiglassvtransparent_nightmode
                    : R.drawable.digiglass_nightmode;
        }
        break;
    }

    return img_idx;
  }

  private int getImageResourceId(WhichOne whichImage, int active, boolean _50percent) {
    int img_idx = -1;

    switch (getFunc()) {
      case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATEWAY:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK:
        img_idx = active == 1 ? R.drawable.gatewayclosed : R.drawable.gatewayopen;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GATE:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
        switch (getAltIcon()) {
          case 1:
            if (_50percent) {
              img_idx = R.drawable.gatealt1closed50percent;
            } else {
              img_idx = active > 0 ? R.drawable.gatealt1closed : R.drawable.gatealt1open;
            }

            break;
          case 2:
            img_idx = active > 0 ? R.drawable.barierclosed : R.drawable.barieropen;
            break;
          default:
            if (_50percent) {
              img_idx = R.drawable.gateclosed50percent;
            } else {
              img_idx = active > 0 ? R.drawable.gateclosed : R.drawable.gateopen;
            }
        }
        break;
      case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GARAGEDOOR:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:
        if (_50percent) {
          img_idx = R.drawable.garagedoorclosed50percent;
        } else {
          img_idx = active > 0 ? R.drawable.garagedoorclosed : R.drawable.garagedooropen;
        }
        break;
      case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_DOOR:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK:
        img_idx = active == 1 ? R.drawable.doorclosed : R.drawable.dooropen;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_ROLLERSHUTTER:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER:
        img_idx = active == 1 ? R.drawable.rollershutterclosed : R.drawable.rollershutteropen;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_ROOFWINDOW:
      case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW:
        img_idx = active == 1 ? R.drawable.roofwindowclosed : R.drawable.roofwindowopen;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH:
        switch (getAltIcon()) {
          case 1:
            img_idx = active == 1 ? R.drawable.tvon : R.drawable.tvoff;
            break;
          case 2:
            img_idx = active == 1 ? R.drawable.radioon : R.drawable.radiooff;
            break;
          case 3:
            img_idx = active == 1 ? R.drawable.pcon : R.drawable.pcoff;
            break;
          case 4:
            img_idx = active == 1 ? R.drawable.fanon : R.drawable.fanoff;
            break;
          default:
            img_idx = active == 1 ? R.drawable.poweron : R.drawable.poweroff;
        }

        break;
      case SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH:
        switch (getAltIcon()) {
          case 1:
            img_idx = active == 1 ? R.drawable.xmastreeon : R.drawable.xmastreeoff;
            break;
          case 2:
            img_idx = active == 1 ? R.drawable.uvon : R.drawable.uvoff;
            break;
          default:
            img_idx = active == 1 ? R.drawable.lighton : R.drawable.lightoff;
        }

        break;
      case SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER:
        switch (getAltIcon()) {
          case 1:
            img_idx = active == 1 ? R.drawable.staircasetimeron_1 : R.drawable.staircasetimeroff_1;
            break;
          default:
            img_idx = active == 1 ? R.drawable.staircasetimeron : R.drawable.staircasetimeroff;
        }

        break;
      case SuplaConst.SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER:
        img_idx = R.drawable.ic_thermostat_dhw;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT:
        img_idx = R.drawable.ic_thermostat_heat;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_COOL:
        img_idx = R.drawable.ic_thermostat_cool;
        break;
        //      case SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO:
        //        img_idx = R.drawable.ic_thermostat_auto;
        //        break;
        //      case SuplaConst.SUPLA_CHANNELFNC_HVAC_DRYER:
        //      case SuplaConst.SUPLA_CHANNELFNC_HVAC_FAN:
        //      case SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_DIFFERENTIAL:
      case SuplaConst.SUPLA_CHANNELFNC_THERMOMETER:
        img_idx = R.drawable.thermometer;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_HUMIDITY:
        img_idx = R.drawable.humidity;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE:
        img_idx = whichImage == WhichOne.First ? R.drawable.thermometer : R.drawable.humidity;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_WINDSENSOR:
        img_idx = R.drawable.wind;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_PRESSURESENSOR:
        img_idx = R.drawable.pressure;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_RAINSENSOR:
        img_idx = R.drawable.rain;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_WEIGHTSENSOR:
        img_idx = R.drawable.weight;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_NOLIQUIDSENSOR:
        img_idx = active == 1 ? R.drawable.liquid : R.drawable.noliquid;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
        img_idx = active == 1 ? R.drawable.dimmeron : R.drawable.dimmeroff;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
        img_idx = active == 1 ? R.drawable.rgbon : R.drawable.rgboff;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
        switch (active) {
          case 0:
            img_idx = R.drawable.dimmerrgboffoff;
            break;
          case 1:
            img_idx = R.drawable.dimmerrgbonoff;
            break;
          case 2:
            img_idx = R.drawable.dimmerrgboffon;
            break;
          case 3:
            img_idx = R.drawable.dimmerrgbonon;
            break;
        }

        break;

      case SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR:
        img_idx = R.drawable.depthsensor;
        break;

      case SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR:
        img_idx = R.drawable.distancesensor;
        break;

      case SuplaConst.SUPLA_CHANNELFNC_OPENINGSENSOR_WINDOW:
        img_idx = active == 1 ? R.drawable.windowclosed : R.drawable.windowopen;
        break;

      case SuplaConst.SUPLA_CHANNELFNC_MAILSENSOR:
        img_idx = active == 1 ? R.drawable.mail : R.drawable.nomail;
        break;

      case SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER:
      case SuplaConst.SUPLA_CHANNELFNC_IC_ELECTRICITY_METER:
        switch (getAltIcon()) {
          case 1:
            img_idx = R.drawable.powerstation;
            break;
          default:
            img_idx = R.drawable.electricitymeter;
        }
        break;

      case SuplaConst.SUPLA_CHANNELFNC_IC_GAS_METER:
        img_idx = R.drawable.gasmeter;
        break;

      case SuplaConst.SUPLA_CHANNELFNC_IC_WATER_METER:
        img_idx = R.drawable.watermeter;
        break;

      case SuplaConst.SUPLA_CHANNELFNC_IC_HEAT_METER:
        img_idx = R.drawable.heatmeter;
        break;

      case SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT:
        switch (getAltIcon()) {
          case 1:
            img_idx = active == 1 ? R.drawable.thermostaton_1 : R.drawable.thermostatoff_1;
            break;
          case 2:
            img_idx = active == 1 ? R.drawable.thermostaton_2 : R.drawable.thermostatoff_2;
            break;
          case 3:
            img_idx = active == 1 ? R.drawable.thermostaton_3 : R.drawable.thermostatoff_3;
            break;
          default:
            img_idx = active == 1 ? R.drawable.thermostaton : R.drawable.thermostatoff;
        }

        break;

      case SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS:
        switch (getAltIcon()) {
          case 1:
            img_idx =
                active == 1
                    ? R.drawable.thermostat_hp_homepluson_1
                    : R.drawable.thermostat_hp_homeplusoff_1;
            break;
          case 2:
            img_idx =
                active == 1
                    ? R.drawable.thermostat_hp_homepluson_2
                    : R.drawable.thermostat_hp_homeplusoff_2;
            break;
          case 3:
            img_idx =
                active == 1
                    ? R.drawable.thermostat_hp_homepluson_3
                    : R.drawable.thermostat_hp_homeplusoff_3;
            break;
          default:
            img_idx =
                active == 1
                    ? R.drawable.thermostat_hp_homepluson
                    : R.drawable.thermostat_hp_homeplusoff;
        }

        break;
      case SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE:
        img_idx = active == 1 ? R.drawable.valveclosed : R.drawable.valveopen;
        break;
      case SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL:
        switch (getAltIcon()) {
          case 1:
            img_idx = active == 1 ? R.drawable.digiglasstransparent1 : R.drawable.digiglass1;
            break;
          default:
            img_idx = active == 1 ? R.drawable.digiglasstransparent : R.drawable.digiglass;
        }
        break;
      case SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL:
        switch (getAltIcon()) {
          case 1:
            img_idx = active == 1 ? R.drawable.digiglassvtransparent1 : R.drawable.digiglass1;
            break;
          default:
            img_idx = active == 1 ? R.drawable.digiglassvtransparent : R.drawable.digiglass;
        }
        break;
      default:
        img_idx = R.drawable.ic_unknown_channel;
    }

    return img_idx;
  }

  public ImageId getImageIdx(boolean nightMode, WhichOne whichImage, int active) {
    if (whichImage != WhichOne.First
        && getFunc() != SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE) {
      return null;
    }

    if (getUserIconId() > 0) {
      ImageId Id;

      switch (getFunc()) {
        case SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE:
          Id = new ImageId(getUserIconId(), whichImage == WhichOne.First ? 2 : 1, profileId);
          break;
        case SuplaConst.SUPLA_CHANNELFNC_THERMOMETER:
          Id = new ImageId(getUserIconId(), 1, profileId);
          break;
        case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE:
        case SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR:
          Id =
              new ImageId(
                  getUserIconId(),
                  (active & 0x1) > 0 ? 2 : ((active & 0x2) > 0 ? 3 : 1),
                  profileId);
          break;
        default:
          Id = new ImageId(getUserIconId(), active + 1, profileId);
          break;
      }

      if (ImageCache.bitmapExists(Id)) {
        return Id;
      }
    }

    boolean _50percent = (active & 0x2) == 0x2 && (active & 0x1) == 0;
    int img_idx;

    if (nightMode) {
      img_idx = getImageResourceIdForNightMode(whichImage, active, _50percent);
    } else {
      img_idx = getImageResourceId(whichImage, active, _50percent);
    }
    return new ImageId(img_idx);
  }

  public ImageId getImageIdx(WhichOne whichImage, int active) {
    return getImageIdx(false, whichImage, active);
  }

  protected ImageId getImageIdx(WhichOne whichImage, ChannelValue value) {
    return getImageIdx(whichImage, imgActive(value));
  }

  public abstract ImageId getImageIdx(WhichOne whichImage);

  public ImageId getImageIdx() {
    return getImageIdx(WhichOne.First);
  }

  @SuppressLint("DefaultLocale")
  protected CharSequence getHumanReadableValue(WhichOne whichOne, ChannelValue value) {

    if (value == null) {
      return "";
    }

    if (whichOne == WhichOne.Second) {

      if (getFunc() == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE) {
        if (getOnLine() && value.getHumidity() >= 0) {
          return String.format("%.1f", value.getHumidity());
        } else {
          return "---";
        }
      }

      return null;
    }

    switch (getFunc()) {
      case SuplaConst.SUPLA_CHANNELFNC_THERMOMETER:
      case SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE:
        double temperature = value.getTemp(getFunc());
        if (getOnLine() && getTemperaturePresenter().isTemperatureDefined(temperature)) {
          return getTemperaturePresenter().getTemperatureString(temperature, true, true);
        } else {
          return "---";
        }

      case SuplaConst.SUPLA_CHANNELFNC_HUMIDITY:
        double humidity = value.getHumidity();

        if (getOnLine() && humidity >= 0) {
          return String.format("%.1f", humidity);
        } else {
          return "---";
        }

      case SuplaConst.SUPLA_CHANNELFNC_WINDSENSOR:
        double wind = value.getDouble(-1);

        if (getOnLine() && wind >= 0) {
          return String.format("%.1f m/s", wind);
        } else {
          return "--- m/s";
        }
      case SuplaConst.SUPLA_CHANNELFNC_PRESSURESENSOR:
        double pressure = value.getDouble(-1);

        if (getOnLine() && pressure >= 0) {
          return String.format("%d hPa", (int) pressure);
        } else {
          return "--- hPa";
        }
      case SuplaConst.SUPLA_CHANNELFNC_RAINSENSOR:
        double rain = value.getDouble(-1);

        if (getOnLine() && rain >= 0) {
          return String.format("%.2f l/m\u00B2", rain / 1000.00);
        } else {
          return "--- l/m\u00B2";
        }
      case SuplaConst.SUPLA_CHANNELFNC_WEIGHTSENSOR:
        double weight = value.getDouble(-1);

        if (getOnLine() && weight >= 0) {
          if (Math.abs(weight) >= 2000) {
            return String.format("%.2f kg", weight / 1000.00);
          } else {
            return String.format("%d g", (int) weight);
          }
        } else {
          return "--- kg";
        }

      case SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR:
      case SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR:
        if (getOnLine() && value.getDistance() >= 0) {

          double distance = value.getDistance();

          if (Math.abs(distance) >= 1000) {

            return String.format("%.2f km", distance / 1000.00);

          } else if (Math.abs(distance) >= 1) {

            return String.format("%.2f m", distance);

          } else {
            distance *= 100;

            if (Math.abs(distance) >= 1) {
              return String.format("%.1f cm", distance);
            } else {
              distance *= 10;

              return String.format("%d mm", (int) distance);
            }
          }

        } else {
          return "--- m";
        }

      case SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER:
        double doubleValue = value.getTotalForwardActiveEnergy();
        if (doubleValue > 0) {
          return String.format("%.2f kWh", value.getTotalForwardActiveEnergy());
        } else {
          return "--- kWh";
        }

      case SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT:
      case SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS:
        return getHumanReadableThermostatTemperature(
            getOnLine() ? value.getMeasuredTemp(getFunc()) : null,
            getOnLine() ? value.getPresetTemp(getFunc()) : null);
    }

    return null;
  }

  protected ValuesFormatter getTemperaturePresenter() {
    return valuesFormatterProvider.getValuesFormatter();
  }

  public abstract CharSequence getHumanReadableValue(WhichOne whichOne);

  public abstract CharSequence getHumanReadableValue();

  public void Assign(SuplaChannelBase base, int profileId) {

    setRemoteId(base.Id);
    setLocationId(base.LocationID);
    setCaption(base.Caption);
    setFunc(base.Func);
    setFlags(base.Flags);
    setAltIcon(base.AltIcon);
    setUserIconId(base.UserIcon);
    setProfileId(profileId);
  }

  public boolean Diff(SuplaChannelBase base) {

    return base.Id != getRemoteId()
        || !base.Caption.equals(getCaption())
        || base.OnLine != getOnLine()
        || base.Flags != getFlags()
        || base.AltIcon != getAltIcon()
        || base.UserIcon != getUserIconId();
  }

  public boolean Diff(ChannelBase base) {

    return base.getRemoteId() != getRemoteId()
        || !base.getCaption().equals(getCaption())
        || base.getOnLine() != getOnLine()
        || base.getFlags() != getFlags()
        || base.getAltIcon() != getAltIcon()
        || base.getUserIconId() != getUserIconId();
  }

  public enum WhichOne {
    First,
    Second
  }
}
