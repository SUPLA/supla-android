package org.supla.android;

import android.app.Activity;
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

import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaClientMsg;
import org.supla.android.lib.SuplaConst;

import java.util.Timer;
import java.util.TimerTask;

public class SuperuserAuthorizationDialog implements View.OnClickListener, DialogInterface.OnCancelListener {
    private Context context;
    private AlertDialog dialog;
    private Button btnCancel;
    private Button btnOK;
    private ProgressBar progressBar;
    private EditText edEmail;
    private EditText edPassword;
    private TextView tvErrorMessage;
    private Handler _sc_msg_handler = null;
    private OnAuthorizarionResultListener onAuthorizarionResultListener;
    private Object object;
    private Timer timeoutTimer;

    SuperuserAuthorizationDialog(Context context) {
        this.context = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.superuser_auth_dialog, null);

        btnCancel = v.findViewById(R.id.btnDialogCancel);
        btnCancel.setOnClickListener(this);

        btnOK = v.findViewById(R.id.btnDialogOK);
        btnOK.setOnClickListener(this);

        edEmail = v.findViewById(R.id.dialogEmail);
        progressBar = v.findViewById(R.id.dialogPBar);
        edPassword = v.findViewById(R.id.dialogPwd);

        Preferences prefs = new Preferences(context);
        edEmail.setText(prefs.getEmail(), EditText.BufferType.EDITABLE);

        tvErrorMessage = v.findViewById(R.id.dialogError);
        tvErrorMessage.setVisibility(View.INVISIBLE);

        builder.setView(v);
        dialog = builder.create();
        dialog.setOnCancelListener(this);
    }

    void cancelTimeoutTimer() {
        if (timeoutTimer != null) {
            timeoutTimer.cancel();
            timeoutTimer = null;
        }
    }

    void show() {
        if (dialog != null) {
            cancelTimeoutTimer();
            dialog.show();
        }
    }

    private void registerMessageHandler() {
        if (_sc_msg_handler != null)
            return;

        final SuperuserAuthorizationDialog dialog = this;

        _sc_msg_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                SuplaClientMsg _msg = (SuplaClientMsg) msg.obj;
                if (_msg != null
                        && _msg.getType() == SuplaClientMsg.onSuperuserAuthorizationResult) {
                    cancelTimeoutTimer();

                    if (onAuthorizarionResultListener != null) {
                        onAuthorizarionResultListener
                                .onSuperuserOnAuthorizarionResult(dialog,
                                        _msg.isSuccess(), _msg.getCode());
                    }

                    if (!_msg.isSuccess()) {

                        switch (_msg.getCode()) {
                            case SuplaConst.SUPLA_RESULTCODE_UNAUTHORIZED:
                                ShowError(R.string.status_bad_credentials);
                                break;
                            case SuplaConst.SUPLA_RESULTCODE_TEMPORARILY_UNAVAILABLE:
                                ShowError(R.string.status_temporarily_unavailable);
                                break;
                            default:
                                ShowError(R.string.status_unknown_err);
                                break;
                        }

                    }
                }
            }
        };

        SuplaApp.getApp().addMsgReceiver(_sc_msg_handler);
    }

    public void ShowError(String msg) {
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
            dialog.cancel();
        } else if (view == btnOK) {
            edPassword.setEnabled(false);
            btnOK.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            tvErrorMessage.setVisibility(View.INVISIBLE);
            registerMessageHandler();

            cancelTimeoutTimer();
            timeoutTimer = new Timer();

            timeoutTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ((Activity) context).runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            cancelTimeoutTimer();
                            ShowError(R.string.time_exceeded);
                        }
                    });
                }

            }, 6000, 1000);

            SuplaClient client = SuplaApp.getApp().getSuplaClient();
            if (client != null) {
                client.superUserAuthorizationRequest(edEmail.getText().toString(),
                        edPassword.getText().toString());
            }
        }
    }

    private void unregisterMessageHandler() {
        if (_sc_msg_handler != null) {
            SuplaApp.getApp().removeMsgReceiver(_sc_msg_handler);
            _sc_msg_handler = null;
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        unregisterMessageHandler();

        if (onAuthorizarionResultListener != null) {
            onAuthorizarionResultListener.authorizationCanceled();
        }
    }

    public void setOnAuthorizarionResultListener(OnAuthorizarionResultListener
                                                         onAuthorizarionResultListener) {
        this.onAuthorizarionResultListener = onAuthorizarionResultListener;
    }

    public void close() {
        unregisterMessageHandler();
        dialog.hide();
        dialog = null;
        onAuthorizarionResultListener = null;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public interface OnAuthorizarionResultListener {
        void onSuperuserOnAuthorizarionResult(SuperuserAuthorizationDialog dialog,
                                              boolean Success, int Code);

        void authorizationCanceled();
    }
}
