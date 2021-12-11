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
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaClientMessageHandler;
import org.supla.android.lib.SuplaClientMsg;
import org.supla.android.lib.SuplaConst;

import org.supla.android.profile.AuthInfo;

import java.util.Timer;
import java.util.TimerTask;

public class SuperuserAuthorizationDialog implements View.OnClickListener, DialogInterface.OnCancelListener, View.OnTouchListener, SuplaClientMessageHandler.OnSuplaClientMessageListener, TextWatcher {
    private Context context;
    private AlertDialog dialog;
    private Button btnCancel;
    private Button btnOK;
    private Button btnViewPassword;
    private ProgressBar progressBar;
    private EditText edEmail;
    private EditText edPassword;
    private TextView tvErrorMessage;
    private OnAuthorizarionResultListener onAuthorizarionResultListener;
    private Object object;
    private Timer timeoutTimer;
    private static SuperuserAuthorizationDialog lastVisibleInstance;
    private AlertDialog.Builder builder;

    SuperuserAuthorizationDialog(Context context) {
        this.context = context;
        builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.superuser_auth_dialog, null);

        btnCancel = v.findViewById(R.id.btnDialogCancel);
        btnCancel.setOnClickListener(this);

        btnOK = v.findViewById(R.id.btnDialogOK);
        btnOK.setOnClickListener(this);

        btnViewPassword = v.findViewById(R.id.btnViewPassword);
        btnViewPassword.setOnTouchListener(this);

        edEmail = v.findViewById(R.id.dialogEmail);
        progressBar = v.findViewById(R.id.dialogPBar);
        edPassword = v.findViewById(R.id.dialogPwd);
        edPassword.addTextChangedListener(this);

        AuthInfo ainfo = SuplaApp.getApp().getProfileManager(context)
            .getCurrentProfile().getAuthInfo();
        edEmail.setText(ainfo.getEmailAddress(),
                        EditText.BufferType.EDITABLE);

        tvErrorMessage = v.findViewById(R.id.dialogError);
        tvErrorMessage.setVisibility(View.INVISIBLE);

        TextView tvInfo = v.findViewById(R.id.tvInfo);
        tvInfo.setText(context.getResources().
                getString(ainfo.getServerForEmail().contains(".supla.org") ?
                        R.string.enter_suplaorg_credentails
                        : R.string.enter_superuser_credentials));

        builder.setView(v);
    }

    boolean isClientRegistered() {
        SuplaClient client = SuplaApp.getApp().getSuplaClient();
        return client != null && client.registered();
    }

    void cancelTimeoutTimer() {
        if (timeoutTimer != null) {
            timeoutTimer.cancel();
            timeoutTimer = null;
        }
    }

    public void showIfNeeded() {
        tvErrorMessage.setText("");
        tvErrorMessage.setVisibility(View.INVISIBLE);
        edPassword.setText("");
        btnOK.setEnabled(false);

        if (isClientRegistered()) {
            edEmail.setEnabled(true);
            SuplaClient client = SuplaApp.getApp().getSuplaClient();
            if (client != null && client.isSuperUserAuthorized()) {
                if (onAuthorizarionResultListener != null) {
                    onAuthorizarionResultListener
                            .onSuperuserOnAuthorizarionResult(this,
                                    true, SuplaConst.SUPLA_RESULTCODE_AUTHORIZED);
                }
                return;
            }
        } else {
            edEmail.setEnabled(false);
        }

        if (dialog == null) {
            dialog = builder.create();
            dialog.setOnCancelListener(this);
        }

        lastVisibleInstance = this;
        cancelTimeoutTimer();
        dialog.show();
    }

    public void ShowError(String msg) {
        cancelTimeoutTimer();
        tvErrorMessage.setText(msg);
        tvErrorMessage.setVisibility(View.VISIBLE);
        edPassword.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        btnOK.setVisibility(View.VISIBLE);
    }

    public void ShowError(int resid) {
        ShowError(context.getResources().getString(resid));
    }

    @Override
    public void onClick(View view) {
        if (view == btnCancel) {
            if (dialog != null) {
                dialog.cancel();
            }

        } else if (view == btnOK) {
            edPassword.setEnabled(false);
            btnOK.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            tvErrorMessage.setVisibility(View.INVISIBLE);
            SuplaClientMessageHandler.getGlobalInstance().registerMessageListener(this);

            cancelTimeoutTimer();
            timeoutTimer = new Timer();

            timeoutTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ((Activity) context).runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            ShowError(R.string.time_exceeded);
                        }
                    });
                }

            }, 10000, 1000);

            SuplaClient client = null;

            if (!isClientRegistered()) {
                SuplaApp.getApp().SuplaClientInitIfNeed(context, edPassword.getText().toString());
            } else {
                client = SuplaApp.getApp().getSuplaClient();
                if (client != null) {
                    client.superUserAuthorizationRequest(edEmail.getText().toString(),
                            edPassword.getText().toString());
                }
            }
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        if (lastVisibleInstance == this) {
            lastVisibleInstance = null;
        }

        SuplaClientMessageHandler.getGlobalInstance().unregisterMessageListener(this);

        if (onAuthorizarionResultListener != null) {
            onAuthorizarionResultListener.authorizationCanceled();
        }
    }

    public void setOnAuthorizarionResultListener(OnAuthorizarionResultListener
                                                         onAuthorizarionResultListener) {
        this.onAuthorizarionResultListener = onAuthorizarionResultListener;
    }

    public void close() {
        if (lastVisibleInstance == this) {
            lastVisibleInstance = null;
        }
        SuplaClientMessageHandler.getGlobalInstance().unregisterMessageListener(this);
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        onAuthorizarionResultListener = null;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == btnViewPassword) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if ((edPassword.getInputType() & InputType.TYPE_TEXT_VARIATION_PASSWORD) > 0) {
                        edPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                    } else {
                        edPassword.setInputType(InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                    break;
            }
        }

        return false;
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    @Override
    public void onSuplaClientMessageReceived(SuplaClientMsg msg) {
        if (msg == null) {
            return;
        }

        if ( msg.getType() == SuplaClientMsg.onSuperuserAuthorizationResult) {

            if (msg.isSuccess()
                    && onAuthorizarionResultListener != null) {
                onAuthorizarionResultListener
                        .onSuperuserOnAuthorizarionResult(this,
                                msg.isSuccess(), msg.getCode());
            }

            if (!msg.isSuccess()) {
                switch (msg.getCode()) {
                    case SuplaConst.SUPLA_RESULTCODE_UNAUTHORIZED:
                        ShowError(R.string.incorrect_email_or_password);
                        break;
                    case SuplaConst.SUPLA_RESULTCODE_TEMPORARILY_UNAVAILABLE:
                        ShowError(R.string.status_temporarily_unavailable);
                        break;
                    default:
                        ShowError(R.string.status_unknown_err);
                        break;
                }
            }
        } else if (msg.getType() == SuplaClientMsg.onRegisterError) {
            ShowError(msg.getRegisterError().codeToString(context, true));
        } else if (msg.getType() == SuplaClientMsg.onRegistered) {
            close();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        btnOK.setEnabled(edPassword.getText().toString().trim().length() > 0);
    }

    public interface OnAuthorizarionResultListener {
        void onSuperuserOnAuthorizarionResult(SuperuserAuthorizationDialog dialog,
                                              boolean Success, int Code);

        void authorizationCanceled();
    }

    public static boolean lastOneIsStillShowing() {
        return lastVisibleInstance != null && lastVisibleInstance.isShowing();
    }

}
