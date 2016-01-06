package org.supla.android;

/*
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

 Author: Przemyslaw Zygmunt p.zygmunt@acsoftware.pl [AC SOFTWARE]
 */

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaClientMsg;
import org.supla.android.lib.SuplaConst;
import org.supla.android.lib.SuplaRegisterError;
import org.supla.android.lib.SuplaRegisterResult;
import org.supla.android.lib.SuplaVersionError;
import org.supla.android.listview.ListViewCursorAdapter;

public class StatusActivity extends NavigationActivity {

    private int mode;
    private Button btn;
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

        btn = (Button)findViewById(R.id.status_btn);
        type = Typeface.createFromAsset(getAssets(),"fonts/OpenSans-Regular.ttf");
        btn.setTypeface(type);
        btn.setTransformationMethod(null);
        btn.setText(getResources().getText(R.string.settings));
        btn.setOnClickListener(this);

        layout = (RelativeLayout)btn.getParent();

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
                btn.setBackground(getResources().getDrawable(R.drawable.rounded_black_btn));
                img.setBackground(getResources().getDrawable(R.drawable.error));
            } else {
                btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_black_btn));
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.error));
            }

            btn.setTextColor(Color.WHITE);
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
                btn.setBackground(getResources().getDrawable(R.drawable.rounded_white_btn));
                img.setBackground(getResources().getDrawable(R.drawable.logosuplawhite));
            } else {
                btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_white_btn));
                img.setBackgroundDrawable(getResources().getDrawable(R.drawable.logosuplawhite));
            }

            btn.setTextColor(getResources().getColor(R.color.activity_status_bg_normal));
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

        if ( v == btn ) {
            NavigationActivity.showCfg(this);
        }
    }

    @Override
    protected void OnDisconnectedMsg() {
        setStatusConnectingProgress(0);
    };

    @Override
    protected void OnConnectingMsg() {
        setStatusConnectingProgress(25);
    };

    @Override
    protected void OnConnectedMsg() {
        setStatusConnectingProgress(50);
    };

    @Override
    protected void OnRegisteringMsg() {
        setStatusConnectingProgress(75);
    };

    @Override
    protected void OnRegisteredMsg() {
        setStatusConnectingProgress(100);
        showMain(this);
    };

    private void _OnRegisterErrorMsg(SuplaRegisterError error) {
        String msg;


        switch(error.ResultCode) {

            case SuplaConst.SUPLA_RESULTCODE_TEMPORARILY_UNAVAILABLE:
                msg = getResources().getString(R.string.status_temporarily_unavailable);
                break;
            case SuplaConst.SUPLA_RESULTCODE_BAD_CREDENTIALS:
                msg = getResources().getString(R.string.status_bad_credentials);
                break;
            case SuplaConst.SUPLA_RESULTCODE_CLIENT_LIMITEXCEEDED:
                msg = getResources().getString(R.string.status_climit_exceded);
                break;
            case SuplaConst.SUPLA_RESULTCODE_CLIENT_DISABLED:
            case SuplaConst.SUPLA_RESULTCODE_ACCESSID_DISABLED:
                msg = getResources().getString(R.string.status_access_disabled);
                break;
            default:
                msg = getResources().getString(R.string.status_unknown_err)+" ("+Integer.toString(error.ResultCode)+")";
        }

        setStatusError(msg);
    }

    @Override
    protected void OnRegisterErrorMsg(SuplaRegisterError error) {

        super.OnRegisterErrorMsg(error);
        _OnRegisterErrorMsg(error);

    };

    @Override
    protected void OnVersionErrorMsg(SuplaVersionError error) {
        setStatusError(getResources().getString(R.string.status_version_error));
    };

    @Override
    public void onBackPressed() {
       gotoMain();
    }
}
