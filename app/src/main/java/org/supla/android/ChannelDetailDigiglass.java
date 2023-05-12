package org.supla.android;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.lib.DigiglassValue;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaConst;
import org.supla.android.listview.DetailLayout;

import java.util.Timer;
import java.util.TimerTask;

public class ChannelDetailDigiglass extends DetailLayout implements View.OnClickListener, DigiglassController.OnSectionClickListener {

    private final static int REFRESH_HOLD_TIME_MS = 3000;

    private Button btnAllTransparent;
    private Button btnAllOpaque;
    private DigiglassController controller;
    private long refreshHold;
    private Timer delayTimer;

    public ChannelDetailDigiglass(Context context) {
        super(context);
    }

    public ChannelDetailDigiglass(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChannelDetailDigiglass(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChannelDetailDigiglass(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init() {
        super.init();
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

        long dimeDiff = refreshHold-System.currentTimeMillis();

        if (dimeDiff <= 0) {
            Channel channel = (Channel) getChannelFromDatabase();
            channelToViews(channel);
        } else {
            delayTimer = new Timer();
            delayTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (getContext() instanceof Activity) {
                        ((Activity) getContext()).runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                delayTimerCancel();
                                Channel channel = (Channel) getChannelFromDatabase();
                                channelToViews(channel);
                            }
                        });
                    }
                }

            }, dimeDiff, 1000);
        }
    }

    public void setData(ChannelBase channel) {
        super.setData(channel);
        controller.setVertical(channel.getFunc() ==
                SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL);
        if (channel instanceof Channel) {
            channelToViews((Channel)channel);
        }
    }

    private void sendDgfTransparency(short mask, short active_bits) {
        SuplaClient client = SuplaApp.getApp().getSuplaClient();
        if (client != null) {
            refreshHold =  System.currentTimeMillis() + REFRESH_HOLD_TIME_MS;
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

        sendDgfTransparency((short)controller.getTransparentSections(), (short)0xFFFF);
    }

    @Override
    public void onGlassSectionClick(DigiglassController controller, int section,
                                    boolean transparent) {
        short bit = (short)(1 << section);
        sendDgfTransparency(transparent ? bit : 0, bit);
    }
}
