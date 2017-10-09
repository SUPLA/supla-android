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

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaConnError;
import org.supla.android.lib.SuplaConst;
import org.supla.android.lib.SuplaRegisterError;
import org.supla.android.lib.SuplaVersionError;

public class StatusActivity extends NavigationActivity {

    private int mode;
    private Button btnSettings;
    private Button btnRetry;
    private TextView msg;
    private ImageView img;
    private RelativeLayout layout;
    private ProgressBar progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_status);

        mode = 0;

        img = (ImageView)findViewById(R.id.status_img);
        progress = (ProgressBar)findViewById(R.id.status_progress);
        progress.setProgressDrawable(getResources().getDrawable(R.drawable.progressbar));

        msg = (TextView)findViewById(R.id.status_text);
        Typeface type = Typeface.createFromAsset(getAssets(),"fonts/Quicksand-Regular.ttf");
        msg.setTypeface(type);

        btnSettings = (Button)findViewById(R.id.status_btn);
        type = Typeface.createFromAsset(getAssets(),"fonts/OpenSans-Regular.ttf");
        btnSettings.setTypeface(type);
        btnSettings.setTransformationMethod(null);
        btnSettings.setText(getResources().getText(R.string.settings));
        btnSettings.setOnClickListener(this);

        btnRetry = (Button)findViewById(R.id.retry_btn);
        btnRetry.setOnClickListener(this);

        layout = (RelativeLayout)msg.getParent();

        setStatusConnectingProgress(0);
        RegisterMessageHandler();
    }

    protected void onResume() {
        super.onResume();

        SuplaClient client = SuplaApp.getApp().getSuplaClient();

        if ( client != null
                && client.Registered() ) {
            showMain(this);
        } else {

            SuplaRegisterError error = SuplaClient.getLastRegisterError();

            if ( error != null )
                _OnRegisterErrorMsg(error);
        }


    }

    public void setStatusError(String message) {

        if ( mode != 1 ) {
            mode = 1;

            layout.setBackgroundColor(getResources().getColor(R.color.activity_status_bg_err));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                btnSettings.setBackground(getResources().getDrawable(R.drawable.rounded_black_btn));
                img.setBackground(getResources().getDrawable(R.drawable.error));
            } else {
                btnSettings.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_black_btn));
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.error));
            }

            btnRetry.setVisibility(View.VISIBLE);
            btnSettings.setTextColor(Color.WHITE);
            msg.setTextColor(Color.BLACK);
            msg.setText(message);
            progress.setVisibility(View.INVISIBLE);

        }


    }

    public void setStatusConnectingProgress(int value) {
        if ( mode != 2 ) {
            mode = 2;

            layout.setBackgroundColor(getResources().getColor(R.color.activity_status_bg_normal));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                btnSettings.setBackground(getResources().getDrawable(R.drawable.rounded_white_btn));
                img.setBackground(getResources().getDrawable(R.drawable.logosuplawhite));
            } else {
                btnSettings.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_white_btn));
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.logosuplawhite));
            }

            btnRetry.setVisibility(View.GONE);
            btnSettings.setTextColor(getResources().getColor(R.color.activity_status_bg_normal));
            msg.setTextColor(Color.WHITE);
            msg.setText(getResources().getText(R.string.status_connecting));
            progress.setVisibility(View.VISIBLE);
        }

        progress.setProgress(value);
    }


    public boolean isErrorMode() {
        return mode == 1;
    }

    public boolean isConnectingMode() {
        return mode == 2;
    }

    @Override
    public void onClick(View v) {

        super.onClick(v);

        if ( v == btnSettings ) {
            NavigationActivity.showCfg(this);
        } else if ( v == btnRetry ) {
            SuplaApp.getApp().SuplaClientInitIfNeed(this).Reconnect();
        }
    }

    @Override
    protected void OnDisconnectedMsg() {
        setStatusConnectingProgress(0);
    }

    @Override
    protected void OnConnectingMsg() {
        setStatusConnectingProgress(25);
    }

    @Override
    protected void OnConnectedMsg() {
        setStatusConnectingProgress(50);
    }

    @Override
    protected void OnRegisteringMsg() {
        setStatusConnectingProgress(75);
    }

    @Override
    protected void OnRegisteredMsg() {
        setStatusConnectingProgress(100);

        if ( CurrentActivity == null
                || !(CurrentActivity instanceof AddWizardActivity) ) {
            showMain(this);
        }

    }

    private void _OnRegisterErrorMsg(SuplaRegisterError error) {
        setStatusError(error.codeToString(this));
    }

    @Override
    protected void OnRegisterErrorMsg(SuplaRegisterError error) {

        super.OnRegisterErrorMsg(error);
        _OnRegisterErrorMsg(error);

    }

    @Override
    protected void OnVersionErrorMsg(SuplaVersionError error) {
        setStatusError(getResources().getString(R.string.status_version_error));
    }

    @Override
    protected void OnConnErrorMsg(SuplaConnError error) {
        if ( error.Code == SuplaConst.SUPLA_RESULTCODE_HOSTNOTFOUND )
            setStatusError(getResources().getString(R.string.err_hostnotfound));
    }

    @Override
    public void onBackPressed() {
       gotoMain();
    }
}
