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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.supla.android.lib.SuplaChannelState;
import org.supla.android.lib.SuplaClient;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class LightsourceLifespanSettingsDialog implements DialogInterface.OnCancelListener, View.OnClickListener {

    private Context context;
    private AlertDialog alertDialog;
    private int remoteId;
    private int lifespan;

    private Button btnClose;
    private Button btnCancel;
    private Button btnOK;
    private EditText edLifespan;
    private CheckBox cbReset;

    public LightsourceLifespanSettingsDialog(Context context, int remoteId,
                                             int lifespan, String title) {
        this.context = context;
        this.remoteId = remoteId;
        this.lifespan = lifespan;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.lightsourcelifespansettings, null);
        builder.setView(view);

        alertDialog = builder.create();
        alertDialog.setOnCancelListener(this);

        TextView tvInfoTitle = view.findViewById(R.id.tvInfoTitle);

        tvInfoTitle.setTypeface(SuplaApp.getApp().getTypefaceQuicksandRegular());
        tvInfoTitle.setText(title);

        Typeface tfOpenSansRegular = SuplaApp.getApp().getTypefaceOpenSansRegular();
        TextView tvLifespanTitle = view.findViewById(R.id.tvLifespanTitle);

        tvLifespanTitle.setTypeface(tfOpenSansRegular);

        edLifespan = view.findViewById(R.id.edLifespan);
        btnClose = view.findViewById(R.id.btnClose);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnOK = view.findViewById(R.id.btnOK);

        btnClose.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnOK.setOnClickListener(this);

        cbReset = view.findViewById(R.id.cbReset);
        cbReset.setTypeface(tfOpenSansRegular);
    }

    public void show() {
        this.edLifespan.setText(Integer.toString(lifespan));
        alertDialog.show();
    }

    public boolean isVisible() {
        return alertDialog.isShowing();
    }

    public int getRemoteId() {
        return remoteId;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        remoteId = 0;
    }

    @Override
    public void onClick(View v) {
        alertDialog.cancel();

        if (v == btnOK) {
            int lifespan = Integer.parseInt(String.valueOf(edLifespan.getText()));
            if (lifespan < 0) {
                lifespan = 0;
            } else if (lifespan > 65535) {
                lifespan = 65535;
            }

            if (lifespan != this.lifespan || cbReset.isChecked()) {
                SuplaClient client = SuplaApp.getApp().getSuplaClient();

                if (client != null) {
                    client.setLightsourceLifespan(remoteId, cbReset.isChecked(),
                            lifespan != this.lifespan, lifespan);
                }
            }
        }
    }

}
;