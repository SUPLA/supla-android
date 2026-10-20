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
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Timer;
import java.util.TimerTask;
import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaConst;
import org.supla.android.listview.DetailLayout;
import org.supla.core.shared.data.model.function.digiglass.DigiglassValue;

@AndroidEntryPoint
public class ChannelDetailDigiglass extends DetailLayout
    implements View.OnClickListener, DigiglassController.OnSectionClickListener {

  private static final int REFRESH_HOLD_TIME_MS = 3000;

  private Button btnAllTransparent;
  private Button btnAllOpaque;
  private DigiglassController controller;
  private long refreshHold;
  private Timer delayTimer;
  private Handler uiHandler;

  public ChannelDetailDigiglass(Context context) {
    super(context);
  }

  public ChannelDetailDigiglass(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ChannelDetailDigiglass(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public ChannelDetailDigiglass(
      Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  protected void init() {
    super.init();
    uiHandler = new Handler();
    btnAllTransparent = findViewById(R.id.btnDgfTransparent);
    btnAllOpaque = findViewById(R.id.btnDgfOpaque);
    controller = findViewById(R.id.dgfController);

    btnAllOpaque.setOnClickListener(this);
    btnAllTransparent.setOnClickListener(this);
    controller.setOnSectionClickListener(this);
  }

  @Override
  public View inflateContentView() {
    return inflateLayout(R.layout.detail_digiglass);
  }

  private void channelToViews(Channel channel) {
    DigiglassValue dgfVal = channel.getValue().getDigiglassValue();
    controller.setSectionCount(dgfVal.getSectionCount());
    controller.setTransparentSections(dgfVal.getMask());
  }

  public void delayTimerCancel() {
    if (delayTimer != null) {
      delayTimer.cancel();
      delayTimer = null;
    }
  }

  @Override
  public void OnChannelDataChanged() {

    delayTimerCancel();

    long dimeDiff = refreshHold - System.currentTimeMillis();

    if (dimeDiff <= 0) {
      Channel channel = (Channel) getChannelFromDatabase();
      channelToViews(channel);
    } else {
      delayTimer = new Timer();
      delayTimer.schedule(
          new TimerTask() {
            @Override
            public void run() {
              uiHandler.post(
                  () -> {
                    delayTimerCancel();
                    channelToViews((Channel) getChannelFromDatabase());
                  });
            }
          },
          dimeDiff,
          1000);
    }
  }

  public void setData(ChannelBase channel) {
    super.setData(channel);
    controller.setVertical(channel.getFunc() == SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL);
    if (channel instanceof Channel) {
      channelToViews((Channel) channel);
    }
  }

  private void sendDgfTransparency(short mask, short active_bits) {
    SuplaClient client = SuplaApp.getApp().getSuplaClient();
    if (client != null) {
      refreshHold = System.currentTimeMillis() + REFRESH_HOLD_TIME_MS;
      delayTimerCancel();
      client.setDfgTransparency(getRemoteId(), mask, active_bits);
    }
  }

  @Override
  public void onClick(View v) {
    if (v == btnAllOpaque) {
      controller.setAllOpaque();
    } else if (v == btnAllTransparent) {
      controller.setAllTransparent();
    } else {
      return;
    }

    sendDgfTransparency((short) controller.getTransparentSections(), (short) 0xFFFF);
  }

  @Override
  public void onGlassSectionClick(
      DigiglassController controller, int section, boolean transparent) {
    short bit = (short) (1 << section);
    sendDgfTransparency(transparent ? bit : 0, bit);
  }
}
