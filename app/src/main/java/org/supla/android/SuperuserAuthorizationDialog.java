package org.supla.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

public class SuperuserAuthorizationDialog implements View.OnClickListener, DialogInterface.OnCancelListener, View.OnTouchListener {
    private Context context;
    private AlertDialog dialog;
    private Button btnCancel;
    private Button btnOK;
    private Button btnViewPassword;
    private ProgressBar progressBar;
    private EditText edEmail;
    private EditText edPassword;
    private TextView tvErrorMessage;
    private Handler _sc_msg_handler = null;
    private OnAuthorizarionResultListener onAuthorizarionResultListener;
    private Object object;
    private Timer timeoutTimer;
    private boolean preAuthorization;

    SuperuserAuthorizationDialog(Context context) {
        this.context = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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

        Preferences prefs = new Preferences(context);
        edEmail.setText(prefs.getEmail(), EditText.BufferType.EDITABLE);

        tvErrorMessage = v.findViewById(R.id.dialogError);
        tvErrorMessage.setVisibility(View.INVISIBLE);

        TextView tvInfo = v.findViewById(R.id.tvInfo);
        tvInfo.setText(context.getResources().
                getString(prefs.getServerAddress().contains(".supla.org") ?
                        R.string.enter_suplaorg_credentails
                        : R.string.enter_superuser_credentials));

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
        preAuthorization = true;
        edPassword.setText("******");
        onClick(btnOK);
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

                    if ((!preAuthorization || _msg.isSuccess())
                            && onAuthorizarionResultListener != null) {
                        onAuthorizarionResultListener
                                .onSuperuserOnAuthorizarionResult(dialog,
                                        _msg.isSuccess(), _msg.getCode());
                    }

                    if (preAuthorization) {
                        ShowError("");
                        edPassword.setText("");
                        preAuthorization = false;
                    } else {
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
            }
        };

        SuplaApp.getApp().addMsgReceiver(_sc_msg_handler);
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
            registerMessageHandler();

            cancelTimeoutTimer();
            timeoutTimer = new Timer();

            timeoutTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ((Activity) context).runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (preAuthorization) {
                                ShowError("");
                                edPassword.setText("");
                                preAuthorization = false;
                            } else {
                                ShowError(R.string.time_exceeded);
                            }

                        }
                    });
                }

            }, 10000, 1000);

            SuplaClient client = SuplaApp.getApp().getSuplaClient();
            if (client != null) {
                if (preAuthorization) {
                    client.getSuperUserAuthorizationResult();
                } else {
                    client.superUserAuthorizationRequest(edEmail.getText().toString(),
                            edPassword.getText().toString());
                }
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == btnViewPassword) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    edPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    edPassword.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    break;
            }
        }

        return false;
    }

    public interface OnAuthorizarionResultListener {
        void onSuperuserOnAuthorizarionResult(SuperuserAuthorizationDialog dialog,
                                              boolean Success, int Code);

        void authorizationCanceled();
    }
}
