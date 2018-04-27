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
import android.widget.TextView;

import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.db.ChannelGroup;
import org.supla.android.db.ColorListItem;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaConst;
import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.DetailLayout;

import java.util.Timer;
import java.util.TimerTask;

public class ChannelDetailRGB extends DetailLayout implements View.OnClickListener, SuplaColorBrightnessPicker.OnColorBrightnessChangeListener, SuplaColorListPicker.OnColorListTouchListener {

    private SuplaColorBrightnessPicker rgbPicker;
    private SuplaColorListPicker clPicker;
    private Button tabRGB;
    private Button tabDimmer;
    private ViewGroup tabs;
    private TextView tvTitle;
    private TextView tvBrightnessCaption;
    private TextView tvBrightness;
    private TextView tvStateCaption;
    private View brightnessLine;
    private ImageView stateImage;
    private long remoteUpdateTime;
    private long changeFinishedTime;
    private Timer delayTimer1;
    private Timer delayTimer2;
    private SuplaChannelStatus status;

    private int lastColor;
    private int lastColorBrightness;
    private int lastBrightness;

    final static private long MIN_REMOTE_UPDATE_PERIOD = 250;
    final static private long MIN_UPDATE_DELAY = 2000;

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

        tabs = (ViewGroup) findViewById(R.id.rlTabs);

        Resources r = getResources();

        status = (SuplaChannelStatus) findViewById(R.id.rgbstatus);
        status.setOnlineColor(getResources().getColor(R.color.channel_dot_on));
        status.setOfflineColor(getResources().getColor(R.color.channel_dot_off));

        clPicker = (SuplaColorListPicker) findViewById(R.id.clPicker);
        clPicker.addItem(Color.WHITE, (short) 100);
        clPicker.addItem();
        clPicker.addItem();
        clPicker.addItem();
        clPicker.addItem();
        clPicker.addItem();
        clPicker.setOnTouchListener(this);

        rgbPicker = (SuplaColorBrightnessPicker) findViewById(R.id.rgbPicker);
        rgbPicker.setPercentVisible(false);
        rgbPicker.setWheelWidth(r.getDimensionPixelSize(R.dimen.rgb_wheel_width));
        rgbPicker.setArrowHeight(r.getDimensionPixelSize(R.dimen.rgb_wheel_arrow_height));

        rgbPicker.setOnChangeListener(this);

        tabRGB = (Button) findViewById(R.id.rgbTabBtn_RGB);
        tabDimmer = (Button) findViewById(R.id.rgbTabBtn_Dimmer);

        tabRGB.setOnClickListener(this);
        tabDimmer.setOnClickListener(this);


        Typeface type = Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Bold.ttf");
        tabRGB.setTypeface(type);
        tabDimmer.setTypeface(type);

        tvBrightnessCaption = (TextView) findViewById(R.id.rgbDetailBrightnessCaption);
        tvBrightnessCaption.setTypeface(type);

        tvBrightness = (TextView) findViewById(R.id.rgbDetailBrightness);
        tvBrightness.setTypeface(type);

        brightnessLine = findViewById(R.id.rgbBrightnessLine);

        tvStateCaption = (TextView) findViewById(R.id.rgbDetailStateCaption);
        tvStateCaption.setTypeface(type);

        type = Typeface.createFromAsset(getContext().getAssets(), "fonts/Quicksand-Regular.ttf");

        tvTitle = (TextView) findViewById(R.id.rgbDetailTitle);
        tvTitle.setTypeface(type);

        stateImage = (ImageView) findViewById(R.id.rgbDetailStateImage);
        stateImage.setOnClickListener(this);

