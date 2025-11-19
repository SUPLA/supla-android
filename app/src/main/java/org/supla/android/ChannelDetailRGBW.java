package org.supla.android;

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

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;
import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.lib.SuplaConst;
import org.supla.android.listview.DetailLayout;
import org.supla.android.navigator.MainNavigator;

@AndroidEntryPoint
public class ChannelDetailRGBW extends DetailLayout {

  @Inject MainNavigator mainNavigator;
  private DimmerCalibrationTool dimmerCalibrationTool = null;

  public ChannelDetailRGBW(Context context) {
    super(context);
  }

  public ChannelDetailRGBW(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ChannelDetailRGBW(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public ChannelDetailRGBW(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  protected void init() {
    super.init();

    findViewById(R.id.rgbAuthorizeButton)
        .setOnClickListener(
            v -> {
              if (dimmerCalibrationTool != null
                  && !dimmerCalibrationTool.isAuthorizationDialogOpened()) {
                dimmerCalibrationTool.Show();
              }
            });
  }

  private void setupDimmerCalibrationTool() {
    if (getChannelBase() instanceof Channel c) {
      if (c.getManufacturerID() == SuplaConst.SUPLA_MFR_DOYLETRATT && c.getProductID() == 1) {
        if (dimmerCalibrationTool == null
            || !(dimmerCalibrationTool instanceof VLCalibrationTool)) {
          dimmerCalibrationTool = new VLCalibrationTool(this, mainNavigator);
        }
      } else if (c.getManufacturerID() == SuplaConst.SUPLA_MFR_ZAMEL) {
        if ((c.getProductID() == SuplaConst.ZAM_PRODID_DIW_01)
            && (dimmerCalibrationTool == null
                || !(dimmerCalibrationTool instanceof DiwCalibrationTool))) {
          dimmerCalibrationTool = new DiwCalibrationTool(this, mainNavigator);
        }
      } else if (c.getManufacturerID() == SuplaConst.SUPLA_MFR_COMELIT) {
        if ((c.getProductID() == SuplaConst.COM_PRODID_WDIM100)
            && (dimmerCalibrationTool == null
                || !(dimmerCalibrationTool instanceof DiwCalibrationTool))) {
          dimmerCalibrationTool = new DiwCalibrationTool(this, mainNavigator);
        }
      }
    }
  }

  @Override
  public void onDetailHide() {
    if (dimmerCalibrationTool != null) {
      dimmerCalibrationTool.Hide();
    }
  }

  public void onDetailShow() {
    if (dimmerCalibrationTool != null) {
      dimmerCalibrationTool.Show();
    }
  }

  public void setData(ChannelBase channel) {
    super.setData(channel);
    setupDimmerCalibrationTool();
  }

  @Override
  public View inflateContentView() {
    return inflateLayout(R.layout.detail_rgbw);
  }

  @Override
  public boolean onBackPressed() {
    if (dimmerCalibrationTool != null && dimmerCalibrationTool.isVisible()) {
      return dimmerCalibrationTool.onBackPressed();
    }
    return false;
  }

  public boolean detailWillHide(boolean offlineReason) {
    if (super.detailWillHide(offlineReason)) {
      return !offlineReason
          || dimmerCalibrationTool == null
          || !dimmerCalibrationTool.isVisible()
          || dimmerCalibrationTool.isExitUnlocked();
    }
    return false;
  }

  @Override
  public void OnChannelDataChanged() {}

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    if (dimmerCalibrationTool != null && dimmerCalibrationTool.isVisible()) {
      return false;
    }
    return super.onTouchEvent(ev);
  }
}
