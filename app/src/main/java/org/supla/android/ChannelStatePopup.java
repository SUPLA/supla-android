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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.supla.android.db.Channel;
import org.supla.android.db.DbHelper;
import org.supla.android.lib.SuplaChannelState;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaConst;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ChannelStatePopup implements DialogInterface.OnCancelListener, View.OnClickListener, SuperuserAuthorizationDialog.OnAuthorizarionResultListener {

    private final int REFRESH_INTERVAL_MS = 4000;

    private long lastRefreshTime;
    private Timer refreshTimer;
    private AlertDialog alertDialog;
    private int remoteId;
    private Context context;
    private int channelFunc;
    private int channelFlags;
    private SuplaChannelState lastState;

    private LinearLayout llChannelID;
    private LinearLayout llIP;
    private LinearLayout llMAC;
    private LinearLayout llBatteryLevel;
    private LinearLayout llBatteryPowered;
    private LinearLayout llWiFiRSSI;
    private LinearLayout llWiFiSignalStrength;
    private LinearLayout llBridgeNodeOnline;
    private LinearLayout llBridgeNodeSignalStrength;
    private LinearLayout llUptime;
    private LinearLayout llConnectionUptime;
    private LinearLayout llBatteryHealth;
    private LinearLayout llLastConnectionResetCause;
    private LinearLayout llLightSourceLifespan;
    private LinearLayout llLightSourceOperatingTime;
    private LinearLayout llProgress;

    private TextView tvInfoTitle;
    private TextView tvChannelID;
    private TextView tvIP;
    private TextView tvMAC;
    private TextView tvBatteryLevel;
    private TextView tvBatteryPowered;
    private TextView tvWiFiRSSI;
    private TextView tvWiFiSignalStrength;
    private TextView tvBridgeNodeOnline;
    private TextView tvBridgeNodeSignalStrength;
    private TextView tvUptime;
    private TextView tvConnectionUptime;
    private TextView tvBatteryHealth;
    private TextView tvLastConnectionResetCause;
    private TextView tvLightSourceLifespan;
    private TextView tvLightSourceOperatingTime;

    private Button btnClose;
    private Button btnReset;

    public ChannelStatePopup(Context context) {
        this.context = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.channelstate, null);
        builder.setView(view);

        llChannelID = view.findViewById(R.id.llChannelID);
        llIP = view.findViewById(R.id.llIP);
        llMAC = view.findViewById(R.id.llMAC);
        llBatteryLevel = view.findViewById(R.id.llBatteryLevel);
        llBatteryPowered = view.findViewById(R.id.llBatteryPowered);
        llWiFiRSSI = view.findViewById(R.id.llWiFiRSSI);
        llWiFiSignalStrength = view.findViewById(R.id.llWiFiSignalStrength);
        llBridgeNodeOnline = view.findViewById(R.id.llBridgeNodeOnline);
        llBridgeNodeSignalStrength = view.findViewById(R.id.llBridgeNodeSignalStrength);
        llUptime = view.findViewById(R.id.llUptime);
        llConnectionUptime = view.findViewById(R.id.llConnectionUptime);
        llBatteryHealth = view.findViewById(R.id.llBatteryHealth);
        llLastConnectionResetCause = view.findViewById(R.id.llLastConnectionResetCause);
        llLightSourceLifespan = view.findViewById(R.id.llLightSourceLifespan);
        llLightSourceOperatingTime = view.findViewById(R.id.llLightSourceOperatingTime);
        llProgress = view.findViewById(R.id.llProgress);

        tvInfoTitle = view.findViewById(R.id.tvInfoTitle);
        tvChannelID = view.findViewById(R.id.tvChannelID);
        tvIP = view.findViewById(R.id.tvIP);
        tvMAC = view.findViewById(R.id.tvMAC);
        tvBatteryLevel = view.findViewById(R.id.tvBatteryLevel);
        tvBatteryPowered = view.findViewById(R.id.tvBatteryPowered);
        tvWiFiRSSI = view.findViewById(R.id.tvWiFiRSSI);
        tvWiFiSignalStrength = view.findViewById(R.id.tvWiFiSignalStrength);
        tvBridgeNodeOnline = view.findViewById(R.id.tvBridgeNodeOnline);
        tvBridgeNodeSignalStrength = view.findViewById(R.id.tvBridgeNodeSignalStrength);
        tvUptime = view.findViewById(R.id.tvUptime);
        tvConnectionUptime = view.findViewById(R.id.tvConnectionUptime);
        tvBatteryHealth = view.findViewById(R.id.tvBatteryHealth);
        tvLastConnectionResetCause = view.findViewById(R.id.tvLastConnectionResetCause);
        tvLightSourceLifespan = view.findViewById(R.id.tvLightSourceLifespan);
        tvLightSourceOperatingTime = view.findViewById(R.id.tvLightSourceOperatingTime);

        tvInfoTitle.setTypeface(SuplaApp.getApp().getTypefaceQuicksandRegular());

        btnClose = view.findViewById(R.id.btnClose);
        btnReset = view.findViewById(R.id.btnReset);

        btnClose.setOnClickListener(this);
        btnReset.setOnClickListener(this);

        alertDialog = builder.create();
        alertDialog.setOnCancelListener(this);
    }

    public void show(int remoteId) {
        this.remoteId = 0;
        update(remoteId);
        alertDialog.show();
    }

    public boolean isVisible() {
        return alertDialog.isShowing();
    }

    public int getRemoteId() {
        return remoteId;
    }

    public void update(SuplaChannelState state) {
        lastState = state;
        llChannelID.setVisibility(View.GONE);
        llIP.setVisibility(View.GONE);
        llMAC.setVisibility(View.GONE);
        llBatteryLevel.setVisibility(View.GONE);
        llBatteryPowered.setVisibility(View.GONE);
        llWiFiRSSI.setVisibility(View.GONE);
        llWiFiSignalStrength.setVisibility(View.GONE);
        llBridgeNodeOnline.setVisibility(View.GONE);
        llBridgeNodeSignalStrength.setVisibility(View.GONE);
        llUptime.setVisibility(View.GONE);
        llConnectionUptime.setVisibility(View.GONE);
        llBatteryHealth.setVisibility(View.GONE);
        llLastConnectionResetCause.setVisibility(View.GONE);
        llLightSourceLifespan.setVisibility(View.GONE);
        llLightSourceOperatingTime.setVisibility(View.GONE);
        llProgress.setVisibility(View.VISIBLE);
        btnReset.setVisibility(View.GONE);

        cancelRefreshTimer();

        if ((channelFlags & SuplaConst.SUPLA_CHANNEL_FLAG_CHANNELSTATE) != 0) {
            refreshTimer = new Timer();
            refreshTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                if (System.currentTimeMillis() - lastRefreshTime >= REFRESH_INTERVAL_MS) {
                                    cancelRefreshTimer();

                                    SuplaClient client = SuplaApp.getApp().getSuplaClient();

                                    if (client != null) {
                                        lastRefreshTime = System.currentTimeMillis();
                                        client.getChannelState(remoteId);
                                    }
                                }
                            }
                        });
                    }
                }
            }, 0, 500);
        }

        if (state == null) {
            return;
        }

        lastRefreshTime = System.currentTimeMillis();

        if (state.getChannelID() != 0) {
            llChannelID.setVisibility(View.VISIBLE);
            llProgress.setVisibility(View.GONE);
            tvChannelID.setText(Integer.toString(state.getChannelID()));
        }

        if (state.getIpv4() != null) {
            llIP.setVisibility(View.VISIBLE);
            llProgress.setVisibility(View.GONE);
            tvIP.setText(state.getIpv4String());
        }

        if (state.getMacAddress() != null) {
            llMAC.setVisibility(View.VISIBLE);
            llProgress.setVisibility(View.GONE);
            tvMAC.setText(state.getMacAddressString());
        }

        if (state.getBatteryLevel() != null) {
            llBatteryLevel.setVisibility(View.VISIBLE);
            llProgress.setVisibility(View.GONE);
            tvBatteryLevel.setText(state.getBatteryLevelString());
        }

        if (state.isBatteryPowered() != null) {
            llBatteryPowered.setVisibility(View.VISIBLE);
            llProgress.setVisibility(View.GONE);
            tvBatteryPowered.setText(state.getBatteryPoweredString(context));
        }

        if (state.getWiFiRSSI() != null) {
            llWiFiRSSI.setVisibility(View.VISIBLE);
            llProgress.setVisibility(View.GONE);
            tvWiFiRSSI.setText(state.getWiFiRSSIString());
        }

        if (state.getWiFiSignalStrength() != null) {
            llWiFiSignalStrength.setVisibility(View.VISIBLE);
            llProgress.setVisibility(View.GONE);
            tvWiFiSignalStrength.setText(state.getWiFiSignalStrengthString());
        }

        if (state.isBridgeNodeOnline() != null) {
            llBridgeNodeOnline.setVisibility(View.VISIBLE);
            llProgress.setVisibility(View.GONE);
            tvBridgeNodeOnline.setText(state.getBridgeNodeOnlineString(context));
        }

        if (state.getBridgeNodeSignalStrength() != null) {
            llBridgeNodeSignalStrength.setVisibility(View.VISIBLE);
            llProgress.setVisibility(View.GONE);
            tvBridgeNodeSignalStrength.setText(state.getBridgeNodeSignalStrengthString(context));
        }

        if (state.getUptime() != null) {
            llUptime.setVisibility(View.VISIBLE);
            llProgress.setVisibility(View.GONE);

            tvUptime.setText(state.getUptimeString(context));
        }

        if (state.getConnectionUptime() != null) {
            llConnectionUptime.setVisibility(View.VISIBLE);
            llProgress.setVisibility(View.GONE);

            tvConnectionUptime.setText(state.getConnectionUptimeString(context));
        }

        if (state.getBatteryHealth() != null) {
            llBatteryHealth.setVisibility(View.VISIBLE);
            llProgress.setVisibility(View.GONE);
            tvBatteryHealth.setText(state.getBatteryHealthString());
        }

        if (state.getLastConnectionResetCause() != null) {
            llLastConnectionResetCause.setVisibility(View.VISIBLE);
            llProgress.setVisibility(View.GONE);
	    int res[] = { R.string.lastconnectionresetcause_unknown,
			  R.string.lastconnectionresetcause_activity_timeout,
			  R.string.lastconnectionresetcause_wifi_connection_lost,
			  R.string.lastconnectionresetcause_server_connection_lost };
	    int reset_cause = state.getLastConnectionResetCause();
	    if(reset_cause >= 0 && reset_cause < res.length)
		tvLastConnectionResetCause.setText(res[reset_cause]);
	    else 
		tvLastConnectionResetCause.setText(state.getLastConnectionResetCauseString());
        }

        if (channelFunc == SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH) {

            if (state.getLightSourceLifespan() != null) {
                if (state.getLightSourceLifespan() > 0) {
                    llLightSourceLifespan.setVisibility(View.VISIBLE);
                    llProgress.setVisibility(View.GONE);
                    tvLightSourceLifespan.setText(state.getLightSourceLifespanString());
                }

                if ((channelFlags & SuplaConst.SUPLA_CHANNEL_FLAG_LIGHTSOURCELIFESPAN_SETTABLE) > 0) {
                    btnReset.setVisibility(View.VISIBLE);
                }
            }

            if (state.getLightSourceOperatingTime() != null) {
                llLightSourceOperatingTime.setVisibility(View.VISIBLE);
                llProgress.setVisibility(View.GONE);
                tvLightSourceOperatingTime.setText(state.getLightSourceOperatingTimeString());
            }

        }
    }


    public void update(int remoteId) {

        if (this.remoteId != remoteId) {
            onCancel(alertDialog);
            lastState = null;
            this.remoteId = remoteId;
        }

        DbHelper dbHelper = DbHelper.getInstance(context);
        Channel channel = dbHelper.getChannel(remoteId);
        channelFlags = 0;

        if (channel != null) {

            tvInfoTitle.setText(channel.getNotEmptyCaption(context));
            channelFunc = channel.getFunc();
            channelFlags = channel.getFlags();

            if ((channelFlags & SuplaConst.SUPLA_CHANNEL_FLAG_CHANNELSTATE) == 0) {
               lastState = channel.getChannelState();
            }

            update(lastState);

        }
    }

    private void cancelRefreshTimer() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        lastRefreshTime = 0;
        cancelRefreshTimer();
    }

    @Override
    public void onClick(View v) {
        alertDialog.cancel();

        if (v == btnReset) {
            SuperuserAuthorizationDialog authDialog =
                    new SuperuserAuthorizationDialog(context);
            authDialog.setOnAuthorizarionResultListener(this);
            authDialog.showIfNeeded();
        }
    }

    @Override
    public void onSuperuserOnAuthorizarionResult(SuperuserAuthorizationDialog dialog,
                                                 boolean Success, int Code) {
        if (Success) {
            LightsourceLifespanSettingsDialog lsdialog =
                    new LightsourceLifespanSettingsDialog(context, remoteId,
                            lastState.getLightSourceLifespan(),
                            tvInfoTitle.getText().toString());
            dialog.close();
            lsdialog.show();
        }
    }

    @Override
    public void authorizationCanceled() {
    }
}
;
