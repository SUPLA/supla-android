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

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import org.supla.android.profile.ProfileManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StatusActivity extends NavigationActivity {

    private int mode;
    private Button btnSettings;
    private Button btnCloud;
    private Button btnRetry;
    private TextView msg;
    private ImageView img;
    private ProgressBar progress;
    private RelativeLayout rlStatus;
    private SuperuserAuthorizationDialog authorizationDialog;

    @Inject ProfileManager profileManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_status);
        rlStatus = findViewById(R.id.rlStatus);

        mode = 0;

        img = findViewById(R.id.status_img);
        progress = findViewById(R.id.status_progress);
        progress.setProgressDrawable(getResources().getDrawable(R.drawable.progressbar));

        msg = findViewById(R.id.status_text);
        msg.setTypeface(SuplaApp.getApp().getTypefaceQuicksandRegular());

        btnSettings = findViewById(R.id.status_btn);
        btnSettings.setTypeface(SuplaApp.getApp().getTypefaceOpenSansRegular());
        btnSettings.setTransformationMethod(null);
        btnSettings.setText(getResources().getText(R.string.profile));
        btnSettings.setOnClickListener(this);

        btnCloud = findViewById(R.id.cloud_btn);
        btnCloud.setTypeface(SuplaApp.getApp().getTypefaceOpenSansRegular());
        btnCloud.setTransformationMethod(null);
        btnCloud.setOnClickListener(this);

        btnRetry = findViewById(R.id.retry_btn);
        btnRetry.setOnClickListener(this);

        setStatusConnectingProgress(0);
        RegisterMessageHandler();
    }

    protected void onResume() {
        super.onResume();

        SuplaClient client = SuplaApp.getApp().getSuplaClient();

        if (client != null
                && client.registered()) {
            showMain(this);
        } else {

            SuplaRegisterError error = SuplaClient.getLastRegisterError();

            if (error != null)
                _OnRegisterErrorMsg(error);
        }


    }

    public void setStatusError(String message) {

        if (mode != 1) {
            mode = 1;

            rlStatus.setBackgroundColor(getResources().getColor(R.color.activity_status_bg_err));
            setStatusBarColor(R.color.activity_status_bg_err);
            btnCloud.setVisibility(View.VISIBLE);

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
        if (mode != 2) {
            mode = 2;

            setStatusBarColor(R.color.activity_status_bg_normal);
            rlStatus.setBackgroundColor(getResources().getColor(R.color.activity_status_bg_normal));
            btnCloud.setVisibility(View.INVISIBLE);

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

        if (v == btnSettings) {
            if(profileManager.getAllProfiles().size() <= 1) {
                NavigationActivity.showAuth(this);
            } else {
                NavigationActivity.showProfile(this);
            }
        } else if (v == btnRetry) {
            SuplaApp.getApp().SuplaClientInitIfNeed(this).reconnect();
        } else if (v == btnCloud) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(getResources().getString(R.string.cloud_url)));
            startActivity(browserIntent);
        }
    }

    protected void setThePointerToNullIfTheAuthDialogIsNotVisible() {
        // Solving the problem with changing the server address
        // where previously authorized to another one.
        // Without this, the authorization window will not be displayed again.
        if (authorizationDialog != null
                && !authorizationDialog.isShowing()) {
            authorizationDialog = null;
        }
    }

    @Override
    protected void onDisconnectedMsg() {
        setThePointerToNullIfTheAuthDialogIsNotVisible();
        setStatusConnectingProgress(0);
    }

    @Override
    protected void onConnectingMsg() {
        setThePointerToNullIfTheAuthDialogIsNotVisible();
        setStatusConnectingProgress(25);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setThePointerToNullIfTheAuthDialogIsNotVisible();
    }

    @Override
    protected void onConnectedMsg() {
        setStatusConnectingProgress(50);
    }

    @Override
    protected void onRegisteringMsg() {
        setStatusConnectingProgress(75);
    }

    @Override
    protected void onRegisteredMsg() {
        setStatusConnectingProgress(100);

        if (!(CurrentActivity instanceof AddDeviceWizardActivity) &&
            CurrentActivity != null) {
            showMain(this);
        }

    }

    private void _OnRegisterErrorMsg(SuplaRegisterError error) {
        setStatusError(error.codeToString(this));

        if (error != null
                && (error.ResultCode == SuplaConst.SUPLA_RESULTCODE_REGISTRATION_DISABLED
                    || error.ResultCode == SuplaConst.SUPLA_RESULTCODE_ACCESSID_NOT_ASSIGNED)) {

            if (profileManager.getCurrentProfile().getAuthInfo().getEmailAuth()) {
                if (authorizationDialog == null) {
                    authorizationDialog = new SuperuserAuthorizationDialog(this);
                }

                if (!authorizationDialog.isShowing()) {
                    authorizationDialog.showIfNeeded();
                }
            }
        }
    }

    @Override
    protected void onRegisterErrorMsg(SuplaRegisterError error) {

        super.onRegisterErrorMsg(error);
        _OnRegisterErrorMsg(error);

    }

    @Override
    protected void onVersionErrorMsg(SuplaVersionError error) {
        setStatusError(getResources().getString(R.string.status_version_error));
    }

    @Override
    protected void onConnErrorMsg(SuplaConnError error) {
        if (error.Code == SuplaConst.SUPLA_RESULTCODE_HOSTNOTFOUND)
            setStatusError(getResources().getString(R.string.err_hostnotfound));
    }

    @Override
    public void onBackPressed() {
        gotoMain();
    }
}
