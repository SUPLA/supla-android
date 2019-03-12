package org.supla.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.supla.android.lib.SuplaClientMsg;

public class SuperuserAuthorizationDialog implements View.OnClickListener, DialogInterface.OnCancelListener {
    private AlertDialog dialog;
    private Button btnCancel;
    private Button btnOK;
    private ProgressBar progressBar;
    private EditText edPassword;
    private TextView tvErrorMessage;
    private Handler _sc_msg_handler = null;

    SuperuserAuthorizationDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.superuser_auth_dialog, null);

        btnCancel = v.findViewById(R.id.btnDialogCancel);
        btnCancel.setOnClickListener(this);

        btnOK = v.findViewById(R.id.btnDialogOK);
        btnOK.setOnClickListener(this);

        progressBar = v.findViewById(R.id.dialogPBar);
        edPassword = v.findViewById(R.id.dialogPwd);

        tvErrorMessage = v.findViewById(R.id.dialogError);

        builder.setView(v);
        dialog = builder.create();
        dialog.setOnCancelListener(this);
    }

    void show() {
        dialog.show();
    }

    private void registerMessageHandler() {
        if ( _sc_msg_handler != null )
            return;

        _sc_msg_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                SuplaClientMsg _msg = (SuplaClientMsg)msg.obj;
                switch(_msg.getType()) {
                }
            }
        };

        SuplaApp.getApp().addMsgReceiver(_sc_msg_handler);
    }

    @Override
    public void onClick(View view) {
        if (view == btnCancel) {
            dialog.cancel();
        } else if (view == btnOK) {
            edPassword.setEnabled(false);
            btnOK.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            registerMessageHandler();
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        if ( _sc_msg_handler != null ) {
            SuplaApp.getApp().removeMsgReceiver(_sc_msg_handler);
            _sc_msg_handler = null;
        }
    }
}
