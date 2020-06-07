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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.db.ChannelGroup;
import org.supla.android.db.ColorListItem;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaConst;
import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.DetailLayout;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ChannelDetailRGB extends DetailLayout implements View.OnClickListener,
        SuplaColorBrightnessPicker.OnColorBrightnessChangeListener,
        SuplaColorListPicker.OnColorListTouchListener {

    final static private long MIN_REMOTE_UPDATE_PERIOD = 250;
    final static private long MIN_UPDATE_DELAY = 2000;
    private SuplaColorBrightnessPicker cbPicker;
    private SuplaColorListPicker clPicker;
    private Button tabRGB;
    private Button tabDimmer;
    private ViewGroup tabs;
    private TextView tvTitle;
    private Button btnSettings;
    private RelativeLayout rlMain;
    private VLCalibrationTool vlCalibrationTool = null;
    private TextView tvStateCaption;
    private ImageView stateImage;
    private long remoteUpdateTime;
    private long changeFinishedTime;
    private Timer delayTimer1;
    private Timer delayTimer2;
    private SuplaChannelStatus status;
    private int lastColor;
    private int lastColorBrightness;
    private int lastBrightness;

    public ChannelDetailRGB(Context context, ChannelListView cLV) {
        super(context, cLV);
    }

    public ChannelDetailRGB(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChannelDetailRGB(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChannelDetailRGB(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void init() {

        super.init();

        tabs = findViewById(R.id.rlTabs);

        Resources r = getResources();

        status = findViewById(R.id.rgbstatus);
        status.setOnlineColor(getResources().getColor(R.color.channel_dot_on));
        status.setOfflineColor(getResources().getColor(R.color.channel_dot_off));

        clPicker = findViewById(R.id.clPicker);
        clPicker.addItem(Color.WHITE, (short) 100);
        clPicker.addItem();
        clPicker.addItem();
        clPicker.addItem();
        clPicker.addItem();
        clPicker.addItem();
        clPicker.setOnTouchListener(this);

        cbPicker = findViewById(R.id.cbPicker);
        cbPicker.setOnChangeListener(this);

        tabRGB = findViewById(R.id.rgbTabBtn_RGB);
        tabDimmer = findViewById(R.id.rgbTabBtn_Dimmer);

        tabRGB.setOnClickListener(this);
        tabDimmer.setOnClickListener(this);

        btnSettings = findViewById(R.id.rgbBtnSettings);
        btnSettings.setOnClickListener(this);
        btnSettings.setVisibility(GONE);

        rlMain = findViewById(R.id.rlRgbMain);
        rlMain.setVisibility(VISIBLE);

        Typeface type = SuplaApp.getApp().getTypefaceOpenSansBold();
        tabRGB.setTypeface(type);
        tabDimmer.setTypeface(type);

        tvStateCaption = findViewById(R.id.rgbDetailStateCaption);
        tvStateCaption.setTypeface(type);

        tvTitle = findViewById(R.id.rgbDetailTitle);
        tvTitle.setTypeface(SuplaApp.getApp().getTypefaceQuicksandRegular());

        stateImage = findViewById(R.id.rgbDetailStateImage);
        stateImage.setOnClickListener(this);

        remoteUpdateTime = 0;
        changeFinishedTime = 0;
        delayTimer1 = null;
        delayTimer2 = null;
    }


    private void showRGB() {
        cbPicker.setColorWheelVisible(true);
        clPicker.setVisibility(View.VISIBLE);

        channelDataToViews();
    }

    private void showDimmer() {

        cbPicker.setColorWheelVisible(false);
        clPicker.setVisibility(View.GONE);

        channelDataToViews();
    }

    public void onBackPressed() {

    }

    @Override
    public void onDetailHide() {
        super.onDetailHide();

        if (vlCalibrationTool != null) {
            vlCalibrationTool.Hide();
        }
    }

    public void onDetailShow() {
        if (vlCalibrationTool != null) {
            vlCalibrationTool.Hide();
        }
        rlMain.setVisibility(VISIBLE);
    }

    public void setData(ChannelBase channel) {

        super.setData(channel);
        btnSettings.setVisibility(GONE);

        switch (channel.getFunc()) {

            case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
                showDimmer();
                tabs.setVisibility(View.GONE);

                if (vlCalibrationTool != null) {
                    vlCalibrationTool.Hide();
                    vlCalibrationTool = null;
                }

                if (channel instanceof Channel) {
                    Channel c = (Channel) channel;
                    if (c.getManufacturerID() == SuplaConst.SUPLA_MFR_DOYLETRATT
                            && c.getProductID() == 1) {
                        vlCalibrationTool = new VLCalibrationTool(this);
                        btnSettings.setVisibility(VISIBLE);
                    }
                }
                break;

            case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
                showRGB();
                tabs.setVisibility(View.GONE);
                break;

            case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
                cbPicker.setColorWheelVisible(true);

                onClick(tabRGB);
                tabs.setVisibility(View.VISIBLE);

                break;
        }


        channelDataToViews();
    }

    private void channelDataToViews() {

        int id = 0;
        cbPicker.setColorMarkers(null);
        cbPicker.setBrightnessMarkers(null);

        if (isGroup()) {
            ChannelGroup cgroup = (ChannelGroup) getChannelFromDatabase();
            tvTitle.setText(cgroup.getNotEmptyCaption(getContext()));

            stateImage.setVisibility(View.GONE);
            tvStateCaption.setVisibility(View.GONE);

            status.setVisibility(View.VISIBLE);
            status.setPercent(cgroup.getOnLinePercent());

            ArrayList<Double> markers;

            markers = cbPicker.isColorWheelVisible() ? cgroup.getColorBrightness()
                    : cgroup.getBrightness();

            if (markers != null) {
                if (markers.size() == 1) {
                    if (markers.get(0).intValue() != (int) cbPicker.getBrightnessValue()) {
                        cbPicker.setBrightnessValue(markers.get(0));
                    }
                } else {
                    cbPicker.setBrightnessMarkers(markers);
                }
            }

            if (cbPicker.isColorWheelVisible()) {

                markers = cgroup.getColors();

                if (markers != null) {
                    if (markers.size() == 1) {
                        if (markers.get(0).intValue() != cbPicker.getColor()) {
                            cbPicker.setColor(markers.get(0).intValue());
                        }
                    } else {
                        cbPicker.setColorMarkers(markers);
                    }
                }
            }


        } else {
            Channel channel = (Channel) getChannelFromDatabase();

            tvTitle.setText(channel.getNotEmptyCaption(getContext()));
            status.setVisibility(View.GONE);

            stateImage.setVisibility(View.VISIBLE);
            tvStateCaption.setVisibility(View.VISIBLE);

            if (cbPicker.isColorWheelVisible()
                    && (int) cbPicker.getBrightnessValue() != (int) channel.getColorBrightness()) {
                cbPicker.setBrightnessValue(channel.getColorBrightness());

            } else if (!cbPicker.isColorWheelVisible()
                    && (int) cbPicker.getBrightnessValue() != (int) channel.getBrightness()) {
                cbPicker.setBrightnessValue(channel.getBrightness());
            }

            if (cbPicker.isColorWheelVisible())
                cbPicker.setColor(channel.getColor());

        }


        for (int a = 1; a < 6; a++) {
            ColorListItem cli = DBH.getColorListItem(getRemoteId(), isGroup(), a);

            if (cli != null) {
                clPicker.setItemColor(a, cli.getColor());
                clPicker.setItemPercent(a, cli.getBrightness());
            } else {
                clPicker.setItemColor(a, Color.TRANSPARENT);
                clPicker.setItemPercent(a, (short) 0);
            }
        }


        pickerToInfoPanel();

    }

    @Override
    public View inflateContentView() {
        return inflateLayout(R.layout.detail_rgb);
    }

    private void setBtnBackground(Button btn, int id) {

        Drawable d = getResources().getDrawable(id);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            btn.setBackground(d);
        } else {
            btn.setBackgroundDrawable(d);
        }
    }

    @SuppressLint("SetTextI18n")
    private void pickerToInfoPanel() {

        lastColor = cbPicker.getColor();

        int brightness = (int) cbPicker.getBrightnessValue();
        stateImage.setImageResource(brightness > 0 ? R.drawable.poweron : R.drawable.poweroff);

        if (cbPicker.isColorWheelVisible())
            lastColorBrightness = brightness;
        else
            lastBrightness = brightness;
    }

    private void sendNewValues(boolean force, boolean turnOnOff) {

        if (delayTimer1 != null) {
            delayTimer1.cancel();
            delayTimer1 = null;
        }

        SuplaClient client = SuplaApp.getApp().getSuplaClient();

        if (client == null || (!isDetailVisible() && !force))
            return;

        if (System.currentTimeMillis() - remoteUpdateTime >= MIN_REMOTE_UPDATE_PERIOD
                && client.setRGBW(getRemoteId(), isGroup(), lastColor,
                lastColorBrightness, lastBrightness, turnOnOff)) {
            remoteUpdateTime = System.currentTimeMillis();

        } else {

            long delayTime = 1;

            if (System.currentTimeMillis() - remoteUpdateTime < MIN_REMOTE_UPDATE_PERIOD)
                delayTime = MIN_REMOTE_UPDATE_PERIOD - (System.currentTimeMillis() - remoteUpdateTime) + 1;

            delayTimer1 = new Timer();

            delayTimer1.schedule(new TimerTask() {
                @Override
                public void run() {

                    if (getContext() instanceof Activity) {
                        ((Activity) getContext()).runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                sendNewValues();
                            }
                        });
                    }

                }

            }, delayTime, 1000);

        }

    }

    private void sendNewValues() {
        sendNewValues(false, false);
    }

    @Override
    public void onClick(View v) {

        if (v == tabRGB) {
            showRGB();

            setBtnBackground(tabRGB, R.drawable.rounded_rgb_left_sel_btn);
            setBtnBackground(tabDimmer, R.drawable.rounded_rgb_right_btn);

            tabRGB.setTextColor(getResources().getColor(R.color.detail_rgb_gb));
            tabDimmer.setTextColor(Color.BLACK);
        } else if (v == tabDimmer) {
            showDimmer();

            setBtnBackground(tabRGB, R.drawable.rounded_rgb_left_btn);
            setBtnBackground(tabDimmer, R.drawable.rounded_rgb_right_sel_btn);

            tabRGB.setTextColor(Color.BLACK);
            tabDimmer.setTextColor(getResources().getColor(R.color.detail_rgb_gb));
        } else if (v == stateImage) {
            cbPicker.setBrightnessValue(cbPicker.getBrightnessValue() > 0 ? 0 : 100);
            pickerToInfoPanel();
            sendNewValues(true, true);
            onChangeFinished(cbPicker);
        } else if (v == btnSettings
                && vlCalibrationTool != null) {
            vlCalibrationTool.Show();
        }

        if (v == tabDimmer || v == tabRGB) {
            channelDataToViews();
        }

    }

    @Override
    public void onColorChanged(SuplaColorBrightnessPicker scbPicker, int color) {
        pickerToInfoPanel();
        sendNewValues();
    }

    @Override
    public void onBrightnessChanged(SuplaColorBrightnessPicker scbPicker, double brightness) {
        pickerToInfoPanel();
        sendNewValues();
    }

    @Override
    public void onPowerButtonClick(SuplaColorBrightnessPicker scbPicker) {

    }

    private void updateDelayed() {

        if (delayTimer2 != null) {
            delayTimer2.cancel();
            delayTimer2 = null;
        }

        if (!isDetailVisible()
                || cbPicker.isMoving())
            return;

        if (System.currentTimeMillis() - changeFinishedTime >= MIN_UPDATE_DELAY) {

            channelDataToViews();

        } else {

            long delayTime = 1;

            if (System.currentTimeMillis() - changeFinishedTime < MIN_UPDATE_DELAY)
                delayTime = MIN_UPDATE_DELAY - (System.currentTimeMillis() - changeFinishedTime) + 1;

            delayTimer2 = new Timer();

            delayTimer2.schedule(new TimerTask() {
                @Override
                public void run() {

                    if (getContext() instanceof Activity) {
                        ((Activity) getContext()).runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                updateDelayed();
                            }
                        });
                    }

                }

            }, delayTime, 1000);

        }


    }

    @Override
    public void onChangeFinished(SuplaColorBrightnessPicker scbPicker) {
        changeFinishedTime = System.currentTimeMillis();
        updateDelayed();
    }

    @Override
    public void OnChannelDataChanged() {
        updateDelayed();

        if (vlCalibrationTool != null) {
            vlCalibrationTool.Hide();
        }
    }


    @Override
    public void onColorTouched(SuplaColorListPicker sclPicker, int color, short percent) {

        if (color != Color.TRANSPARENT && cbPicker.isColorWheelVisible()) {
            cbPicker.setColor(color);
            cbPicker.setBrightnessValue(percent);

            onColorChanged(cbPicker, color);
        }

    }

    @Override
    public void onEdit(SuplaColorListPicker sclPicker, int idx) {

        if (idx > 0 && cbPicker.isColorWheelVisible()) {
            sclPicker.setItemColor(idx, cbPicker.getColor());
            sclPicker.setItemPercent(idx, (short) cbPicker.getBrightnessValue());

            if (getRemoteId() != 0) {

                ColorListItem cli = new ColorListItem();
                cli.setRemoteId(getRemoteId());
                cli.setGroup(isGroup());
                cli.setIdx(idx);
                cli.setColor(cbPicker.getColor());
                cli.setBrightness((short) cbPicker.getBrightnessValue());

                DBH.updateColorListItemValue(cli);
            }

        }
    }


}