        remoteUpdateTime = 0;
        changeFinishedTime = 0;
        delayTimer1 = null;
        delayTimer2 = null;
    }


    private void showRGB() {

        rgbPicker.setColorWheelVisible(true);
        rgbPicker.setColorBrightnessWheelVisible(true);
        clPicker.setVisibility(View.VISIBLE);

        channelDataToViews();
    }

    private void showDimmer() {

        rgbPicker.setBWBrightnessWheelVisible(true);
        clPicker.setVisibility(View.GONE);

        channelDataToViews();
    }

    public void setData(ChannelBase channel) {

        super.setData(channel);

        switch (channel.getFunc()) {

            case SuplaConst.SUPLA_CHANNELFNC_DIMMER:
                showDimmer();
                tabs.setVisibility(View.GONE);
                break;

            case SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING:
                showRGB();
                tabs.setVisibility(View.GONE);
                break;

            case SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING:
                rgbPicker.setColorWheelVisible(true);
                rgbPicker.setColorBrightnessWheelVisible(true);

                onClick(tabRGB);
                tabs.setVisibility(View.VISIBLE);

                break;
        }


        channelDataToViews();
    }

    private void channelDataToViews() {

        int id = 0;

        if (isGroup()) {
            ChannelGroup cgroup = (ChannelGroup)getChannelFromDatabase();
            tvTitle.setText(cgroup.getNotEmptyCaption(getContext()));
            status.setVisibility(View.VISIBLE);
            status.setPercent(cgroup.getOnLinePercent());

            if (cgroup.getFunc() == SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING) {

            } else {

            }

        } else {
            Channel channel = (Channel)getChannelFromDatabase();

            tvTitle.setText(channel.getNotEmptyCaption(getContext()));
            status.setVisibility(View.GONE);

            if (rgbPicker.getColorBrightnessWheelVisible()
                    && (int) rgbPicker.getBrightnessValue() != (int) channel.getColorBrightness())
                rgbPicker.setBrightnessValue(channel.getColorBrightness());

            if (rgbPicker.getBWBrightnessWheelVisible()
                    && (int) rgbPicker.getBrightnessValue() != (int) channel.getBrightness())
                rgbPicker.setBrightnessValue(channel.getBrightness());

            if (rgbPicker.getColorWheelVisible())
                rgbPicker.setColor(channel.getColor());

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
    public View getContentView() {
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

        lastColor = rgbPicker.getColor();
        int brightness = (int) rgbPicker.getBrightnessValue();

        tvBrightness.setText(Integer.toString(brightness) + "%");

        stateImage.setImageResource(rgbPicker.getBrightnessValue() > 0 ? R.drawable.poweron : R.drawable.poweroff);

        if (rgbPicker.getColorWheelVisible())
            lastColorBrightness = brightness;
        else
            lastBrightness = brightness;
    }

    private void sendNewValues() {

        if (delayTimer1 != null) {
            delayTimer1.cancel();
            delayTimer1 = null;
        }

        SuplaClient client = SuplaApp.getApp().getSuplaClient();

        if (client == null || !isDetailVisible())
            return;

        if (System.currentTimeMillis() - remoteUpdateTime >= MIN_REMOTE_UPDATE_PERIOD
                && client.setRGBW(getRemoteId(), isGroup(), lastColor, lastColorBrightness, lastBrightness)) {
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

            rgbPicker.setBrightnessValue(rgbPicker.getBrightnessValue() > 0 ? 0 : 100);
            pickerToInfoPanel();
            sendNewValues();
            onChangeFinished();
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

    private void updateDelayed() {

        if (delayTimer2 != null) {
            delayTimer2.cancel();
            delayTimer2 = null;
        }

        if (!isDetailVisible()
                || rgbPicker.getMoving())
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
    public void onChangeFinished() {

        changeFinishedTime = System.currentTimeMillis();
        updateDelayed();
    }

    @Override
    public void OnChannelDataChanged() {
        updateDelayed();
    }


    @Override
    public void onColorTouched(SuplaColorListPicker sclPicker, int color, short percent) {

        if (color != Color.TRANSPARENT && rgbPicker.getColorBrightnessWheelVisible()) {
            rgbPicker.setColor(color);
            rgbPicker.setBrightnessValue(percent);

            onColorChanged(rgbPicker, color);
        }

    }

    @Override
    public void onEdit(SuplaColorListPicker sclPicker, int idx) {

        if (idx > 0 && rgbPicker.getColorBrightnessWheelVisible()) {
            sclPicker.setItemColor(idx, rgbPicker.getColor());
            sclPicker.setItemPercent(idx, (short) rgbPicker.getBrightnessValue());

            if (getRemoteId() != 0) {

                ColorListItem cli = new ColorListItem();
                cli.setRemoteId(getRemoteId());
                cli.setGroup(isGroup());
                cli.setIdx(idx);
                cli.setColor(rgbPicker.getColor());
                cli.setBrightness((short) rgbPicker.getBrightnessValue());

                DBH.updateColorListItemValue(cli);
            }

        }

    }
}
