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
import org.supla.android.SuplaApp;
import org.supla.android.ValuesFormatterProvider;
import org.supla.android.core.shared.LocalizedStringExtensionsKt;
import org.supla.android.data.ValuesFormatter;
import org.supla.android.extensions.ContextExtensionsKt;
import org.supla.android.lib.SuplaChannelBase;
import org.supla.core.shared.data.model.general.SuplaFunction;

public abstract class ChannelBase extends DbItem {

  private int RemoteId; // SuplaChannelBase.Id
  private String Caption;
  private int Func;
  private int Visible;
  private long LocationId;
  private int AltIcon;
  private int UserIconId;
  private long Flags;
  private long profileId;

  private final ValuesFormatterProvider valuesFormatterProvider;

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

    measured = tp.getTemperatureString(measuredTempFrom, true);
    if (tp.isTemperatureDefined(measuredTempTo)) {
      measured += " - " + tp.getTemperatureString(measuredTempTo, true);
    }

    preset = "/" + tp.getTemperatureString(presetTempFrom, true);
    if (tp.isTemperatureDefined(presetTempTo)) {
      preset += " - " + tp.getTemperatureString(presetTempTo, true);
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

  public int getRemoteId() {
    return RemoteId;
  }

  public void setRemoteId(int id) {
    RemoteId = id;
  }

  protected String getCaption() {
    return Caption;
  }

  public void setCaption(String caption) {
    Caption = caption;
  }

  public String getCaption(Context context) {
    if (Caption == null || Caption.trim().isEmpty()) {
      return LocalizedStringExtensionsKt.invoke(
          ContextExtensionsKt.getGetChannelDefaultCaptionUseCase(context)
              .invoke(SuplaFunction.Companion.from(Func)),
          context);
    }
    return Caption;
  }

  public boolean hasCustomCaption() {
    return !Caption.trim().isEmpty();
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

  public long getFlags() {
    return Flags;
  }

  public void setFlags(long flags) {
    Flags = flags;
  }

  public long getProfileId() {
    return profileId;
  }

  public void setProfileId(long pid) {
    profileId = pid;
  }

  protected ValuesFormatter getTemperaturePresenter() {
    return valuesFormatterProvider.getValuesFormatter();
  }

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
        || base.getStatus().getOnline() != getOnLine()
        || base.Flags != getFlags()
        || base.AltIcon != getAltIcon()
        || base.UserIcon != getUserIconId();
  }
}
